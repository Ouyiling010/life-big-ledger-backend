package com.life.ledger.common;

/**
 * 全局业务常量
 */
public final class Constants {

    private Constants() {
    }

    /** 归属类型：0系统 1个人 2家庭 */
    public static final int OWNER_SYSTEM = 0;
    public static final int OWNER_PERSONAL = 1;
    public static final int OWNER_FAMILY = 2;

    /** 账单类型：1支出 2收入 3转账 */
    public static final int BILL_EXPENSE = 1;
    public static final int BILL_INCOME = 2;
    public static final int BILL_TRANSFER = 3;

    /** 账单来源：1手动 2CSV导入 3AI 4模板 */
    public static final int SOURCE_MANUAL = 1;
    public static final int SOURCE_CSV = 2;
    public static final int SOURCE_AI = 3;
    public static final int SOURCE_TEMPLATE = 4;

    /** 上下文：1个人 2家庭 */
    public static final int CONTEXT_PERSONAL = 1;
    public static final int CONTEXT_FAMILY = 2;

    /** 账户类型：1现金 2储蓄卡 3信用卡 4虚拟 */
    public static final int ACCOUNT_CASH = 1;
    public static final int ACCOUNT_SAVINGS = 2;
    public static final int ACCOUNT_CREDIT = 3;
    public static final int ACCOUNT_VIRTUAL = 4;

    /** 通用状态 */
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;

    /** 回收站保留天数 */
    public static final int RECYCLE_KEEP_DAYS = 30;

    /** AI 日志保留天数 */
    public static final int AI_LOG_KEEP_DAYS = 90;

    /** 缓存 key 前缀 */
    public static final String CACHE_STAT = "stat:user:";
    public static final String CACHE_CATEGORY = "category:user:";
    public static final String CACHE_ACCOUNT_BALANCE = "account:balance:";
    public static final String CACHE_AI_ANALYZE = "ai:analyze:";

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}
