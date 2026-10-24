package com.life.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.cache.CacheService;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.dto.CategoryDTO;
import com.life.ledger.entity.Category;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.CategoryMapper;
import com.life.ledger.service.CategoryService;
import com.life.ledger.service.ContextService;
import com.life.ledger.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final BillMapper billMapper;
    private final ContextService contextService;
    private final CacheService cacheService;

    @Override
    public List<Category> listByCurrentContext(Integer type) {
        Integer ownerType = contextService.ownerType();
        Long userId = contextService.currentUserId();
        Long familyId = contextService.familyId();
        LambdaQueryWrapper<Category> q = Wrappers.<Category>lambdaQuery();
        if (type != null) {
            q.eq(Category::getType, type);
        }
        q.and(w -> w.eq(Category::getOwnerType, Constants.OWNER_SYSTEM)
                .or(o -> {
                    o.eq(Category::getOwnerType, ownerType);
                    if (familyId != null) {
                        o.eq(Category::getFamilyId, familyId);
                    } else {
                        o.eq(Category::getUserId, userId).isNull(Category::getFamilyId);
                    }
                }));
        q.orderByAsc(Category::getType).orderByAsc(Category::getSort).orderByAsc(Category::getId);
        return categoryMapper.selectList(q);
    }

    @Override
    public Long findIdByName(String name, Integer type) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Long systemId = null;
        for (Category c : listByCurrentContext(type)) {
            if (!name.equals(c.getName())) {
                continue;
            }
            if (c.getIsSystem() == null || c.getIsSystem() != 1) {
                return c.getId();
            }
            if (systemId == null) {
                systemId = c.getId();
            }
        }
        return systemId;
    }

    @Override
    public List<Category> listForAi() {
        return listByCurrentContext(null);
    }

    @Override
    public Category getOwnedCategory(Long categoryId) {
        Category category = categoryId == null ? null : categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException(404, "分类不存在");
        }
        if (Objects.equals(category.getIsSystem(), 1)) {
            return category;
        }
        if (!matchesContext(category)) {
            throw new BizException(403, "无权操作该分类");
        }
        return category;
    }

    @Override
    public List<CategoryVO> tree(Integer type) {
        List<Category> flat = listByCurrentContext(type);
        Map<Long, CategoryVO> voMap = new LinkedHashMap<>();
        for (Category c : flat) {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setParentId(c.getParentId());
            vo.setName(c.getName());
            vo.setIcon(c.getIcon());
            vo.setColor(c.getColor());
            vo.setType(c.getType());
            vo.setIsSystem(c.getIsSystem());
            vo.setSort(c.getSort());
            voMap.put(c.getId(), vo);
        }
        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : voMap.values()) {
            CategoryVO parent = vo.getParentId() == null ? null : voMap.get(vo.getParentId());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO create(CategoryDTO dto) {
        Category parent = null;
        if (dto.getParentId() != null) {
            parent = getOwnedCategory(dto.getParentId());
            if (!isRoot(parent)) {
                throw new BizException(400, "分类最多两级，不能在子分类下继续创建");
            }
        }
        Integer type = parent != null ? parent.getType() : dto.getType();
        Category category = new Category();
        category.setOwnerType(contextService.ownerType());
        category.setUserId(contextService.currentUserId());
        category.setFamilyId(contextService.familyId());
        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setColor(dto.getColor());
        category.setType(type == null ? Constants.BILL_EXPENSE : type);
        category.setIsSystem(0);
        category.setSort(0);
        categoryMapper.insert(category);
        clearCache();
        return toVo(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryDTO dto) {
        Category category = getOwnedCategory(id);
        if (Objects.equals(category.getIsSystem(), 1)) {
            throw new BizException(403, "系统预置分类不允许修改");
        }
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getIcon() != null) {
            category.setIcon(dto.getIcon());
        }
        if (dto.getColor() != null) {
            category.setColor(dto.getColor());
        }
        if (dto.getType() != null) {
            category.setType(dto.getType());
        }
        if (dto.getParentId() != null && !Objects.equals(dto.getParentId(), id)) {
            Category parent = getOwnedCategory(dto.getParentId());
            if (!isRoot(parent)) {
                throw new BizException(400, "分类最多两级");
            }
            category.setParentId(dto.getParentId());
        }
        categoryMapper.updateById(category);
        clearCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Category category = getOwnedCategory(id);
        if (Objects.equals(category.getIsSystem(), 1)) {
            throw new BizException(403, "系统预置分类不允许删除");
        }
        Long childCount = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException(409, "请先删除子分类");
        }
        int refCount = billMapper.countByCategory(id);
        if (refCount > 0) {
            throw new BizException(409, "该分类存在 " + refCount + " 笔账单引用，无法删除");
        }
        categoryMapper.deleteById(id);
        clearCache();
    }

    /* ---------------- private helpers ---------------- */

    /** 一级分类：parentId 为 null 或 0（数据库默认值为 0） */
    private boolean isRoot(Category category) {
        return category.getParentId() == null || category.getParentId() == 0L;
    }

    private boolean matchesContext(Category category) {
        if (!Objects.equals(category.getOwnerType(), contextService.ownerType())) {
            return false;
        }
        Long familyId = contextService.familyId();
        if (familyId != null) {
            return Objects.equals(category.getFamilyId(), familyId);
        }
        return category.getFamilyId() == null && Objects.equals(category.getUserId(), contextService.currentUserId());
    }

    private void clearCache() {
        cacheService.deleteByPrefix(Constants.CACHE_CATEGORY + contextService.currentUserId());
    }

    private CategoryVO toVo(Category c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setName(c.getName());
        vo.setIcon(c.getIcon());
        vo.setColor(c.getColor());
        vo.setType(c.getType());
        vo.setIsSystem(c.getIsSystem());
        vo.setSort(c.getSort());
        return vo;
    }
}
