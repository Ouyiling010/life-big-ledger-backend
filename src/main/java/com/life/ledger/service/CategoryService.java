package com.life.ledger.service;

import com.life.ledger.dto.CategoryDTO;
import com.life.ledger.entity.Category;
import com.life.ledger.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务
 * 说明：本接口中「查询类」方法由 Bill / AI / CSV 模块调用，Category 模块可继续补充 CRUD 方法。
 */
public interface CategoryService {

    /** 当前上下文下的分类（含系统预置），type 为 null 时返回支出+收入 */
    List<Category> listByCurrentContext(Integer type);

    /** 按名称+类型在当前上下文查找分类ID（找不到返回 null） */
    Long findIdByName(String name, Integer type);

    /** 提供给 AI 的分类选项（扁平 父>子 名称 + id） */
    List<Category> listForAi();

    /** 校验分类属于当前上下文或为系统分类 */
    Category getOwnedCategory(Long categoryId);

    /** 当前上下文分类树（两级） */
    List<CategoryVO> tree(Integer type);

    /** 新建分类 */
    CategoryVO create(CategoryDTO dto);

    /** 编辑分类（系统分类禁止修改） */
    void update(Long id, CategoryDTO dto);

    /** 删除分类（系统分类禁止删除；被账单引用禁止删除） */
    void delete(Long id);
}
