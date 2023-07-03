---
title: 基于disruptor实现简单topic分发消息功能
comments: true
categories: 实战
tags:
  - disruptor
  - 队列
abbrlink: 69313fd8
date: 2020-10-28 10:45:16
---
#### 前言
disruptor 性能的确很强 但是只是做了 队列的功能  如果有多种消息 就必须自己去扩展一下 或者用多个队列  
自己手写了一套简易的 基于disruptor 点对点的 topic分发功能

#### 思路
通过一个分发处理器 将收到消息按照topic 分发到不同的处理器 
所有的消息由DistributeEventHandler 按照DisruptorTopicEnum 进行分发到不同的BaseEventHandler实现类 

发送消息通过DisruptorService 


#### 示例
包结构如下图:
![包结构图](https://www.xujiuming.com/ming-static/disruptor%E8%87%AA%E5%AE%9A%E4%B9%89topic%E7%9B%B8%E5%85%B3%E5%8C%85%E7%BB%93%E6%9E%84%E6%88%AA%E5%9B%BE.png)

* Element
```java
package com.ming.core.disruptor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试disruptor的element
 *
 * @author ming
 * @date 2020-10-27 16:17
 */
@Slf4j
@Data
public class Element<T> {
    private DisruptorTopicEnum topic;
    private T data;
}
```

* DisruptorService
```java
package com.ming.core.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ThreadFactory;

/**
 * disruptor 服务
 *
 * @author ming
 * @date 2020-10-27 17:03
 */
@Primary
@Component
@Slf4j
public class DisruptorService {
    @Autowired
    private DistributeEventHandler distributeEventHandler;


    private Disruptor<Element> elementDisruptor;

    /**
     * 发送消息
     *
     * @author ming
     * @date 2020-10-27 17:08
     */
    @SuppressWarnings("unchecked")
    public <T> void sendMessage(DisruptorTopicEnum disruptorTopicEnum, T data) {
        RingBuffer<Element> ringBuffer = elementDisruptor.getRingBuffer();
        // 获取下一个可用位置的下标
        long sequence = ringBuffer.next();
        try {
            // 返回可用位置的元素
            Element<T> event = ringBuffer.get(sequence);
            // 设置该位置元素的值
            event.setData(data);
            event.setTopic(disruptorTopicEnum);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 初始化 disruptor队列
     *
     * @author ming
     * @date 2020-10-27 17:12
     */
    @PostConstruct
    public void init() {
        //初始化disruptor
        // 生产者的线程工厂
        ThreadFactory threadFactory = r -> new Thread(r, "simpleThread");
        // RingBuffer生产工厂,初始化RingBuffer的时候使用
        EventFactory<Element> factory = Element::new;
        // 阻塞策略
        BlockingWaitStrategy strategy = new BlockingWaitStrategy();
        // 指定RingBuffer的大小
        int bufferSize = 16;
        // 创建disruptor，采用单生产者模式
        elementDisruptor = new Disruptor(factory, bufferSize, threadFactory, ProducerType.SINGLE, strategy);
        // 设置EventHandler 并且后置清理消费过的数据
        elementDisruptor.handleEventsWith(distributeEventHandler);
        elementDisruptor.handleExceptionsFor(distributeEventHandler).with(disruptorExceptionHandler);
        // 启动disruptor的线程
        elementDisruptor.start();
    }

    /**
     * 销毁 disruptor队列
     *
     * @author ming
     * @date 2020-10-27 17:12
     */
    @PreDestroy
    public void destroy() {
        //销毁 disruptor
        elementDisruptor.shutdown();
    }
}

```

* DisruptorTopicEnum
```java
package com.ming.core.disruptor;

/**
 * disruptor topic 枚举
 *
 * @author ming
 * @date 2020-10-27 16:58
 */
public enum DisruptorTopicEnum {
    LOG_TOPIC, TEST1_TOPIC, TEST2_TOPIC;
}

```

* DistributeEventHandler
```java
package com.ming.core.disruptor;

import com.lmax.disruptor.EventHandler;
import com.ming.core.disruptor.handler.LogEventHandler;
import com.ming.core.disruptor.handler.Test1EventHandler;
import com.ming.core.disruptor.handler.Test2EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 基础event handler
 * 根据topic 分发数据
 *
 * @author ming
 * @date 2020-10-27 17:15
 */
@Component
public class DistributeEventHandler implements EventHandler {
    @Autowired
    private Test1EventHandler test1EventHandler;
    @Autowired
    private Test2EventHandler test2EventHandler;
    @Autowired
    private LogEventHandler logEventHandler;

    /**
     * Called when a publisher has published an event to the {@link RingBuffer}.  The {@link BatchEventProcessor} will
     * read messages from the {@link RingBuffer} in batches, where a batch is all of the events available to be
     * processed without having to wait for any new event to arrive.  This can be useful for event handlers that need
     * to do slower operations like I/O as they can group together the data from multiple events into a single
     * operation.  Implementations should ensure that the operation is always performed when endOfBatch is true as
     * the time between that message an the next one is inderminate.
     *
     * @param event      published to the {@link RingBuffer}
     * @param sequence   of the event being processed
     * @param endOfBatch flag to indicate if this is the last event in a batch from the {@link RingBuffer}
     * @throws Exception if the EventHandler would like the exception handled further up the chain.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(Object event, long sequence, boolean endOfBatch) throws Exception {
        if (!(event instanceof Element)) {
            throw new RuntimeException("类型错误,必须为Element类型~");
        }
        Element<?> element = (Element<?>) event;
        //分发topic  到对应的handler
        switch (element.getTopic()) {
            case LOG_TOPIC -> logEventHandler.onEvent((Element<String>) event, sequence, endOfBatch);
            case TEST1_TOPIC -> test1EventHandler.onEvent((Element<String>) event, sequence, endOfBatch);
            case TEST2_TOPIC -> test2EventHandler.onEvent((Element<String>) event, sequence, endOfBatch);
            default -> throw new RuntimeException("topic未注册!无法分发消息");
        }
    }
}

```

* DisruptorExceptionHandler<T> 
```java
package com.ming.core.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import com.ming.core.utils.JacksonJsonSingleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * disruptor 异常处理
 *
 * @author ming
 * @date 2020-10-28 11:32
 */
@Component
@Slf4j
public class DisruptorExceptionHandler<T> implements ExceptionHandler<T> {
    /**
     * <p>Strategy for handling uncaught exceptions when processing an event.</p>
     *
     * <p>If the strategy wishes to terminate further processing by the {@link BatchEventProcessor}
     * then it should throw a {@link RuntimeException}.</p>
     *
     * @param ex       the exception that propagated from the {@link EventHandler}.
     * @param sequence of the event which cause the exception.
     * @param event    being processed when the exception occurred.  This can be null.
     */
    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        log.error("处理disruptor事件异常,异常内容:{},编号:{},事件内容:{}", ex.getMessage(), sequence, JacksonJsonSingleton.writeString(event));
        ex.printStackTrace();

    }

    /**
     * Callback to notify of an exception during {@link LifecycleAware#onStart()}
     *
     * @param ex throw during the starting process.
     */
    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("处理disruptor启动异常,异常内容:{}", ex.getMessage());
        ex.printStackTrace();
    }

    /**
     * Callback to notify of an exception during {@link LifecycleAware#onShutdown()}
     *
     * @param ex throw during the shutdown process.
     */
    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("处理disruptor关闭异常,异常内容:{}", ex.getMessage());
        ex.printStackTrace();
    }
}

```
* BaseEventHandler<T, R>
```java
package com.ming.core.disruptor.handler;

import com.ming.core.disruptor.Element;

/**
 * 基础 事件处理接口
 *
 * @author ming
 * @date 2020-10-28 09:37
 */
public interface BaseEventHandler<T, R> {

    R onEvent(Element<T> event, long sequence, boolean endOfBatch);
}
```

* LogEventHandler
```java
package com.ming.core.disruptor.handler;

import com.ming.core.disruptor.Element;
import org.springframework.stereotype.Component;

/**
 * 基础event handler
 * 根据topic 分发数据
 *
 * @author ming
 * @date 2020-10-27 17:15
 */
@Component
public class LogEventHandler implements BaseEventHandler<String, String> {


    @Override
    public String onEvent(Element<String> event, long sequence, boolean endOfBatch) {
        System.out.println("log:" + event.getData());
        return "log";
    }
}
```

* Test1EventHandler
```java
package com.ming.core.disruptor.handler;

import com.ming.core.disruptor.Element;
import org.springframework.stereotype.Component;

/**
 * 基础event handler
 * 根据topic 分发数据
 *
 * @author ming
 * @date 2020-10-27 17:15
 */
@Component
public class Test1EventHandler implements BaseEventHandler<String, String> {


    @Override
    public String onEvent(Element<String> event, long sequence, boolean endOfBatch) {
        System.out.println("Test1:" + event.getData());
        return "Test1";
    }
}
```

* Test2EventHandler
```java
package com.ming.core.disruptor.handler;

import com.ming.core.disruptor.Element;
import org.springframework.stereotype.Component;

/**
 * 基础event handler
 * 根据topic 分发数据
 *
 * @author ming
 * @date 2020-10-27 17:15
 */
@Component
public class Test2EventHandler implements BaseEventHandler<String, String> {


    @Override
    public String onEvent(Element<String> event, long sequence, boolean endOfBatch) {
        System.out.println("Test2:" + event.getData());
        return "Test2";
    }
}
```

* 测试用例 
```java
package com.ming.core.disruptor;

import com.ming.Start;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * disruptor service test
 *
 * @author ming
 * @date 2020-10-28 10:21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
class DisruptorServiceTest {
    @Autowired
    private DisruptorService disruptorService;

    @Test
    void sendMessage() {
        int max = 10000;
        long now = System.currentTimeMillis();
        new Thread(() -> {
            for (int i = 0; i < max; i++) {
                disruptorService.sendMessage(DisruptorTopicEnum.LOG_TOPIC, i + "minglog" + (System.currentTimeMillis()-now));
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < max; i++) {
                disruptorService.sendMessage(DisruptorTopicEnum.TEST1_TOPIC, i + "mingtest1" + (System.currentTimeMillis()-now));
            }
        }).start();

        new Thread(() -> {
            for (int i = 0; i < max; i++) {
                disruptorService.sendMessage(DisruptorTopicEnum.TEST2_TOPIC, i + "mingtest2" + (System.currentTimeMillis()-now));
            }
        }).start();


        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
```


#### 总结
disruptor 性能是很高 但是他本身只是负责队列部分 
大多数场景需要根据具体需求进行扩展调整 
大多数用jdk的queue的 都可以用disruptor替换  

