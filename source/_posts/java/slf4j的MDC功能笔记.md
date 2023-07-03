---
title: slf4j的MDC功能笔记
comments: true
categories: 笔记
tags:
  - slf4j
  - MDC
abbrlink: fbe53b3d
date: 2021-11-25 14:22:24
---
#### 前言 
记录日志想通用记录一下用户id 请求id之类的参数方便搜索     
找了一些资料 发现mdc最合适    
>参考文档:
> https://www.jianshu.com/p/1dea7479eb07
> https://logback.qos.ch/xref/chapters/mdc/SimpleMDC.html

#### 示例
1. 调整log pattern    
```text
# 引用mdc中的mingId变量  %X{变量名称}
%X{mingId}-%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50}- %msg%n
```
2. 代码示例
```java
package com.ming;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest(classes = Start.class)
@Slf4j
public class TestSlf4jMDC {
    @Test
    public void test() {
        MDC.put("mingId", UUID.randomUUID().toString());
        log.info("testMingId");
        MDC.clear();
        log.info("testMingIdClean");
    }
}
```
3. 输出日志 
```log
#日期前的就是设置的mingId
d4b03ec6-bea4-452d-8f47-45c9fd03b1ab-2021-11-25 14:21:15.539 [main] INFO  com.ming.TestSlf4jMDC- testMingId
#当没有mingId 的时候 不显示  
-2021-11-25 14:21:15.539 [main] INFO  com.ming.TestSlf4jMDC- testMingIdClean
```

#### 实际使用方案
filter、Interceptor、aop、都可以使用  配合应用的上下文 可以做到很多日志的打印        
mdc就当成一个slf4j自己的一个threadLocal使用就是的     
##### 使用filter为日志增加全局requestId 
* pattern 
```text
%X{url}-%X{method}-%X{requestId}:%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50}- %msg%n
```
* filter实现  
```java
package com.ming.base.mvc.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * log mdc参数过滤器
 *
 * @author ming
 * @date 2021-11-25 14:35:12
 */
@Component
public class LogMdcFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("url", request.getRequestURI());
            MDC.put("method", request.getMethod());
            filterChain.doFilter(servletRequest, servletResponse);
        }finally {
            MDC.clear();
        }
    }
}
```
* 接口实现 
```java
package com.ming.view;

import com.ming.base.mvc.annotation.NotNeedLogin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@NotNeedLogin
public class LogMdcTestController {

    @GetMapping("/testLog")
    public String testLog() {
        log.debug("测试log");
        log.info("测试log");
        log.warn("测试log");
        log.error("测试log");
        return "测试log";
    }
}

```
* 打印出来的日志 
```log
/testLog-GET-1e4f17f8-31ca-41cf-92c2-83fec76b4eaa:2021-11-25 14:43:49.634 [XNIO-1 task-1] INFO  com.ming.view.LogMdcTestController- 测试log
/testLog-GET-1e4f17f8-31ca-41cf-92c2-83fec76b4eaa:2021-11-25 14:43:49.634 [XNIO-1 task-1] WARN  com.ming.view.LogMdcTestController- 测试log
/testLog-GET-1e4f17f8-31ca-41cf-92c2-83fec76b4eaa:2021-11-25 14:43:49.634 [XNIO-1 task-1] ERROR com.ming.view.LogMdcTestController- 测试log
```
#### 总结 
mdc 就是slf4j提供的一个 threadLocal     
直接使用就是的   
很多场景可以使用    
例如    
记录每次请求的整个链路、
记录某个用户的请求等等     
没有zipkin之类链路跟踪工具 用这个mdc 也能简化查询log    
















