package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类创建/编辑请求体
 */
@Data
public class CategoryDTO implements Serializable {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** 父分类ID，空表示一级分类 */
    private Long parentId;

    private String icon;

    private String color;

    /** 1支出 2收入 */
    private Integer type;
}
