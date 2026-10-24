package com.life.ledger.dto;

import com.life.ledger.email.EmailScene;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送邮箱验证码参数
 */
@Data
public class SendEmailCodeDTO implements Serializable {

    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱格式不正确")
    private String email;

    /** 场景：REGISTER / RESET_PASSWORD */
    @NotNull(message = "scene 不能为空")
    private EmailScene scene;
}
