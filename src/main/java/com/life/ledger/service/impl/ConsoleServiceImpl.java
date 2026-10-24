package com.life.ledger.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.life.ledger.common.BizException;
import com.life.ledger.common.PageResult;
import com.life.ledger.entity.AiLog;
import com.life.ledger.entity.Account;
import com.life.ledger.entity.Bill;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.Family;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.entity.User;
import com.life.ledger.mapper.AccountMapper;
import com.life.ledger.mapper.AiLogMapper;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.FamilyMapper;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.mapper.UserMapper;
import com.life.ledger.service.ConsoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 隐藏后台服务实现：仅做只读统计与状态操作，不返回任何敏感明细
 */
@Service
@RequiredArgsConstructor
public class ConsoleServiceImpl implements ConsoleService {

    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;
    private final BookMapper bookMapper;
    private final AccountMapper accountMapper;
    private final BillMapper billMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final AiLogMapper aiLogMapper;

    @Override
    public Map<String, Object> stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalFamilies", familyMapper.selectCount(null));
        data.put("totalBooks", bookMapper.selectCount(null));
        data.put("totalBills", billMapper.selectCount(null));
        data.put("todayNewUsers", userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .ge(User::getCreateTime, todayStart)));
        data.put("todayNewBills", billMapper.selectCount(Wrappers.<Bill>lambdaQuery()
                .ge(Bill::getCreateTime, todayStart)));
        return data;
    }

    @Override
    public PageResult<Map<String, Object>> users(long current, long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery().orderByDesc(User::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        Page<User> page = userMapper.selectPage(new Page<>(current, size), wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (User user : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("email", user.getEmail());
            item.put("phone", user.getPhone());
            item.put("status", user.getStatus());
            item.put("isAdmin", user.getIsAdmin());
            item.put("createTime", user.getCreateTime());
            records.add(item);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(400, "状态不合法");
        }
        User existing = userMapper.selectById(userId);
        if (existing == null) {
            throw new BizException(404, "用户不存在");
        }
        User update = new User();
        update.setId(userId);
        update.setStatus(status);
        userMapper.updateById(update);
    }

    @Override
    public PageResult<Map<String, Object>> families(long current, long size, String keyword) {
        LambdaQueryWrapper<Family> wrapper = Wrappers.<Family>lambdaQuery().orderByDesc(Family::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Family::getName, keyword);
        }
        Page<Family> page = familyMapper.selectPage(new Page<>(current, size), wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Family family : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", family.getId());
            item.put("name", family.getName());
            item.put("ownerId", family.getOwnerId());
            item.put("memberCount", family.getMemberCount());
            item.put("createTime", family.getCreateTime());
            records.add(item);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public Map<String, Object> familyDetail(Long familyId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("familyId", familyId);
        data.put("memberCount", familyMemberMapper.selectCount(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getStatus, 1)));
        data.put("bookCount", bookMapper.selectCount(Wrappers.<Book>lambdaQuery()
                .eq(Book::getFamilyId, familyId)));
        data.put("accountCount", accountMapper.selectCount(Wrappers.<Account>lambdaQuery()
                .eq(Account::getFamilyId, familyId)));
        data.put("billCount", billMapper.selectCount(Wrappers.<Bill>lambdaQuery()
                .eq(Bill::getFamilyId, familyId)
                .eq(Bill::getIsExcluded, 0)));

        QueryWrapper<Bill> sumWrapper = Wrappers.<Bill>query()
                .select("IFNULL(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS totalExpense",
                        "IFNULL(SUM(CASE WHEN type = 2 THEN amount ELSE 0 END), 0) AS totalIncome")
                .eq("family_id", familyId)
                .eq("is_excluded", 0);
        List<Map<String, Object>> sums = billMapper.selectMaps(sumWrapper);
        Map<String, Object> row = sums.isEmpty() ? Collections.emptyMap() : sums.get(0);
        data.put("totalExpense", Convert.toLong(row.get("totalExpense"), 0L));
        data.put("totalIncome", Convert.toLong(row.get("totalIncome"), 0L));
        return data;
    }

    @Override
    public Map<String, Object> aiStats(String period) {
        QueryWrapper<AiLog> wrapper = Wrappers.<AiLog>query()
                .select("COUNT(*) AS totalCalls",
                        "IFNULL(SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END), 0) AS successCalls",
                        "IFNULL(AVG(cost_ms), 0) AS avgCostMs",
                        "IFNULL(SUM(tokens), 0) AS totalTokens");
        if (StringUtils.hasText(period)) {
            try {
                YearMonth ym = YearMonth.parse(period.trim());
                wrapper.ge("create_time", ym.atDay(1).atStartOfDay())
                        .lt("create_time", ym.plusMonths(1).atDay(1).atStartOfDay());
            } catch (Exception ignored) {
                // 非法 period 时忽略时间过滤
            }
        }
        List<Map<String, Object>> rows = aiLogMapper.selectMaps(wrapper);
        Map<String, Object> row = rows.isEmpty() ? Collections.emptyMap() : rows.get(0);
        long totalCalls = Convert.toLong(row.get("totalCalls"), 0L);
        long successCalls = Convert.toLong(row.get("successCalls"), 0L);
        double avgCostMs = Convert.toDouble(row.get("avgCostMs"), 0.0);
        long totalTokens = Convert.toLong(row.get("totalTokens"), 0L);
        double successRate = totalCalls == 0 ? 0.0
                : Math.round(successCalls * 10000.0 / totalCalls) / 100.0;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalCalls", totalCalls);
        data.put("successCalls", successCalls);
        data.put("successRate", successRate);
        data.put("avgCostMs", Math.round(avgCostMs));
        data.put("totalTokens", totalTokens);
        return data;
    }
}
