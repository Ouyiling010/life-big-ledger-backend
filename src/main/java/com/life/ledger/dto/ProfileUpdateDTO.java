package com.life.ledger.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 个人资料修改参数
 */
@Data
public class ProfileUpdateDTO implements Serializable {

    private String nickname;

    private String avatar;

    /** 选填；填了必须是合法邮箱（允许空字符串表示不填） */
    @Pattern(regexp = "^$|^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱格式不正确")
    private String email;

    /** 选填；填了必须是 11 位大陆手机号 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 选填；固定枚举 0未知 1男 2女，前端可直接用下拉框 */
    @Min(value = 0, message = "性别只能是 0未知、1男、2女")
    @Max(value = 2, message = "性别只能是 0未知、1男、2女")
    private Integer gender;
}
