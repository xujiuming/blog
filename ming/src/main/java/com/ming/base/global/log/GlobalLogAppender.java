package com.ming.base.global.log;

import com.ming.common.entity.log.LogLog4j2;
import com.ming.common.service.log.Log4j2LogService;
import com.ming.core.utils.JacksonSingleton;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * 全局日志 处理
 *
 * @author ming
 * @date 2018-09-06 17:39:21
 */
@Plugin(name = "GlobalLogAppender", category = "Core", elementType = "appender", printObject = true)
public class GlobalLogAppender extends AbstractAppender {
    private static Log4j2LogService log4j2LogService;
    private static HikariDataSource hikariDataSource;

    protected GlobalLogAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    @PluginFactory
    public static GlobalLogAppender create(@PluginAttribute("name") String name, @NonNull Log4j2LogService log4j2LogService, @NonNull HikariDataSource hikariDataSource) {
        GlobalLogAppender.log4j2LogService = log4j2LogService;
        GlobalLogAppender.hikariDataSource = hikariDataSource;
        return new GlobalLogAppender(name, null, PatternLayout.createDefaultLayout());
    }

    /**
     * 具体输出日志的方案
     * 使用 日志服务输出到 db
     *
     * @author ming
     * @date 2018-09-06 10:34:10
     */
    @Override
    public void append(LogEvent event) {
        //当日志服务不存在 或者 数据源不存在 或者 数据源已经关闭 不进行记录日志操作
        if (null == log4j2LogService || null == hikariDataSource || hikariDataSource.isClosed()) {
            return;
        }
        LogLog4j2 logLog4j2 = new LogLog4j2();
        logLog4j2.setCreateInstant(Instant.now());
        logLog4j2.setLoggerFqcn(event.getLoggerFqcn());
        logLog4j2.setLevel(event.getLevel().name());
        logLog4j2.setLoggerName(event.getLoggerName());
        Message message = event.getMessage();
        logLog4j2.setMessageFormat(message.getFormat());
        logLog4j2.setMessageFormattedMessage(message.getFormattedMessage());
        logLog4j2.setTimeMillis(event.getTimeMillis());
        logLog4j2.setThreadName(event.getThreadName());
        logLog4j2.setThreadId(event.getThreadId());
        logLog4j2.setThreadPriority(event.getThreadPriority());
        StackTraceElement source = event.getSource();
        logLog4j2.setSourceMethodName(source.getMethodName());
        logLog4j2.setSourceFileName(source.getFileName());
        logLog4j2.setSourceLineNumber(source.getLineNumber());
        logLog4j2.setSourceClassName(source.getClassName());
        logLog4j2.setSourceNativeMethod(source.isNativeMethod());
        logLog4j2.setLogEvent(JacksonSingleton.writeAsString(event));
        log4j2LogService.save(logLog4j2);
        //将对象指向到null  让gc能够快速识别回收该对象
        logLog4j2 = null;
    }


}
