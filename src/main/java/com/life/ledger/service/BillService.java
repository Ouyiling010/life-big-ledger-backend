package com.life.ledger.service;

import com.life.ledger.common.PageResult;
import com.life.ledger.dto.BillQueryDTO;
import com.life.ledger.dto.BillSaveDTO;
import com.life.ledger.entity.Bill;
import com.life.ledger.vo.BillVO;
import com.life.ledger.vo.RecycleBillVO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单服务
 * 说明：本接口先声明跨模块/定时任务需要的方法，Bill 模块可在此基础上继续补充 CRUD 方法（如 create/update/page/detail/delete/batchDelete/restore/purge/export）。
 */
public interface BillService {

    /** 回收站分页 */
    PageResult<Bill> recyclePage(long current, long size);

    /** 恢复回收站账单 */
    void restore(Long billId);

    /** 彻底删除 */
    void purge(Long billId);

    /** 定时任务：清理超过 30 天的回收站账单 */
    int purgeDeletedBefore(LocalDateTime before);

    /** 后台统计：账单总数 */
    long countAll();

    /** 后台统计：指定时间之后新增账单数 */
    long countSince(LocalDateTime time);

    /** 当前上下文全部账单（导出/全量导出使用） */
    List<Bill> listAllForExport();

    /** 分页列表（倒序 happened_at，含筛选） */
    PageResult<BillVO> page(BillQueryDTO query);

    /** 记一笔 */
    Bill create(BillSaveDTO dto);

    /** 详情 */
    BillVO detail(Long billId);

    /** 编辑 */
    void update(Long billId, BillSaveDTO dto);

    /** 删除（逻辑删除） */
    void delete(Long billId);

    /** 批量删除（逻辑删除），返回删除条数 */
    int batchDelete(List<Long> ids);

    /** 回收站分页（VO） */
    PageResult<RecycleBillVO> recyclePageVo(long current, long size);

    /** 导出 Excel 到响应流 */
    void export(BillQueryDTO query, HttpServletResponse response);
}
