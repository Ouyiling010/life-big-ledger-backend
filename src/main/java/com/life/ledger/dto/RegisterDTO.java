package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 注册参数
 */
@Data
@ToString
public class RegisterDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需为4-20位")
    private String username;

    /** 密码，禁止出现在操作日志中；8-32 位，需含大写、小写和特殊符号，且不能有空格 */
    @ToString.Exclude
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需为8-32位")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\\s])\\S{8,32}$",
            message = "密码需同时包含大写字母、小写字母和特殊符号，且不能包含空格")
    private String password;

    private String nickname;

    /** 必填：注册时需先调 /api/auth/send-email-code 发送验证码 */
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱格式不正确")
    private String email;

    /** 邮箱验证码，必填；注册时会与 REGISTER 场景的验证码比对 */
    @NotBlank(message = "邮箱验证码不能为空")
    private String emailCode;

    /** 选填；填了必须是 11 位大陆手机号（1 开头，第二位 3-9） */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

