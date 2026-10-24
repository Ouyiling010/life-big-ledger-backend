package com.life.ledger.service;

import com.life.ledger.dto.BookDTO;
import com.life.ledger.vo.BookVO;

import java.util.List;

/**
 * 账本服务
 */
public interface BookService {

    /** 当前上下文账本列表（含账单数/收支概览） */
    List<BookVO> listByCurrentContext();

    /** 新建账本 */
    BookVO create(BookDTO dto);

    /** 编辑账本 */
    void update(Long id, BookDTO dto);

    /** 删除账本（有账单时需 force=true；家庭账本仅 OWNER/ADMIN 可删） */
    void delete(Long id, boolean force);

    /** 设为默认账本（同上下文仅一个默认） */
    void setDefault(Long id);
}
