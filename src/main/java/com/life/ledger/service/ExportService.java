package com.life.ledger.service;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * 全量导出服务
 */
public interface ExportService {

    /** 导出当前上下文全部数据（不含密码等敏感字段） */
    Map<String, Object> exportAll();

    /** 导出当前上下文账单明细为 Excel */
    void exportBillsExcel(HttpServletResponse response);
}
