package com.life.ledger.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.life.ledger.annotation.RateLimit;
import com.life.ledger.common.BizException;
import com.life.ledger.common.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口限流切面：基于 Caffeine 本地计数器实现（按用户 + 注解 key 计数）
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final Cache<String, Counter> cache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfter(new Expiry<String, Counter>() {
                @Override
                public long expireAfterCreate(String key, Counter value, long currentTime) {
                    return value.ttlNanos;
                }

                @Override
                public long expireAfterUpdate(String key, Counter value, long currentTime,
                                              @NonNegative long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(String key, Counter value, long currentTime,
                                            @NonNegative long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String uid = currentUid();
        String baseKey = rateLimit.key() == null || rateLimit.key().isEmpty()
                ? pjp.getSignature().getName() : rateLimit.key();
        String key = "rate:" + baseKey + ":" + uid;

        long ttlNanos = TimeUnit.SECONDS.toNanos(Math.max(rateLimit.period(), 1));
        Counter counter = cache.get(key, k -> new Counter(ttlNanos));
        int current = counter.count.incrementAndGet();
        if (current > rateLimit.count()) {
            throw new BizException(429, "操作过于频繁，请稍后再试");
        }
        return pjp.proceed();
    }

    private String currentUid() {
        Long userId = UserContext.userIdOrNull();
        if (userId != null) {
            return "u:" + userId;
        }
        // 未登录（登录/注册等接口）按客户端 IP 计数，避免所有匿名用户共用一个计数桶
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = attrs == null ? null : UserContext.clientIp(attrs.getRequest());
        return "ip:" + (ip == null ? "unknown" : ip);
    }

    /** 计数器：保存计数与本次窗口的过期时长 */
    private static final class Counter {
        private final AtomicInteger count = new AtomicInteger(0);
        private final long ttlNanos;

        private Counter(long ttlNanos) {
            this.ttlNanos = ttlNanos;
        }
    }
}
