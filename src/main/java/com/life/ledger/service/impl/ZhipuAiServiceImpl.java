package com.life.ledger.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.ledger.cache.CacheService;
import com.life.ledger.common.Constants;
import com.life.ledger.common.MoneyUtil;
import com.life.ledger.config.AppProperties;
import com.life.ledger.entity.Account;
import com.life.ledger.entity.AiLog;
import com.life.ledger.entity.Category;
import com.life.ledger.mapper.AiLogMapper;
import com.life.ledger.service.AccountService;
import com.life.ledger.service.AiService;
import com.life.ledger.service.CategoryService;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.StatService;
import com.life.ledger.vo.AiAnalyzeVO;
import com.life.ledger.vo.BillDraftVO;
import com.life.ledger.vo.StatOverviewVO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智谱（GLM）AI 服务实现。
 * 具备完整降级能力：未启用 / 无 API Key / 调用异常时改用本地规则，绝不抛异常、绝不让接口 500。
 */
@Slf4j
@Service
public class ZhipuAiServiceImpl implements AiService {

    private static final String SCENE_PARSE = "parse_bill";
    private static final String SCENE_ANALYZE = "analyze";
    private static final long CACHE_TTL = 3600L;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern AMOUNT_WITH_UNIT = Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:元|块|圆|￥|¥)");
    private static final Pattern UNIT_BEFORE = Pattern.compile("(?:￥|¥)\\s*([0-9]+(?:\\.[0-9]{1,2})?)");
    private static final Pattern ANY_NUMBER = Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)");

    private final AppProperties appProperties;
    private final ContextService contextService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final AiLogMapper aiLogMapper;
    private final CacheService cacheService;
    private final StatService statService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient;

    public ZhipuAiServiceImpl(AppProperties appProperties,
                              ContextService contextService,
                              CategoryService categoryService,
                              AccountService accountService,
                              AiLogMapper aiLogMapper,
                              CacheService cacheService,
                              StatService statService) {
        this.appProperties = appProperties;
        this.contextService = contextService;
        this.categoryService = categoryService;
        this.accountService = accountService;
        this.aiLogMapper = aiLogMapper;
        this.cacheService = cacheService;
        this.statService = statService;
        int timeout = appProperties.getAi().getTimeoutSeconds();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    // ------------------------------------------------------------------ 记账解析

    @Override
    public BillDraftVO parseBill(String text) {
        Long userId = contextService.currentUserId();
        List<Category> categories = safeCategories();
        List<Account> accounts = safeAccounts();
        String model = appProperties.getAi().getModel();

        long startMs = System.currentTimeMillis();
        BillDraftVO draft;
        boolean success;
        Integer tokens = null;
        String error = null;
        if (aiAvailable()) {
            try {
                ChatResult result = chat(buildParsePrompt(text, categories, accounts));
                draft = toDraft(result.content(), categories, accounts);
                tokens = result.tokens();
                success = true;
            } catch (Exception e) {
                error = e.getMessage();
                draft = localParse(text, categories, accounts);
                success = false;
            }
        } else {
            error = "AI 未启用或未配置 API Key，已降级为本地解析";
            draft = localParse(text, categories, accounts);
            success = false;
        }
        long costMs = System.currentTimeMillis() - startMs;
        writeLog(userId, SCENE_PARSE, text, toJson(draft), model, tokens, costMs, success, error);
        return draft;
    }

    // ------------------------------------------------------------------ 账单分析

    @Override
    public AiAnalyzeVO analyze(String period) {
        Long userId = contextService.currentUserId();
        String p = normalizePeriod(period);
        // 缓存 key 必须带上下文（个人/家庭），否则同一用户切换模式会命中他人口径的分析
        String key = Constants.CACHE_AI_ANALYZE + scopeKey(userId) + ":" + p;
        Object cached = cacheService.get(key);
        if (cached instanceof AiAnalyzeVO) {
            AiAnalyzeVO hit = (AiAnalyzeVO) cached;
            hit.setCached(true);
            return hit;
        }

        long startMs = System.currentTimeMillis();
        StatOverviewVO current = null;
        StatOverviewVO last = null;
        List<Map<String, Object>> categories = Collections.emptyList();
        try {
            current = statService.overview(p, null, null);
            categories = statService.categoryStat(p, Constants.BILL_EXPENSE, null, null);
            last = statService.overview(YearMonth.parse(p).minusMonths(1).toString(), null, null);
        } catch (Exception e) {
            log.warn("AI 分析聚合数据失败，使用兜底数据: {}", e.getMessage());
        }

        String model = appProperties.getAi().getModel();
        String analysis;
        boolean success;
        Integer tokens = null;
        String error = null;
        if (aiAvailable()) {
            try {
                ChatResult result = chat(buildAnalyzePrompt(p, current, last, categories));
                analysis = result.content();
                tokens = result.tokens();
                success = true;
            } catch (Exception e) {
                error = e.getMessage();
                analysis = localAnalysis(current, last, categories);
                success = false;
            }
        } else {
            error = "AI 未启用或未配置 API Key，已降级为本地分析";
            analysis = localAnalysis(current, last, categories);
            success = false;
        }

        long costMs = System.currentTimeMillis() - startMs;
        AiAnalyzeVO vo = new AiAnalyzeVO();
        vo.setAnalysis(analysis);
        vo.setCached(false);
        // 仅缓存真实 AI 结果；降级/失败结果不缓存，避免 API 恢复后 1 小时内仍返回兜底文案
        if (success) {
            cacheService.set(key, vo, CACHE_TTL);
        }
        writeLog(userId, SCENE_ANALYZE, p, analysis, model, tokens, costMs, success, error);
        return vo;
    }

    // ------------------------------------------------------------------ AI 调用

    private boolean aiAvailable() {
        AppProperties.Ai ai = appProperties.getAi();
        return ai.isEnabled() && ai.getApiKey() != null && !ai.getApiKey().isBlank();
    }

    /** 调用模型，返回 choices[0].message.content 与 usage.total_tokens */
    private ChatResult chat(String prompt) throws Exception {
        AppProperties.Ai ai = appProperties.getAi();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", ai.getModel());
        payload.put("messages", Collections.singletonList(userMessage(prompt)));
        payload.put("temperature", 0.1);

        String bodyJson = objectMapper.writeValueAsString(payload);
        Request request = new Request.Builder()
                .url(ai.getBaseUrl())
                .addHeader("Authorization", "Bearer " + ai.getApiKey())
                .post(RequestBody.create(bodyJson, JSON))
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("AI 接口返回 HTTP " + response.code() + "：" + respBody);
            }
            JsonNode root = objectMapper.readTree(respBody);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new IllegalStateException("AI 返回格式异常：" + respBody);
            }
            String content = choices.get(0).path("message").path("content").asText("");
            int totalTokens = root.path("usage").path("total_tokens").asInt(0);
            return new ChatResult(content, totalTokens > 0 ? totalTokens : null);
        }
    }

    /** 单次模型调用结果：内容 + token 用量 */
    private record ChatResult(String content, Integer tokens) {
    }

    /** AI 缓存上下文隔离：家庭优先，个人按 userId */
    private String scopeKey(Long userId) {
        Long familyId = contextService.familyId();
        return familyId != null ? "f:" + familyId : "u:" + userId;
    }

    private Map<String, Object> userMessage(String prompt) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        return message;
    }

    private String buildParsePrompt(String text, List<Category> categories, List<Account> accounts) {
        StringBuilder categorySb = new StringBuilder();
        for (Category c : categories) {
            if (categorySb.length() > 0) {
                categorySb.append("，");
            }
            categorySb.append(c.getId()).append("=").append(c.getName())
                    .append("(").append(isIncome(c) ? "收入" : "支出").append(")");
        }
        StringBuilder accountSb = new StringBuilder();
        for (Account a : accounts) {
            if (accountSb.length() > 0) {
                accountSb.append("，");
            }
            accountSb.append(a.getId()).append("=").append(a.getName());
        }
        return "你是记账助手。请从用户输入中解析出一条记账草稿，只输出 JSON，不要输出任何解释文字或代码块标记。\n"
                + "可选分类：" + (categorySb.length() == 0 ? "无" : categorySb) + "\n"
                + "可选账户：" + (accountSb.length() == 0 ? "无" : accountSb) + "\n"
                + "JSON 字段：type(1支出/2收入/3转账，整数)、amount(金额，单位分，整数)、categoryId(整数)、"
                + "accountId(整数)、happenedAt(格式 yyyy-MM-dd HH:mm:ss)、remark(备注)\n"
                + "用户输入：" + text;
    }

    private BillDraftVO toDraft(String content, List<Category> categories, List<Account> accounts) throws Exception {
        String json = extractJson(content);
        JsonNode node = objectMapper.readTree(json);
        BillDraftVO vo = new BillDraftVO();
        vo.setType(node.path("type").asInt(Constants.BILL_EXPENSE));
        vo.setAmount(node.path("amount").asLong(0L));
        Long categoryId = node.hasNonNull("categoryId") && node.get("categoryId").isNumber()
                ? node.get("categoryId").asLong() : null;
        Long accountId = node.hasNonNull("accountId") && node.get("accountId").isNumber()
                ? node.get("accountId").asLong() : null;
        vo.setCategoryId(categoryId);
        vo.setAccountId(accountId);
        vo.setHappenedAt(parseDateTime(node.path("happenedAt").asText("")));
        vo.setRemark(node.path("remark").asText(""));
        vo.setCategoryName(findName(categories, categoryId));
        vo.setAccountName(findAccountName(accounts, accountId));
        vo.setConfidence(0.9);
        return vo;
    }

    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("AI 返回内容为空");
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("AI 未返回有效 JSON");
        }
        return content.substring(start, end + 1);
    }

    // ------------------------------------------------------------------ 本地降级：记账解析

    private BillDraftVO localParse(String text, List<Category> categories, List<Account> accounts) {
        BillDraftVO vo = new BillDraftVO();
        int type = containsAny(text, "工资", "红包", "报销", "收入", "收到")
                ? Constants.BILL_INCOME : Constants.BILL_EXPENSE;
        vo.setType(type);

        BigDecimal amount = extractAmount(text);
        vo.setAmount(amount == null ? 0L : MoneyUtil.toCent(amount));

        String categoryName = matchCategoryName(text);
        Long categoryId = categoryService.findIdByName(categoryName, type);
        if (categoryId == null) {
            categoryId = categoryService.findIdByName("其他", type);
        }
        if (categoryId == null) {
            categoryId = firstCategoryId(categories, type);
        }
        vo.setCategoryId(categoryId);
        String resolvedName = findName(categories, categoryId);
        vo.setCategoryName(resolvedName != null ? resolvedName : categoryName);

        Account account = pickAccount(accounts);
        vo.setAccountId(account == null ? null : account.getId());
        vo.setAccountName(account == null ? null : account.getName());

        vo.setHappenedAt(localHappenedAt(text));
        vo.setRemark(text);
        vo.setConfidence(0.5);
        return vo;
    }

    private BigDecimal extractAmount(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = AMOUNT_WITH_UNIT.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        matcher = UNIT_BEFORE.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        matcher = ANY_NUMBER.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }

    private String matchCategoryName(String text) {
        if (containsAny(text, "吃", "饭", "餐", "外卖", "奶茶", "咖啡")) {
            return "餐饮";
        }
        if (containsAny(text, "打车", "地铁", "公交", "交通", "滴滴", "加油")) {
            return "交通";
        }
        if (containsAny(text, "淘宝", "京东", "拼多多", "购物", "超市")) {
            return "购物";
        }
        return "其他";
    }

    private LocalDateTime localHappenedAt(String text) {
        LocalDateTime now = LocalDateTime.now();
        if (text == null) {
            return now;
        }
        if (text.contains("前天")) {
            return now.minusDays(2);
        }
        if (text.contains("昨天")) {
            return now.minusDays(1);
        }
        return now;
    }

    private Account pickAccount(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        for (Account account : accounts) {
            if (account.getName() != null && account.getName().contains("支付宝")) {
                return account;
            }
        }
        return accounts.get(0);
    }

    private Long firstCategoryId(List<Category> categories, int type) {
        if (categories == null) {
            return null;
        }
        for (Category category : categories) {
            if (category.getType() != null && category.getType() == type) {
                return category.getId();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ 本地降级：账单分析

    private String localAnalysis(StatOverviewVO current, StatOverviewVO last, List<Map<String, Object>> categories) {
        return "本月账单分析：总支出 " + yuan(expense(current)) + " 元，总收入 " + yuan(income(current))
                + " 元，结余 " + yuan(balance(current)) + " 元。环比上月支出" + momText(current, last)
                + "。支出主要集中在：" + topCategoriesText(categories, 3)
                + "。建议关注高占比分类，合理规划预算，控制非必要支出。";
    }

    private String buildAnalyzePrompt(String period, StatOverviewVO current, StatOverviewVO last,
                                      List<Map<String, Object>> categories) {
        return "你是财务分析助手。请根据以下本月账单数据，用3~5句中文给出消费分析和建议，"
                + "直接输出文字，不要输出 JSON 或代码块。\n"
                + "本月（" + period + "）总支出 " + yuan(expense(current)) + " 元，总收入 " + yuan(income(current))
                + " 元，结余 " + yuan(balance(current)) + " 元。\n"
                + "支出 Top 分类：" + topCategoriesText(categories, 3) + "\n"
                + "环比上月支出：" + momText(current, last);
    }

    private String topCategoriesText(List<Map<String, Object>> categories, int limit) {
        if (categories == null || categories.isEmpty()) {
            return "无";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map<String, Object> row : categories) {
            if (count >= limit) {
                break;
            }
            if (count > 0) {
                sb.append("、");
            }
            sb.append(Objects.toString(row.get("categoryName"), "未分类"))
                    .append(" ").append(yuan(toLong(row.get("amount")))).append(" 元");
            count++;
        }
        return sb.length() == 0 ? "无" : sb.toString();
    }

    private String momText(StatOverviewVO current, StatOverviewVO last) {
        long cur = expense(current);
        long pre = expense(last);
        if (pre <= 0) {
            return "上月无支出，暂无环比";
        }
        BigDecimal pct = BigDecimal.valueOf(cur - pre)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(pre), 1, RoundingMode.HALF_UP);
        return (pct.signum() >= 0 ? "上涨 " : "下降 ") + pct.abs().toPlainString() + "%";
    }

    // ------------------------------------------------------------------ 日志

    private void writeLog(Long userId, String scene, String input, String output,
                          String model, Integer tokens, long costMs, boolean success, String error) {
        try {
            AiLog aiLog = new AiLog();
            aiLog.setUserId(userId);
            aiLog.setScene(scene);
            aiLog.setInputText(input);
            aiLog.setOutputText(output);
            aiLog.setModel(model);
            aiLog.setTokens(tokens);
            aiLog.setCostMs((int) costMs);
            aiLog.setSuccess(success ? 1 : 0);
            aiLog.setErrorMsg(error);
            aiLogMapper.insert(aiLog);
        } catch (Exception e) {
            log.warn("写 ai_log 失败: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------ 工具方法

    private List<Category> safeCategories() {
        try {
            List<Category> categories = categoryService.listForAi();
            return categories == null ? new ArrayList<>() : categories;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Account> safeAccounts() {
        try {
            List<Account> accounts = accountService.listByCurrentContext();
            return accounts == null ? new ArrayList<>() : accounts;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private boolean isIncome(Category category) {
        return category != null && category.getType() != null && category.getType() == Constants.BILL_INCOME;
    }

    private String findName(List<Category> categories, Long id) {
        if (id == null || categories == null) {
            return null;
        }
        for (Category category : categories) {
            if (id.equals(category.getId())) {
                return category.getName();
            }
        }
        return null;
    }

    private String findAccountName(List<Account> accounts, Long id) {
        if (id == null || accounts == null) {
            return null;
        }
        for (Account account : accounts) {
            if (id.equals(account.getId())) {
                return account.getName();
            }
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(text.trim(), DATE_TIME_FMT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return YearMonth.now().toString();
        }
        try {
            return YearMonth.parse(period.trim()).toString();
        } catch (Exception e) {
            return YearMonth.now().toString();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String yuan(long cent) {
        return MoneyUtil.toYuan(cent).toPlainString();
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return new BigDecimal(value.toString()).longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    private long expense(StatOverviewVO vo) {
        return vo == null || vo.getTotalExpense() == null ? 0L : vo.getTotalExpense();
    }

    private long income(StatOverviewVO vo) {
        return vo == null || vo.getTotalIncome() == null ? 0L : vo.getTotalIncome();
    }

    private long balance(StatOverviewVO vo) {
        return vo == null || vo.getBalance() == null ? 0L : vo.getBalance();
    }
}
