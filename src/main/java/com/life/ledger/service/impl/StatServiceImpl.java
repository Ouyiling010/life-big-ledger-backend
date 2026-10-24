package com.life.ledger.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.cache.CacheService;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.entity.Family;
import com.life.ledger.entity.User;
import com.life.ledger.mapper.FamilyMapper;
import com.life.ledger.mapper.StatMapper;
import com.life.ledger.mapper.UserMapper;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.StatService;
import com.life.ledger.vo.StatOverviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计报表服务实现
 * 统一口径：ownerType + familyId + userId 由 ContextService 提供。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final StatMapper statMapper;
    private final ContextService contextService;
    private final CacheService cacheService;
    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;

    /** 统计缓存 TTL（秒） */
    private static final long CACHE_TTL = 3600L;

    @Override
    public StatOverviewVO overview(String period, String startDate, String endDate) {
        Range range = resolveRange(period, startDate, endDate);
        return loadOverview(currentScope(), range);
    }

    @Override
    public List<Map<String, Object>> categoryStat(String period, Integer billType,
                                                  String startDate, String endDate) {
        Range range = resolveRange(period, startDate, endDate);
        int type = billType == null ? Constants.BILL_EXPENSE : billType;
        return loadCategory(currentScope(), range, type);
    }

    @Override
    public List<Map<String, Object>> trend(String period, String granularity,
                                           String startDate, String endDate) {
        boolean monthly = "month".equalsIgnoreCase(granularity);
        Range range;
        if (hasCustomRange(startDate, endDate)) {
            // 自定义区间优先，仍按 granularity 决定按日/按月聚合
            range = resolveRange(period, startDate, endDate);
        } else if (monthly) {
            Year year = resolveYear(period);
            String label = (period == null || period.isBlank()) ? year.toString() : period.trim();
            range = new Range(startOfYear(year), startOfYear(year.plusYears(1)), label, false);
        } else {
            YearMonth ym = resolveMonth(period);
            range = new Range(startOfMonth(ym), startOfMonth(ym.plusMonths(1)), ym.toString(), true);
        }
        return loadTrend(currentScope(), range, monthly);
    }

    @Override
    public List<Map<String, Object>> calendar(String period, String startDate, String endDate) {
        Range range = resolveRange(period, startDate, endDate);
        Scope scope = currentScope();
        return statMapper.calendar(scope.ownerType(), scope.familyId(), scope.userId(),
                range.start(), range.end());
    }

    @Override
    public List<Map<String, Object>> accountOverview() {
        Scope scope = currentScope();
        return statMapper.accountBalance(scope.ownerType(), scope.familyId(), scope.userId());
    }

    @Override
    public List<Map<String, Object>> bookStat(String period) {
        // 账本对比口径为全量账单，period 仅用于保持接口一致（底层聚合未按时间过滤）
        resolveMonth(period);
        Scope scope = currentScope();
        return statMapper.bookStat(scope.ownerType(), scope.familyId(), scope.userId());
    }

    @Override
    public List<Map<String, Object>> memberStat(String period) {
        Scope scope = currentScope();
        if (scope.ownerType() == null || scope.ownerType() != Constants.OWNER_FAMILY
                || scope.familyId() == null) {
            return Collections.emptyList();
        }
        YearMonth ym = resolveMonth(period);
        return statMapper.memberStat(scope.familyId(), startOfMonth(ym), startOfMonth(ym.plusMonths(1)));
    }

    @Override
    public Map<String, Object> ranking(String period) {
        YearMonth ym = resolveMonth(period);
        Scope scope = currentScope();
        LocalDateTime start = startOfMonth(ym);
        LocalDateTime end = startOfMonth(ym.plusMonths(1));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topCategories",
                statMapper.topCategories(scope.ownerType(), scope.familyId(), scope.userId(), start, end));
        result.put("maxSingleExpense",
                statMapper.maxSingleExpense(scope.ownerType(), scope.familyId(), scope.userId(), start, end));
        return result;
    }

    /**
     * 预热上月统计缓存。
     * 定时任务运行在无登录态的线程中，不能走 currentScope()，因此显式遍历
     * 所有启用用户（个人口径）与所有家庭（家庭口径），逐 scope 构建并写入缓存。
     */
    @Override
    public void warmUpLastMonth() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        Range range = new Range(startOfMonth(lastMonth), startOfMonth(lastMonth.plusMonths(1)),
                lastMonth.toString(), true);
        int failed = 0;

        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getStatus, Constants.STATUS_ENABLED));
        for (User user : users) {
            failed += warmScope(new Scope(Constants.OWNER_PERSONAL, null, user.getId()), range) ? 1 : 0;
        }

        List<Family> families = familyMapper.selectList(Wrappers.<Family>lambdaQuery());
        for (Family family : families) {
            failed += warmScope(new Scope(Constants.OWNER_FAMILY, family.getId(), 0L), range) ? 1 : 0;
        }
        log.info("统计缓存预热完成: period={}, users={}, families={}, failed={}",
                lastMonth, users.size(), families.size(), failed);
    }

    @Override
    public void evictCache() {
        cacheService.deleteByPrefix(Constants.CACHE_STAT + scopeKey(currentScope()) + ":");
    }

    // ------------------------------------------------------------------ 内部方法

    /** 预热单个 scope，失败仅记录日志不影响其他 scope */
    private boolean warmScope(Scope scope, Range range) {
        try {
            loadOverview(scope, range);
            loadCategory(scope, range, Constants.BILL_EXPENSE);
            loadTrend(scope, range, false);
            return false;
        } catch (Exception e) {
            log.warn("统计缓存预热失败: scope={}, error={}", scope, e.getMessage());
            return true;
        }
    }

    private StatOverviewVO loadOverview(Scope scope, Range range) {
        String key = cacheKey(scope, range.label()) + ":overview";
        Object cached = cacheService.get(key);
        if (cached instanceof StatOverviewVO) {
            return (StatOverviewVO) cached;
        }
        StatOverviewVO vo = buildOverview(scope, range);
        cacheService.set(key, vo, CACHE_TTL);
        return vo;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadCategory(Scope scope, Range range, int type) {
        String key = cacheKey(scope, range.label()) + ":category:" + type;
        Object cached = cacheService.get(key);
        if (cached instanceof List) {
            return (List<Map<String, Object>>) cached;
        }
        List<Map<String, Object>> list = statMapper.categoryStat(
                scope.ownerType(), scope.familyId(), scope.userId(), type, range.start(), range.end());
        cacheService.set(key, list, CACHE_TTL);
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadTrend(Scope scope, Range range, boolean monthly) {
        String key = cacheKey(scope, range.label()) + ":trend:" + (monthly ? "month" : "day");
        Object cached = cacheService.get(key);
        if (cached instanceof List) {
            return (List<Map<String, Object>>) cached;
        }
        String fmt = monthly ? "%Y-%m" : "%Y-%m-%d";
        List<Map<String, Object>> list = statMapper.trend(
                scope.ownerType(), scope.familyId(), scope.userId(), fmt, range.start(), range.end());
        cacheService.set(key, list, CACHE_TTL);
        return list;
    }

    private StatOverviewVO buildOverview(Scope scope, Range range) {
        Map<String, Object> row = statMapper.overview(scope.ownerType(), scope.familyId(),
                scope.userId(), range.start(), range.end());
        long income = toLong(row == null ? null : row.get("totalIncome"));
        long expense = toLong(row == null ? null : row.get("totalExpense"));
        StatOverviewVO vo = new StatOverviewVO();
        vo.setPeriod(range.label());
        vo.setTotalIncome(income);
        vo.setTotalExpense(expense);
        vo.setBalance(income - expense);
        vo.setBillCount(toLong(row == null ? null : row.get("billCount")));
        vo.setTransferCount(toLong(row == null ? null : row.get("transferCount")));

        // 同比 / 环比：均以「支出」为口径
        long prevExpense;
        long lastYearExpense;
        if (range.monthBased()) {
            YearMonth ym = YearMonth.from(range.start());
            prevExpense = rangeExpense(scope, startOfMonth(ym.minusMonths(1)), startOfMonth(ym));
            lastYearExpense = rangeExpense(scope, startOfMonth(ym.minusYears(1)),
                    startOfMonth(ym.minusYears(1).plusMonths(1)));
        } else {
            long days = ChronoUnit.DAYS.between(range.start(), range.end());
            prevExpense = rangeExpense(scope, range.start().minusDays(days), range.start());
            lastYearExpense = rangeExpense(scope, range.start().minusYears(1), range.end().minusYears(1));
        }
        vo.setPrevMonthExpense(prevExpense);
        vo.setLastYearExpense(lastYearExpense);
        vo.setExpenseMomRate(rate(expense, prevExpense));
        vo.setExpenseYoyRate(rate(expense, lastYearExpense));
        return vo;
    }

    /** 指定区间的总支出（分） */
    private long rangeExpense(Scope scope, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> row = statMapper.overview(scope.ownerType(), scope.familyId(),
                scope.userId(), start, end);
        return toLong(row == null ? null : row.get("totalExpense"));
    }

    /** 解析时间范围：优先 startDate/endDate 自定义区间，否则按 period（yyyy-MM）整月 */
    private Range resolveRange(String period, String startDate, String endDate) {
        if (hasCustomRange(startDate, endDate)) {
            LocalDate start;
            LocalDate end;
            try {
                start = LocalDate.parse(startDate.trim());
                end = LocalDate.parse(endDate.trim());
            } catch (DateTimeParseException e) {
                throw new BizException(400, "日期格式应为 yyyy-MM-dd");
            }
            if (end.isBefore(start)) {
                throw new BizException(400, "结束日期不能早于开始日期");
            }
            return new Range(start.atStartOfDay(), end.plusDays(1).atStartOfDay(),
                    start + "~" + end, false);
        }
        if (StrUtil.isNotBlank(startDate) || StrUtil.isNotBlank(endDate)) {
            throw new BizException(400, "自定义区间需同时传 startDate 和 endDate");
        }
        YearMonth ym = resolveMonth(period);
        return new Range(startOfMonth(ym), startOfMonth(ym.plusMonths(1)), ym.toString(), true);
    }

    private boolean hasCustomRange(String startDate, String endDate) {
        return StrUtil.isNotBlank(startDate) && StrUtil.isNotBlank(endDate);
    }

    /** 增长率（%），基数为 0 时无法计算返回 null */
    private Double rate(long current, long base) {
        if (base == 0) {
            return null;
        }
        return BigDecimal.valueOf(current - base)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(base), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Scope currentScope() {
        return new Scope(contextService.ownerType(), contextService.familyId(), contextService.currentUserId());
    }

    /** 缓存 key 前缀：个人 stat:user:u:{userId}:，家庭 stat:user:f:{familyId}: */
    private String cacheKey(Scope scope, String period) {
        return Constants.CACHE_STAT + scopeKey(scope) + ":month:" + period;
    }

    /** 缓存隔离维度：家庭优先，避免同一用户切换个人/家庭时串数据 */
    private String scopeKey(Scope scope) {
        return scope.familyId() != null ? "f:" + scope.familyId() : "u:" + scope.userId();
    }

    private YearMonth resolveMonth(String period) {
        if (period == null || period.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(period.trim());
        } catch (Exception e) {
            throw new BizException(400, "period 格式应为 yyyy-MM");
        }
    }

    private Year resolveYear(String period) {
        if (period == null || period.isBlank()) {
            return Year.now();
        }
        try {
            return Year.parse(period.trim());
        } catch (Exception e) {
            throw new BizException(400, "period 格式应为 yyyy");
        }
    }

    private LocalDateTime startOfMonth(YearMonth ym) {
        return ym.atDay(1).atStartOfDay();
    }

    private LocalDateTime startOfYear(Year year) {
        return year.atDay(1).atStartOfDay();
    }

    /** 统计时间范围（end 为开区间） */
    private record Range(LocalDateTime start, LocalDateTime end, String label, boolean monthBased) {
    }

    /** 统计口径：个人=ownerType 1 + userId；家庭=ownerType 2 + familyId */
    private record Scope(Integer ownerType, Long familyId, Long userId) {
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return new BigDecimal(value.toString()).longValue();
        } catch (Exception e) {
            return 0L;
        }
    }
}
