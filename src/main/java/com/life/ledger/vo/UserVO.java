package com.life.ledger.vo;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.life.ledger.entity.User;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息
 */
@Data
public class UserVO implements Serializable {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private Integer gender;

    private Integer isAdmin;

    private Integer hasGuided;

    private Integer currentContext;

    private Long currentFamilyId;

    private Long defaultFamilyId;

    private Integer status;

    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        // 敏感字段脱敏：手机号 → 138****5678，邮箱 → u***@qq.com
        vo.setEmail(StrUtil.isBlank(user.getEmail()) ? user.getEmail() : DesensitizedUtil.email(user.getEmail()));
        vo.setPhone(StrUtil.isBlank(user.getPhone()) ? user.getPhone() : DesensitizedUtil.mobilePhone(user.getPhone()));
        vo.setGender(user.getGender());
        vo.setIsAdmin(user.getIsAdmin());
        vo.setHasGuided(user.getHasGuided());
        vo.setCurrentContext(user.getCurrentContext());
        vo.setCurrentFamilyId(user.getCurrentFamilyId());
        vo.setDefaultFamilyId(user.getDefaultFamilyId());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
