package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 登录参数
 */
@Data
@ToString
public class LoginDTO implements Serializable {

    /** 用户名或邮箱 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 密码，禁止出现在操作日志中 */
    @ToString.Exclude
    @NotBlank(message = "密码不能为空")
    private String password;
}
