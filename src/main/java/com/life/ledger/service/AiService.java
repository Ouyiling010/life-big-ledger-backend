package com.life.ledger.service;

import com.life.ledger.vo.AiAnalyzeVO;
import com.life.ledger.vo.BillDraftVO;

/**
 * AI 服务
 */
public interface AiService {

    /** 自然语言解析记账草稿（失败自动降级为本地规则解析，不抛异常） */
    BillDraftVO parseBill(String text);

    /** 账单分析（无 Key 时本地降级，保证结构化返回） */
    AiAnalyzeVO analyze(String period);
}
