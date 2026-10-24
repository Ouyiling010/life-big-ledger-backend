package com.life.ledger.aspect;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.common.UserContext;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面：拦截被 @OpLog 标注的方法，记录耗时并异步写入 operation_log
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OpLogAspect {

    private static final int DETAIL_MAX_LEN = 900;

    private final OperationLogService operationLogService;
    private final ContextService contextService;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            writeLog(pjp, opLog, (int) (System.currentTimeMillis() - start), buildDetail(pjp, e));
            throw e;
        }
        writeLog(pjp, opLog, (int) (System.currentTimeMillis() - start), buildDetail(pjp, null));
        return result;
    }

    private void writeLog(ProceedingJoinPoint pjp, OpLog opLog, int costMs, String detail) {
        try {
            Long userId = UserContext.userIdOrNull();
            Long familyId = null;
            try {
                familyId = contextService.familyId();
            } catch (Exception ignore) {
                // 无家庭上下文时 familyId 为 null
            }
            Long targetId = resolveTargetId(pjp.getArgs());
            String ip = currentIp();
            operationLogService.saveAsync(userId, familyId, opLog.module(), opLog.action(),
                    targetId, detail, ip, costMs);
        } catch (Exception e) {
            log.warn("记录操作日志失败: {}", e.getMessage());
        }
    }

    private Long resolveTargetId(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }

    private String currentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return UserContext.clientIp(request);
    }

    private String buildDetail(ProceedingJoinPoint pjp, Throwable error) {
        StringBuilder sb = new StringBuilder(pjp.getSignature().getName());
        Object[] args = pjp.getArgs();
        if (args != null && args.length > 0) {
            sb.append('(');
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(args[i]);
            }
            sb.append(')');
        }
        if (error != null) {
            sb.append(" | 异常: ").append(error.getClass().getSimpleName())
                    .append(": ").append(error.getMessage());
        }
        return truncate(sb.toString(), DETAIL_MAX_LEN);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
