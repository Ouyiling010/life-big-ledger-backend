package com.life.ledger.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 进程内缓存实现（默认）。
 * 说明：文档中的 Redis 缓存场景，在未安装 Redis 的环境下由此实现代偿，
 * 接口语义与 Redis CacheService 完全一致，替换成本极低。
 */
@Service
@ConditionalOnProperty(name = "app.cache.type", havingValue = "local", matchIfMissing = true)
public class LocalCacheService implements CacheService {

    private final Cache<String, Entry> cache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfter(new Expiry<String, Entry>() {
                @Override
                public long expireAfterCreate(String key, Entry value, long currentTime) {
                    return value.ttlNanos;
                }

                @Override
                public long expireAfterUpdate(String key, Entry value, long currentTime, @NonNegative long currentDuration) {
                    return value.ttlNanos;
                }

                @Override
                public long expireAfterRead(String key, Entry value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    @Override
    public void set(String key, Object value, long ttlSeconds) {
        long ttl = ttlSeconds <= 0 ? Duration.ofHours(1).toNanos() : Duration.ofSeconds(ttlSeconds).toNanos();
        cache.put(key, new Entry(value, ttl));
    }

    @Override
    public Object get(String key) {
        Entry entry = cache.getIfPresent(key);
        return entry == null ? null : entry.value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            return null;
        }
        return (T) value;
    }

    @Override
    public void delete(String key) {
        cache.invalidate(key);
    }

    @Override
    public void deleteByPrefix(String prefix) {
        cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    @Override
    public boolean exists(String key) {
        return cache.getIfPresent(key) != null;
    }

    private static final class Entry {
        private final Object value;
        private final long ttlNanos;

        private Entry(Object value, long ttlNanos) {
            this.value = value;
            this.ttlNanos = ttlNanos;
        }
    }

    /** 供限流等场景使用的原子计数器（不影响主缓存语义） */
    public long incr(String key, long ttlSeconds) {
        return increment(key, ttlSeconds);
    }

    private final Cache<String, long[]> counters = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    private long increment(String key, long ttlSeconds) {
        return counters.asMap().compute(key, (k, v) -> {
            if (v == null) {
                return new long[]{1L};
            }
            v[0] = v[0] + 1;
            return v;
        })[0];
    }
}
