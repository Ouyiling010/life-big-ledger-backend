package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家庭列表/创建返回 VO
 */
@Data
public class FamilyVO implements Serializable {

    private Long id;

    private String name;

    private String avatar;

    private String description;

    private Long ownerId;

    private String inviteCode;

    private LocalDateTime inviteExpireTime;

    private Integer memberCount;

    private Integer maxMember;

    private String notice;

    /** 当前用户在该家庭的角色：1OWNER 2ADMIN 3MEMBER */
    private Integer myRole;

    private String myRoleName;

    private LocalDateTime createTime;
}
