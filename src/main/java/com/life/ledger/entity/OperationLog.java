package com.life.ledger.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long familyId;

    /** bill / book / account / family */
    private String module;

    /** create / update / delete */
    private String action;

    private Long targetId;

    private String detail;

    private String ip;

    private Integer costMs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
