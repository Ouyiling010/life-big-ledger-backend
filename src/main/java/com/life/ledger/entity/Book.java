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
 * 账本表（场景维度，无余额）
 */
@Data
@TableName("book")
public class Book implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 1个人 2家庭 */
    private Integer ownerType;

    private Long userId;

    private Long familyId;

    private Long creatorId;

    private String name;

    private String cover;

    private String icon;

    private String currency;

    private String description;

    /** 0否 1是 */
    private Integer isDefault;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
