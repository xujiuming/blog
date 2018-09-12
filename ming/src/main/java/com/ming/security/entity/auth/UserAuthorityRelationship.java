package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * 用户权限关系表
 *
 * @author ming
 * @date 2018-09-10 14:24:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class UserAuthorityRelationship extends InStringId {

    private String userId;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "permission_id")
    private Permission permission;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "resource_id")
    private Resource resource;

}
