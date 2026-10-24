package com.life.ledger.controller;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.common.Result;
import com.life.ledger.dto.ContextSwitchDTO;
import com.life.ledger.dto.PasswordChangeDTO;
import com.life.ledger.dto.ProfileUpdateDTO;
import com.life.ledger.service.FamilyService;
import com.life.ledger.service.UserService;
import com.life.ledger.vo.FamilyBriefVO;
import com.life.ledger.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户接口
 */
@Tag(name = "用户")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FamilyService familyService;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.ok(userService.profile());
    }

    @Operation(summary = "修改个人资料")
    @OpLog(module = "user", action = "update")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        return Result.ok(userService.updateProfile(dto));
    }

    @Operation(summary = "修改密码")
    @OpLog(module = "user", action = "update")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto) {
        userService.changePassword(dto);
        return Result.ok();
    }

    @Operation(summary = "上传头像")
    @OpLog(module = "user", action = "update")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.ok(userService.uploadAvatar(file));
    }

    @Operation(summary = "我的家庭列表")
    @GetMapping("/families")
    public Result<List<FamilyBriefVO>> myFamilies() {
        return Result.ok(userService.myFamilies());
    }

    @Operation(summary = "切换个人/家庭上下文")
    @OpLog(module = "user", action = "update")
    @PutMapping("/context")
    public Result<UserVO> switchContext(@Valid @RequestBody ContextSwitchDTO dto) {
        return Result.ok(userService.switchContext(dto));
    }

    @Operation(summary = "标记已完成引导")
    @OpLog(module = "user", action = "update")
    @PutMapping("/guided")
    public Result<Void> markGuided() {
        userService.markGuided();
        return Result.ok();
    }

    @Operation(summary = "修改自己在某家庭的昵称")
    @OpLog(module = "family", action = "update")
    @PutMapping("/family-nickname")
    public Result<Void> updateFamilyNickname(@RequestParam Long familyId,
                                             @RequestParam(required = false) String nicknameInFamily) {
        familyService.updateMyNickname(familyId, nicknameInFamily);
        return Result.ok();
    }
}
