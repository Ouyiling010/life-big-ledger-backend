package com.life.ledger.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.life.ledger.common.BizException;
import com.life.ledger.common.Constants;
import com.life.ledger.common.FamilyRole;
import com.life.ledger.config.AppProperties;
import com.life.ledger.dto.ContextSwitchDTO;
import com.life.ledger.dto.PasswordChangeDTO;
import com.life.ledger.dto.ProfileUpdateDTO;
import com.life.ledger.entity.Family;
import com.life.ledger.entity.FamilyMember;
import com.life.ledger.entity.User;
import com.life.ledger.mapper.FamilyMapper;
import com.life.ledger.mapper.FamilyMemberMapper;
import com.life.ledger.mapper.UserMapper;
import com.life.ledger.service.ContextService;
import com.life.ledger.service.UserService;
import com.life.ledger.vo.FamilyBriefVO;
import com.life.ledger.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final ContextService contextService;
    private final AppProperties appProperties;

    @Override
    public UserVO profile() {
        return UserVO.from(contextService.currentUser());
    }

    @Override
    public UserVO updateProfile(ProfileUpdateDTO dto) {
        User user = contextService.currentUser();
        if (dto.getEmail() != null) {
            // 空串归一化为 NULL，避免与 uk_email 唯一索引冲突
            String email = StrUtil.isBlank(dto.getEmail()) ? null : dto.getEmail();
            if (email != null && !email.equals(user.getEmail())) {
                long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, email)
                        .ne(User::getId, user.getId()));
                if (count > 0) {
                    throw new BizException(409, "邮箱已被使用");
                }
            }
            user.setEmail(email);
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getPhone() != null) {
            user.setPhone(StrUtil.isBlank(dto.getPhone()) ? null : dto.getPhone());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        userMapper.updateById(user);
        return UserVO.from(user);
    }

    @Override
    public void changePassword(PasswordChangeDTO dto) {
        User user = contextService.currentUser();
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(400, "原密码错误");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword(), BCrypt.gensalt()));
        userMapper.updateById(user);
        // 改密后踢掉该用户全部会话，强制用新密码重新登录
        try {
            StpUtil.logout(user.getId());
        } catch (Exception ignore) {
            // 没有在线会话时 logout 会抛异常，忽略
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "请选择文件");
        }
        String suffix = StrUtil.nullToEmpty(FileUtil.getSuffix(file.getOriginalFilename())).toLowerCase();
        // 白名单校验：/uploads/** 为静态资源目录，防止上传 html/svg 等可执行内容
        if (!"jpg".equals(suffix) && !"jpeg".equals(suffix) && !"png".equals(suffix)
                && !"gif".equals(suffix) && !"webp".equals(suffix)) {
            throw new BizException(400, "头像仅支持 jpg/jpeg/png/gif/webp 格式");
        }
        // 统一用绝对路径，避免相对路径在不同启动目录（IDE / mvn / jar）下解析不一致
        Path dir = Paths.get(appProperties.getUpload().getDir(), "avatar").toAbsolutePath().normalize();
        String fileName = IdUtil.fastSimpleUUID() + "." + suffix;
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName).toFile());
        } catch (Exception e) {
            log.error("头像上传失败，目录={}", dir, e);
            throw new BizException(500, "头像上传失败");
        }
        return "/uploads/avatar/" + fileName;
    }

    @Override
    public List<FamilyBriefVO> myFamilies() {
        User user = contextService.currentUser();
        List<FamilyMember> members = familyMemberMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getUserId, user.getId())
                .eq(FamilyMember::getStatus, 1));
        List<FamilyBriefVO> result = new ArrayList<>();
        for (FamilyMember member : members) {
            Family family = familyMapper.selectById(member.getFamilyId());
            if (family == null) {
                continue;
            }
            FamilyRole role = FamilyRole.of(member.getRole());
            FamilyBriefVO vo = new FamilyBriefVO();
            vo.setFamilyId(family.getId());
            vo.setName(family.getName());
            vo.setAvatar(family.getAvatar());
            vo.setRole(role.getValue());
            vo.setRoleName(role.getName());
            vo.setMemberCount(family.getMemberCount());
            vo.setIsDefault(family.getId().equals(user.getDefaultFamilyId()) ? 1 : 0);
            vo.setJoinedAt(member.getJoinTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    public UserVO switchContext(ContextSwitchDTO dto) {
        User user = contextService.currentUser();
        Integer context = dto.getContext();
        if (context == null
                || (context != Constants.CONTEXT_PERSONAL && context != Constants.CONTEXT_FAMILY)) {
            throw new BizException(400, "无效的上下文类型");
        }

        Long familyId = null;
        if (context == Constants.CONTEXT_FAMILY) {
            familyId = dto.getFamilyId();
            if (familyId == null) {
                throw new BizException(400, "请选择家庭");
            }
            long count = familyMemberMapper.selectCount(Wrappers.<FamilyMember>lambdaQuery()
                    .eq(FamilyMember::getFamilyId, familyId)
                    .eq(FamilyMember::getUserId, user.getId())
                    .eq(FamilyMember::getStatus, 1));
            if (count == 0) {
                throw new BizException(403, "你不是该家庭成员");
            }
        }

        // 个人模式需把 currentFamilyId 置空，故使用 UpdateWrapper 显式 set
        userMapper.update(null, Wrappers.<User>lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(User::getCurrentContext, context)
                .set(User::getCurrentFamilyId, familyId));

        user.setCurrentContext(context);
        user.setCurrentFamilyId(familyId);
        return UserVO.from(user);
    }

    @Override
    public void markGuided() {
        User user = contextService.currentUser();
        user.setHasGuided(1);
        userMapper.updateById(user);
    }
}
