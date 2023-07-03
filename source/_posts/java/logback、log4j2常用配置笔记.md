---
title: logback、log4j2常用配置笔记
comments: true
categories: 笔记
tags:
  - slf4j
  - log4j2
  - logback
abbrlink: 59cb2f62
date: 2020-07-20 09:45:16
---
#### 前言 
日志框架经常使用  老是现场查询  最近抽空做个整理  方便自己速查 

虽然现在在一些k8s 之类的集群上 日志收集一般是收集 stdout stderr 来采集日志  
不过更多的拥有自己的elk集群或者log文件分析集群   所以还是需要log框架来输出日志    

日志主要就是layouts（样式）、appender(追加器)、 
> slf4j 是一中日志框架标准 其中常用的实现为log4j2  logback   

#### logback 
> 参考文档: http://www.logback.cn/ 

spring boot 默认就是使用logback  这个是现在用的比较多的方式   
##### 常用xml配置
输出到console 和生成log文件 按照日期建立目录    单个文件100mb  最大90天   日志最大100GB
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!--自定义变量-->
    <property name="LOG_DIR" value="./logs/"/>
    <property name="LOG_APP_NAME" value="ming-workbench"/>
    <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度%msg，50是logger名称最大长度：日志消息，%n是换行符-->
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50}- %msg%n"/>

    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder charset="UTF-8">
            <!--%highlight(%-5level) %cyan表示高亮日志等级，并使用藏青色打印logger部分-->
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{50}) - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 输出格式 appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder charset="UTF-8">
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
        <!--按照logback提供的大小和时间切分策略分隔日志文件-->
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/%d{yyyy-MM-dd}/${LOG_APP_NAME}-%i.log</fileNamePattern>
            <!--单个文件最大为100Mb-->
            <maxFileSize>100MB</maxFileSize>
            <!--日志最多保存 90天-->
            <maxHistory>90</maxHistory>
            <!--日志最大 100GB-->
            <totalSizeCap>40GB</totalSizeCap>
        </rollingPolicy>
    </appender>

    <!--默认的日志级别，如果上面的logger没有命中，则按照root的级别打印日志，root是所有logger的父节点，如果addtivity是false，则不会抛到这里-->
    <root level="INFO">
        <appender-ref ref="Console"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```


##### 自定义appender
* 自定义appender  

继承于AppenderBase  

```java
package com.ming.base;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.ming.core.utils.JacksonJsonSingleton;

/**
 * ming  appender
 *
 * @author ming
 * @date 2020-07-17 14:13
 */
public class MingAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        System.out.println("------------------------");
        System.out.println(JacksonJsonSingleton.writeString(eventObject));
        System.out.println("------------------------");
    }
}

```
* 配置使用 自定义appender 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!--自定义变量-->
    <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度%msg，50是logger名称最大长度：日志消息，%n是换行符-->
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50}- %msg%n"/>

    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder charset="UTF-8">
            <!--%highlight(%-5level) %cyan表示高亮日志等级，并使用藏青色打印logger部分-->
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{50}) - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="mingAppender" class="com.ming.base.MingAppender"/>

    <!--默认的日志级别，如果上面的logger没有命中，则按照root的级别打印日志，root是所有logger的父节点，如果addtivity是false，则不会抛到这里-->
    <root level="INFO">
        <appender-ref ref="Console"/>
        <appender-ref ref="mingAppender"/>
    </root>
</configuration>
```

#### log4j2 
> 参考文档:     
>http://logging.apache.org/log4j/2.x/download.html  
>https://www.cnblogs.com/frankwin608/p/log4j2.html  
>https://blog.csdn.net/lnkToKing/article/details/79563460   


log4j2 也不错  配置方法和logback差不多  主要是增强了异步输出日志的特性  其他的差不多功能  

##### 常用xml配置 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--日志级别以及优先级排序: OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL -->
<!--status="WARN" :用于设置log4j2自身内部日志的信息输出级别，默认是OFF-->
<!--monitorInterval="30"  :间隔秒数,自动检测配置文件的变更和重新配置本身-->
<configuration status="warn" monitorInterval="60" strict="true">
    <properties>
        <!--自定义一些常量，之后使用${变量名}引用-->
        <property name="logpath">./logs</property>
        <property name="charset">UTF-8</property>
        <!--自定义的输出格式-->
        <property name="pattern">%-d{yyyy-MM-dd HH:mm:ss.SSS}@@%p@@%X{ip}@@%t %C@@%X{requestId} %M %m %n </property>
    </properties>
    <!--appenders:定义输出内容,输出格式,输出方式,日志保存策略等,常用其下三种标签[console,File,RollingFile]-->
    <!--Appender可以理解为日志的输出目的地-->
    <appenders>
        <!--console :控制台输出的配置-->
        <Console name="console" target="SYSTEM_OUT">
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
        </Console>
        <!--RollingRandomAccessFile性能比RollingFile提升官网宣称是20-200%-->
        <RollingRandomAccessFile name="MING.TRACE" immediateFlush="true" bufferSize="1024"
                                 fileName="${logpath}/trace.log"
                                 filePattern="${logpath}/trace.log.%d{yyyy-MM-dd}.gz">
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
            <TimeBasedTriggeringPolicy/>
            <DefaultRolloverStrategy>
                <Delete basePath="${logpath}" maxDepth="2" followLinks="true">
                    <IfFileName glob="trace.log.*.gz"/>
                    <IfLastModified age="3d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingRandomAccessFile>
        <RollingRandomAccessFile name="MING.SYSTEM" immediateFlush="true" bufferSize="4096"
                                 fileName="${logpath}/system.log"
                                 filePattern="${logpath}/system.log.%d{yyyy-MM-dd}.gz"
                                 ignoreExceptions="false">
            <!--引用上面自定义的输出格式-->
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
            <Filters>
                <!--ThresholdFilter :日志输出过滤-->
                <!--level="info" :日志级别,onMatch="ACCEPT" :级别在info之上则接受,onMismatch="DENY" :级别在info之下则拒绝-->
                <!--与logger、root中定义的日志级别相配合，相当于两个闸门，先判断logger、root的级别，符合了才会用到该filter中的level，此时再进行一次筛选-->
                <ThresholdFilter level="TRACE" onMatch="ACCEPT" onMismatch="DENY"/>
                <!--<ThresholdFilter level="DEBUG" onMatch="ACCEPT" onMismatch="DENY"/>-->
                <!--<ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY"/>-->
            </Filters>
            <!-- Policies :日志滚动策略-->
            <Policies>
                <!--<TimeBasedTriggeringPolicy interval="1" modulate="true"/>-->
                <CronTriggeringPolicy schedule="0 0 2 * * ?" evaluateOnStartup="true"/>
            </Policies>
            <!-- DefaultRolloverStrategy属性如不设置，则默认为最多同一文件夹下7个文件-->
            <DefaultRolloverStrategy>
                <Delete basePath="${logpath}" maxDepth="2" followLinks="true">
                    <IfFileName glob="system.log.*.gz"/>
                    <!--只保留7天，超过则删除-->
                    <IfLastModified age="7d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingRandomAccessFile>
        <RollingRandomAccessFile name="MING.ERROR" immediateFlush="true" bufferSize="4096"
                                 fileName="${logpath}/error.log"
                                 filePattern="${logpath}/error.log.%d{yyyy-MM-dd}.gz"
                                 ignoreExceptions="false">
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
            <Filters>
                <ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>
            </Filters>
            <TimeBasedTriggeringPolicy/>
            <DefaultRolloverStrategy>
                <Delete basePath="${logpath}" maxDepth="2" followLinks="true">
                    <IfFileName glob="error.log.*.gz"/>
                    <IfLastModified age="7d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingRandomAccessFile>
        <RollingRandomAccessFile name="MING.AUDIT" immediateFlush="false" bufferSize="8192"
                                 fileName="${logpath}/audit.log"
                                 filePattern="${logpath}/audit.log.%d{yyyy-MM-dd}.gz"
                                 ignoreExceptions="false">
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
            <Filters>
                <ThresholdFilter level="WARN" onMatch="ACCEPT" onMismatch="DENY"/>
            </Filters>
            <TimeBasedTriggeringPolicy/>
            <DefaultRolloverStrategy>
                <Delete basePath="${logpath}" maxDepth="2" followLinks="true">
                    <IfFileName glob="audit.log.*.gz"/>
                    <IfLastModified age="7d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingRandomAccessFile>
        <RollingRandomAccessFile name="MING.POOL" immediateFlush="true" bufferSize="1024"
                                 fileName="${logpath}/pool.log"
                                 filePattern="${logpath}/pool.log.%d{yyyy-MM-dd}.gz"
                                 ignoreExceptions="false">
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
            <TimeBasedTriggeringPolicy/>
            <DefaultRolloverStrategy>
                <Delete basePath="${logpath}" maxDepth="2" followLinks="true">
                    <IfFileName glob="pool.log.*.gz"/>
                    <IfLastModified age="3d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingRandomAccessFile>
        <RollingRandomAccessFile name="MING.MONITOR" immediateFlush="true" bufferSize="1024"
                                 fileName="${logpath}/monitor.log"
                                 filePattern="${logpath}/pool.log.%d{yyyy-MM-dd}.gz"
                                 ignoreExceptions="false">
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
            <TimeBasedTriggeringPolicy/>
            <DefaultRolloverStrategy>
                <Delete basePath="${logpath}" maxDepth="2" followLinks="true">
                    <IfFileName glob="pool.log.*.gz"/>
                    <IfLastModified age="3d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingRandomAccessFile>
        <RollingRandomAccessFile name="MING.BIZ" immediateFlush="true"
                                 fileName="${logpath}/biz.log"
                                 filePattern="${logpath}/biz.log.%d{yyyy-MM-dd}.gz"
                                 ignoreExceptions="false">
            <PatternLayout pattern="${pattern}" charset="${charset}"/>
            <TimeBasedTriggeringPolicy/>
            <DefaultRolloverStrategy>
                <Delete basePath="${logpath}" maxDepth="2" followLinks="true">
                    <IfFileName glob="biz.log.*.gz"/>
                    <IfLastModified age="7d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingRandomAccessFile>
    </appenders>

    <!--然后定义logger，只有定义了logger并引入的appender，appender才会生效-->
    <loggers>
        <!--additivity="false"表示在该logger中输出的日志不会再延伸到父层logger。这里如果改为true，则会延伸到Root Logger，遵循Root Logger的配置也输出一次。-->
        <Logger additivity="false" name="MING.TRACE" level="INFO">
            <AppenderRef ref="MING.TRACE"/>
        </Logger>
        <Logger additivity="false" name="MING.SYSTEM" level="INFO">
            <AppenderRef ref="MING.SYSTEM"/>
            <AppenderRef ref="MING.ERROR"/>
        </Logger>
        <Logger additivity="false" name="MING.BIZ" level="INFO">
            <AppenderRef ref="MING.BIZ"/>
        </Logger>
        <!--Logger节点用来单独指定日志的形式，name为包路径,比如要为org.apache包下所有日志指定为INFO级别等。 -->
        <Logger additivity="false" name="org.apache" level="INFO">
            <AppenderRef ref="console"/>
        </Logger>
        <Logger additivity="false"
                name="com.alibaba.dubbo.common.threadpool.monitor.MonitorPoolRunnable" level="INFO">
            <AppenderRef ref="MING.POOL"/>
        </Logger>
        <Logger additivity="false" name="com.alibaba.dubbo.monitor.dubbo.sfextend.SfMonitorExtend"
                level="INFO">
            <AppenderRef ref="MING.MONITOR"/>
        </Logger>
        <!--针对,request以及reponse的信息配置输出级别,生产线请配置为error-->
        <Logger additivity="true" name="com.alibaba.dubbo.rpc.protocol.rest.support" level="INFO">
            <AppenderRef ref="console"/>
        </Logger>
        <!-- 在开发和测试环境启用,输出sql -->
        <Logger additivity="true" name="com.MING.mapper" level="DEBUG">
        </Logger>

        <!-- 异步输出日志  -->
        <AsyncLogger name="com.foo.Bar" level="trace" includeLocation="true">
            <AppenderRef ref="MING.TRACE"/>
        </AsyncLogger>
        
        <!-- Root节点用来指定项目的根日志，如果没有单独指定Logger，那么就会默认使用该Root日志输出 -->
        <Root level="DEBUG" includeLocation="true">
            <AppenderRef ref="console"/>
            <AppenderRef ref="MING.SYSTEM"/>
            <AppenderRef ref="MING.ERROR"/>
            <AppenderRef ref="MING.AUDIT"/>
        </Root>
    </loggers>
</configuration>
```

##### 自定义appender  
* 自定义appender 
```java
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
* 使用自定义appender 
 ```xml
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


#### 总结 
java 基本上使用的都是slf4j 
logback 和log4j2 各有千秋 一个实践时间长 一个拥有更好的性能 
具体的可以看情况  总的来说 没有默认的appender 就自定义appender来配合其他组件    

