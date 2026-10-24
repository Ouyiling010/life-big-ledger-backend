package com.life.ledger.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 账单导出 Excel 行模型
 */
@Data
public class BillExportVO implements Serializable {

    @ExcelProperty("时间")
    private String happenedAt;

    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("金额")
    private String amount;

    @ExcelProperty("分类")
    private String categoryName;

    @ExcelProperty("账户")
    private String accountName;

    @ExcelProperty("账本")
    private String bookName;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("标签")
    private String tags;

    @ExcelProperty("记账人")
    private String creatorName;
}
