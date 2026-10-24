package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送邮箱验证码返回
 */
@Data
public class SendEmailCodeVO implements Serializable {

    /** 是否发送成功 */
    private Boolean sent;

    /**
     * 仅 dev 模式下返回验证码（log 模式 + app.email.dev-return-code=true）。
     * 生产环境该字段恒为 null，验证码通过邮件送达。
     */
    private String devCode;
}
