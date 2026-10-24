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
 * 消息通知表
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** family_invite / member_change / new_bill */
    private String type;

    private String title;

    private String content;

    private Long refId;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
