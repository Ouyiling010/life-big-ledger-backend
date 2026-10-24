package com.life.ledger.controller;

import com.life.ledger.annotation.FamilyPermission;
import com.life.ledger.annotation.OpLog;
import com.life.ledger.annotation.RateLimit;
import com.life.ledger.common.FamilyRole;
import com.life.ledger.common.PageResult;
import com.life.ledger.common.Result;
import com.life.ledger.dto.FamilyCreateDTO;
import com.life.ledger.dto.FamilyJoinDTO;
import com.life.ledger.dto.FamilyUpdateDTO;
import com.life.ledger.dto.RoleUpdateDTO;
import com.life.ledger.dto.TransferDTO;
import com.life.ledger.entity.OperationLog;
import com.life.ledger.service.FamilyService;
import com.life.ledger.vo.FamilyDetailVO;
import com.life.ledger.vo.FamilyMemberVO;
import com.life.ledger.vo.FamilyVO;
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
 * 家庭接口
 */
@Tag(name = "家庭组")
@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "创建家庭")
    @PostMapping
    @OpLog(module = "family", action = "create")
    public Result<FamilyVO> create(@RequestBody @Valid FamilyCreateDTO dto) {
        return Result.ok(familyService.create(dto));
    }

    @Operation(summary = "我的家庭列表")
    @GetMapping
    public Result<List<FamilyVO>> list() {
        return Result.ok(familyService.listMy());
    }

    @Operation(summary = "家庭详情")
    @GetMapping("/{id}")
    @FamilyPermission(familyIdParam = "id")
    public Result<FamilyDetailVO> detail(@PathVariable Long id) {
        return Result.ok(familyService.detail(id));
    }

    @Operation(summary = "编辑家庭信息")
    @PutMapping("/{id}")
    @FamilyPermission(minRole = FamilyRole.ADMIN, familyIdParam = "id")
    @OpLog(module = "family", action = "update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid FamilyUpdateDTO dto) {
        familyService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "解散家庭")
    @DeleteMapping("/{id}")
    @FamilyPermission(minRole = FamilyRole.OWNER, familyIdParam = "id")
    @OpLog(module = "family", action = "dissolve")
    public Result<Void> dissolve(@PathVariable Long id) {
        familyService.dissolve(id);
        return Result.ok();
    }

    @Operation(summary = "重置邀请码")
    @PostMapping("/{id}/invite/reset")
    @FamilyPermission(minRole = FamilyRole.ADMIN, familyIdParam = "id")
    @OpLog(module = "family", action = "resetInvite")
    public Result<String> resetInvite(@PathVariable Long id) {
        return Result.ok(familyService.resetInvite(id));
    }

    @Operation(summary = "通过邀请码加入家庭")
    @PostMapping("/join")
    @RateLimit(count = 10, period = 60)
    @OpLog(module = "family", action = "join")
    public Result<FamilyVO> join(@RequestBody @Valid FamilyJoinDTO dto) {
        return Result.ok(familyService.join(dto));
    }

    @Operation(summary = "成员列表")
    @GetMapping("/{id}/members")
    @FamilyPermission(familyIdParam = "id")
    public Result<List<FamilyMemberVO>> members(@PathVariable Long id) {
        return Result.ok(familyService.members(id));
    }

    @Operation(summary = "修改成员角色")
    @PutMapping("/{id}/members/{mid}/role")
    @FamilyPermission(minRole = FamilyRole.OWNER, familyIdParam = "id")
    @OpLog(module = "family", action = "updateRole")
    public Result<Void> updateRole(@PathVariable Long id, @PathVariable Long mid,
                                   @RequestBody @Valid RoleUpdateDTO dto) {
        familyService.updateRole(id, mid, dto.getRole());
        return Result.ok();
    }

    @Operation(summary = "移除成员")
    @DeleteMapping("/{id}/members/{mid}")
    @FamilyPermission(minRole = FamilyRole.ADMIN, familyIdParam = "id")
    @OpLog(module = "family", action = "removeMember")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long mid) {
        familyService.removeMember(id, mid);
        return Result.ok();
    }

    @Operation(summary = "退出家庭")
    @PostMapping("/{id}/quit")
    @FamilyPermission(familyIdParam = "id")
    @OpLog(module = "family", action = "quit")
    public Result<Void> quit(@PathVariable Long id) {
        familyService.quit(id);
        return Result.ok();
    }

    @Operation(summary = "转让群主")
    @PostMapping("/{id}/transfer")
    @FamilyPermission(minRole = FamilyRole.OWNER, familyIdParam = "id")
    @OpLog(module = "family", action = "transfer")
    public Result<Void> transfer(@PathVariable Long id, @RequestBody @Valid TransferDTO dto) {
        familyService.transfer(id, dto.getTargetMemberId());
        return Result.ok();
    }

    @Operation(summary = "家庭操作日志")
    @GetMapping("/{id}/logs")
    @FamilyPermission(minRole = FamilyRole.ADMIN, familyIdParam = "id")
    public Result<PageResult<OperationLog>> logs(@PathVariable Long id,
                                                 @RequestParam(required = false) String module,
                                                 @RequestParam(defaultValue = "1") long current,
                                                 @RequestParam(defaultValue = "10") long size) {
        return Result.ok(familyService.logs(id, module, current, size));
    }
}
