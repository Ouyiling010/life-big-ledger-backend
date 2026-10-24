package com.life.ledger.service;

import com.life.ledger.common.PageResult;
import com.life.ledger.dto.FamilyCreateDTO;
import com.life.ledger.dto.FamilyJoinDTO;
import com.life.ledger.dto.FamilyUpdateDTO;
import com.life.ledger.entity.OperationLog;
import com.life.ledger.vo.FamilyDetailVO;
import com.life.ledger.vo.FamilyMemberVO;
import com.life.ledger.vo.FamilyVO;

import java.util.List;

/**
 * 家庭服务
 */
public interface FamilyService {

    /** 创建家庭 */
    FamilyVO create(FamilyCreateDTO dto);

    /** 我参与的家庭列表 */
    List<FamilyVO> listMy();

    /** 家庭详情（信息 + 成员 + 概览） */
    FamilyDetailVO detail(Long familyId);

    /** 编辑家庭（ADMIN） */
    void update(Long familyId, FamilyUpdateDTO dto);

    /** 解散家庭（OWNER） */
    void dissolve(Long familyId);

    /** 重置邀请码，返回新邀请码（ADMIN） */
    String resetInvite(Long familyId);

    /** 邀请码加入家庭 */
    FamilyVO join(FamilyJoinDTO dto);

    /** 成员列表（含消费统计） */
    List<FamilyMemberVO> members(Long familyId);

    /** 修改成员角色（OWNER） */
    void updateRole(Long familyId, Long memberId, Integer role);

    /** 移除成员（OWNER/ADMIN） */
    void removeMember(Long familyId, Long memberId);

    /** 主动退出家庭 */
    void quit(Long familyId);

    /** 转移家庭创建者（OWNER） */
    void transfer(Long familyId, Long targetMemberId);

    /** 家庭操作日志分页（ADMIN） */
    PageResult<OperationLog> logs(Long familyId, String module, long current, long size);

    /** 修改自己在某家庭的昵称（F-13） */
    void updateMyNickname(Long familyId, String nicknameInFamily);
}
