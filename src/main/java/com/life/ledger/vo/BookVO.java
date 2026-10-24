package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账本 VO
 */
@Data
public class BookVO implements Serializable {

    private Long id;

    private String name;

    private String cover;

    private String icon;

    private String currency;

    private String description;

    private Integer isDefault;

    private Integer sort;

    private Long billCount;

    private Long expense;

    private Long income;

    private LocalDateTime createTime;
}
