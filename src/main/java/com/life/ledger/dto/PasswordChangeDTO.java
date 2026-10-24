package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 修改密码参数
 */
@Data
@ToString
public class PasswordChangeDTO implements Serializable {

    /** 原密码，禁止出现在操作日志中 */
    @ToString.Exclude
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码，禁止出现在操作日志中；8-32 位，需含大写、小写和特殊符号，且不能有空格 */
    @ToString.Exclude
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "新密码长度需为8-32位")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\\s])\\S{8,32}$",
            message = "新密码需同时包含大写字母、小写字母和特殊符号，且不能包含空格")
    private String newPassword;
}
