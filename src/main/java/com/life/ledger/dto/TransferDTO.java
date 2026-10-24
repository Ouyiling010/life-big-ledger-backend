package com.life.ledger.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 转移家庭创建者请求体
 */
@Data
public class TransferDTO implements Serializable {

    @NotNull(message = "目标成员不能为空")
    private Long targetMemberId;
}
