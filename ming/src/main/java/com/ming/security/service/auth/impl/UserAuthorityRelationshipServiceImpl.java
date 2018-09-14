package com.ming.security.service.auth.impl;

import com.ming.security.entity.auth.UserAuthorityRelationship;
import com.ming.security.repository.auth.UserAuthorityRelationshipRepository;
import com.ming.security.service.auth.UserAuthorityRelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户关系服务实现
 *
 * @author ming
 * @date 2018-09-10 15:24:08
 */
@Service
public class UserAuthorityRelationshipServiceImpl implements UserAuthorityRelationshipService {
    @Autowired
    private UserAuthorityRelationshipRepository userAuthorityRelationshipRepository;

    @Override
    public void save(UserAuthorityRelationship userAuthorityRelationship) {
        userAuthorityRelationshipRepository.save(userAuthorityRelationship);
    }
}
