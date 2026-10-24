package com.life.ledger.interceptor;

import com.life.ledger.annotation.AdminOnly;
import com.life.ledger.common.BizException;
import com.life.ledger.common.UserContext;
import com.life.ledger.entity.User;
import com.life.ledger.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 隐藏后台管理员拦截器：仅标注 @AdminOnly 的接口会校验 is_admin=1
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    public AdminInterceptor(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        if (!method.hasMethodAnnotation(AdminOnly.class) && !method.getBeanType().isAnnotationPresent(AdminOnly.class)) {
            return true;
        }
        Long userId = UserContext.userId();
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsAdmin() == null || user.getIsAdmin() != 1) {
            throw new BizException(403, "无权限");
        }
        return true;
    }
}
