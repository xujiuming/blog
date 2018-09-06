package com.ming.common.service.log.impl;

import com.ming.common.entity.log.LogLog4j2;
import com.ming.common.repository.log.Log4j2LogRepository;
import com.ming.common.service.log.Log4j2LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * log服务实现
 *
 * @author ming
 * @date 2018-09-06 17:39:41
 */
@Service
public class Log4j2LogServiceImpl implements Log4j2LogService {
    @Autowired
    private Log4j2LogRepository log4j2LogRepository;

    @Override
    public void save(LogLog4j2 logLog4j2) {
        log4j2LogRepository.save(logLog4j2);
    }
}
