package com.life.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.cache.CacheService;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.common.FamilyRole;
import com.life.ledger.dto.AccountAdjustDTO;
import com.life.ledger.dto.AccountDTO;
import com.life.ledger.entity.Account;
import com.life.ledger.entity.Bill;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.mapper.AccountMapper;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.service.AccountService;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.StatService;
import com.life.ledger.vo.AccountVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 账户服务实现
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final BillMapper billMapper;
    private final BookMapper bookMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final ContextService contextService;
    private final CacheService cacheService;
    private final StatService statService;

    @Override
    public List<Account> listByCurrentContext() {
        return accountMapper.selectList(scope().orderByAsc(Account::getSort).orderByAsc(Account::getId));
    }

    @Override
    public List<AccountVO> listAccounts() {
        List<AccountVO> result = new ArrayList<>();
        for (Account account : listByCurrentContext()) {
            result.add(toVo(account));
        }
        return result;
    }

    @Override
    public Account getOwnedAccount(Long accountId) {
        Account account = accountId == null ? null : accountMapper.selectById(accountId);
        if (account == null) {
            throw new BizException(404, "账户不存在");
        }
        if (!matchesContext(account)) {
            throw new BizException(403, "无权操作该账户");
        }
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountVO createAccount(AccountDTO dto) {
        Account account = new Account();
        account.setOwnerType(contextService.ownerType());
        account.setUserId(contextService.currentUserId());
        account.setFamilyId(contextService.familyId());
        account.setCreatorId(contextService.creatorId());
        account.setName(dto.getName());
        account.setType(dto.getType() == null ? Constants.ACCOUNT_CASH : dto.getType());
        account.setIcon(dto.getIcon());
        account.setRemark(dto.getRemark());
        account.setBelongMemberId(dto.getBelongMemberId());
        account.setIncludeInTotal(dto.getIncludeInTotal() == null ? 1 : dto.getIncludeInTotal());
        long init = dto.getInitBalance() == null ? 0L : dto.getInitBalance();
        account.setInitBalance(init);
        account.setBalance(init);
        account.setSort(0);
        accountMapper.insert(account);
        return toVo(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccount(Long id, AccountDTO dto) {
        Account account = getOwnedAccount(id);
        requireFamilyAdmin(account);
        if (dto.getName() != null) {
            account.setName(dto.getName());
        }
        if (dto.getType() != null) {
            account.setType(dto.getType());
        }
        if (dto.getIcon() != null) {
            account.setIcon(dto.getIcon());
        }
        if (dto.getRemark() != null) {
            account.setRemark(dto.getRemark());
        }
        if (dto.getBelongMemberId() != null) {
            account.setBelongMemberId(dto.getBelongMemberId());
        }
        if (dto.getIncludeInTotal() != null) {
            account.setIncludeInTotal(dto.getIncludeInTotal());
        }
        boolean initChanged = dto.getInitBalance() != null
                && !Objects.equals(account.getInitBalance(), dto.getInitBalance());
        if (dto.getInitBalance() != null) {
            account.setInitBalance(dto.getInitBalance());
        }
        accountMapper.updateById(account);
        if (initChanged) {
            recalcBalance(id);
        }
        cacheService.delete(Constants.CACHE_ACCOUNT_BALANCE + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long id) {
        requireFamilyAdmin(getOwnedAccount(id));
        int refCount = billMapper.countByAccount(id);
        if (refCount > 0) {
            throw new BizException(409, "该账户存在 " + refCount + " 笔账单引用，无法删除");
        }
        accountMapper.deleteById(id);
        cacheService.delete(Constants.CACHE_ACCOUNT_BALANCE + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountVO adjustBalance(Long id, AccountAdjustDTO dto) {
        Account account = getOwnedAccount(id);
        requireFamilyAdmin(account);
        long diff = dto.getTargetBalance() - currentBalance(account);
        if (diff != 0) {
            Long creatorId = contextService.creatorId();
            Long familyId = contextService.familyId();
            Bill bill = new Bill();
            bill.setOwnerType(contextService.ownerType());
            bill.setUserId(contextService.currentUserId());
            bill.setFamilyId(familyId);
            bill.setCreatorId(creatorId);
            bill.setMemberId(familyId != null ? creatorId : null);
            bill.setAccountId(id);
            bill.setBookId(currentDefaultBookId());
            bill.setType(diff > 0 ? Constants.BILL_INCOME : Constants.BILL_EXPENSE);
            bill.setAmount(Math.abs(diff));
            bill.setCurrency("CNY");
            bill.setHappenedAt(LocalDateTime.now());
            bill.setRemark("余额校正");
            bill.setIsExcluded(0);
            bill.setSource(Constants.SOURCE_MANUAL);
            billMapper.insert(bill);
        }
        recalcBalance(id);
        // 余额校正会插入一条收支账单，统计缓存必须同步失效
        statService.evictCache();
        return toVo(accountMapper.selectById(id));
    }

    @Override
    public void recalcBalance(Long accountId) {
        Account account = accountId == null ? null : accountMapper.selectById(accountId);
        if (account == null) {
            return;
        }
        account.setBalance(account.getInitBalance() == null ? 0L : account.getInitBalance() + flow(accountId));
        accountMapper.updateById(account);
        cacheService.delete(Constants.CACHE_ACCOUNT_BALANCE + accountId);
    }

    @Override
    public void recalcAllBalance() {
        List<Account> accounts = accountMapper.selectList(Wrappers.<Account>lambdaQuery());
        for (Account account : accounts) {
            account.setBalance(account.getInitBalance() == null ? 0L : account.getInitBalance() + flow(account.getId()));
            accountMapper.updateById(account);
            cacheService.delete(Constants.CACHE_ACCOUNT_BALANCE + account.getId());
        }
    }

    @Override
    public long totalAsset() {
        List<Account> accounts = accountMapper.selectList(scope().eq(Account::getIncludeInTotal, 1));
        long total = 0L;
        for (Account account : accounts) {
            total += account.getBalance() == null ? 0L : account.getBalance();
        }
        return total;
    }

    /* ---------------- private helpers ---------------- */

    private LambdaQueryWrapper<Account> scope() {
        Integer ownerType = contextService.ownerType();
        Long userId = contextService.currentUserId();
        Long familyId = contextService.familyId();
        LambdaQueryWrapper<Account> q = Wrappers.<Account>lambdaQuery().eq(Account::getOwnerType, ownerType);
        if (familyId != null) {
            q.eq(Account::getFamilyId, familyId);
        } else {
            q.isNull(Account::getFamilyId).eq(Account::getUserId, userId);
        }
        return q;
    }

    private boolean matchesContext(Account account) {
        if (!Objects.equals(account.getOwnerType(), contextService.ownerType())) {
            return false;
        }
        Long familyId = contextService.familyId();
        if (familyId != null) {
            return Objects.equals(account.getFamilyId(), familyId);
        }
        return account.getFamilyId() == null && Objects.equals(account.getUserId(), contextService.currentUserId());
    }

    /** 家庭共享账户的修改/删除/余额校正需 ADMIN 及以上角色 */
    private void requireFamilyAdmin(Account account) {
        if (!Objects.equals(account.getOwnerType(), Constants.OWNER_FAMILY)) {
            return;
        }
        FamilyMember member = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, account.getFamilyId())
                .eq(FamilyMember::getUserId, contextService.currentUserId())
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED));
        if (member == null || !FamilyRole.of(member.getRole()).satisfies(FamilyRole.ADMIN)) {
            throw new BizException(403, "无权限操作该家庭账户");
        }
    }

    private long flow(Long accountId) {
        Long flow = billMapper.sumAccountFlow(accountId);
        return flow == null ? 0L : flow;
    }

    private long currentBalance(Account account) {
        return (account.getInitBalance() == null ? 0L : account.getInitBalance()) + flow(account.getId());
    }

    private Long currentDefaultBookId() {
        Book book = bookMapper.selectOne(scopeBook()
                .orderByDesc(Book::getIsDefault)
                .orderByAsc(Book::getSort)
                .last("LIMIT 1"));
        return book == null ? null : book.getId();
    }

    private LambdaQueryWrapper<Book> scopeBook() {
        Integer ownerType = contextService.ownerType();
        Long userId = contextService.currentUserId();
        Long familyId = contextService.familyId();
        LambdaQueryWrapper<Book> q = Wrappers.<Book>lambdaQuery().eq(Book::getOwnerType, ownerType);
        if (familyId != null) {
            q.eq(Book::getFamilyId, familyId);
        } else {
            q.isNull(Book::getFamilyId).eq(Book::getUserId, userId);
        }
        return q;
    }

    private AccountVO toVo(Account account) {
        AccountVO vo = new AccountVO();
        vo.setId(account.getId());
        vo.setName(account.getName());
        vo.setType(account.getType());
        vo.setTypeName(typeName(account.getType()));
        vo.setIcon(account.getIcon());
        vo.setInitBalance(account.getInitBalance());
        vo.setBalance(account.getBalance());
        vo.setRemark(account.getRemark());
        vo.setBelongMemberId(account.getBelongMemberId());
        vo.setIncludeInTotal(account.getIncludeInTotal());
        vo.setSort(account.getSort());
        return vo;
    }

    private static String typeName(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case Constants.ACCOUNT_CASH -> "现金";
            case Constants.ACCOUNT_SAVINGS -> "储蓄卡";
            case Constants.ACCOUNT_CREDIT -> "信用卡";
            case Constants.ACCOUNT_VIRTUAL -> "虚拟账户";
            default -> "其他";
        };
    }
}
