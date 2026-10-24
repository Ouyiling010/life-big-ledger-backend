package com.life.ledger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流（基于本地计数缓存实现）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    int count() default 10;

    /** 统计周期，单位秒 */
    int period() default 60;

    String key() default "";
}
