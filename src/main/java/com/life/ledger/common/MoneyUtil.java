package com.life.ledger.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具：数据库统一用 BIGINT 存「分」
 */
public final class MoneyUtil {

    private MoneyUtil() {
    }

    /** 元 -> 分 */
    public static long toCent(BigDecimal yuan) {
        if (yuan == null) {
            return 0L;
        }
        return yuan.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /** 分 -> 元 */
    public static BigDecimal toYuan(Long cent) {
        if (cent == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(cent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
