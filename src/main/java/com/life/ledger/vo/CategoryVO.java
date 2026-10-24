package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类 VO（两级树形结构，children 仅一级子分类）
 */
@Data
public class CategoryVO implements Serializable {

    private Long id;

    private Long parentId;

    private String name;

    private String icon;

    private String color;

    /** 1支出 2收入 */
    private Integer type;

    private Integer isSystem;

    private Integer sort;

    private List<CategoryVO> children = new ArrayList<>();
}
