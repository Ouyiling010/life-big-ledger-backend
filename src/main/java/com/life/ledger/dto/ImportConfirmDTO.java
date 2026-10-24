package com.life.ledger.dto;

import com.life.ledger.vo.ImportItemVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * CSV 导入确认 DTO
 */
@Data
public class ImportConfirmDTO implements Serializable {

    /** 待入库明细 */
    private List<ImportItemVO> items;

    /** 默认账本（items 未指定时使用） */
    private Long bookId;

    /** 默认账户（items 未指定时使用） */
    private Long accountId;

    /** 为谁花的（家庭模式） */
    private Long memberId;
}
