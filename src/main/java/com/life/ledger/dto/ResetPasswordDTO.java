package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 忘记密码 → 重置密码参数
 */
@Data
@ToString
public class ResetPasswordDTO implements Serializable {

    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 新密码，8-32 位，需含大小写+特殊符号，不能含空格；禁止出现在操作日志中 */
    @ToString.Exclude
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需为8-32位")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\\s])\\S{8,32}$",
            message = "密码需同时包含大写字母、小写字母和特殊符号，且不能包含空格")
    private String newPassword;
}
