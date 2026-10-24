package com.life.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.common.FamilyRole;
import com.life.ledger.dto.BookDTO;
import com.life.ledger.entity.Bill;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.mapper.StatMapper;
import com.life.ledger.service.AccountService;
import com.life.ledger.service.BookService;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.StatService;
import com.life.ledger.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 账本服务实现
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BillMapper billMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final StatMapper statMapper;
    private final ContextService contextService;
    private final AccountService accountService;
    private final StatService statService;

    @Override
    public List<BookVO> listByCurrentContext() {
        List<Book> books = bookMapper.selectList(scope()
                .orderByDesc(Book::getIsDefault)
                .orderByAsc(Book::getSort)
                .orderByAsc(Book::getId));
        Map<Long, Map<String, Object>> statMap = new HashMap<>();
        List<Map<String, Object>> rows = statMapper.bookStat(contextService.ownerType(),
                contextService.familyId(), contextService.currentUserId());
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object bookId = row.get("bookId");
                if (bookId instanceof Number) {
                    statMap.put(((Number) bookId).longValue(), row);
                }
            }
        }
        List<BookVO> result = new ArrayList<>();
        for (Book book : books) {
            result.add(toVo(book, statMap.get(book.getId())));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookVO create(BookDTO dto) {
        Book book = new Book();
        book.setOwnerType(contextService.ownerType());
        book.setUserId(contextService.currentUserId());
        book.setFamilyId(contextService.familyId());
        book.setCreatorId(contextService.creatorId());
        book.setName(dto.getName());
        book.setCover(dto.getCover());
        book.setIcon(dto.getIcon());
        book.setCurrency(dto.getCurrency() == null || dto.getCurrency().isBlank() ? "CNY" : dto.getCurrency());
        book.setDescription(dto.getDescription());
        book.setSort(0);
        book.setIsDefault(countDefault() == 0 ? 1 : 0);
        bookMapper.insert(book);
        return toVo(book, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, BookDTO dto) {
        Book book = getOwnedBook(id);
        if (Objects.equals(book.getOwnerType(), Constants.OWNER_FAMILY)) {
            requireFamilyRole(book.getFamilyId(), FamilyRole.ADMIN);
        }
        if (dto.getName() != null) {
            book.setName(dto.getName());
        }
        if (dto.getCover() != null) {
            book.setCover(dto.getCover());
        }
        if (dto.getIcon() != null) {
            book.setIcon(dto.getIcon());
        }
        if (dto.getCurrency() != null) {
            book.setCurrency(dto.getCurrency());
        }
        if (dto.getDescription() != null) {
            book.setDescription(dto.getDescription());
        }
        bookMapper.updateById(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, boolean force) {
        Book book = getOwnedBook(id);
        if (Objects.equals(book.getOwnerType(), Constants.OWNER_FAMILY)) {
            requireFamilyRole(book.getFamilyId(), FamilyRole.ADMIN);
        }
        int billCount = billMapper.countByBook(id);
        if (billCount > 0 && !force) {
            throw new BizException(409, "该账本下存在 " + billCount + " 笔账单，请确认后使用 force=true 删除");
        }
        if (force) {
            // force=true：连同账本下的账单一起逻辑删除，并重算受影响账户余额
            List<Bill> bills = billMapper.selectList(Wrappers.<Bill>lambdaQuery().eq(Bill::getBookId, id));
            if (!bills.isEmpty()) {
                Set<Long> accountIds = new HashSet<>();
                for (Bill bill : bills) {
                    if (bill.getAccountId() != null) {
                        accountIds.add(bill.getAccountId());
                    }
                    if (bill.getToAccountId() != null) {
                        accountIds.add(bill.getToAccountId());
                    }
                }
                billMapper.delete(Wrappers.<Bill>lambdaQuery().eq(Bill::getBookId, id));
                accountIds.forEach(accountService::recalcBalance);
                statService.evictCache();
            }
        }
        bookMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Book book = getOwnedBook(id);
        if (Objects.equals(book.getOwnerType(), Constants.OWNER_FAMILY)) {
            requireFamilyRole(book.getFamilyId(), FamilyRole.ADMIN);
        }
        List<Book> books = bookMapper.selectList(scope().eq(Book::getIsDefault, 1));
        for (Book b : books) {
            if (!Objects.equals(b.getId(), id)) {
                b.setIsDefault(0);
                bookMapper.updateById(b);
            }
        }
        book.setIsDefault(1);
        bookMapper.updateById(book);
    }

    /* ---------------- private helpers ---------------- */

    private LambdaQueryWrapper<Book> scope() {
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

    private long countDefault() {
        Long count = bookMapper.selectCount(scope().eq(Book::getIsDefault, 1));
        return count == null ? 0L : count;
    }

    private Book getOwnedBook(Long id) {
        Book book = id == null ? null : bookMapper.selectById(id);
        if (book == null) {
            throw new BizException(404, "账本不存在");
        }
        if (!matchesContext(book)) {
            throw new BizException(403, "无权操作该账本");
        }
        return book;
    }

    private boolean matchesContext(Book book) {
        if (!Objects.equals(book.getOwnerType(), contextService.ownerType())) {
            return false;
        }
        Long familyId = contextService.familyId();
        if (familyId != null) {
            return Objects.equals(book.getFamilyId(), familyId);
        }
        return book.getFamilyId() == null && Objects.equals(book.getUserId(), contextService.currentUserId());
    }

    private void requireFamilyRole(Long familyId, FamilyRole minRole) {
        Long userId = contextService.currentUserId();
        FamilyMember member = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED));
        if (member == null || !FamilyRole.of(member.getRole()).satisfies(minRole)) {
            throw new BizException(403, "无权限操作该家庭账本");
        }
    }

    private BookVO toVo(Book book, Map<String, Object> stat) {
        BookVO vo = new BookVO();
        vo.setId(book.getId());
        vo.setName(book.getName());
        vo.setCover(book.getCover());
        vo.setIcon(book.getIcon());
        vo.setCurrency(book.getCurrency());
        vo.setDescription(book.getDescription());
        vo.setIsDefault(book.getIsDefault());
        vo.setSort(book.getSort());
        vo.setCreateTime(book.getCreateTime());
        if (stat != null) {
            vo.setBillCount(toLong(stat.get("billCount")));
            vo.setExpense(toLong(stat.get("expense")));
            vo.setIncome(toLong(stat.get("income")));
        }
        return vo;
    }

    private Long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}
