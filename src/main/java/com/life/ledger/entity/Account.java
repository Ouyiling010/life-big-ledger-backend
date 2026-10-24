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
 * 账户表（资金维度，有余额）
 */
@Data
@TableName("account")
public class Account implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 1个人 2家庭 */
    private Integer ownerType;

    private Long userId;

    private Long familyId;

    private Long creatorId;

    private Long belongMemberId;

    private String name;

    /** 1现金 2储蓄卡 3信用卡 4虚拟 */
    private Integer type;

    private String icon;

    private Long initBalance;

    private Long balance;

    private String remark;

    private Integer includeInTotal;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
