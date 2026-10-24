package com.life.ledger.controller;

import com.life.ledger.annotation.OpLog;
import com.life.ledger.common.Result;
import com.life.ledger.dto.BillTemplateDTO;
import com.life.ledger.service.BillTemplateService;
import com.life.ledger.vo.BillTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 快捷记账模板接口
 */
@Tag(name = "快捷记账模板")
@RestController
@RequestMapping("/api/bill-templates")
@RequiredArgsConstructor
public class BillTemplateController {

    private final BillTemplateService billTemplateService;

    /** 模板列表 */
    @Operation(summary = "模板列表")
    @GetMapping
    public Result<List<BillTemplateVO>> list() {
        return Result.ok(billTemplateService.list());
    }

    /** 新建模板 */
    @Operation(summary = "新建模板")
    @PostMapping
    @OpLog(module = "billTemplate", action = "create")
    public Result<BillTemplateVO> create(@RequestBody BillTemplateDTO dto) {
        return Result.ok(billTemplateService.create(dto));
    }

    /** 编辑模板 */
    @Operation(summary = "编辑模板")
    @PutMapping("/{id}")
    @OpLog(module = "billTemplate", action = "update")
    public Result<Void> update(@PathVariable Long id, @RequestBody BillTemplateDTO dto) {
        billTemplateService.update(id, dto);
        return Result.ok();
    }

    /** 删除模板 */
    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    @OpLog(module = "billTemplate", action = "delete")
    public Result<Void> delete(@PathVariable Long id) {
        billTemplateService.delete(id);
        return Result.ok();
    }

    /** 使用模板：use_count + 1 并返回模板 */
    @Operation(summary = "使用模板（一键记账）")
    @PostMapping("/{id}/use")
    public Result<BillTemplateVO> use(@PathVariable Long id) {
        return Result.ok(billTemplateService.use(id));
    }
}
