package com.life.ledger.controller;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.common.Result;
import com.life.ledger.dto.AccountAdjustDTO;
import com.life.ledger.dto.AccountDTO;
import com.life.ledger.service.AccountService;
import com.life.ledger.vo.AccountVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账户接口
 */
@Tag(name = "账户")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "账户列表")
    @GetMapping
    public Result<List<AccountVO>> list() {
        return Result.ok(accountService.listAccounts());
    }

    @Operation(summary = "新建账户")
    @PostMapping
    @OpLog(module = "account", action = "create")
    public Result<AccountVO> create(@RequestBody @Valid AccountDTO dto) {
        return Result.ok(accountService.createAccount(dto));
    }

    @Operation(summary = "编辑账户")
    @PutMapping("/{id}")
    @OpLog(module = "account", action = "update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid AccountDTO dto) {
        accountService.updateAccount(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除账户")
    @DeleteMapping("/{id}")
    @OpLog(module = "account", action = "delete")
    public Result<Void> delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return Result.ok();
    }

    @Operation(summary = "总资产")
    @GetMapping("/total")
    public Result<Long> total() {
        return Result.ok(accountService.totalAsset());
    }

    @Operation(summary = "余额校正")
    @PostMapping("/{id}/adjust")
    @OpLog(module = "account", action = "adjust")
    public Result<AccountVO> adjust(@PathVariable Long id, @RequestBody @Valid AccountAdjustDTO dto) {
        return Result.ok(accountService.adjustBalance(id, dto));
    }
}
