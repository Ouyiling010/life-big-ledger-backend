package com.life.ledger.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 快捷记账模板 VO
 */
@Data
public class BillTemplateVO implements Serializable {

    private Long id;

    private String name;

    /** 1支出 2收入 3转账 */
    private Integer type;

    private Long amount;

    private Long categoryId;

    private String categoryName;

    private Long accountId;

    private String accountName;

    private Long bookId;

    private String bookName;

    private String remark;

    private Integer useCount;

    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
