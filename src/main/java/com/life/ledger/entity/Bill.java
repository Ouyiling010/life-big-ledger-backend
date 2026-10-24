package com.life.ledger.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账单表（核心）
 */
@Data
@TableName("bill")
public class Bill implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 1个人 2家庭 */
    private Integer ownerType;

    private Long userId;

    private Long familyId;

    /** 记账人：个人=user_id；家庭=family_member.id */
    private Long creatorId;

    /** 为谁花的：family_member.id */
    private Long memberId;

    private Long bookId;

    private Long accountId;

    private Long toAccountId;

    private Long categoryId;

    /** 1支出 2收入 3转账 */
    private Integer type;

    private Long amount;

    private String currency;

    private LocalDateTime happenedAt;

    private String remark;

    private String tags;

    private String image;

    private Integer isExcluded;

    /** 1手动 2CSV导入 3AI 4模板 */
    private Integer source;

    /** CSV 导入去重指纹 */
    private String fingerprint;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
