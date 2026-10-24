package com.life.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 账户创建/编辑请求体
 * initBalance 单位「分」
 */
@Data
public class AccountDTO implements Serializable {

    @NotBlank(message = "账户名称不能为空")
    private String name;

    /** 1现金 2储蓄卡 3信用卡 4虚拟 */
    private Integer type;

    private Long initBalance;

    private String icon;

    private String remark;

    /** 归属家庭成员（family_member.id），可空 */
    private Long belongMemberId;

    /** 0否 1是，是否计入总资产 */
    private Integer includeInTotal;
}
