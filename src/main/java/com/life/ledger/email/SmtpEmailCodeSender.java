package com.life.ledger.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 真正的邮件发送器，仅当 {@code app.email.mode=smtp} 时启用。
 * <p>
 * 需要在 application.yml 的 {@code spring.mail} 配置真实的 SMTP 账号与授权码。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.email.mode", havingValue = "smtp")
@RequiredArgsConstructor
public class SmtpEmailCodeSender implements EmailCodeSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@life-ledger.local}")
    private String from;

    @Override
    public void send(String email, EmailScene scene, String code, long ttlSec) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(email);
            msg.setSubject("生活大作账 · " + sceneLabel(scene) + "验证码");
            msg.setText(buildBody(scene, code, ttlSec));
            mailSender.send(msg);
            log.info("验证码邮件已发送: scene={}, email={}", scene, email);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: scene={}, email={}", scene, email, e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage(), e);
        }
    }

    private String sceneLabel(EmailScene scene) {
        return EmailScene.REGISTER.equals(scene) ? "注册" : "重置密码";
    }

    private String buildBody(EmailScene scene, String code, long ttlSec) {
        String action = EmailScene.REGISTER.equals(scene) ? "注册账号" : "重置密码";
        return String.format(
                "【生活大作账】您正在%s，验证码是：%s。\n" +
                        "验证码有效期 %d 分钟，请勿泄露给他人。\n" +
                        "如非本人操作，请忽略此邮件。",
                action, code, ttlSec / 60);
    }
}
