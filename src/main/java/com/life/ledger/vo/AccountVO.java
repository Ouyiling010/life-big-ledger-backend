package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 账户 VO
 * 金额字段单位「分」
 */
@Data
public class AccountVO implements Serializable {

    private Long id;

    private String name;

    /** 1现金 2储蓄卡 3信用卡 4虚拟 */
    private Integer type;

    private String typeName;

    private String icon;

    private Long initBalance;

    /** 推算余额（分） */
    private Long balance;

    private String remark;

    private Long belongMemberId;

    private Integer includeInTotal;

    private Integer sort;
}
