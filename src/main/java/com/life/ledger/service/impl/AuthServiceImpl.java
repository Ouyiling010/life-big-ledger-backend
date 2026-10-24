package com.life.ledger.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.dto.LoginDTO;
import com.life.ledger.dto.RegisterDTO;
import com.life.ledger.dto.ResetPasswordDTO;
import com.life.ledger.dto.SendEmailCodeDTO;
import com.life.ledger.email.EmailCodeService;
import com.life.ledger.email.EmailScene;
import com.life.ledger.entity.Account;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.User;
import com.life.ledger.mapper.AccountMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.UserMapper;
import com.life.ledger.service.AuthService;
import com.life.ledger.vo.LoginVO;
import com.life.ledger.vo.SendEmailCodeVO;
import com.life.ledger.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final AccountMapper accountMapper;
    private final EmailCodeService emailCodeService;

    /** log 模式下是否在响应里返回验证码（演示用，生产关闭） */
    @Value("${app.email.dev-return-code:false}")
    private boolean devReturnCode;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        long nameCount = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, dto.getUsername()));
        if (nameCount > 0) {
            throw new BizException(409, "用户名已存在");
        }
        // 先校验邮箱验证码：必须持有该邮箱才能走到这里，避免「邮箱已被注册」成为账号枚举入口
        emailCodeService.verifyOrThrow(dto.getEmail(), EmailScene.REGISTER, dto.getEmailCode());
        long emailCount = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, dto.getEmail()));
        if (emailCount > 0) {
            throw new BizException(409, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setNickname(StrUtil.isBlank(dto.getNickname()) ? dto.getUsername() : dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(StrUtil.isBlank(dto.getPhone()) ? null : dto.getPhone());
        user.setGender(0);
        user.setCurrentContext(Constants.CONTEXT_PERSONAL);
        user.setHasGuided(0);
        user.setStatus(Constants.STATUS_ENABLED);
        user.setIsAdmin(0);
        userMapper.insert(user);

        Long userId = user.getId();

        // 默认账本
        Book book = new Book();
        book.setOwnerType(Constants.OWNER_PERSONAL);
        book.setUserId(userId);
        book.setCreatorId(userId);
        book.setName("日常账本");
        book.setCurrency("CNY");
        book.setIsDefault(1);
        book.setSort(0);
        bookMapper.insert(book);

        // 默认账户
        createDefaultAccount(userId, "现金", Constants.ACCOUNT_CASH);
        createDefaultAccount(userId, "支付宝", Constants.ACCOUNT_VIRTUAL);
        createDefaultAccount(userId, "微信", Constants.ACCOUNT_VIRTUAL);

        return UserVO.from(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 先按用户名匹配，未命中再按邮箱匹配：避免用户名与另一账号邮箱相同时取到不确定的记录
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, dto.getAccount())
                .last("LIMIT 1"));
        if (user == null) {
            user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                    .eq(User::getEmail, dto.getAccount())
                    .last("LIMIT 1"));
        }
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BizException(400, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BizException(403, "账号已被禁用");
        }
        StpUtil.login(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUser(UserVO.from(user));
        return vo;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public SendEmailCodeVO sendEmailCode(SendEmailCodeDTO dto) {
        // 统一返回 sent=true，不区分邮箱是否已注册，防止账号枚举攻击
        String code = emailCodeService.generateAndSend(dto.getEmail(), dto.getScene());

        SendEmailCodeVO vo = new SendEmailCodeVO();
        vo.setSent(true);
        // 仅 log 模式 + dev-return-code=true 时把验证码返回给前端，方便演示
        vo.setDevCode(devReturnCode ? code : null);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            // 不暴露账号是否存在，返回模糊错误
            throw new BizException(400, "邮箱或验证码错误");
        }
        // 验证码校验（RESET_PASSWORD 场景），通过后立即失效
        emailCodeService.verifyOrThrow(dto.getEmail(), EmailScene.RESET_PASSWORD, dto.getCode());

        user.setPassword(BCrypt.hashpw(dto.getNewPassword(), BCrypt.gensalt()));
        userMapper.updateById(user);

        // 重置成功后踢掉该用户所有会话，强制重新登录
        try {
            StpUtil.logout(user.getId());
        } catch (Exception ignore) {
            // 没有在线会话时 logout 会抛异常，忽略
        }
    }

    private void createDefaultAccount(Long userId, String name, int type) {
        Account account = new Account();
        account.setOwnerType(Constants.OWNER_PERSONAL);
        account.setUserId(userId);
        account.setCreatorId(userId);
        account.setName(name);
        account.setType(type);
        account.setInitBalance(0L);
        account.setBalance(0L);
        account.setIncludeInTotal(1);
        accountMapper.insert(account);
    }
}

