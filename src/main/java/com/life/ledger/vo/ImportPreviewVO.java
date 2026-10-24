package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * CSV 导入预览结果
 */
@Data
public class ImportPreviewVO implements Serializable {

    /** 解析出的明细总数 */
    private Integer total;

    /** 疑似重复条数 */
    private Integer duplicated;

    private List<ImportItemVO> list;
}
