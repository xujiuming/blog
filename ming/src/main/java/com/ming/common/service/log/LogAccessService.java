package com.ming.common.service.log;

import com.ming.common.entity.log.LogAccess;

/**
 * 访问日志 服务
 *
 * @author ming
 * @date 2018-09-07 14:45:08
 */
public interface LogAccessService {
    /**
     * 保存访问日志
     *
     * @param logAccess
     * @author ming
     * @date 2018-09-07 14:45:18
     */
    void save(LogAccess logAccess);
}
