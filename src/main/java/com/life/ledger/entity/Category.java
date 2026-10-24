package com.life.ledger.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类表（用途维度，两级树）
 */
@Data
@TableName("category")
public class Category implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 0系统 1个人 2家庭 */
    private Integer ownerType;

    private Long userId;

    private Long familyId;

    private Long parentId;

    private String name;

    private String icon;

    private String color;

    /** 1支出 2收入 */
    private Integer type;

    private Integer isSystem;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
