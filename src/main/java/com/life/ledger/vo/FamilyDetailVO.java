package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 家庭详情 VO：家庭信息 + 成员列表 + 数据概览
 */
@Data
public class FamilyDetailVO implements Serializable {

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

    private LocalDateTime createTime;

    /** 当前用户角色 */
    private Integer myRole;

    private String myRoleName;

    /** 成员列表 */
    private List<FamilyMemberVO> members;

    /** 数据概览：账本数 */
    private Long bookCount;

    /** 数据概览：账户数 */
    private Long accountCount;

    /** 数据概览：计入总资产账户余额合计（分） */
    private Long totalAsset;
}
