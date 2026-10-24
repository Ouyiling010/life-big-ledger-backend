package com.life.ledger.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 账户余额校正请求体
 * targetBalance 单位「分」
 */
@Data
public class AccountAdjustDTO implements Serializable {

    @NotNull(message = "目标余额不能为空")
    private Long targetBalance;
}
