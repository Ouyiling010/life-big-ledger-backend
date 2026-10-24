package com.life.ledger.controller;

import com.life.ledger.common.Result;
import com.life.ledger.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 全量导出接口
 */
@Tag(name = "数据导出")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /** 导出当前上下文全部数据（JSON） */
    @Operation(summary = "全量数据导出（JSON）")
    @GetMapping("/all")
    public Result<Map<String, Object>> exportAll() {
        return Result.ok(exportService.exportAll());
    }

    /** 导出账单明细（Excel） */
    @Operation(summary = "账单明细导出（Excel）")
    @GetMapping("/all/excel")
    public void exportExcel(HttpServletResponse response) {
        exportService.exportBillsExcel(response);
    }
}
