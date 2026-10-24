package com.life.ledger.controller;

import cn.hutool.core.convert.Convert;
import com.life.ledger.annotation.AdminOnly;
import com.life.ledger.common.PageResult;
import com.life.ledger.common.Result;
import com.life.ledger.service.ConsoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 隐藏后台接口：仅系统管理员可访问（@AdminOnly 由 AdminInterceptor 校验）
 */
@Tag(name = "隐藏后台")
@AdminOnly
@RestController
@RequestMapping("/api/console")
@RequiredArgsConstructor
public class ConsoleController {

    private final ConsoleService consoleService;

    @Operation(summary = "平台数据概览")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(consoleService.stats());
    }

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<PageResult<Map<String, Object>>> users(@RequestParam(defaultValue = "1") long current,
                                                         @RequestParam(defaultValue = "10") long size,
                                                         @RequestParam(required = false) String keyword) {
        return Result.ok(consoleService.users(current, size, keyword));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @RequestBody(required = false) Map<String, Object> body) {
        Integer status = body == null ? null : Convert.toInt(body.get("status"));
        consoleService.updateUserStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "家庭列表")
    @GetMapping("/families")
    public Result<PageResult<Map<String, Object>>> families(@RequestParam(defaultValue = "1") long current,
                                                            @RequestParam(defaultValue = "10") long size,
                                                            @RequestParam(required = false) String keyword) {
        return Result.ok(consoleService.families(current, size, keyword));
    }

    @Operation(summary = "家庭详情（仅统计数据）")
    @GetMapping("/families/{id}")
    public Result<Map<String, Object>> familyDetail(@PathVariable Long id) {
        return Result.ok(consoleService.familyDetail(id));
    }

    @Operation(summary = "AI 调用统计")
    @GetMapping("/ai-stats")
    public Result<Map<String, Object>> aiStats(@RequestParam(required = false) String period) {
        return Result.ok(consoleService.aiStats(period));
    }
}
