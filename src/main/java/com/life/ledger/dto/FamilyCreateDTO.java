package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建家庭请求体
 */
@Data
public class FamilyCreateDTO implements Serializable {

    @NotBlank(message = "家庭名称不能为空")
    private String name;

    private String avatar;

    private String description;
}
