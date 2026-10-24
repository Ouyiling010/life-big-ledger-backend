package com.life.ledger.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.dto.BillTemplateDTO;
import com.life.ledger.entity.BillTemplate;
import com.life.ledger.entity.Book;
import com.life.ledger.mapper.AccountMapper;
import com.life.ledger.mapper.BillTemplateMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.CategoryMapper;
import com.life.ledger.service.AccountService;
import com.life.ledger.service.BillTemplateService;
import com.life.ledger.service.CategoryService;
import com.life.ledger.service.ContextService;
import com.life.ledger.vo.BillTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 快捷记账模板实现
 */
@Service
@RequiredArgsConstructor
public class BillTemplateServiceImpl implements BillTemplateService {

    private final BillTemplateMapper billTemplateMapper;
    private final CategoryMapper categoryMapper;
    private final AccountMapper accountMapper;
    private final BookMapper bookMapper;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final ContextService contextService;

    @Override
    public List<BillTemplateVO> list() {
        List<BillTemplate> templates = billTemplateMapper.selectList(Wrappers.<BillTemplate>lambdaQuery()
                .eq(BillTemplate::getUserId, contextService.currentUserId())
                .orderByAsc(BillTemplate::getSort)
                .orderByAsc(BillTemplate::getId));
        return buildVoList(templates);
    }

    @Override
    public BillTemplateVO create(BillTemplateDTO dto) {
        BillTemplate template = new BillTemplate();
        template.setUserId(contextService.currentUserId());
        applyDto(template, dto);
        template.setUseCount(0);
        if (template.getSort() == null) {
            template.setSort(0);
        }
        billTemplateMapper.insert(template);
        return buildVoList(List.of(template)).get(0);
    }

    @Override
    public void update(Long id, BillTemplateDTO dto) {
        BillTemplate template = loadOwned(id);
        applyDto(template, dto);
        billTemplateMapper.updateById(template);
    }

    @Override
    public void delete(Long id) {
        loadOwned(id);
        billTemplateMapper.deleteById(id);
    }

    @Override
    public BillTemplateVO use(Long id) {
        BillTemplate template = loadOwned(id);
        template.setUseCount(template.getUseCount() == null ? 1 : template.getUseCount() + 1);
        billTemplateMapper.updateById(template);
        return buildVoList(List.of(template)).get(0);
    }

    private void applyDto(BillTemplate template, BillTemplateDTO dto) {
        validateRefs(dto);
        template.setName(dto.getName());
        template.setType(dto.getType());
        template.setAmount(dto.getAmount());
        template.setCategoryId(dto.getCategoryId());
        template.setAccountId(dto.getAccountId());
        template.setBookId(dto.getBookId());
        template.setRemark(dto.getRemark());
        if (dto.getSort() != null) {
            template.setSort(dto.getSort());
        }
    }

    /** 模板引用的分类/账户/账本必须属于当前上下文 */
    private void validateRefs(BillTemplateDTO dto) {
        if (dto.getCategoryId() != null) {
            categoryService.getOwnedCategory(dto.getCategoryId());
        }
        if (dto.getAccountId() != null) {
            accountService.getOwnedAccount(dto.getAccountId());
        }
        if (dto.getBookId() != null) {
            Book book = bookMapper.selectById(dto.getBookId());
            if (book == null || !matchesContext(book)) {
                throw new BizException(400, "账本不属于当前上下文");
            }
        }
    }

    private boolean matchesContext(Book book) {
        if (!Objects.equals(book.getOwnerType(), contextService.ownerType())) {
            return false;
        }
        Long familyId = contextService.familyId();
        if (familyId != null) {
            return Objects.equals(book.getFamilyId(), familyId);
        }
        return book.getFamilyId() == null
                && Objects.equals(book.getUserId(), contextService.currentUserId());
    }

    private BillTemplate loadOwned(Long id) {
        BillTemplate template = billTemplateMapper.selectById(id);
        if (template == null) {
            throw new BizException(404, "模板不存在");
        }
        if (!contextService.currentUserId().equals(template.getUserId())) {
            throw new BizException(403, "无权操作该模板");
        }
        return template;
    }

    private List<BillTemplateVO> buildVoList(List<BillTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> accountIds = new HashSet<>();
        Set<Long> bookIds = new HashSet<>();
        for (BillTemplate t : templates) {
            if (t.getCategoryId() != null) {
                categoryIds.add(t.getCategoryId());
            }
            if (t.getAccountId() != null) {
                accountIds.add(t.getAccountId());
            }
            if (t.getBookId() != null) {
                bookIds.add(t.getBookId());
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
            bookMapper.selectBatchIds(bookIds).forEach(b -> bookNames.put(b.getId(), b.getName()));
        }
        List<BillTemplateVO> result = new ArrayList<>(templates.size());
        for (BillTemplate t : templates) {
            BillTemplateVO vo = BeanUtil.copyProperties(t, BillTemplateVO.class);
            vo.setCategoryName(categoryNames.get(t.getCategoryId()));
            vo.setAccountName(accountNames.get(t.getAccountId()));
            vo.setBookName(bookNames.get(t.getBookId()));
            result.add(vo);
        }
        return result;
    }
}
