package com.life.ledger.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 上下文切换参数
 */
@Data
public class ContextSwitchDTO implements Serializable {

    /** 固定枚举 1个人 2家庭，前端可直接用下拉框；家庭模式必须同时传 familyId */
    @NotNull(message = "上下文类型不能为空")
    @Min(value = 1, message = "上下文只能是 1个人 或 2家庭")
    @Max(value = 2, message = "上下文只能是 1个人 或 2家庭")
    private Integer context;

    /** 家庭模式必填 */
    private Long familyId;
}
