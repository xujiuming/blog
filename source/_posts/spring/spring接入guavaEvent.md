---
title: spring接入guavaEvent
comments: true
categories: 实战
tags:
  - spring
  - guava
abbrlink: 3e6ecea1
date: 2022-05-12 15:18:19
---
#### 前言   
spring虽然也有事件 但是麻烦    
大多数情况下  guava的event足够使用 需要高性能  可以采用disruptor 參考:  {% post_link java/disruptor使用笔记 %}   {% post_link 综合应用/基于disruptor实现简单topic分发消息功能 %}      

#### 实战    
##### BaseEvent  

```java
package com.ming.service.event;

/**
 * event 基类
 *
 * @author ming
 * @date 2021-09-02 15:37:12
 */
public interface BaseEvent {
    /**
     * 事件是否为异步  默认异步
     *
     * @author ming
     * @date 2021-09-02 15:37:03
     */
    default boolean async() {
        return true;
    }
}

```

##### BaseHandler  
 
```java
package com.ming.service.event;

/**
 * event 处理器基类
 *
 * @author ming
 * @date 2021-09-02 15:37:25
 */
public interface BaseHandler<T extends BaseEvent> {

    void handle(T event);
}

```

##### EventService  

```java
package com.ming.service.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ming.core.utils.JSONSingleton;
import com.ming.core.utils.SpringBeanManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 时间服务
 *
 * @author ming
 * @date 2021-09-02 15:32:53
 */
@Component
@Slf4j
public class EventService {


    @Autowired
    List<BaseHandler<?>> handlers;

    private EventBus eventBus;
    private AsyncEventBus asyncEventBus;

    /**
     * 静态函数  发送事件
     *
     * @param event 事件内容
     * @author ming
     * @date 2021-09-22 11:20:37
     */
    public static <T extends BaseEvent> void staticPost(BaseEvent event) {
        SpringBeanManager.getBean(EventService.class).post(event);
    }

    @PostConstruct
    private void init() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("eventBus-pool-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        eventBus = new EventBus();
        asyncEventBus = new AsyncEventBus(pool);
        handlers.forEach(h -> {
            eventBus.register(h);
            asyncEventBus.register(h);
            log.info("{}注册消息总线", h.getClass().getSimpleName());
        });
    }

    /**
     * 发送事件
     *
     * @param event 事件内容
     * @author ming
     * @date 2021-09-02 15:32:04
     */
    public <T extends BaseEvent> void post(BaseEvent event) {
        log.debug("event:{}", JSONSingleton.writeString(event));
        if (event.async()) {
            asyncEventBus.post(event);
        } else {
            eventBus.post(event);
        }
    }
}
```
##### 使用    
* 定义event类型      

```java
package com.ming.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestEvent implements BaseEvent {
    private long number;
}
```

* 定义对应的eventHandler        
> 注意  @Subscribe 表明订阅事件       @AllowConcurrentEvents 表明并发安全   然后内部是根据event类型来分发到不同的订阅方的     

```java
package com.ming.service.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.springframework.stereotype.Component;

@Component
public class TestEventHandler implements BaseHandler<TestEvent> {

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handle(TestEvent event) {
        System.out.println(event.toString());
    }
}

```

* 发送事件   

```java
package com.ming.service.core;

import com.ming.service.event.EventService;
import com.ming.service.event.TestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestEventService {

    @Autowired
    private EventService eventService;

    public void test() {
        eventService.post(new TestEvent(1));
        EventService.staticPost(new TestEvent(2));
    }
}

```

#### 总结    
guava 的event 简单粗暴    
需要简单明了的事件处理 使用 guava的 event      
需要高性能的 可以使用disruptor自己实现一套简单的      
