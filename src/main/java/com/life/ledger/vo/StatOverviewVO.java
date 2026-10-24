package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计总览 VO
 * 金额字段单位为「分」，与数据库一致，前端自行除以 100 展示。
 */
@Data
public class StatOverviewVO implements Serializable {

    private String period;

    private Long totalIncome;

    private Long totalExpense;

    /** 结余 = 收入 - 支出 */
    private Long balance;

    private Long billCount;

    private Long transferCount;

    /** 上月支出（分），用于环比 */
    private Long prevMonthExpense;

    /** 去年同月支出（分），用于同比 */
    private Long lastYearExpense;

    /** 支出环比（%），上月支出为 0 时返回 null */
    private Double expenseMomRate;

    /** 支出同比（%），去年同月支出为 0 时返回 null */
    private Double expenseYoyRate;
}
