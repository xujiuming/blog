package com.ming.security.entity.user;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 用户信息
 *
 * @author ming
 * @date 2018-09-10 09:52:39
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class UserInfo extends InStringId {


    /**
     * 显示用户名
     */
    private String name;
    /**
     * 头像地址
     */
    private String titleImageUrl;
    /**
     * 登陆用户明
     */
    private String userName;
    /**
     * 密码使用des 加密 存储
     */
    private String password;


}
