package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 角色组表
 *
 * @author ming
 * @date 2018-09-10 14:33:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class RoleGroup extends InStringId {
    private String name;

    private String enabled;
}
