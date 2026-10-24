package com.life.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.life.ledger.config.FlexibleLocalDateTimeDeserializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 记账保存 DTO（新增/编辑共用）
 * 金额单位「分」。
 */
@Data
public class BillSaveDTO implements Serializable {

    @NotNull(message = "账本不能为空")
    private Long bookId;

    private Long accountId;

    /** 转账目标账户，仅 type=3 时必填 */
    private Long toAccountId;

    private Long categoryId;

    /** 为谁花的：family_member.id（家庭模式） */
    private Long memberId;

    /** 1支出 2收入 3转账 */
    @NotNull(message = "账单类型不能为空")
    private Integer type;

    /** 金额（分） */
    @NotNull(message = "金额不能为空")
    @Min(value = 1, message = "金额必须大于0")
    private Long amount;

    private String currency;

    /** 兼容 yyyy-MM-dd HH:mm:ss、ISO-8601（含 Z/时区）与纯日期，见 FlexibleLocalDateTimeDeserializer */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime happenedAt;

    private String remark;

    private String tags;

    private String image;

    /** 是否不计入统计：0否 1是 */
    private Integer isExcluded;

    /** 1手动 2CSV 3AI 4模板 */
    private Integer source;
}
