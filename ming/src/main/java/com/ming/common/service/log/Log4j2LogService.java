package com.ming.common.service.log;

import com.ming.common.entity.log.LogLog4j2;

/**
 * log4j2 日志服务
 *
 * @author ming
 * @date 2018-09-06 17:38:35
 */
public interface Log4j2LogService {
    /**
     * 保存日志对象
     *
     * @param logLog4j2
     * @author ming
     * @date 2018-09-06 17:38:23
     */
    void save(LogLog4j2 logLog4j2);
}
