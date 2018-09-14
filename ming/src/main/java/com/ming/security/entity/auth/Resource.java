package com.ming.security.entity.auth;

import com.ming.base.orm.InStringId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 资源表
 *
 * @author ming
 * @date 2018-09-10 14:33:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Resource extends InStringId {

    /**
     * 资源名称
     */
    private String name;
    /**
     * 资源类型
     *
     * @see Type
     */
    private String type;
    /**
     * 资源内容
     */
    private String content;

    public enum Type {
        /**
         * 页面资源
         */
        VIEW,
        /**
         * api资源
         */
        API,
        /**
         * 数据资源
         */
        DATA;
    }
}
