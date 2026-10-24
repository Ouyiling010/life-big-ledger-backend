package com.life.ledger.aspect;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.annotation.FamilyPermission;
import com.life.ledger.common.BizException;
import com.life.ledger.common.FamilyRole;
import com.life.ledger.common.UserContext;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 家庭权限切面：当前用户必须是指定家庭成员，且角色满足 minRole
 */
@Aspect
@Component
@RequiredArgsConstructor
public class FamilyPermissionAspect {

    private final FamilyMemberMapper familyMemberMapper;
    private final ContextService contextService;

    @Around("@annotation(familyPermission)")
    public Object around(ProceedingJoinPoint pjp, FamilyPermission familyPermission) throws Throwable {
        Long userId = UserContext.userId();
        Long familyId = resolveFamilyId(pjp, familyPermission);
        if (familyId == null) {
            throw new BizException(403, "缺少家庭上下文");
        }
        FamilyMember member = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getStatus, 1));
        if (member == null) {
            throw new BizException(403, "你不是该家庭成员");
        }
        FamilyRole role = FamilyRole.of(member.getRole());
        if (!role.satisfies(familyPermission.minRole())) {
            throw new BizException(403, "权限不足");
        }
        return pjp.proceed();
    }

    /**
     * 解析家庭ID：优先取方法参数名匹配的参数值，否则回退当前上下文家庭ID
     */
    private Long resolveFamilyId(ProceedingJoinPoint pjp, FamilyPermission familyPermission) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] names = signature.getParameterNames();
        Object[] args = pjp.getArgs();
        if (names != null && args != null) {
            for (int i = 0; i < names.length && i < args.length; i++) {
                String name = names[i];
                if (name == null) {
                    continue;
                }
                boolean matched = name.equals(familyPermission.familyIdParam())
                        || name.equals("familyId")
                        || name.equals("id");
                if (matched) {
                    Long value = toLong(args[i]);
                    if (value != null) {
                        return value;
                    }
                }
            }
        }
        try {
            return contextService.familyId();
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
