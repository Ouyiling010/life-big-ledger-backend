package com.life.ledger.email;

import com.life.ledger.cache.CacheService;
import com.life.ledger.common.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * 邮箱验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    private final CacheService cacheService;
    private final EmailCodeSender emailCodeSender;

    @Value("${app.email.code-ttl:60}")
    private long codeTtl;

    @Value("${app.email.code-length:6}")
    private int codeLength;

    /** 单个验证码允许的最大错误次数，超过后作废 */
    @Value("${app.email.max-attempts:5}")
    private int maxAttempts;

    private static final String ATTEMPT_PREFIX = "email:attempt:";

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateAndSend(String email, EmailScene scene) {
        String code = randomCode();
        String key = cacheKey(email, scene);
        cacheService.set(key, code, codeTtl);
        cacheService.delete(attemptKey(email, scene));
        try {
            emailCodeSender.send(email, scene, code, codeTtl);
        } catch (Exception e) {
            // 发送失败时清理已写入的验证码，避免用户收不到却占用配额
            cacheService.delete(key);
            log.error("验证码发送失败: email={}, scene={}", email, scene, e);
            throw new BizException(500, "验证码发送失败，请稍后重试");
        }
        return code;
    }

    @Override
    public void verifyOrThrow(String email, EmailScene scene, String code) {
        String key = cacheKey(email, scene);
        String saved = cacheService.get(key, String.class);
        if (saved == null) {
            throw new BizException(400, "验证码已过期，请重新获取");
        }
        if (!saved.equals(code)) {
            // 错误计数：超过阈值直接作废验证码，防止 6 位码被暴力枚举
            String attemptKey = attemptKey(email, scene);
            Integer used = cacheService.get(attemptKey, Integer.class);
            int attempts = used == null ? 1 : used + 1;
            if (attempts >= maxAttempts) {
                cacheService.delete(key);
                cacheService.delete(attemptKey);
                throw new BizException(400, "验证码错误次数过多，请重新获取验证码");
            }
            cacheService.set(attemptKey, attempts, codeTtl);
            throw new BizException(400, "验证码错误");
        }
        // 一次性使用
        cacheService.delete(key);
        cacheService.delete(attemptKey(email, scene));
    }

    private String cacheKey(String email, EmailScene scene) {
        return "email:code:" + scene.name() + ":" + email;
    }

    private String attemptKey(String email, EmailScene scene) {
        return ATTEMPT_PREFIX + scene.name() + ":" + email;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
