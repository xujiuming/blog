package com.ming.security.service.auth;

import com.ming.security.entity.auth.UserAuthorityRelationship;

/**
 * 用户关系服务
 *
 * @author ming
 * @date 2018-09-10 15:24:17
 */
public interface UserAuthorityRelationshipService {
    /**
     * 保存 用户权限关系对象
     *
     * @param userAuthorityRelationship
     * @author ming
     * @date 2018-09-10 15:23:05
     */
    void save(UserAuthorityRelationship userAuthorityRelationship);
}
