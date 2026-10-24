package com.life.ledger.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账单分页查询 DTO
 */
@Data
public class BillQueryDTO implements Serializable {

    private Long current;

    private Long size;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 1支出 2收入 3转账 */
    private Integer type;

    private Long categoryId;

    private Long accountId;

    private Long bookId;

    /** 记账人 */
    private Long creatorId;

    /** 为谁花的（家庭成员） */
    private Long memberId;

    /** 金额区间（分） */
    private Long minAmount;

    private Long maxAmount;

    /** 关键字（备注模糊） */
    private String keyword;

    private String tag;
}
