package com.life.ledger.annotation;

import com.life.ledger.common.FamilyRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 家庭操作权限校验：当前用户必须是该家庭成员，且角色满足 minRole
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FamilyPermission {

    FamilyRole minRole() default FamilyRole.MEMBER;

    /** 方法中代表 familyId 的参数名 */
    String familyIdParam() default "familyId";
}
