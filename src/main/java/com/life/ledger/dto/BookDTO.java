package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 账本创建/编辑请求体
 */
@Data
public class BookDTO implements Serializable {

    @NotBlank(message = "账本名称不能为空")
    private String name;

    private String cover;

    private String icon;

    private String currency;

    private String description;
    
}
