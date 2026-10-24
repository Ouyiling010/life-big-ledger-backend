package com.life.ledger.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.life.ledger.common.PageResult;
import com.life.ledger.entity.OperationLog;
import com.life.ledger.mapper.OperationLogMapper;
import com.life.ledger.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现：异步落库，失败静默不影响主流程
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Async
    @Override
    public void saveAsync(Long userId, Long familyId, String module, String action,
                          Long targetId, String detail, String ip, Integer costMs) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setFamilyId(familyId);
            operationLog.setModule(module);
            operationLog.setAction(action);
            operationLog.setTargetId(targetId);
            operationLog.setDetail(detail);
            operationLog.setIp(ip);
            operationLog.setCostMs(costMs);
            operationLogMapper.insert(operationLog);
        } catch (Exception ignored) {
            // 静默失败
        }
    }

    @Override
    public PageResult<OperationLog> pageByFamily(Long familyId, String module, long current, long size) {
        Page<OperationLog> page = new Page<>(current, size);
        Page<OperationLog> result = operationLogMapper.selectPage(page, Wrappers.<OperationLog>lambdaQuery()
                .eq(familyId != null, OperationLog::getFamilyId, familyId)
                .eq(module != null, OperationLog::getModule, module)
                .orderByDesc(OperationLog::getId));
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }
}
