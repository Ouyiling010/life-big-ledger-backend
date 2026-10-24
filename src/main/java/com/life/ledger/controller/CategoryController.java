package com.life.ledger.controller;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.common.Result;
import com.life.ledger.dto.CategoryDTO;
import com.life.ledger.service.CategoryService;
import com.life.ledger.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口
 */
@Tag(name = "分类")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表（两级树）")
    @GetMapping
    public Result<List<CategoryVO>> tree(@RequestParam(required = false) Integer type) {
        return Result.ok(categoryService.tree(type));
    }

    @Operation(summary = "新建分类")
    @PostMapping
    @OpLog(module = "category", action = "create")
    public Result<CategoryVO> create(@RequestBody @Valid CategoryDTO dto) {
        return Result.ok(categoryService.create(dto));
    }

    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    @OpLog(module = "category", action = "update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @OpLog(module = "category", action = "delete")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
