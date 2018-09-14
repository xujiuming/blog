package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 角色表
 *
 * @author ming
 * @date 2018-09-10 14:33:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Role extends InStringId {
    /**
     * 角色名称
     */
    private String name;
    /**
     * 是否启用角色
     */
    private Boolean enabled;
}
