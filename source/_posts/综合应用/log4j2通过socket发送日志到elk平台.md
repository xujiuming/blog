---
title: log4j2通过socket发送日志到elk平台
comments: true
categories: 实战
tags: 
  -log4j2 
  -socket 
  -elk
abbrlink: b83d1b10
date: 2018-06-25 14:09:00
---
#### 前言
尝试过很多 spring cloud中日志的方案 好多还是需要 集群的支持 
但是现在公司还没有那么吊的基础设施 那么 这个时候 就需要 项目能够自己直接通过tcp或者udp直接投递日志到elk或者生成日志文件去采集了
但是 项目是使用docker 去部署在swarm或者k8s中 这个时候 生成日志文件 相对来说有点扯淡  
但是公司的swarm集群又没有办法采集到标准输出和错误输出  那么就需要项目自己去投递日志到elk或者队列中让elk去接受了
由于时间较为紧急 直接采用log4j2的socketAppender 来投递日志 使用自定义jsonLayout去格式化 并且适配logstash 
#### 实践

##### 编写jsonLayout
```
package com.ming.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

import java.io.File;
import java.nio.charset.Charset;

/**
 * boss json格式日志
 * <p>
 * 配合elk的配置 使用
 * 原作者文章地址: https://blog.csdn.net/lnkToKing/article/details/79563460
 *
 * @author ming
 * @date 2018-06-22 10:59:56
 */
@Plugin(name = "MingJsonPatternLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class MingJsonPatternLayout extends AbstractStringLayout {
    /**
     * 项目路径
     */
    private static String PROJECT_PATH;

    private PatternLayout patternLayout;

    private String projectName;
    private String logType;

    static {
        PROJECT_PATH = new File("").getAbsolutePath();
    }

    private BossJsonPatternLayout(Configuration config, RegexReplacement replace, String eventPattern,
                                  PatternSelector patternSelector, Charset charset, boolean alwaysWriteExceptions,
                                  boolean noConsoleNoAnsi, String headerPattern, String footerPattern, String projectName, String logType) {
        super(config, charset,
                PatternLayout.createSerializer(config, replace, headerPattern, null, patternSelector, alwaysWriteExceptions,
                        noConsoleNoAnsi),
                PatternLayout.createSerializer(config, replace, footerPattern, null, patternSelector, alwaysWriteExceptions,
                        noConsoleNoAnsi));

        this.projectName = projectName;
        this.logType = logType;
        this.patternLayout = PatternLayout.newBuilder()
                .withPattern(eventPattern)
                .withPatternSelector(patternSelector)
                .withConfiguration(config)
                .withRegexReplacement(replace)
                .withCharset(charset)
                .withAlwaysWriteExceptions(alwaysWriteExceptions)
                .withNoConsoleNoAnsi(noConsoleNoAnsi)
                .withHeader(headerPattern)
                .withFooter(footerPattern)
                .build();
    }

    @Override
    public String toSerializable(LogEvent event) {
        //在这里处理日志内容
        String message = patternLayout.toSerializable(event);
        String jsonStr = new JsonLoggerInfo(projectName, message, event.getLevel().name(), logType, event.getTimeMillis()).toString();
        return jsonStr + "\n";
    }

    @PluginFactory
    public static BossJsonPatternLayout createLayout(
            @PluginAttribute(value = "pattern", defaultString = PatternLayout.DEFAULT_CONVERSION_PATTERN) final String pattern,
            @PluginElement("PatternSelector") final PatternSelector patternSelector,
            @PluginConfiguration final Configuration config,
            @PluginElement("Replace") final RegexReplacement replace,
            // LOG4J2-783 use platform default by default, so do not specify defaultString for charset
            @PluginAttribute(value = "charset") final Charset charset,
            @PluginAttribute(value = "alwaysWriteExceptions", defaultBoolean = true) final boolean alwaysWriteExceptions,
            @PluginAttribute(value = "noConsoleNoAnsi", defaultBoolean = false) final boolean noConsoleNoAnsi,
            @PluginAttribute("header") final String headerPattern,
            @PluginAttribute("footer") final String footerPattern,
            @PluginAttribute("projectName") final String projectName,
            @PluginAttribute("logType") final String logType) {


        return new BossJsonPatternLayout(config, replace, pattern, patternSelector, charset,
                alwaysWriteExceptions, noConsoleNoAnsi, headerPattern, footerPattern, projectName, logType);
    }

    /**
     * 输出的日志内容
     */
    public static class JsonLoggerInfo {
        /**
         * 项目名
         */
        private String projectName;
        /**
         * 项目目录路径
         */
        private String projectPath;
        /**
         * 日志信息
         */
        private String message;
        /**
         * 日志级别
         */
        private String level;
        /**
         * 日志分类
         */
        private String logType;
        /**
         * 日志时间
         */
        private String time;

        public JsonLoggerInfo(String projectName, String message, String level, String logType, long timeMillis) {
            this.projectName = projectName;
            this.projectPath = PROJECT_PATH;
            this.message = message;
            this.level = level;
            this.logType = logType;
            this.time = DateFormatUtils.format(timeMillis, "yyyy-MM-dd HH:mm:ss.SSS");
        }

        public String getProjectName() {
            return projectName;
        }

        public String getProjectPath() {
            return projectPath;
        }

        public String getMessage() {
            return message;
        }

        public String getLevel() {
            return level;
        }

        public String getLogType() {
            return logType;
        }

        public String getTime() {
            return time;
        }

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

```
#####  配置log4j2.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<!-- monitorInterval="60"表示每60秒配置文件会动态加载一次。在程序运行过程中，如果修改配置文件，程序会随之改变。 -->
<configuration status="warn" monitorInterval="1">
    <!-- 定义通用的属性 -->
    <Properties>
        <Property name="PROJECT_NAME">ming</Property>
        <Property name="ELK_LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss} %-5p  thread[%thread] %l  %msg %n</Property>
    </Properties>

    <appenders>
        <!--测试环境 elk的logstash 入口-->
        <Socket name="logstash" host="<logstash-ip>" port="<logstash-port>" protocol="TCP">
            <MingJsonPatternLayout pattern="${ELK_LOG_PATTERN}" projectName="${PROJECT_NAME}" logType="ming" />
        </Socket>

    </appenders>


    <Loggers>

        <!-- 配置项目的 日志等级输出 -->
        <root level="DEBUG">
            <!-- 通过tcp 传输到logstash-->
            <appender-ref ref="logstash"/>
        </root>
    </Loggers>
</configuration>
```
替换xml中 <logstash-ip> <logstash-port>为自己的 logstash ip和port

##### 配置 logstash

```
input {
    #开启远程输入日志服务
    tcp {
        port => "<logstash-port>"
        mode => "server"
        type => "log4j2"
    }
}

filter {
    #将日志转成json对象
    json {
        source => "message"
    }
    #将远程客户端的日志时间设置为插入时间，不设置默认为当前系统时间，可能会存在时间误差
    date {
        match => ["time", "yyyy-MM-dd HH:mm:ss.SSS"]
        remove_field => ["time"]
    }
}

output {
    elasticsearch {
        hosts => ["<es-ip>:<es-port>"]
        index  => "application-%{+YYYY.MM.dd}"
    }
}
```
替换上面配置中的<logstash-port>(和log4j2中port保持一致)、<es-ip>、<es-port>(默认为9200)
修改后重启logstash 启动配置 即可

#### 总结
其实最终是期望 项目直接输出标准输出和错误输出 由swarm或者k8s直接统一采集 标准输出和错误输出 这样 又避免了生成实际文件 有简化了项目的配置 
这个直接使用socketAppender投递日志存在缺陷 一是socket比较简略 如果追求高性能需要自己重写 socketAppender 二个 需要logstash的解析和log4j2中的Layout进行匹配 
否则 输出的日志 一坨翔  

