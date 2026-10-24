package com.life.ledger.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.entity.Account;
import com.life.ledger.entity.Bill;
import com.life.ledger.entity.BillTemplate;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.Category;
import com.life.ledger.mapper.AccountMapper;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.BillTemplateMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.CategoryMapper;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全量导出服务实现：按当前上下文（个人/家庭）导出，不包含密码等敏感字段
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BookMapper bookMapper;
    private final AccountMapper accountMapper;
    private final CategoryMapper categoryMapper;
    private final BillMapper billMapper;
    private final BillTemplateMapper billTemplateMapper;
    private final ContextService contextService;

    @Override
    public Map<String, Object> exportAll() {
        Long userId = contextService.currentUserId();
        Integer ownerType = contextService.ownerType();
        Long familyId = contextService.familyId();
        boolean family = Constants.OWNER_FAMILY == ownerType;

        List<Book> books = bookMapper.selectList(Wrappers.<Book>lambdaQuery()
                .eq(Book::getOwnerType, ownerType)
                .eq(family, Book::getFamilyId, familyId)
                .eq(!family, Book::getUserId, userId)
                .orderByAsc(Book::getSort));

        List<Account> accounts = accountMapper.selectList(Wrappers.<Account>lambdaQuery()
                .eq(Account::getOwnerType, ownerType)
                .eq(family, Account::getFamilyId, familyId)
                .eq(!family, Account::getUserId, userId)
                .orderByAsc(Account::getSort));

        List<Category> categories = new ArrayList<>(categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getOwnerType, Constants.OWNER_SYSTEM)));
        categories.addAll(categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getOwnerType, ownerType)
                .eq(family, Category::getFamilyId, familyId)
                .eq(!family, Category::getUserId, userId)
                .orderByAsc(Category::getSort)));

        List<Bill> bills = billMapper.selectList(Wrappers.<Bill>lambdaQuery()
                .eq(Bill::getOwnerType, ownerType)
                .eq(family, Bill::getFamilyId, familyId)
                .eq(!family, Bill::getUserId, userId)
                .orderByDesc(Bill::getHappenedAt));

        List<BillTemplate> billTemplates = billTemplateMapper.selectList(Wrappers.<BillTemplate>lambdaQuery()
                .eq(BillTemplate::getUserId, userId)
                .orderByAsc(BillTemplate::getSort));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("generateTime", LocalDateTime.now().format(TIME_FORMATTER));
        data.put("ownerType", ownerType);
        data.put("books", books);
        data.put("accounts", accounts);
        data.put("categories", categories);
        data.put("bills", bills);
        data.put("billTemplates", billTemplates);
        return data;
    }

    @Override
    public void exportBillsExcel(HttpServletResponse response) {
        try {
            Long userId = contextService.currentUserId();
            Integer ownerType = contextService.ownerType();
            Long familyId = contextService.familyId();
            boolean family = Constants.OWNER_FAMILY == ownerType;

            List<Bill> bills = billMapper.selectList(Wrappers.<Bill>lambdaQuery()
                    .eq(Bill::getOwnerType, ownerType)
                    .eq(family, Bill::getFamilyId, familyId)
                    .eq(!family, Bill::getUserId, userId)
                    .orderByDesc(Bill::getHappenedAt));

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("bills", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName + ".xlsx");

            List<List<String>> head = new ArrayList<>();
            head.add(Collections.singletonList("账单ID"));
            head.add(Collections.singletonList("类型"));
            head.add(Collections.singletonList("金额(分)"));
            head.add(Collections.singletonList("发生时间"));
            head.add(Collections.singletonList("分类ID"));
            head.add(Collections.singletonList("账户ID"));
            head.add(Collections.singletonList("账本ID"));
            head.add(Collections.singletonList("备注"));
            head.add(Collections.singletonList("标签"));

            List<List<Object>> data = new ArrayList<>();
            for (Bill bill : bills) {
                List<Object> row = new ArrayList<>();
                row.add(bill.getId());
                row.add(billTypeName(bill.getType()));
                row.add(bill.getAmount());
                row.add(bill.getHappenedAt() == null ? null : bill.getHappenedAt().format(TIME_FORMATTER));
                row.add(bill.getCategoryId());
                row.add(bill.getAccountId());
                row.add(bill.getBookId());
                row.add(bill.getRemark());
                row.add(bill.getTags());
                data.add(row);
            }
            EasyExcel.write(response.getOutputStream()).head(head).sheet("账单明细").doWrite(data);
        } catch (Exception e) {
            log.error("导出账单 Excel 失败", e);
            throw new BizException(500, "导出失败，请稍后重试");
        }
    }

    private String billTypeName(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case Constants.BILL_EXPENSE -> "支出";
            case Constants.BILL_INCOME -> "收入";
            case Constants.BILL_TRANSFER -> "转账";
            default -> "";
        };
    }
}
