package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 账单分析结果
 */
@Data
public class AiAnalyzeVO implements Serializable {

    /** 分析文本 */
    private String analysis;

    /** 是否命中缓存 */
    private Boolean cached;
}
