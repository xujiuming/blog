package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 权限表
 *
 * @author ming
 * @date 2018-09-10 14:32:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Permission extends InStringId {
    /**
     * 允许内容名称
     */
    private String name;
    /**
     * 允许的操作类型
     */
    private String type;
    /**
     * 允许的规则  保留字段
     */
    private String rule;

    public enum Type {
        /**
         * 新增
         */
        ADD,
        /**
         * 修改
         **/
        UPDATE,
        /**
         * 删除
         */
        DELETE,
        /**
         * 查询
         */
        QUERY;
    }


}
