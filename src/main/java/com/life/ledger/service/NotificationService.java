package com.life.ledger.service;

/**
 * 消息通知服务
 */
public interface NotificationService {

    /** 给单个用户推送通知 */
    void push(Long userId, String type, String title, String content, Long refId);

    /** 给家庭全体成员（排除某用户）推送通知 */
    void pushFamilyMembers(Long familyId, Long excludeUserId, String type, String title, String content, Long refId);

    /** 家庭新账单提醒（家庭模式下记一笔时触发） */
    void pushNewBill(Long familyId, Long excludeUserId, String remark, Long billId);
}
