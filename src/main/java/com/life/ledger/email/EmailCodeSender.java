package com.life.ledger.email;

/**
 * 邮箱验证码发送器抽象。
 * <p>
 * 默认实现 {@link LogEmailCodeSender} 把验证码打到日志（演示 / 开发用）；
 * 生产环境配置 {@code app.email.mode=smtp} 后由 {@link SmtpEmailCodeSender} 真正发送。
 * 切换实现不需要改动业务代码。
 */
public interface EmailCodeSender {

    /**
     * 发送验证码
     *
     * @param email  收件邮箱
     * @param scene  场景（注册 / 忘记密码）
     * @param code   6 位数字验证码
     * @param ttlSec 验证码有效期（秒），用于邮件正文展示
     */
    void send(String email, EmailScene scene, String code, long ttlSec);
}
