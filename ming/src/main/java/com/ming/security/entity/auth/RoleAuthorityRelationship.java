package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 角色权限关系表
 *
 * @author ming
 * @date 2018-09-10 14:25:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class RoleAuthorityRelationship extends InStringId {
}
