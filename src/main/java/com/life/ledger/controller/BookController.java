package com.life.ledger.controller;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.common.Result;
import com.life.ledger.dto.BookDTO;
import com.life.ledger.service.BookService;
import com.life.ledger.vo.BookVO;
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
 * 账本接口
 */
@Tag(name = "账本")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "账本列表")
    @GetMapping
    public Result<List<BookVO>> list() {
        return Result.ok(bookService.listByCurrentContext());
    }

    @Operation(summary = "新建账本")
    @PostMapping
    @OpLog(module = "book", action = "create")
    public Result<BookVO> create(@RequestBody @Valid BookDTO dto) {
        return Result.ok(bookService.create(dto));
    }

    @Operation(summary = "编辑账本")
    @PutMapping("/{id}")
    @OpLog(module = "book", action = "update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid BookDTO dto) {
        bookService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除账本")
    @DeleteMapping("/{id}")
    @OpLog(module = "book", action = "delete")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestParam(defaultValue = "false") boolean force) {
        bookService.delete(id, force);
        return Result.ok();
    }

    @Operation(summary = "设为默认账本")
    @PutMapping("/{id}/default")
    @OpLog(module = "book", action = "setDefault")
    public Result<Void> setDefault(@PathVariable Long id) {
        bookService.setDefault(id);
        return Result.ok();
    }
}
