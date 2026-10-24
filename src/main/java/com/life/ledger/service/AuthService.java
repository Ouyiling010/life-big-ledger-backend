package com.life.ledger.service;

import com.life.ledger.dto.LoginDTO;
import com.life.ledger.dto.RegisterDTO;
import com.life.ledger.dto.ResetPasswordDTO;
import com.life.ledger.dto.SendEmailCodeDTO;
import com.life.ledger.vo.LoginVO;
import com.life.ledger.vo.SendEmailCodeVO;
import com.life.ledger.vo.UserVO;

/**
 * 认证服务：注册、登录、登出、邮箱验证码、忘记密码
 */
public interface AuthService {

    UserVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    void logout();

    /**
     * 发送邮箱验证码（注册 / 忘记密码）。
     * <p>
     * 出于安全考虑，无论邮箱是否已注册 / 是否已存在，均统一返回 {@code sent=true}，避免账号枚举。
     */
    SendEmailCodeVO sendEmailCode(SendEmailCodeDTO dto);

    /**
     * 忘记密码 → 通过邮箱验证码重置密码。
     * 重置成功后强制踢掉该用户所有登录会话。
     */
    void resetPassword(ResetPasswordDTO dto);
}

