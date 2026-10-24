package com.life.ledger.controller;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.annotation.RateLimit;
import com.life.ledger.common.Result;
import com.life.ledger.dto.LoginDTO;
import com.life.ledger.dto.RegisterDTO;
import com.life.ledger.dto.ResetPasswordDTO;
import com.life.ledger.dto.SendEmailCodeDTO;
import com.life.ledger.service.AuthService;
import com.life.ledger.vo.LoginVO;
import com.life.ledger.vo.SendEmailCodeVO;
import com.life.ledger.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "发送邮箱验证码（注册 / 忘记密码）")
    @RateLimit(count = 5, period = 60)
    @PostMapping("/send-email-code")
    public Result<SendEmailCodeVO> sendEmailCode(@Valid @RequestBody SendEmailCodeDTO dto) {
        return Result.ok(authService.sendEmailCode(dto));
    }

    @Operation(summary = "注册")
    @RateLimit(count = 10, period = 60)
    @OpLog(module = "user", action = "register")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @Operation(summary = "登录")
    @RateLimit(count = 10, period = 60)
    @OpLog(module = "user", action = "login")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @Operation(summary = "登出")
    @OpLog(module = "user", action = "update")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @Operation(summary = "忘记密码 → 重置密码")
    @RateLimit(count = 10, period = 60)
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.ok();
    }
}

