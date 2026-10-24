package com.life.ledger.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑家庭请求体
 */
@Data
public class FamilyUpdateDTO implements Serializable {

    private String name;

    private String avatar;

    private String description;

    private String notice;
}
