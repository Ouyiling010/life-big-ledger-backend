package com.life.ledger.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.common.FamilyRole;
import com.life.ledger.common.PageResult;
import com.life.ledger.common.UserContext;
import com.life.ledger.dto.FamilyCreateDTO;
import com.life.ledger.dto.FamilyJoinDTO;
import com.life.ledger.dto.FamilyUpdateDTO;
import com.life.ledger.entity.Account;
import com.life.ledger.entity.Bill;
import com.life.ledger.entity.Book;
import com.life.ledger.entity.Category;
import com.life.ledger.entity.Family;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.entity.OperationLog;
import com.life.ledger.entity.User;
import com.life.ledger.mapper.AccountMapper;
import com.life.ledger.mapper.BillMapper;
import com.life.ledger.mapper.BookMapper;
import com.life.ledger.mapper.CategoryMapper;
import com.life.ledger.mapper.FamilyMapper;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.mapper.StatMapper;
import com.life.ledger.mapper.UserMapper;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.FamilyService;
import com.life.ledger.service.NotificationService;
import com.life.ledger.service.OperationLogService;
import com.life.ledger.vo.FamilyDetailVO;
import com.life.ledger.vo.FamilyMemberVO;
import com.life.ledger.vo.FamilyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 家庭服务实现
 */
@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private static final int DEFAULT_MAX_MEMBER = 10;
    private static final int INVITE_EXPIRE_DAYS = 7;

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final AccountMapper accountMapper;
    private final CategoryMapper categoryMapper;
    private final BillMapper billMapper;
    private final StatMapper statMapper;
    private final ContextService contextService;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyVO create(FamilyCreateDTO dto) {
        User user = contextService.currentUser();
        LocalDateTime now = LocalDateTime.now();

        Family family = new Family();
        family.setName(dto.getName());
        family.setAvatar(dto.getAvatar());
        family.setDescription(dto.getDescription());
        family.setOwnerId(user.getId());
        family.setInviteCode(generateInviteCode());
        family.setInviteExpireTime(now.plusDays(INVITE_EXPIRE_DAYS));
        family.setMemberCount(1);
        family.setMaxMember(DEFAULT_MAX_MEMBER);
        family.setNotice("欢迎加入家庭");
        familyMapper.insert(family);

        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(user.getId());
        member.setRole(FamilyRole.OWNER.getValue());
        member.setNicknameInFamily(user.getNickname());
        member.setAvatarInFamily(user.getAvatar());
        member.setJoinTime(now);
        member.setStatus(Constants.STATUS_ENABLED);
        familyMemberMapper.insert(member);

        // 默认家庭账本
        Book book = new Book();
        book.setOwnerType(Constants.OWNER_FAMILY);
        book.setUserId(user.getId());
        book.setFamilyId(family.getId());
        book.setCreatorId(member.getId());
        book.setName("家庭账本");
        book.setCurrency("CNY");
        book.setIsDefault(1);
        book.setSort(0);
        bookMapper.insert(book);

        // 三个默认账户
        createDefaultAccount(family.getId(), user.getId(), member.getId(), "现金", Constants.ACCOUNT_CASH, 0);
        createDefaultAccount(family.getId(), user.getId(), member.getId(), "支付宝", Constants.ACCOUNT_VIRTUAL, 1);
        createDefaultAccount(family.getId(), user.getId(), member.getId(), "微信", Constants.ACCOUNT_VIRTUAL, 2);

        return toVo(family, FamilyRole.OWNER.getValue());
    }

    @Override
    public List<FamilyVO> listMy() {
        Long userId = contextService.currentUserId();
        List<FamilyMember> memberships = familyMemberMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED));
        if (memberships.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Integer> roleMap = new HashMap<>();
        for (FamilyMember m : memberships) {
            roleMap.put(m.getFamilyId(), m.getRole());
        }
        List<Family> families = familyMapper.selectBatchIds(new ArrayList<>(roleMap.keySet()));
        List<FamilyVO> result = new ArrayList<>();
        for (Family f : families) {
            result.add(toVo(f, roleMap.get(f.getId())));
        }
        result.sort((a, b) -> {
            LocalDateTime t1 = a.getCreateTime();
            LocalDateTime t2 = b.getCreateTime();
            if (t1 == null || t2 == null) {
                return 0;
            }
            return t2.compareTo(t1);
        });
        return result;
    }

    @Override
    public FamilyDetailVO detail(Long familyId) {
        FamilyMember me = requireMember(familyId, FamilyRole.MEMBER);
        Family family = requireFamily(familyId);

        FamilyDetailVO vo = new FamilyDetailVO();
        vo.setId(family.getId());
        vo.setName(family.getName());
        vo.setAvatar(family.getAvatar());
        vo.setDescription(family.getDescription());
        vo.setOwnerId(family.getOwnerId());
        vo.setInviteCode(family.getInviteCode());
        vo.setInviteExpireTime(family.getInviteExpireTime());
        vo.setMemberCount(family.getMemberCount());
        vo.setMaxMember(family.getMaxMember());
        vo.setNotice(family.getNotice());
        vo.setCreateTime(family.getCreateTime());
        vo.setMyRole(me.getRole());
        vo.setMyRoleName(roleName(me.getRole()));
        vo.setMembers(members(familyId));
        vo.setBookCount(bookMapper.selectCount(Wrappers.<Book>lambdaQuery()
                .eq(Book::getOwnerType, Constants.OWNER_FAMILY)
                .eq(Book::getFamilyId, familyId)));
        vo.setAccountCount(accountMapper.selectCount(Wrappers.<Account>lambdaQuery()
                .eq(Account::getOwnerType, Constants.OWNER_FAMILY)
                .eq(Account::getFamilyId, familyId)));
        List<Account> accounts = accountMapper.selectList(Wrappers.<Account>lambdaQuery()
                .eq(Account::getOwnerType, Constants.OWNER_FAMILY)
                .eq(Account::getFamilyId, familyId)
                .eq(Account::getIncludeInTotal, 1));
        long totalAsset = 0L;
        for (Account a : accounts) {
            totalAsset += a.getBalance() == null ? 0L : a.getBalance();
        }
        vo.setTotalAsset(totalAsset);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long familyId, FamilyUpdateDTO dto) {
        requireMember(familyId, FamilyRole.ADMIN);
        Family family = requireFamily(familyId);
        if (dto.getName() != null) {
            family.setName(dto.getName());
        }
        if (dto.getAvatar() != null) {
            family.setAvatar(dto.getAvatar());
        }
        if (dto.getDescription() != null) {
            family.setDescription(dto.getDescription());
        }
        if (dto.getNotice() != null) {
            family.setNotice(dto.getNotice());
        }
        familyMapper.updateById(family);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolve(Long familyId) {
        requireMember(familyId, FamilyRole.OWNER);
        Family family = requireFamily(familyId);
        List<FamilyMember> members = familyMemberMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED));
        for (FamilyMember m : members) {
            m.setStatus(Constants.STATUS_DISABLED);
            familyMemberMapper.updateById(m);
            clearUserFamilyContext(m.getUserId(), familyId);
        }
        // 清理家庭下的共享数据（逻辑删除），避免解散后留下无法访问的孤儿记录
        billMapper.delete(Wrappers.<Bill>lambdaQuery().eq(Bill::getFamilyId, familyId));
        bookMapper.delete(Wrappers.<Book>lambdaQuery().eq(Book::getFamilyId, familyId));
        accountMapper.delete(Wrappers.<Account>lambdaQuery().eq(Account::getFamilyId, familyId));
        categoryMapper.delete(Wrappers.<Category>lambdaQuery().eq(Category::getFamilyId, familyId));
        familyMapper.deleteById(family.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetInvite(Long familyId) {
        requireMember(familyId, FamilyRole.ADMIN);
        Family family = requireFamily(familyId);
        String code = generateInviteCode();
        family.setInviteCode(code);
        family.setInviteExpireTime(LocalDateTime.now().plusDays(INVITE_EXPIRE_DAYS));
        familyMapper.updateById(family);
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyVO join(FamilyJoinDTO dto) {
        Long userId = contextService.currentUserId();
        User user = contextService.currentUser();
        String code = dto.getInviteCode() == null ? null : dto.getInviteCode().trim().toUpperCase();
        Family family = familyMapper.selectOne(Wrappers.<Family>lambdaQuery()
                .eq(Family::getInviteCode, code));
        if (family == null) {
            throw new BizException(404, "邀请码无效");
        }
        if (family.getInviteExpireTime() != null && family.getInviteExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException(400, "邀请码已过期");
        }
        int memberCount = family.getMemberCount() == null ? 0 : family.getMemberCount();
        int maxMember = family.getMaxMember() == null ? DEFAULT_MAX_MEMBER : family.getMaxMember();
        if (memberCount >= maxMember) {
            throw new BizException(400, "家庭成员已达上限");
        }
        FamilyMember existing = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, family.getId())
                .eq(FamilyMember::getUserId, userId));
        if (existing != null && Objects.equals(existing.getStatus(), Constants.STATUS_ENABLED)) {
            throw new BizException(409, "您已是该家庭成员");
        }
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setRole(FamilyRole.MEMBER.getValue());
            existing.setNicknameInFamily(user.getNickname());
            existing.setAvatarInFamily(user.getAvatar());
            existing.setJoinTime(now);
            existing.setStatus(Constants.STATUS_ENABLED);
            familyMemberMapper.updateById(existing);
        } else {
            FamilyMember member = new FamilyMember();
            member.setFamilyId(family.getId());
            member.setUserId(userId);
            member.setRole(FamilyRole.MEMBER.getValue());
            member.setNicknameInFamily(user.getNickname());
            member.setAvatarInFamily(user.getAvatar());
            member.setJoinTime(now);
            member.setStatus(Constants.STATUS_ENABLED);
            familyMemberMapper.insert(member);
        }
        family.setMemberCount(memberCount + 1);
        familyMapper.updateById(family);

        notificationService.pushFamilyMembers(family.getId(), userId, "family",
                "新成员加入", displayName(user.getNickname()) + " 加入了家庭「" + family.getName() + "」", family.getId());
        notificationService.push(userId, "family", "加入成功",
                "您已加入家庭「" + family.getName() + "」", family.getId());

        return toVo(family, FamilyRole.MEMBER.getValue());
    }

    @Override
    public List<FamilyMemberVO> members(Long familyId) {
        requireMember(familyId, FamilyRole.MEMBER);
        List<FamilyMember> list = familyMemberMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED)
                .orderByAsc(FamilyMember::getRole)
                .orderByAsc(FamilyMember::getJoinTime));
        List<FamilyMemberVO> result = new ArrayList<>();
        if (list.isEmpty()) {
            return result;
        }
        List<Long> userIds = new ArrayList<>();
        for (FamilyMember m : list) {
            if (m.getUserId() != null) {
                userIds.add(m.getUserId());
            }
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User u : userMapper.selectBatchIds(userIds)) {
                userMap.put(u.getId(), u);
            }
        }
        Map<Long, Long> expenseMap = memberExpenseMap(familyId);
        for (FamilyMember m : list) {
            User u = userMap.get(m.getUserId());
            FamilyMemberVO vo = new FamilyMemberVO();
            vo.setMemberId(m.getId());
            vo.setUserId(m.getUserId());
            String nickname = m.getNicknameInFamily() != null ? m.getNicknameInFamily()
                    : (u == null ? null : u.getNickname());
            vo.setNickname(nickname);
            String avatar = m.getAvatarInFamily() != null ? m.getAvatarInFamily()
                    : (u == null ? null : u.getAvatar());
            vo.setAvatar(avatar);
            vo.setRole(m.getRole());
            vo.setRoleName(roleName(m.getRole()));
            vo.setJoinTime(m.getJoinTime());
            vo.setTotalExpense(expenseMap.getOrDefault(m.getId(), 0L));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long familyId, Long memberId, Integer role) {
        FamilyMember me = requireMember(familyId, FamilyRole.OWNER);
        FamilyMember target = requireMemberRecord(familyId, memberId);
        if (Objects.equals(target.getId(), me.getId())) {
            throw new BizException(400, "不能修改自己的角色");
        }
        if (role == null || role == FamilyRole.OWNER.getValue()) {
            throw new BizException(400, "该角色请使用转移家庭功能设置");
        }
        if (role != FamilyRole.ADMIN.getValue() && role != FamilyRole.MEMBER.getValue()) {
            throw new BizException(400, "角色取值不合法");
        }
        target.setRole(role);
        familyMemberMapper.updateById(target);
        notificationService.push(target.getUserId(), "family", "角色变更",
                "您在家庭中的角色已变更为 " + roleName(role), familyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long familyId, Long memberId) {
        FamilyMember me = requireMember(familyId, FamilyRole.ADMIN);
        FamilyMember target = requireMemberRecord(familyId, memberId);
        if (Objects.equals(target.getRole(), FamilyRole.OWNER.getValue())) {
            throw new BizException(403, "不能移除家庭创建者");
        }
        if (Objects.equals(target.getId(), me.getId())) {
            throw new BizException(400, "请使用退出家庭功能");
        }
        target.setStatus(Constants.STATUS_DISABLED);
        familyMemberMapper.updateById(target);
        decreaseMemberCount(familyId);
        clearUserFamilyContext(target.getUserId(), familyId);
        notificationService.push(target.getUserId(), "family", "已被移出家庭",
                "您已被移出家庭", familyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quit(Long familyId) {
        FamilyMember me = requireMember(familyId, FamilyRole.MEMBER);
        if (Objects.equals(me.getRole(), FamilyRole.OWNER.getValue())) {
            throw new BizException(409, "家庭创建者请先转让或解散家庭");
        }
        me.setStatus(Constants.STATUS_DISABLED);
        familyMemberMapper.updateById(me);
        decreaseMemberCount(familyId);
        clearUserFamilyContext(me.getUserId(), familyId);
        notificationService.pushFamilyMembers(familyId, me.getUserId(), "family",
                "成员退出", "有成员退出了家庭", familyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long familyId, Long targetMemberId) {
        FamilyMember me = requireMember(familyId, FamilyRole.OWNER);
        FamilyMember target = requireMemberRecord(familyId, targetMemberId);
        if (Objects.equals(target.getId(), me.getId())) {
            throw new BizException(400, "不能转让给自己");
        }
        Family family = requireFamily(familyId);
        me.setRole(FamilyRole.ADMIN.getValue());
        target.setRole(FamilyRole.OWNER.getValue());
        familyMemberMapper.updateById(me);
        familyMemberMapper.updateById(target);
        family.setOwnerId(target.getUserId());
        familyMapper.updateById(family);
        notificationService.push(target.getUserId(), "family", "成为家庭创建者",
                "您已成为家庭「" + family.getName() + "」的创建者", familyId);
    }

    @Override
    public PageResult<OperationLog> logs(Long familyId, String module, long current, long size) {
        requireMember(familyId, FamilyRole.ADMIN);
        return operationLogService.pageByFamily(familyId, module, current, size);
    }

    /* ---------------- private helpers ---------------- */

    private Family requireFamily(Long familyId) {
        Family family = familyId == null ? null : familyMapper.selectById(familyId);
        if (family == null) {
            throw new BizException(404, "家庭不存在");
        }
        return family;
    }

    /** 校验当前用户是家庭成员且角色满足 minRole，返回当前用户成员记录 */
    private FamilyMember requireMember(Long familyId, FamilyRole minRole) {
        requireFamily(familyId);
        Long userId = contextService.currentUserId();
        FamilyMember member = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED));
        if (member == null) {
            throw new BizException(403, "您不是该家庭成员");
        }
        if (!FamilyRole.of(member.getRole()).satisfies(minRole)) {
            throw new BizException(403, "无权限操作");
        }
        return member;
    }

    private FamilyMember requireMemberRecord(Long familyId, Long memberId) {
        FamilyMember member = memberId == null ? null : familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getId, memberId)
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED));
        if (member == null) {
            throw new BizException(404, "成员不存在或已退出");
        }
        return member;
    }

    private void decreaseMemberCount(Long familyId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            return;
        }
        int count = family.getMemberCount() == null ? 0 : family.getMemberCount();
        family.setMemberCount(Math.max(0, count - 1));
        familyMapper.updateById(family);
    }

    /** 若用户当前上下文指向被退出的家庭，则切回个人模式 */
    private void clearUserFamilyContext(Long userId, Long familyId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        if (Objects.equals(user.getCurrentFamilyId(), familyId)) {
            boolean clearDefault = Objects.equals(user.getDefaultFamilyId(), familyId);
            // updateById 会忽略 null 字段，这里必须显式 set null 才能真正清空家庭上下文
            userMapper.update(null, Wrappers.<User>lambdaUpdate()
                    .eq(User::getId, userId)
                    .set(User::getCurrentContext, Constants.CONTEXT_PERSONAL)
                    .set(User::getCurrentFamilyId, null)
                    .set(clearDefault, User::getDefaultFamilyId, null));
        }
    }

    private Map<Long, Long> memberExpenseMap(Long familyId) {
        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        List<Map<String, Object>> rows = statMapper.memberStat(familyId, start, end);
        Map<Long, Long> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object memberId = row.get("memberId");
            Object total = row.get("totalExpense");
            if (memberId instanceof Number && total instanceof Number) {
                map.put(((Number) memberId).longValue(), ((Number) total).longValue());
            }
        }
        return map;
    }

    private String generateInviteCode() {
        for (int i = 0; i < 10; i++) {
            String code = RandomUtil.randomString(8).toUpperCase();
            Long count = familyMapper.selectCount(Wrappers.<Family>lambdaQuery()
                    .eq(Family::getInviteCode, code));
            if (count == null || count == 0L) {
                return code;
            }
        }
        throw new BizException(500, "邀请码生成失败，请稍后重试");
    }

    private void createDefaultAccount(Long familyId, Long userId, Long memberId, String name, int type, int sort) {
        Account account = new Account();
        account.setOwnerType(Constants.OWNER_FAMILY);
        account.setUserId(userId);
        account.setFamilyId(familyId);
        account.setCreatorId(memberId);
        account.setName(name);
        account.setType(type);
        account.setInitBalance(0L);
        account.setBalance(0L);
        account.setIncludeInTotal(1);
        account.setSort(sort);
        accountMapper.insert(account);
    }

    private FamilyVO toVo(Family family, Integer myRole) {
        FamilyVO vo = new FamilyVO();
        vo.setId(family.getId());
        vo.setName(family.getName());
        vo.setAvatar(family.getAvatar());
        vo.setDescription(family.getDescription());
        vo.setOwnerId(family.getOwnerId());
        vo.setInviteCode(family.getInviteCode());
        vo.setInviteExpireTime(family.getInviteExpireTime());
        vo.setMemberCount(family.getMemberCount());
        vo.setMaxMember(family.getMaxMember());
        vo.setNotice(family.getNotice());
        vo.setMyRole(myRole);
        vo.setMyRoleName(roleName(myRole));
        vo.setCreateTime(family.getCreateTime());
        return vo;
    }

    private String displayName(String nickname) {
        return nickname == null || nickname.isBlank() ? "新成员" : nickname;
    }

    private static String roleName(Integer role) {
        if (role == null) {
            return FamilyRole.MEMBER.name();
        }
        if (role == FamilyRole.OWNER.getValue()) {
            return "创建者";
        }
        if (role == FamilyRole.ADMIN.getValue()) {
            return "管理员";
        }
        return "成员";
    }

    @Override
    public void updateMyNickname(Long familyId, String nicknameInFamily) {
        Long userId = UserContext.userId();
        FamilyMember member = familyMemberMapper.selectOne(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getStatus, Constants.STATUS_ENABLED));
        if (member == null) {
            throw new BizException(404, "你不是该家庭成员");
        }
        if (nicknameInFamily != null && nicknameInFamily.length() > 50) {
            throw new BizException(400, "家庭内昵称最长 50 个字符");
        }
        // 空串归一化为 null，前端传 "" 表示恢复默认（使用用户昵称）
        member.setNicknameInFamily(StrUtil.isBlank(nicknameInFamily) ? null : nicknameInFamily.trim());
        familyMemberMapper.updateById(member);
    }
}
