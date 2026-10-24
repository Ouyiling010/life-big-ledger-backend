package com.life.ledger.cache;

/**
 * 缓存抽象：默认使用进程内缓存（LocalCacheService）。
 * 若部署环境提供 Redis，可实现一个 RedisCacheService 并调整 app.cache.type 即可无缝替换。
 */
public interface CacheService {

    void set(String key, Object value, long ttlSeconds);

    Object get(String key);

    <T> T get(String key, Class<T> type);

    void delete(String key);

    /** 按前缀批量删除（简单实现用于分类/统计缓存失效） */
    void deleteByPrefix(String prefix);

    boolean exists(String key);
}
