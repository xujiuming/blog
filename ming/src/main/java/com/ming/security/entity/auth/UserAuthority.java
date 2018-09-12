package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 用户权限表
 *
 * @author ming
 * @date 2018-09-10 14:33:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class UserAuthority extends InStringId {
    /**
     * 用户id
     */
    private Long staffId;
    /**
     * 权限来源类型
     *
     * @see Type
     */
    private String type;
    /**
     * 来源id
     */
    private Long originId;

    public enum Type {
        /**
         * 来源用户信息
         */
        STAFF,
        /**
         * 来源角色
         */
        ROLE,
        /**
         * 来源于角色组
         */
        ROLE_GROUP;
    }
}
