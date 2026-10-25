package com.life.ledger.service;

import com.life.ledger.vo.StatOverviewVO;

import java.util.List;
import java.util.Map;

/**
 * 统计报表服务
 */
public interface StatService {

    /** 总览：收入、支出、结余、账单数，含同比/环比；period=yyyy-MM，或 startDate/endDate=yyyy-MM-dd */
    StatOverviewVO overview(String period, Long bookId, String startDate, String endDate);

    /** 分类占比；支持 period 或 startDate/endDate */
    List<Map<String, Object>> categoryStat(String period, Long bookId, Integer billType, String startDate, String endDate);

    /** 收支趋势 granularity=day/month；支持 period 或 startDate/endDate */
    List<Map<String, Object>> trend(String period, Long bookId, String granularity, String startDate, String endDate);

    /** 日历热力图；支持 period 或 startDate/endDate */
    List<Map<String, Object>> calendar(String period, Long bookId, String startDate, String endDate);

    /** 账户余额概览 */
    List<Map<String, Object>> accountOverview();

    /** 账本消费对比 */
    List<Map<String, Object>> bookStat(String period);

    /** 成员消费对比（家庭） */
    List<Map<String, Object>> memberStat(String period);

    /** 排行榜：Top 分类 + 最大单笔 */
    Map<String, Object> ranking(String period);

    /** 定时任务：预热上月统计缓存 */
    void warmUpLastMonth();

    /** 统计相关缓存失效 */
    void evictCache();
}