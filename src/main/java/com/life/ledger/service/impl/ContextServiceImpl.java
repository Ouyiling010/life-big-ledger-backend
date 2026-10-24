package com.life.ledger.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.common.UserContext;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.entity.User;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.mapper.UserMapper;
import com.life.ledger.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 上下文服务实现
 */
@Service
@RequiredArgsConstructor
public class ContextServiceImpl implements ContextService {

    private final UserMapper userMapper;
    private final FamilyMemberMapper familyMemberMapper;

    @Override
    public Long currentUserId() {
        return UserContext.userId();
    }

    @Override
    public User currentUser() {
        User user = userMapper.selectById(currentUserId());
        if (user == null) {
            throw new BizException(401, "用户不存在或已被删除");
        }
        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BizException(403, "账号已被禁用");
        }
        return user;
    }

    @Override
    public Integer ownerType() {
        User user = currentUser();
        if (isFamilyContext(user)) {
            return Constants.OWNER_FAMILY;
        }
        return Constants.OWNER_PERSONAL;
    }

    @Override
    public Long familyId() {
        User user = currentUser();
        if (isFamilyContext(user)) {
            return user.getCurrentFamilyId();
        }
        return null;
    }

    @Override
    public Long creatorId() {
        User user = currentUser();
        if (isFamilyContext(user)) {
            FamilyMember member = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                    .eq(FamilyMember::getFamilyId, user.getCurrentFamilyId())
                    .eq(FamilyMember::getUserId, user.getId())
                    .eq(FamilyMember::getStatus, 1));
            if (member != null) {
                return member.getId();
            }
        }
        return user.getId();
    }

    private boolean isFamilyContext(User user) {
        return user.getCurrentContext() != null
                && user.getCurrentContext() == Constants.CONTEXT_FAMILY
                && user.getCurrentFamilyId() != null;
    }
}
