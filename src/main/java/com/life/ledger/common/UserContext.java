package com.life.ledger.common;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前登录用户与请求工具
 */
public final class UserContext {

    private UserContext() {
    }

    /** 当前登录用户ID，未登录直接抛 401 */
    public static Long userId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            throw new BizException(401, "未登录或登录已过期");
        }
    }

    public static Long userIdOrNull() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 客户端 IP。
     * 不直接信任 X-Forwarded-For / X-Real-IP（客户端可伪造，绕过限流），
     * 需要反向代理时请开启 server.forward-headers-strategy=framework，
     * 由 Spring 统一解析并覆盖 remoteAddr。
     */
    public static String clientIp(HttpServletRequest request) {
        return request == null ? null : request.getRemoteAddr();
    }
}
