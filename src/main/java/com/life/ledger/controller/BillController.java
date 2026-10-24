package com.life.ledger.controller;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.annotation.RateLimit;
import com.life.ledger.common.PageResult;
import com.life.ledger.common.Result;
import com.life.ledger.dto.BillBatchDeleteDTO;
import com.life.ledger.dto.BillQueryDTO;
import com.life.ledger.dto.BillSaveDTO;
import com.life.ledger.dto.ImportConfirmDTO;
import com.life.ledger.entity.Bill;
import com.life.ledger.service.BillService;
import com.life.ledger.service.CsvImportService;
import com.life.ledger.vo.BillVO;
import com.life.ledger.vo.ImportPreviewVO;
import com.life.ledger.vo.RecycleBillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 账单接口
 */
@Tag(name = "账单")
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;
    private final CsvImportService csvImportService;

    /** 分页列表（倒序 happened_at） */
    @Operation(summary = "账单分页列表（多条件筛选）")
    @GetMapping
    public Result<PageResult<BillVO>> page(BillQueryDTO query) {
        return Result.ok(billService.page(query));
    }

    /** 记一笔 */
    @Operation(summary = "记一笔（支出/收入/转账）")
    @PostMapping
    @OpLog(module = "bill", action = "create")
    @RateLimit(count = 60, period = 60)
    public Result<BillVO> create(@Valid @RequestBody BillSaveDTO dto) {
        Bill bill = billService.create(dto);
        return Result.ok(billService.detail(bill.getId()));
    }

    /** 详情 */
    @Operation(summary = "账单详情")
    @GetMapping("/{id}")
    public Result<BillVO> detail(@PathVariable Long id) {
        return Result.ok(billService.detail(id));
    }

    /** 编辑 */
    @Operation(summary = "编辑账单")
    @PutMapping("/{id}")
    @OpLog(module = "bill", action = "update")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BillSaveDTO dto) {
        billService.update(id, dto);
        return Result.ok();
    }

    /** 删除（逻辑删除） */
    @Operation(summary = "删除账单（进回收站）")
    @DeleteMapping("/{id}")
    @OpLog(module = "bill", action = "delete")
    public Result<Void> delete(@PathVariable Long id) {
        billService.delete(id);
        return Result.ok();
    }

    /** 批量删除 */
    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    @OpLog(module = "bill", action = "batchDelete")
    public Result<Integer> batchDelete(@RequestBody BillBatchDeleteDTO dto) {
        return Result.ok(billService.batchDelete(dto.getIds()));
    }

    /** 导出 Excel */
    @Operation(summary = "导出账单 Excel")
    @GetMapping("/export")
    public void export(BillQueryDTO query, HttpServletResponse response) {
        billService.export(query, response);
    }

    /** 回收站列表 */
    @Operation(summary = "回收站列表")
    @GetMapping("/recycle")
    public Result<PageResult<RecycleBillVO>> recycle(@RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "10") long size) {
        return Result.ok(billService.recyclePageVo(current, size));
    }

    /** 回收站恢复 */
    @Operation(summary = "恢复账单")
    @PutMapping("/{id}/restore")
    @OpLog(module = "bill", action = "restore")
    public Result<Void> restore(@PathVariable Long id) {
        billService.restore(id);
        return Result.ok();
    }

    /** 回收站彻底删除 */
    @Operation(summary = "彻底删除")
    @DeleteMapping("/{id}/purge")
    @OpLog(module = "bill", action = "purge")
    public Result<Void> purge(@PathVariable Long id) {
        billService.purge(id);
        return Result.ok();
    }

    /** CSV 导入预览 */
    @Operation(summary = "CSV 导入预览（支付宝/微信账单）")
    @PostMapping("/import/preview")
    public Result<ImportPreviewVO> importPreview(@RequestParam("file") MultipartFile file) {
        return Result.ok(csvImportService.preview(file));
    }

    /** CSV 导入确认 */
    @Operation(summary = "CSV 导入确认")
    @PostMapping("/import/confirm")
    @OpLog(module = "bill", action = "import")
    public Result<Integer> importConfirm(@RequestBody ImportConfirmDTO dto) {
        return Result.ok(csvImportService.confirm(dto));
    }
}
