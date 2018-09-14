package com.ming.common.entity.log;

import com.ming.base.orm.InLongId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 访问日志
 *
 * @author ming
 * @date 2018-09-07 14:42:37
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class LogAccess extends InLongId {

    private String cookies;
    private String uri;
    private String host;
    private String params;
    private String headers;
    private String method;
    private String userAgent;

}
