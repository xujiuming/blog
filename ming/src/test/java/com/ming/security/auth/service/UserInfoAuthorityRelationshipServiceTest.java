package com.ming.security.auth.service;

import com.ming.Start;
import com.ming.security.entity.auth.Permission;
import com.ming.security.entity.auth.Resource;
import com.ming.security.entity.auth.UserAuthorityRelationship;
import com.ming.security.service.auth.UserAuthorityRelationshipService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Start.class)
public class UserInfoAuthorityRelationshipServiceTest {
    @Autowired
    private UserAuthorityRelationshipService userAuthorityRelationshipService;

    @Test
    public void save() {
        UserAuthorityRelationship userAuthorityRelationship = new UserAuthorityRelationship();
        userAuthorityRelationship.setUserId("mingId");
        Permission permission = new Permission();
        permission.setName("mingPermission");
        permission.setType(Permission.Type.ADD.name());
        userAuthorityRelationship.setPermission(permission);

        Resource resource = new Resource();
        resource.setName("mingResource");
        resource.setType(Resource.Type.DATA.name());
        resource.setContent("mingResourceContent");
        userAuthorityRelationship.setResource(resource);
        userAuthorityRelationshipService.save(userAuthorityRelationship);

        UserAuthorityRelationship userAuthorityRelationship1 = new UserAuthorityRelationship();
        userAuthorityRelationship.setUserId("mingId1");
        Permission permission1 = new Permission();
        permission1.setId("1");
        permission1.setName("mingPermission");
        permission1.setType(Permission.Type.ADD.name());
        userAuthorityRelationship1.setPermission(permission1);

        Resource resource1 = new Resource();
        resource1.setId("1");
        resource1.setName("mingResource");
        resource1.setType(Resource.Type.DATA.name());
        resource1.setContent("mingResourceContent");
        userAuthorityRelationship1.setResource(resource1);
        userAuthorityRelationshipService.save(userAuthorityRelationship1);


    }
}