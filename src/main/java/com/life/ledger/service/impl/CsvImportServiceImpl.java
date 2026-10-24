package com.life.ledger.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.common.MoneyUtil;
import com.life.ledger.dto.ImportConfirmDTO;
import com.life.ledger.entity.Account;
import com.life.ledger.entity.Bill;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.Category;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.CategoryMapper;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.service.AccountService;
import com.life.ledger.service.CategoryService;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.CsvImportService;
import com.life.ledger.service.StatService;
import com.life.ledger.vo.ImportItemVO;
import com.life.ledger.vo.ImportPreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CSV 账单导入实现（支付宝/微信账单）
 */
@Service
@RequiredArgsConstructor
public class CsvImportServiceImpl implements CsvImportService {

    private static final DateTimeFormatter FINGERPRINT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<DateTimeFormatter> TIME_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

    /** 关键字 -> 系统分类名 */
    private static final Map<String, String> KEYWORD_CATEGORY = new LinkedHashMap<>();

    static {
        KEYWORD_CATEGORY.put("美团", "餐饮");
        KEYWORD_CATEGORY.put("饿了么", "餐饮");
        KEYWORD_CATEGORY.put("肯德基", "餐饮");
        KEYWORD_CATEGORY.put("麦当劳", "餐饮");
        KEYWORD_CATEGORY.put("星巴克", "餐饮");
        KEYWORD_CATEGORY.put("外卖", "餐饮");
        KEYWORD_CATEGORY.put("餐饮", "餐饮");
        KEYWORD_CATEGORY.put("滴滴", "交通");
        KEYWORD_CATEGORY.put("地铁", "交通");
        KEYWORD_CATEGORY.put("公交", "交通");
        KEYWORD_CATEGORY.put("打车", "交通");
        KEYWORD_CATEGORY.put("出租车", "交通");
        KEYWORD_CATEGORY.put("高铁", "交通");
        KEYWORD_CATEGORY.put("火车", "交通");
        KEYWORD_CATEGORY.put("加油", "交通");
        KEYWORD_CATEGORY.put("淘宝", "购物");
        KEYWORD_CATEGORY.put("京东", "购物");
        KEYWORD_CATEGORY.put("拼多多", "购物");
        KEYWORD_CATEGORY.put("天猫", "购物");
        KEYWORD_CATEGORY.put("超市", "购物");
        KEYWORD_CATEGORY.put("便利店", "购物");
        KEYWORD_CATEGORY.put("电影", "娱乐");
        KEYWORD_CATEGORY.put("影院", "娱乐");
        KEYWORD_CATEGORY.put("游戏", "娱乐");
        KEYWORD_CATEGORY.put("房租", "居住");
        KEYWORD_CATEGORY.put("物业", "居住");
        KEYWORD_CATEGORY.put("水费", "居住");
        KEYWORD_CATEGORY.put("电费", "居住");
        KEYWORD_CATEGORY.put("医院", "医疗");
        KEYWORD_CATEGORY.put("药店", "医疗");
        KEYWORD_CATEGORY.put("话费", "通讯");
        KEYWORD_CATEGORY.put("流量", "通讯");
        KEYWORD_CATEGORY.put("宽带", "通讯");
    }

    private final BillMapper billMapper;
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final ContextService contextService;
    private final StatService statService;

    @Override
    public ImportPreviewVO preview(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "上传文件不能为空");
        }
        List<List<String>> rows = readCsv(file);

        ImportPreviewVO vo = new ImportPreviewVO();
        List<ImportItemVO> items = new ArrayList<>();
        int headerIndex = findHeaderRow(rows);
        if (headerIndex < 0 || rows.size() <= headerIndex + 1) {
            vo.setTotal(0);
            vo.setDuplicated(0);
            vo.setList(items);
            return vo;
        }

        Map<String, Integer> header = buildHeader(rows.get(headerIndex));
        Long userId = contextService.currentUserId();
        Integer ownerType = contextService.ownerType();
        Long familyId = contextService.familyId();

        int duplicated = 0;
        for (int i = headerIndex + 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            String timeStr = cell(row, header, "交易时间", "交易创建时间", "时间", "交易日期");
            String amountStr = cell(row, header, "交易金额", "金额", "交易金额(元)", "金额(元)",
                    "交易金额（元）", "金额（元）");
            if (StrUtil.isBlank(timeStr) && StrUtil.isBlank(amountStr)) {
                continue;
            }
            Long amount = parseAmount(amountStr);
            if (amount == null || amount <= 0) {
                continue;
            }
            String counterparty = cell(row, header, "交易对方", "对方", "对方名称");
            String remark = cell(row, header, "商品说明", "备注", "商品", "交易说明", "交易类型");
            Integer type = parseType(cell(row, header, "收/支", "收支", "收/支类型"));
            LocalDateTime happenedAt = parseTime(timeStr);

            Long categoryId = suggestCategory(userId, ownerType, familyId, counterparty, remark, type);
            String categoryName = categoryId == null ? null : categoryName(categoryId);
            String fingerprint = fingerprint(happenedAt, amount, counterparty);
            boolean isDuplicate = billMapper.countByFingerprint(userId, fingerprint) > 0;
            if (isDuplicate) {
                duplicated++;
            }

            ImportItemVO item = new ImportItemVO();
            item.setHappenedAt(happenedAt);
            item.setAmount(amount);
            item.setCounterparty(counterparty);
            item.setRemark(remark);
            item.setType(type);
            item.setSuggestedCategoryId(categoryId);
            item.setSuggestedCategoryName(categoryName);
            item.setIsDuplicate(isDuplicate);
            item.setSelected(!isDuplicate);
            items.add(item);
        }
        vo.setTotal(items.size());
        vo.setDuplicated(duplicated);
        vo.setList(items);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirm(ImportConfirmDTO dto) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return 0;
        }
        Integer ownerType = contextService.ownerType();
        Long familyId = contextService.familyId();
        Long userId = contextService.currentUserId();
        Long creatorId = contextService.creatorId();
        Long memberId = ownerType == Constants.OWNER_FAMILY ? dto.getMemberId() : null;
        if (memberId != null) {
            FamilyMember member = familyMemberMapper.selectById(memberId);
            if (member == null || !familyId.equals(member.getFamilyId())
                    || member.getStatus() == null || member.getStatus() != 1) {
                throw new BizException(400, "成员不属于当前家庭");
            }
        }
        Long defaultBookId = dto.getBookId() != null ? dto.getBookId() : defaultBookId(ownerType, userId, familyId);
        Long defaultAccountId = dto.getAccountId() != null ? dto.getAccountId() : defaultAccountId();

        Set<Long> touchedAccounts = new LinkedHashSet<>();
        int count = 0;
        for (ImportItemVO item : dto.getItems()) {
            if (Boolean.FALSE.equals(item.getSelected()) || Boolean.TRUE.equals(item.getIsDuplicate())) {
                continue;
            }
            if (item.getAmount() == null || item.getAmount() <= 0) {
                continue;
            }
            Long accountId = item.getAccountId() != null ? item.getAccountId() : defaultAccountId;
            Long bookId = item.getBookId() != null ? item.getBookId() : defaultBookId;
            if (accountId != null) {
                accountService.getOwnedAccount(accountId);
            }
            if (bookId != null) {
                validateBook(bookId, ownerType, userId, familyId);
            }
            Long categoryId = item.getSuggestedCategoryId();
            Integer type = item.getType() != null ? item.getType() : categoryType(categoryId);
            if (type == null || (type != Constants.BILL_EXPENSE && type != Constants.BILL_INCOME)) {
                throw new BizException(400, "导入仅支持支出/收入类型的账单");
            }
            if (categoryId != null) {
                // 分类必须属于当前上下文，且类型与账单一致（不信任客户端提交值）
                Category category = categoryService.getOwnedCategory(categoryId);
                if (category.getType() != null && !category.getType().equals(type)) {
                    throw new BizException(400, "分类类型与账单类型不一致");
                }
            }

            LocalDateTime happenedAt = item.getHappenedAt() == null ? LocalDateTime.now() : item.getHappenedAt();
            String remark = StrUtil.isNotBlank(item.getRemark()) ? item.getRemark() : item.getCounterparty();
            String fingerprint = fingerprint(happenedAt, item.getAmount(), item.getCounterparty());
            if (billMapper.countByFingerprint(userId, fingerprint) > 0) {
                // 预览到确认之间可能已被其他请求导入，落库前复查指纹
                continue;
            }

            Bill bill = new Bill();
            bill.setOwnerType(ownerType);
            bill.setUserId(userId);
            bill.setFamilyId(familyId);
            bill.setCreatorId(creatorId);
            bill.setMemberId(memberId);
            bill.setBookId(bookId);
            bill.setAccountId(accountId);
            bill.setCategoryId(categoryId);
            bill.setType(type);
            bill.setAmount(item.getAmount());
            bill.setCurrency("CNY");
            bill.setHappenedAt(happenedAt);
            bill.setRemark(remark);
            bill.setIsExcluded(0);
            bill.setSource(Constants.SOURCE_CSV);
            bill.setFingerprint(fingerprint);
            billMapper.insert(bill);
            if (accountId != null) {
                touchedAccounts.add(accountId);
            }
            count++;
        }
        touchedAccounts.forEach(accountService::recalcBalance);
        statService.evictCache();
        return count;
    }

    // ==================== 解析辅助 ====================

    /** 逐行读取并切分 CSV（支持双引号包裹字段、UTF-8 BOM） */
    private List<List<String>> readCsv(MultipartFile file) {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (StrUtil.isBlank(line)) {
                    continue;
                }
                rows.add(splitCsvLine(line));
            }
        } catch (IOException e) {
            throw new BizException(400, "文件读取失败：" + e.getMessage());
        }
        return rows;
    }

    private List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());
        return fields;
    }

    /** 定位真正的表头行（支付宝/微信账单前部常含元信息行） */
    private int findHeaderRow(List<List<String>> rows) {
        for (int i = 0; i < rows.size(); i++) {
            for (String raw : rows.get(i)) {
                String c = raw.replace("\uFEFF", "").trim();
                if (c.equals("交易时间") || c.equals("交易创建时间") || c.equals("交易金额")
                        || c.contains("交易对方") || c.contains("收/支") || c.contains("商品说明")
                        || c.contains("交易类型")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Map<String, Integer> buildHeader(List<String> headRow) {
        Map<String, Integer> header = new LinkedHashMap<>();
        for (int i = 0; i < headRow.size(); i++) {
            String name = headRow.get(i).replace("\uFEFF", "").trim();
            if (!name.isEmpty()) {
                header.put(name, i);
            }
        }
        return header;
    }

    private String cell(List<String> row, Map<String, Integer> header, String... names) {
        for (String name : names) {
            Integer index = header.get(name);
            if (index == null || index >= row.size()) {
                continue;
            }
            String value = row.get(index);
            if (StrUtil.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private LocalDateTime parseTime(String text) {
        if (StrUtil.isBlank(text)) {
            return LocalDateTime.now();
        }
        String value = text.trim();
        for (DateTimeFormatter format : TIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, format);
            } catch (Exception ignored) {
                // 尝试下一个格式
            }
        }
        return LocalDateTime.now();
    }

    private Long parseAmount(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String value = text.replace("¥", "")
                .replace("￥", "")
                .replace(",", "")
                .replace(" ", "")
                .replace("+", "")
                .replace("-", "")
                .trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return MoneyUtil.toCent(new BigDecimal(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseType(String text) {
        if (StrUtil.isNotBlank(text) && text.contains("收入")) {
            return Constants.BILL_INCOME;
        }
        return Constants.BILL_EXPENSE;
    }

    private Long suggestCategory(Long userId, Integer ownerType, Long familyId,
                                 String counterparty, String remark, Integer type) {
        String keyword = StrUtil.isNotBlank(counterparty) ? counterparty : remark;
        if (StrUtil.isNotBlank(keyword)) {
            Long lastCategoryId = billMapper.selectLastCategoryIdByKeyword(userId, ownerType, familyId, keyword, type);
            if (lastCategoryId != null) {
                return lastCategoryId;
            }
        }
        String text = StrUtil.nullToEmpty(counterparty) + StrUtil.nullToEmpty(remark);
        String name = matchKeyword(text);
        if (name != null) {
            Long id = categoryService.findIdByName(name, type);
            if (id != null) {
                return id;
            }
        }
        Long fallback = categoryService.findIdByName("其他", type);
        if (fallback == null) {
            fallback = categoryService.findIdByName("其他", Constants.BILL_EXPENSE);
        }
        return fallback;
    }

    private String matchKeyword(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        for (Map.Entry<String, String> entry : KEYWORD_CATEGORY.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String categoryName(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    private Integer categoryType(Long categoryId) {
        if (categoryId != null) {
            Category category = categoryMapper.selectById(categoryId);
            if (category != null && category.getType() != null) {
                return category.getType();
            }
        }
        return Constants.BILL_EXPENSE;
    }

    private String fingerprint(LocalDateTime happenedAt, Long amount, String counterparty) {
        String time = happenedAt == null ? "" : happenedAt.format(FINGERPRINT_FORMAT);
        return DigestUtil.md5Hex(time + "|" + amount + "|" + StrUtil.nullToEmpty(counterparty));
    }

    private void validateBook(Long bookId, Integer ownerType, Long userId, Long familyId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getOwnerType() == null || !book.getOwnerType().equals(ownerType)) {
            throw new BizException(400, "账本不属于当前上下文");
        }
        if (ownerType == Constants.OWNER_FAMILY) {
            if (familyId == null || !familyId.equals(book.getFamilyId())) {
                throw new BizException(400, "账本不属于当前上下文");
            }
        } else if (!userId.equals(book.getUserId()) || book.getFamilyId() != null) {
            throw new BizException(400, "账本不属于当前上下文");
        }
    }

    private Long defaultBookId(Integer ownerType, Long userId, Long familyId) {
        LambdaQueryWrapper<Book> wrapper = Wrappers.<Book>lambdaQuery().eq(Book::getOwnerType, ownerType);
        if (ownerType == Constants.OWNER_FAMILY) {
            wrapper.eq(Book::getFamilyId, familyId);
        } else {
            wrapper.eq(Book::getUserId, userId).isNull(Book::getFamilyId);
        }
        wrapper.orderByDesc(Book::getIsDefault).orderByAsc(Book::getSort).last("LIMIT 1");
        Book book = bookMapper.selectOne(wrapper);
        return book == null ? null : book.getId();
    }

    private Long defaultAccountId() {
        List<Account> accounts = accountService.listByCurrentContext();
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        Account account = accounts.stream()
                .min(Comparator.comparingInt((Account a) -> a.getSort() == null ? 0 : a.getSort()))
                .orElse(null);
        return account == null ? null : account.getId();
    }
}
