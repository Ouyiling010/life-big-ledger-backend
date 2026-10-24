package com.life.ledger.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.entity.Notification;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.mapper.NotificationMapper;
import com.life.ledger.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final FamilyMemberMapper familyMemberMapper;

    @Override
    public void push(Long userId, String type, String title, String content, Long refId) {
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRefId(refId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    public void pushFamilyMembers(Long familyId, Long excludeUserId, String type,
                                  String title, String content, Long refId) {
        if (familyId == null) {
            return;
        }
        List<FamilyMember> members = familyMemberMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getStatus, 1));
        for (FamilyMember member : members) {
            Long userId = member.getUserId();
            if (userId == null || userId.equals(excludeUserId)) {
                continue;
            }
            push(userId, type, title, content, refId);
        }
    }

    @Override
    public void pushNewBill(Long familyId, Long excludeUserId, String remark, Long billId) {
        pushFamilyMembers(familyId, excludeUserId, "new_bill", "家庭新账单", remark, billId);
    }
}
