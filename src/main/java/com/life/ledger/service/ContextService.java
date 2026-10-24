package com.life.ledger.service;

import com.life.ledger.entity.User;

/**
 * 上下文服务：解析当前用户所处的模式（个人 / 家庭）
 * 所有数据模块读取数据时必须使用本服务确定 owner_type / family_id / creator_id
 */
public interface ContextService {

    Long currentUserId();

    User currentUser();

    /** 1个人 2家庭 */
    Integer ownerType();

    /** 家庭模式下返回家庭ID，个人模式返回 null */
    Long familyId();

    /** 记账人ID：个人模式=user_id；家庭模式=family_member.id */
    Long creatorId();
}
