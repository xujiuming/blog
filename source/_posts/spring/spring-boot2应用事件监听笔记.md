---
title: spring-boot2应用事件监听笔记
comments: true
categories: 笔记
tags:
  - 事件监听
  - spring
abbrlink: 604c60f3
date: 2020-09-14 16:49:00
---
#### 前言
最近要调整应用启动的时候做一些功能 例如加载缓存、通知集群服务可用性等  
顺手把spring 应用加载相关的事件做个笔记 

参考文档: https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/html/spring-boot-features.html#boot-features-application-events-and-listeners

##### spring boot启动支持的事件   
|名称|功能|备注|  
|:--|:--|:---|  
|SpringApplicationEvent             |抽象类|继承此类的事件都是spring 应用的事件|
|ApplicationContextInitializedEvent |ApplicationContext初始化完毕事件|spring应用上下文初始化完毕事件，在加载任何bean定义之前调用|
|ApplicationEnvironmentPreparedEvent|spring应用启动完毕、环境属性可以使用和修改的事件||
|ApplicationFailedEvent             |spring应用启动失败||
|ApplicationPreparedEvent           |ApplicationContext完全准备完毕、并且Environment可以使用|bean将要加载的时候|
|ApplicationReadyEvent              |应用服务可以使用 全部准备完毕||
|ApplicationStartedEvent            |应用准备之后加载bean的时候|
|ApplicationStartingEvent           |运行就触发|
|EventPublishingRunListener         |启动监听器皿|

##### 加载顺序 
从上到下    
 
> 参考:https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/html/spring-boot-features.html#boot-features-application-events-and-listeners

* ApplicationStartingEvent
在运行开始时发送，但在任何处理之前发送，但侦听器和初始化器的注册除外。

* ApplicationEnvironmentPreparedEvent
当 要在上下文中使用的 是已知的，但在创建上下文之前发送的。

* ApplicationContextInitializedEvent
在已准备就绪且已调用应用程序上下文初始化器，但在加载任何 bean 定义之前，将发送 。

* ApplicationPreparedEvent
在刷新开始之前发送，但在加载 bean 定义之后发送。

* ApplicationStartedEvent
在刷新上下文后，但在调用任何应用程序和命令行运行程序之前，将发送 。

* AvailabilityChangeEvent
以指示应用程序被视为实时应用程序。LivenessState.CORRECT

* ApplicationReadyEvent
调用任何应用程序和命令行运行程序后发送。

* AvailabilityChangeEvent
以指示应用程序已准备好为请求提供服务。ReadinessState.ACCEPTING_TRAFFIC

> ApplicationFailedEvent 启动时存在异常 发送此事件

#### 示例
##### 直接注册
实现ApplicationListener 并且注入到ioc中 
下面使用ApplicationFailedEvent 启动失败时间来演示  
```java
package com.ming.base.event;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 失败监听器
 *
 * @author ming
 * @date 2020-09-14 16:35
 */
@Component
public class FailedEventListener implements ApplicationListener<ApplicationFailedEvent> {
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        //。。。代码 
    }
}

```

##### 启动类中注册
启动的时候 手动注册到SpringApplication中
这种维护起来麻烦  
* 监听器   
不手动注册到ioc容器中 启动的时候 手动加入进去   
```java
package com.ming.base.event;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 失败监听器
 *
 * @author ming
 * @date 2020-09-14 16:35
 */
public class FailedEventListener implements ApplicationListener<ApplicationFailedEvent> {
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {

    }
}

```
* 启动注册监听器 
```java
    public static void main(String[] args) {
        log.info("启动环境变量:{}", JacksonJsonSingleton.writeString(args));
        SpringApplication.run(Start.class, args)
            .addApplicationListener(new FailedEventListener());
        log.info("start init: main()");
    }
```

#### 总结
spring  默认提供很多事件 这个应用加载相关事件只是一部分 
