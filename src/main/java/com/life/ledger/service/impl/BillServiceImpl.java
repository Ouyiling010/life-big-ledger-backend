package com.life.ledger.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.common.FamilyRole;
import com.life.ledger.common.MoneyUtil;
import com.life.ledger.common.PageResult;
import com.life.ledger.dto.BillQueryDTO;
import com.life.ledger.dto.BillSaveDTO;
import com.life.ledger.entity.Bill;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.mapper.AccountMapper;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.CategoryMapper;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.mapper.UserMapper;
import com.life.ledger.service.AccountService;
import com.life.ledger.service.BillService;
import com.life.ledger.service.CategoryService;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.NotificationService;
import com.life.ledger.service.StatService;
import com.life.ledger.vo.BillExportVO;
import com.life.ledger.vo.BillVO;
import com.life.ledger.vo.RecycleBillVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账单服务实现（核心模块）
 */
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;
    private final BookMapper bookMapper;
    private final AccountMapper accountMapper;
    private final CategoryMapper categoryMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final UserMapper userMapper;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final NotificationService notificationService;
    private final ContextService contextService;
    private final StatService statService;

    // ==================== 已有接口 ====================

    @Override
    public PageResult<Bill> recyclePage(long current, long size) {
        IPage<Bill> page = billMapper.selectRecycleBin(new Page<>(current, size),
                contextService.currentUserId(), contextService.familyId());
        return PageResult.of(page.getTotal(), current, size, page.getRecords());
    }

    @Override
    public PageResult<RecycleBillVO> recyclePageVo(long current, long size) {
        IPage<Bill> page = billMapper.selectRecycleBin(new Page<>(current, size),
                contextService.currentUserId(), contextService.familyId());
        List<RecycleBillVO> vos = buildVoList(page.getRecords()).stream()
                .map(v -> BeanUtil.copyProperties(v, RecycleBillVO.class))
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), current, size, vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long billId) {
        Bill bill = billMapper.selectDeletedById(billId);
        if (bill == null) {
            throw new BizException(404, "账单不存在");
        }
        checkWritable(bill);
        billMapper.restoreById(billId);
        // 恢复后重新纳入余额推算
        recalcAccounts(bill.getAccountId(), bill.getToAccountId());
        invalidateStatCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purge(Long billId) {
        Bill bill = billMapper.selectDeletedById(billId);
        if (bill == null) {
            throw new BizException(404, "账单不存在");
        }
        checkWritable(bill);
        billMapper.physicalDeleteById(billId);
    }

    @Override
    public int purgeDeletedBefore(LocalDateTime before) {
        return billMapper.purgeDeletedBefore(before);
    }

    @Override
    public long countAll() {
        return billMapper.selectCount(null);
    }

    @Override
    public long countSince(LocalDateTime time) {
        return billMapper.selectCount(Wrappers.<Bill>lambdaQuery().ge(Bill::getCreateTime, time));
    }

    @Override
    public List<Bill> listAllForExport() {
        Integer ownerType = contextService.ownerType();
        Long userId = contextService.currentUserId();
        List<Long> userIds = new ArrayList<>();
        if (ownerType == Constants.OWNER_FAMILY) {
            Long familyId = contextService.familyId();
            List<FamilyMember> members = familyMemberMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                    .eq(FamilyMember::getFamilyId, familyId)
                    .eq(FamilyMember::getStatus, 1));
            members.forEach(m -> userIds.add(m.getUserId()));
        }
        if (userIds.isEmpty()) {
            userIds.add(userId);
        }
        return billMapper.selectForExport(userIds);
    }

    // ==================== CRUD ====================

    @Override
    public PageResult<BillVO> page(BillQueryDTO query) {
        long current = query == null || query.getCurrent() == null ? 1L : query.getCurrent();
        long size = query == null || query.getSize() == null ? 10L : query.getSize();
        LambdaQueryWrapper<Bill> wrapper = buildWrapper(query);
        IPage<Bill> page = billMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page.getTotal(), current, size, buildVoList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill create(BillSaveDTO dto) {
        checkType(dto.getType());
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new BizException(400, "金额必须大于0");
        }
        Long userId = contextService.currentUserId();
        Integer ownerType = contextService.ownerType();
        Long familyId = contextService.familyId();
        Long creatorId = contextService.creatorId();

        validateSave(dto, ownerType, userId, familyId);

        // 🌟🌟🌟 新增：防重复提交校验（同一用户/家庭，1分钟内，金额、类型与备注完全一致的账单）
        LocalDateTime checkTime = LocalDateTime.now().minusMinutes(1);
        LambdaQueryWrapper<Bill> duplicateWrapper = Wrappers.<Bill>lambdaQuery()
                .eq(Bill::getUserId, userId)
                .eq(ownerType == Constants.OWNER_FAMILY, Bill::getFamilyId, familyId)
                .eq(Bill::getAmount, dto.getAmount())
                .eq(Bill::getType, dto.getType())
                .eq(StrUtil.isNotBlank(dto.getRemark()), Bill::getRemark, dto.getRemark())
                .ge(Bill::getCreateTime, checkTime);

        if (billMapper.selectCount(duplicateWrapper) > 0) {
            throw new BizException(400, "检测到1分钟内有一笔相同的账单，请勿重复提交！");
        }
        // 🌟🌟🌟 防重校验结束

        Bill bill = new Bill();
        bill.setOwnerType(ownerType);
        bill.setUserId(userId);
        bill.setFamilyId(familyId);
        bill.setCreatorId(creatorId);
        bill.setMemberId(ownerType == Constants.OWNER_FAMILY ? dto.getMemberId() : null);
        bill.setBookId(dto.getBookId());
        bill.setAccountId(dto.getAccountId());
        bill.setToAccountId(dto.getType() == Constants.BILL_TRANSFER ? dto.getToAccountId() : null);
        bill.setCategoryId(dto.getCategoryId());
        bill.setType(dto.getType());
        bill.setAmount(dto.getAmount());
        bill.setCurrency(StrUtil.isBlank(dto.getCurrency()) ? "CNY" : dto.getCurrency());
        bill.setHappenedAt(dto.getHappenedAt() == null ? LocalDateTime.now() : dto.getHappenedAt());
        bill.setRemark(dto.getRemark());
        bill.setTags(dto.getTags());
        bill.setImage(dto.getImage());
        bill.setIsExcluded(dto.getIsExcluded() == null ? 0 : dto.getIsExcluded());
        bill.setSource(dto.getSource() == null ? Constants.SOURCE_MANUAL : dto.getSource());

        billMapper.insert(bill);

        recalcAccounts(bill.getAccountId(), bill.getToAccountId());
        if (ownerType == Constants.OWNER_FAMILY) {
            notificationService.pushNewBill(familyId, userId, bill.getRemark(), bill.getId());
        }
        invalidateStatCache();
        return bill;
    }

    @Override
    public BillVO detail(Long billId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BizException(404, "账单不存在");
        }
        checkContextOwnership(bill);
        List<BillVO> vos = buildVoList(List.of(bill));
        return vos.isEmpty() ? null : vos.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long billId, BillSaveDTO dto) {
        Bill bill = loadForWrite(billId);
        checkType(dto.getType());
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new BizException(400, "金额必须大于0");
        }
        Long userId = contextService.currentUserId();
        Integer ownerType = contextService.ownerType();
        Long familyId = contextService.familyId();

        validateSave(dto, ownerType, userId, familyId);

        Long oldAccountId = bill.getAccountId();
        Long oldToAccountId = bill.getToAccountId();

        bill.setMemberId(ownerType == Constants.OWNER_FAMILY ? dto.getMemberId() : null);
        bill.setBookId(dto.getBookId());
        bill.setAccountId(dto.getAccountId());
        bill.setToAccountId(dto.getType() == Constants.BILL_TRANSFER ? dto.getToAccountId() : null);
        bill.setCategoryId(dto.getCategoryId());
        bill.setType(dto.getType());
        bill.setAmount(dto.getAmount());
        if (StrUtil.isNotBlank(dto.getCurrency())) {
            bill.setCurrency(dto.getCurrency());
        }
        if (dto.getHappenedAt() != null) {
            bill.setHappenedAt(dto.getHappenedAt());
        }
        bill.setRemark(dto.getRemark());
        bill.setTags(dto.getTags());
        bill.setImage(dto.getImage());
        if (dto.getIsExcluded() != null) {
            bill.setIsExcluded(dto.getIsExcluded());
        }
        billMapper.updateById(bill);

        recalcAccounts(oldAccountId, oldToAccountId);
        recalcAccounts(bill.getAccountId(), bill.getToAccountId());
        invalidateStatCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long billId) {
        Bill bill = loadForWrite(billId);
        billMapper.deleteById(billId);
        recalcAccounts(bill.getAccountId(), bill.getToAccountId());
        invalidateStatCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Set<Long> accounts = new LinkedHashSet<>();
        int count = 0;
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            Bill bill = loadForWrite(id);
            billMapper.deleteById(id);
            if (bill.getAccountId() != null) {
                accounts.add(bill.getAccountId());
            }
            if (bill.getToAccountId() != null) {
                accounts.add(bill.getToAccountId());
            }
            count++;
        }
        accounts.forEach(accountService::recalcBalance);
        invalidateStatCache();
        return count;
    }

    // ==================== 导出 ====================

    @Override
    public void export(BillQueryDTO query, HttpServletResponse response) {
        LambdaQueryWrapper<Bill> wrapper = buildWrapper(query);
        List<Bill> bills = billMapper.selectList(wrapper);
        List<BillExportVO> rows = buildVoList(bills).stream().map(this::toExportVo).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("bills", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        try {
            EasyExcel.write(response.getOutputStream(), BillExportVO.class).sheet("账单").doWrite(rows);
        } catch (IOException e) {
            throw new BizException(500, "导出失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private void checkType(Integer type) {
        if (type == null || (type != Constants.BILL_EXPENSE
                && type != Constants.BILL_INCOME && type != Constants.BILL_TRANSFER)) {
            throw new BizException(400, "非法的账单类型");
        }
    }

    /** 校验账本/账户/分类/成员归属当前上下文 */
    private void validateSave(BillSaveDTO dto, Integer ownerType, Long userId, Long familyId) {
        if (dto.getAccountId() == null) {
            throw new BizException(400, "账户不能为空");
        }
        if (dto.getBookId() == null) {
            throw new BizException(400, "账本不能为空");
        }
        Book book = bookMapper.selectById(dto.getBookId());
        if (book == null || !belongsToContext(book.getOwnerType(), book.getUserId(), book.getFamilyId(),
                ownerType, userId, familyId)) {
            throw new BizException(400, "账本不属于当前上下文");
        }
        accountService.getOwnedAccount(dto.getAccountId());
        if (dto.getType() == Constants.BILL_TRANSFER) {
            if (dto.getToAccountId() == null) {
                throw new BizException(400, "转账必须指定目标账户");
            }
            if (dto.getToAccountId().equals(dto.getAccountId())) {
                throw new BizException(400, "转账目标账户不能与转出账户相同");
            }
            accountService.getOwnedAccount(dto.getToAccountId());
        }
        if (dto.getCategoryId() != null) {
            categoryService.getOwnedCategory(dto.getCategoryId());
        }
        if (ownerType == Constants.OWNER_FAMILY && dto.getMemberId() != null) {
            FamilyMember member = familyMemberMapper.selectById(dto.getMemberId());
            if (member == null || !familyId.equals(member.getFamilyId())
                    || member.getStatus() == null || member.getStatus() != 1) {
                throw new BizException(400, "成员不属于当前家庭");
            }
        }
    }

    private boolean belongsToContext(Integer ownerType, Long userId, Long familyId,
                                     Integer ctxOwnerType, Long ctxUserId, Long ctxFamilyId) {
        if (ownerType == null || !ownerType.equals(ctxOwnerType)) {
            return false;
        }
        if (ctxOwnerType == Constants.OWNER_FAMILY) {
            return ctxFamilyId != null && ctxFamilyId.equals(familyId);
        }
        return ctxUserId.equals(userId) && familyId == null;
    }

    /** 读取并校验写权限（家庭 MEMBER 仅能操作自己创建的） */
    private Bill loadForWrite(Long billId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BizException(404, "账单不存在");
        }
        checkWritable(bill);
        return bill;
    }

    /** 写权限：上下文归属 + 家庭 MEMBER 仅能操作本人创建的账单（回收站同样适用） */
    private void checkWritable(Bill bill) {
        checkContextOwnership(bill);
        if (bill.getOwnerType() != null && bill.getOwnerType() == Constants.OWNER_FAMILY && isMemberRole()
                && !java.util.Objects.equals(bill.getCreatorId(), contextService.creatorId())) {
            throw new BizException(403, "仅能操作本人创建的账单");
        }
    }

    /** 校验账单归属当前上下文 */
    private void checkContextOwnership(Bill bill) {
        Integer ownerType = contextService.ownerType();
        if (bill.getOwnerType() == null || !bill.getOwnerType().equals(ownerType)) {
            throw new BizException(403, "无权访问该账单");
        }
        if (ownerType == Constants.OWNER_FAMILY) {
            Long familyId = contextService.familyId();
            if (familyId == null || !familyId.equals(bill.getFamilyId())) {
                throw new BizException(403, "无权访问该账单");
            }
        } else {
            Long userId = contextService.currentUserId();
            if (!userId.equals(bill.getUserId()) || bill.getFamilyId() != null) {
                throw new BizException(403, "无权访问该账单");
            }
        }
    }

    private boolean isMemberRole() {
        if (contextService.ownerType() != Constants.OWNER_FAMILY) {
            return false;
        }
        FamilyMember member = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, contextService.familyId())
                .eq(FamilyMember::getUserId, contextService.currentUserId())
                .eq(FamilyMember::getStatus, 1));
        return member != null && FamilyRole.of(member.getRole()) == FamilyRole.MEMBER;
    }

    private void recalcAccounts(Long accountId, Long toAccountId) {
        Set<Long> ids = new LinkedHashSet<>();
        if (accountId != null) {
            ids.add(accountId);
        }
        if (toAccountId != null) {
            ids.add(toAccountId);
        }
        ids.forEach(accountService::recalcBalance);
    }

    private void invalidateStatCache() {
        // 按当前 scope（个人 userId / 家庭 familyId）失效，家庭成员记账后共享统计同步失效
        statService.evictCache();
    }

    private LambdaQueryWrapper<Bill> buildWrapper(BillQueryDTO q) {
        Integer ownerType = contextService.ownerType();
        Long userId = contextService.currentUserId();
        Long familyId = contextService.familyId();
        LambdaQueryWrapper<Bill> w = Wrappers.lambdaQuery();
        w.eq(Bill::getOwnerType, ownerType);
        if (ownerType == Constants.OWNER_FAMILY) {
            w.eq(Bill::getFamilyId, familyId);
        } else {
            w.eq(Bill::getUserId, userId).isNull(Bill::getFamilyId);
        }
        if (q != null) {
            if (q.getStartTime() != null) {
                w.ge(Bill::getHappenedAt, q.getStartTime());
            }
            if (q.getEndTime() != null) {
                w.le(Bill::getHappenedAt, q.getEndTime());
            }
            if (q.getType() != null) {
                w.eq(Bill::getType, q.getType());
            }
            if (q.getCategoryId() != null) {
                w.eq(Bill::getCategoryId, q.getCategoryId());
            }
            if (q.getAccountId() != null) {
                Long accountId = q.getAccountId();
                w.and(x -> x.eq(Bill::getAccountId, accountId).or().eq(Bill::getToAccountId, accountId));
            }
            if (q.getBookId() != null) {
                w.eq(Bill::getBookId, q.getBookId());
            }
            if (q.getCreatorId() != null) {
                w.eq(Bill::getCreatorId, q.getCreatorId());
            }
            if (q.getMemberId() != null) {
                w.eq(Bill::getMemberId, q.getMemberId());
            }
            if (q.getMinAmount() != null) {
                w.ge(Bill::getAmount, q.getMinAmount());
            }
            if (q.getMaxAmount() != null) {
                w.le(Bill::getAmount, q.getMaxAmount());
            }
            if (StrUtil.isNotBlank(q.getKeyword())) {
                w.like(Bill::getRemark, q.getKeyword());
            }
            if (StrUtil.isNotBlank(q.getTag())) {
                w.like(Bill::getTags, q.getTag());
            }
        }
        w.orderByDesc(Bill::getHappenedAt).orderByDesc(Bill::getId);
        return w;
    }

    private List<BillVO> buildVoList(List<Bill> bills) {
        if (bills == null || bills.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> accountIds = new HashSet<>();
        Set<Long> bookIds = new HashSet<>();
        Set<Long> memberIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (Bill b : bills) {
            if (b.getCategoryId() != null) {
                categoryIds.add(b.getCategoryId());
            }
            if (b.getAccountId() != null) {
                accountIds.add(b.getAccountId());
            }
            if (b.getToAccountId() != null) {
                accountIds.add(b.getToAccountId());
            }
            if (b.getBookId() != null) {
                bookIds.add(b.getBookId());
            }
            if (b.getUserId() != null) {
                userIds.add(b.getUserId());
            }
            boolean family = b.getOwnerType() != null && b.getOwnerType() == Constants.OWNER_FAMILY;
            if (family) {
                if (b.getCreatorId() != null) {
                    memberIds.add(b.getCreatorId());
                }
                if (b.getMemberId() != null) {
                    memberIds.add(b.getMemberId());
                }
            } else if (b.getCreatorId() != null) {
                userIds.add(b.getCreatorId());
            }
        }

        Map<Long, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryMapper.selectBatchIds(categoryIds).forEach(c -> categoryNames.put(c.getId(), c.getName()));
        }
        Map<Long, String> accountNames = new HashMap<>();
        if (!accountIds.isEmpty()) {
            accountMapper.selectBatchIds(accountIds).forEach(a -> accountNames.put(a.getId(), a.getName()));
        }
        Map<Long, String> bookNames = new HashMap<>();
        if (!bookIds.isEmpty()) {
            bookMapper.selectBatchIds(bookIds).forEach(bk -> bookNames.put(bk.getId(), bk.getName()));
        }
        Map<Long, FamilyMember> members = new HashMap<>();
        if (!memberIds.isEmpty()) {
            familyMemberMapper.selectBatchIds(memberIds).forEach(m -> {
                members.put(m.getId(), m);
                if (m.getUserId() != null) {
                    userIds.add(m.getUserId());
                }
            });
        }
        Map<Long, String> userNames = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userNames.put(u.getId(), u.getNickname()));
        }

        List<BillVO> result = new ArrayList<>(bills.size());
        for (Bill b : bills) {
            BillVO vo = BeanUtil.copyProperties(b, BillVO.class);
            vo.setCategoryName(categoryNames.get(b.getCategoryId()));
            vo.setAccountName(accountNames.get(b.getAccountId()));
            vo.setToAccountName(accountNames.get(b.getToAccountId()));
            vo.setBookName(bookNames.get(b.getBookId()));
            boolean family = b.getOwnerType() != null && b.getOwnerType() == Constants.OWNER_FAMILY;
            vo.setCreatorName(family ? memberName(members, userNames, b.getCreatorId(), b.getUserId())
                    : userNames.get(b.getCreatorId()));
            vo.setMemberName(family ? memberName(members, userNames, b.getMemberId(), null) : null);
            result.add(vo);
        }
        return result;
    }

    private String memberName(Map<Long, FamilyMember> members, Map<Long, String> userNames,
                              Long memberId, Long fallbackUserId) {
        if (memberId != null) {
            FamilyMember m = members.get(memberId);
            if (m != null) {
                if (StrUtil.isNotBlank(m.getNicknameInFamily())) {
                    return m.getNicknameInFamily();
                }
                return userNames.get(m.getUserId());
            }
        }
        return fallbackUserId == null ? null : userNames.get(fallbackUserId);
    }

    private BillExportVO toExportVo(BillVO vo) {
        BillExportVO e = new BillExportVO();
        e.setHappenedAt(vo.getHappenedAt() == null ? "" : vo.getHappenedAt().toString().replace('T', ' '));
        e.setType(typeName(vo.getType()));
        e.setAmount(MoneyUtil.toYuan(vo.getAmount()).toPlainString());
        e.setCategoryName(vo.getCategoryName());
        e.setAccountName(vo.getAccountName());
        e.setBookName(vo.getBookName());
        e.setRemark(vo.getRemark());
        e.setTags(vo.getTags());
        e.setCreatorName(vo.getCreatorName());
        return e;
    }

    private String typeName(Integer type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case Constants.BILL_EXPENSE:
                return "支出";
            case Constants.BILL_INCOME:
                return "收入";
            case Constants.BILL_TRANSFER:
                return "转账";
            default:
                return "";
        }
    }
}
