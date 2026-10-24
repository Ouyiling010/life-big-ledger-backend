package com.life.ledger.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 默认邮件发送器：把验证码打到日志，不真正发送邮件。
 * <p>
 * 仅在 {@code app.email.mode=log}（未配置时默认）生效；生产环境把 {@code app.email.mode}
 * 改为 {@code smtp} 后本类不注册，自动切换到 {@link SmtpEmailCodeSender}。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.email.mode", havingValue = "log", matchIfMissing = true)
public class LogEmailCodeSender implements EmailCodeSender {

    @Override
    public void send(String email, EmailScene scene, String code, long ttlSec) {
        log.info("[邮箱验证码 - {}] 收件人={}, 验证码={}, 有效期={}秒", scene, email, code, ttlSec);
    }
}
