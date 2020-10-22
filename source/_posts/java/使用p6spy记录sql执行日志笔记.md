---
title: 使用p6spy记录sql执行日志笔记
comments: true
categories: 笔记
tags:
  - sql
  - log
abbrlink: 94bfb5a4
date: 2020-10-22 13:56:44
---
#### 前言 
最近切换 druid 到hikari  
要做个sql 统计、审查功能  发现hikari没的  只能用其他的工具拦截datasource  
看了一下 各种框架 发现p6spy用的人比较多 热度较高 
所以记录一下 使用p6spy 来记录sql  的笔记  

#### 示例
>参考文档:    
>http://p6spy.readthedocs.io/en/latest/configandusage.html#common-property-file-settings
>https://blog.csdn.net/xyf13920745534/article/details/107305377

##### 引入依赖
```xml
<!-- https://mvnrepository.com/artifact/p6spy/p6spy -->
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

##### 配置引用
* 更换driver    
将原本的driver换成  com.p6spy.engine.spy.P6SpyDriver 
* url增加p6spy    
url 增加p6spy    
h2: jdbc:h2:mem:ming -> jdbc:p6spy:h2:mem:ming   
postgres: jdbc:postgresql://host:port/postgres  -> jdbc:p6spy:postgresql://host:port/postgres    

* 新增spy.properties    
注意填写原本的driver 名称    
```properties
# http://p6spy.readthedocs.io/en/latest/configandusage.html#common-property-file-settings
# 使用p6spy driver来做代理
deregisterdrivers=true
#实际驱动
driverlist=org.h2.Driver
#基本设置
autoflush=false
dateformat=yyyy-MM-dd HH:mm:ss
reloadproperties=false
reloadpropertiesinterval=60
#定制输出
#自定义日志appender  录入到db
appender=com.p6spy.engine.spy.appender.Slf4JLogger
#appender=com.ming.base.orm.P6spyDbAndLogAppender
#使用自定义 日志格式化
logMessageFormat=com.ming.base.orm.P6spyStrategy
customLogMessageFormat=%(executionTime)ms | %(sqlSingleLine)
#数据库日期
databaseDialectDateFormat=yyyy-MM-dd HH:mm:ss
databaseDialectBooleanFormat=boolean
#过滤不需要的SQL语句
filter=true
exclude=
#排除的语句类型
excludecategories=info,debug,result,resultset,commit,rollback
# 是否开启慢SQL记录
outagedetection=true
# 慢SQL记录标准 秒
outagedetectioninterval=2
```

###### 自定义扩展 
* 扩展sql日志输出格式
```java
package com.ming.base.orm;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

/**
 * p6spy 格式化消息
 *
 * @author ming
 * @date 2020-10-22 10:49
 */
public class P6spyStrategy implements MessageFormattingStrategy {
    @Override
    public String formatMessage(int i, String s, long l, String s1, String s2, String s3, String s4) {
        return "p6::" + i + ";" + s + ";" + l + ";" + s1 + ";" + s2 + ";" + s3 + ";" + s4;
    }
}

```

* 自定义appender 
```java
package com.ming.base.orm;

import com.ming.core.utils.JacksonJsonSingleton;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.P6Logger;

/**
 * 自扩展appender
 *
 * @author ming
 * @date 2020-10-22 13:24
 */
public class P6spyAppender implements P6Logger {
    /**
     * Logs the stacktrace of the exception.
     *
     * @param e exception holding the stacktrace to be logged.
     */
    @Override
    public void logException(Exception e) {
        e.printStackTrace();
        System.out.println("p6spyappender::e" + e.getMessage());
    }

    /**
     * Logs the text.
     *
     * @param text to be logged
     */
    @Override
    public void logText(String text) {
        System.out.println("p6spyappender::t" + text);
    }

    /**
     * 判断是否输出日志的sql类别
     *
     * @param category the category to be evaluated.
     * @return {@code true} if category is enabled. Otherwise returns
     * {@code false}
     */
    @Override
    public boolean isCategoryEnabled(Category category) {
        System.out.println("p6spyappender::c" + JacksonJsonSingleton.writeString(category));
        return true;
    }

    /**
     * Logs the {@code SQL}.
     *
     * @param connectionId connection identifier.
     * @param now          current time.
     * @param elapsed
     * @param category     the category to be used for logging.
     * @param prepared     the prepared statement to be logged.
     * @param sql          the {@code SQL} to be logged.
     * @param url
     */
    @Override
    public void logSQL( int connectionId, String now, long elapsed, Category category, String prepared, String sql, String url) {
        System.out.println("p6appender::" + connectionId + ";" + now + ";" + elapsed + ";" + category + ";" + prepared + ";" + sql + ";" + url);
    }
}

```
#### 总结 
不管啥数据源 都可以用p6spy 
功能虽然单一 好用就行   
一般来说 记录这种sql的时候 
要注意脱敏  和  兼顾性能   
