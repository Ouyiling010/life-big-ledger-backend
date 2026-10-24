package com.life.ledger.common;

import lombok.Getter;

/**
 * 家庭成员角色  1OWNER 2ADMIN 3MEMBER
 */
@Getter
public enum FamilyRole {

    OWNER(1, "OWNER"),
    ADMIN(2, "ADMIN"),
    MEMBER(3, "MEMBER");

    private final int value;
    private final String name;

    FamilyRole(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static FamilyRole of(Integer value) {
        if (value == null) {
            return MEMBER;
        }
        for (FamilyRole role : values()) {
            if (role.value == value) {
                return role;
            }
        }
        return MEMBER;
    }

    /** 当前角色是否满足最低角色要求（数值越小权限越大） */
    public boolean satisfies(FamilyRole minRole) {
        return this.value <= minRole.value;
    }
}
