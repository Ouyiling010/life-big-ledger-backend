package com.life.ledger.controller;

import com.life.ledger.annotation.RateLimit;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Result;
import com.life.ledger.service.AiService;
import com.life.ledger.vo.AiAnalyzeVO;
import com.life.ledger.vo.BillDraftVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 接口
 */
@Tag(name = "AI 助手")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** 自然语言解析记账草稿 */
    @Operation(summary = "自然语言记账（一句话解析成账单草稿）")
    @PostMapping("/parse-bill")
    @RateLimit(count = 10, period = 60)
    public Result<BillDraftVO> parseBill(@RequestBody(required = false) Map<String, Object> body) {
        Object text = body == null ? null : body.get("text");
        if (text == null || text.toString().isBlank()) {
            throw new BizException(400, "请输入要解析的记账内容");
        }
        return Result.ok(aiService.parseBill(text.toString()));
    }

    /** 账单智能分析 */
    @Operation(summary = "AI 账单分析（月度报告）")
    @GetMapping("/analyze")
    public Result<AiAnalyzeVO> analyze(@RequestParam(required = false) String period) {
        return Result.ok(aiService.analyze(period));
    }
}
