package com.life.ledger.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回：token + 用户信息
 */
@Data
public class LoginVO implements Serializable {

    private String token;

    private UserVO user;
}
