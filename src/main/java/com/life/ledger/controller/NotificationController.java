package com.life.ledger.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.life.ledger.common.BizException;
import com.life.ledger.common.PageResult;
import com.life.ledger.common.Result;
import com.life.ledger.common.UserContext;
import com.life.ledger.entity.Notification;
import com.life.ledger.mapper.NotificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息通知接口
 */
@Tag(name = "消息通知")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;

    /** 我的通知分页（倒序） */
    @Operation(summary = "我的通知分页")
    @GetMapping
    public Result<PageResult<Notification>> page(@RequestParam(defaultValue = "1") long current,
                                                 @RequestParam(defaultValue = "10") long size) {
        Long userId = UserContext.userId();
        Page<Notification> page = new Page<>(current, size);
        Page<Notification> result = notificationMapper.selectPage(page, Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime)
                .orderByDesc(Notification::getId));
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(),
                result.getSize(), result.getRecords()));
    }

    /** 标记单条已读 */
    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        Long userId = UserContext.userId();
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !userId.equals(notification.getUserId())) {
            throw new BizException(403, "无权操作该通知");
        }
        Notification update = new Notification();
        update.setId(id);
        update.setIsRead(1);
        notificationMapper.updateById(update);
        return Result.ok();
    }

    /** 全部标记已读 */
    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Void> readAll() {
        Long userId = UserContext.userId();
        Notification update = new Notification();
        update.setIsRead(1);
        notificationMapper.update(update, Wrappers.<Notification>lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return Result.ok();
    }

    /** 未读数量 */
    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long userId = UserContext.userId();
        Long count = notificationMapper.selectCount(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return Result.ok(count);
    }
}
