package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 邀请码加入家庭请求体
 */
@Data
public class FamilyJoinDTO implements Serializable {

    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
