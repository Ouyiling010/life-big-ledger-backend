package com.life.ledger.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账单 VO
 * 金额字段单位为「分」，前端自行除以 100 展示。
 */
@Data
public class BillVO implements Serializable {

    private Long id;

    private Integer ownerType;

    private Long userId;

    private Long familyId;

    private Long creatorId;

    private Long memberId;

    private Long bookId;

    private Long accountId;

    private Long toAccountId;

    private Long categoryId;

    /** 1支出 2收入 3转账 */
    private Integer type;

    private Long amount;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime happenedAt;

    private String remark;

    private String tags;

    private String image;

    private Integer isExcluded;

    private Integer source;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 关联展示字段 */
    private String categoryName;

    private String accountName;

    private String toAccountName;

    private String bookName;

    private String creatorName;

    private String memberName;
}
