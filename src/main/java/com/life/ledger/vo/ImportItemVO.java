package com.life.ledger.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CSV 导入预览明细项
 * 金额单位「分」。
 */
@Data
public class ImportItemVO implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime happenedAt;

    private Long amount;

    /** 交易对方 */
    private String counterparty;

    private String remark;

    /** 1支出 2收入（由「收/支」列推断） */
    private Integer type;

    private Long suggestedCategoryId;

    private String suggestedCategoryName;

    private Boolean isDuplicate;

    private Boolean selected;

    /** 可选：明细自带的账本/账户 */
    private Long bookId;

    private Long accountId;
}
