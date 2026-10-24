package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 解析出的记账草稿
 * amount 单位为「分」。
 */
@Data
public class BillDraftVO implements Serializable {

    /** 1支出 2收入 3转账 */
    private Integer type;

    /** 金额（分） */
    private Long amount;

    private Long categoryId;

    private String categoryName;

    private Long accountId;

    private String accountName;

    private LocalDateTime happenedAt;

    private String remark;

    /** 置信度 0~1 */
    private Double confidence;
}
