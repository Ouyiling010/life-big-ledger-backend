package com.life.ledger.email;

/**
 * 邮箱验证码服务：生成 / 发送 / 校验。
 * <p>
 * 验证码存储在 {@link com.life.ledger.cache.CacheService}（Caffeine），TTL 由
 * {@code app.email.code-ttl} 决定，默认 1 分钟。
 */
public interface EmailCodeService {

    /**
     * 生成并发送验证码。
     * <p>
     * 同一邮箱同一场景在验证码未过期时重新调用，会覆盖旧码。
     *
     * @param email 收件邮箱
     * @param scene 场景
     * @return 生成的验证码（供 log 模式下在响应中回传给前端；smtp 模式同样返回用于日志）
     */
    String generateAndSend(String email, EmailScene scene);

    /**
     * 校验验证码，校验通过后立即失效（一次性）。
     *
     * @throws com.life.ledger.common.BizException 400 验证码错误或已过期
     */
    void verifyOrThrow(String email, EmailScene scene, String code);
}
