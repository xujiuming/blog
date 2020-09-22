---
title: spring-boot2启动加载的几种方式示例
comments: true
categories: 示例
tags:
  - spring boot2
abbrlink: 25b3aeb5
date: 2020-09-14 17:31:55
---
#### 前言
做项目的时候 总是会遇到各种各样需要启动处理的需求 
例如加载热数据、初始化环境、等等
spring boot 也提供了很多初始化的口子
这里干脆统一记录一下  方便后续查阅  

#### 方案比较 

|名称|说明|备注|
|:---|:---|:---|
|CommandLineRunner|实现CommandLineRunner接口|会在web容器启动后调用|
|@PostConstruct|使用注解包装函数|会在web容器启动前调用|
|事件监听|利用spring应用的事件来处理|选择合适的事件, 参考: {% post_link spring/spring-boot2应用事件监听笔记 %}    |
|SpringApplication.run()之后加载|在main函数中执行加载||


#### 示例 
##### 继承commandLineRunner方式 
```java
package com.ming.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 启动初始化
 *
 * @author ming
 * @date 2020-06-30
 */
@Slf4j
@Component
public class StartInit implements CommandLineRunner {


    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("start init: run(String... args)");
    }
}

```

##### @PostConstruct方式
```java
package com.ming.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 启动初始化
 *
 * @author ming
 * @date 2020-06-30
 */
@Slf4j
@Component
public class StartInit {
    @PostConstruct
    public void init() {
        log.info("start init: init()");
    }
}

```

##### spring application事件监听方式     

参考文档: {% post_link spring/spring-boot2应用事件监听笔记 %}   

选择合适的 事件即可    示例选择的为启动失败事件 
```java
package com.ming.base.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 失败监听器
 *
 * @author ming
 * @date 2020-09-14 16:35
 */
@Slf4j
@Component
public class FailedEventListener implements ApplicationListener<ApplicationFailedEvent> {
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        log.error("application start fail");
    }
}

```
##### main函数中加载 
* 此处尽量不要做耗时较长的加载项 或者丢进线程池执行  不要直接main线程执行  

```java
package com.ming;

import com.ming.core.utils.JacksonJsonSingleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ming
 * @date 2019-03-28 10:00:28
 */
@SpringBootApplication
@Slf4j
public class Start {

    public static void main(String[] args) {
        log.info("启动环境变量:{}", JacksonJsonSingleton.writeString(args));
        SpringApplication.run(Start.class, args);
        log.info("start init: main()");
    }
}

```

#### 总结 
启动加载  一般之用来加载一些简单的业务     如预热数据 如一些通知之类的操作 
一般的话 建议使用 前三种方式     
事件监听更加精细一点        



