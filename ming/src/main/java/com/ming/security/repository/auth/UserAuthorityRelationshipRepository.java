package com.ming.security.repository.auth;

import com.ming.base.orm.jpa.BaseRepository;
import com.ming.security.entity.auth.UserAuthorityRelationship;
import org.springframework.stereotype.Repository;

/**
 * 用户权限关系 仓库
 *
 * @author ming
 * @date 2018-09-10 15:22:20
 */
@Repository
public interface UserAuthorityRelationshipRepository extends BaseRepository<UserAuthorityRelationship, String> {

}
