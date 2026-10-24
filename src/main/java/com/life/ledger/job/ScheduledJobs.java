package com.life.ledger.job;

import com.life.ledger.common.Constants;
import com.life.ledger.mapper.AiLogMapper;
import com.life.ledger.service.AccountService;
import com.life.ledger.service.BillService;
import com.life.ledger.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时任务：每日账务重算 / 回收站清理 / 统计预热 / 每周 AI 日志清理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJobs {

    private final AccountService accountService;
    private final BillService billService;
    private final StatService statService;
    private final AiLogMapper aiLogMapper;

    /** 每日 01:00 预热上月统计缓存 */
    @Scheduled(cron = "0 0 1 * * ?")
    public void warmUpStatJob() {
        try {
            statService.warmUpLastMonth();
            log.info("定时任务[统计缓存预热]执行完成");
        } catch (Exception e) {
            log.error("定时任务[统计缓存预热]执行失败", e);
        }
    }

    /** 每日 02:00 重算所有账户余额 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void recalcBalanceJob() {
        try {
            accountService.recalcAllBalance();
            log.info("定时任务[账户余额重算]执行完成");
        } catch (Exception e) {
            log.error("定时任务[账户余额重算]执行失败", e);
        }
    }

    /** 每日 03:00 清理回收站中超过 30 天的账单 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeDeletedBillsJob() {
        try {
            int count = billService.purgeDeletedBefore(
                    LocalDateTime.now().minusDays(Constants.RECYCLE_KEEP_DAYS));
            log.info("定时任务[回收站清理]执行完成，清理账单 {} 条", count);
        } catch (Exception e) {
            log.error("定时任务[回收站清理]执行失败", e);
        }
    }

    /** 每周一 04:00 清理超过 90 天的 AI 日志 */
    @Scheduled(cron = "0 0 4 ? * MON")
    public void cleanAiLogJob() {
        try {
            int count = aiLogMapper.deleteBefore(
                    LocalDateTime.now().minusDays(Constants.AI_LOG_KEEP_DAYS));
            log.info("定时任务[AI日志清理]执行完成，清理 {} 条", count);
        } catch (Exception e) {
            log.error("定时任务[AI日志清理]执行失败", e);
        }
    }
}
