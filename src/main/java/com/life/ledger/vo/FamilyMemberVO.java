package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家庭成员 VO
 */
@Data
public class FamilyMemberVO implements Serializable {

    /** 成员记录ID（family_member.id） */
    private Long memberId;

    private Long userId;

    private String nickname;

    private String avatar;

    /** 1OWNER 2ADMIN 3MEMBER */
    private Integer role;

    private String roleName;

    private LocalDateTime joinTime;

    /** 本月支出（分） */
    private Long totalExpense;
}
