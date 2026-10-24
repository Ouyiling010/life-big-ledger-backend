package com.life.ledger.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改成员角色请求体
 */
@Data
public class RoleUpdateDTO implements Serializable {

    /** 目标角色：2ADMIN 3MEMBER */
    @NotNull(message = "角色不能为空")
    private Integer role;
}
