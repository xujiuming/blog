package com.ming.common.service.log.impl;

import com.ming.common.entity.log.LogAccess;
import com.ming.common.repository.log.LogAccessRepository;
import com.ming.common.service.log.LogAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 访问日志服务实现
 *
 * @author ming
 * @date 2018-09-07 14:45:00
 */
@Service
public class LogAccessServiceImpl implements LogAccessService {
    @Autowired
    private LogAccessRepository logAccessRepository;

    @Override
    public void save(LogAccess logAccess) {
        logAccessRepository.save(logAccess);
    }
}
