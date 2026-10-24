package com.life.ledger.service;

import com.life.ledger.common.PageResult;

import java.util.Map;

/**
 * 隐藏后台（仅管理员）服务
 */
public interface ConsoleService {

    /** 平台总览统计 */
    Map<String, Object> stats();

    /** 用户分页（不含密码等敏感字段） */
    PageResult<Map<String, Object>> users(long current, long size, String keyword);

    /** 启用/禁用用户：status 0禁用 1正常 */
    void updateUserStatus(Long userId, Integer status);

    /** 家庭分页 */
    PageResult<Map<String, Object>> families(long current, long size, String keyword);

    /** 家庭详情（仅统计，不返回账单明细） */
    Map<String, Object> familyDetail(Long familyId);

    /** AI 调用统计 */
    Map<String, Object> aiStats(String period);
}
