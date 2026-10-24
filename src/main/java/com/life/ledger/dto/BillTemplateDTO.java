package com.life.ledger.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 快捷记账模板 DTO
 */
@Data
public class BillTemplateDTO implements Serializable {

    private String name;

    /** 1支出 2收入 3转账 */
    private Integer type;

    /** 金额（分） */
    private Long amount;

    private Long categoryId;

    private Long accountId;

    private Long bookId;

    private String remark;

    private Integer sort;
}
