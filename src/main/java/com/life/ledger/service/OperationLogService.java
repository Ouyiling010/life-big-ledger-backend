package com.life.ledger.service;

import com.life.ledger.common.PageResult;
import com.life.ledger.entity.OperationLog;

/**
 * 操作日志服务（AOP 异步写入）
 */
public interface OperationLogService {

    /** 异步落库 */
    void saveAsync(Long userId, Long familyId, String module, String action,
                   Long targetId, String detail, String ip, Integer costMs);

    /** 家庭操作日志分页 */
    PageResult<OperationLog> pageByFamily(Long familyId, String module, long current, long size);
}
