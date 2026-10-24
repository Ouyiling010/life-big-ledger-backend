package com.life.ledger.service;

import com.life.ledger.dto.ContextSwitchDTO;
import com.life.ledger.dto.PasswordChangeDTO;
import com.life.ledger.dto.ProfileUpdateDTO;
import com.life.ledger.vo.FamilyBriefVO;
import com.life.ledger.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户服务：资料、密码、头像、家庭列表、上下文切换、引导标记
 */
public interface UserService {

    UserVO profile();

    UserVO updateProfile(ProfileUpdateDTO dto);

    void changePassword(PasswordChangeDTO dto);

    /** 上传头像，返回可访问 url */
    String uploadAvatar(MultipartFile file);

    List<FamilyBriefVO> myFamilies();

    UserVO switchContext(ContextSwitchDTO dto);

    void markGuided();
}
