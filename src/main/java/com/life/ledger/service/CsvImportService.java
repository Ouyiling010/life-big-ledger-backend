package com.life.ledger.service;

import com.life.ledger.dto.ImportConfirmDTO;
import com.life.ledger.vo.ImportPreviewVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * CSV 账单导入服务（支付宝/微信账单）
 */
public interface CsvImportService {

    /** 解析 CSV 并生成预览（含智能分类与查重） */
    ImportPreviewVO preview(MultipartFile file);

    /** 确认导入，返回成功入库条数 */
    int confirm(ImportConfirmDTO dto);
}
