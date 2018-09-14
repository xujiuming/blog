package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 角色组权限关系表
 *
 * @author ming
 * @date 2018-09-10 14:26:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity

public class RoleGroupAuthorityRelaftionship extends InStringId {
}
