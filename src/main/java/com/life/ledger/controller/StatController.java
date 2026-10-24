package com.life.ledger.controller;

import com.life.ledger.common.Result;
import com.life.ledger.service.StatService;
import com.life.ledger.vo.StatOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计报表接口
 */
@Tag(name = "统计报表")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    /** 总览 */
    @Operation(summary = "总览（收入/支出/结余/账单数/同比环比）")
    @GetMapping("/overview")
    public Result<StatOverviewVO> overview(@RequestParam(required = false) String period,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate) {
        return Result.ok(statService.overview(period, startDate, endDate));
    }

    /** 分类占比 */
    @Operation(summary = "分类占比（饼图）")
    @GetMapping("/category")
    public Result<List<Map<String, Object>>> category(@RequestParam(required = false) String period,
                                                      @RequestParam(required = false) Integer type,
                                                      @RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate) {
        return Result.ok(statService.categoryStat(period, type, startDate, endDate));
    }

    /** 收支趋势 */
    @Operation(summary = "收支趋势（折线图，按日/月）")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(required = false) String period,
                                                   @RequestParam(required = false) String granularity,
                                                   @RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        return Result.ok(statService.trend(period, granularity, startDate, endDate));
    }

    /** 日历热力图 */
    @Operation(summary = "日历热力图")
    @GetMapping("/calendar")
    public Result<List<Map<String, Object>>> calendar(@RequestParam(required = false) String period,
                                                      @RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate) {
        return Result.ok(statService.calendar(period, startDate, endDate));
    }

    /** 账户余额概览 */
    @Operation(summary = "账户余额概览")
    @GetMapping("/accounts")
    public Result<List<Map<String, Object>>> accounts() {
        return Result.ok(statService.accountOverview());
    }

    /** 账本消费对比 */
    @Operation(summary = "账本消费对比")
    @GetMapping("/books")
    public Result<List<Map<String, Object>>> books(@RequestParam(required = false) String period) {
        return Result.ok(statService.bookStat(period));
    }

    /** 成员消费对比（仅家庭） */
    @Operation(summary = "成员消费对比（仅家庭）")
    @GetMapping("/members")
    public Result<List<Map<String, Object>>> members(@RequestParam(required = false) String period) {
        return Result.ok(statService.memberStat(period));
    }

    /** 排行榜：Top 分类 + 最大单笔 */
    @Operation(summary = "排行榜（Top5 分类 + 最大单笔）")
    @GetMapping("/ranking")
    public Result<Map<String, Object>> ranking(@RequestParam(required = false) String period) {
        return Result.ok(statService.ranking(period));
    }
}
