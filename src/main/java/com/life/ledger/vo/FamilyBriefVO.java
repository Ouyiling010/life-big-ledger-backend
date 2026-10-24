package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家庭简要信息（用于用户所属家庭列表）
 */
@Data
public class FamilyBriefVO implements Serializable {

    private Long familyId;

    private String name;

    private String avatar;

    /** 成员角色值 1OWNER 2ADMIN 3MEMBER */
    private Integer role;

    /** 角色名称 */
    private String roleName;

    private Integer memberCount;

    /** 0否 1是 */
    private Integer isDefault;

    private LocalDateTime joinedAt;
}
