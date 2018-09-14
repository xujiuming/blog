package com.ming.base.global.log;

import com.ming.common.service.log.Log4j2LogService;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 全局 log4j2 初始化
 *
 * @author ming
 * @date 2018-09-06 10:23:58
 */
@Component
public class GlobalLog4j2Init {
    @Autowired
    private Log4j2LogService log4j2LogService;
    @Autowired
    private HikariDataSource hikariDataSource;

    @PostConstruct
    private void init() {
        System.out.println("init log4j2 log appender start....");
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Appender appender = GlobalLogAppender.create("ming-log4j2-appender", log4j2LogService, hikariDataSource);
        appender.start();
        config.addAppender(appender);
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.addAppender(appender, Level.INFO, null);
        ctx.updateLoggers();
        System.out.println("init log4j2 log appender end!");
    }
}

