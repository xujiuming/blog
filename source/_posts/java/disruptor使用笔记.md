---
title: disruptor使用笔记
comments: true
categories: 笔记
tags:
  - 高性能
  - 队列
abbrlink: c2fd2cb7
date: 2020-10-27 15:34:09
---
#### 前言
最近做一些日志的采集工作  jdk的队列 有点顶不住了 
然后看到 很多知名的中间件 工具 都用这个来替代 
由于这个东西用的人很多 文档非常丰富 这里只做例子 实际架构图等等 去官网看即可 

>参考文档: 
>美团:https://tech.meituan.com/2016/11/18/disruptor.html
>官方:http://lmax-exchange.github.io/disruptor/
>动态消费者博客:https://zhuanlan.zhihu.com/p/100386603### 
>https://www.cnblogs.com/luozhiyun/p/11631305.html

##### 基础概念
* nP-nC n个生产者-n个消费者   
* Ring Buffer  
Ring Buffer在3.0版本以前被认为是Disruptor的核心组件，但是在之后的版本中只是负责存储和更新数据。在一些高级使用案例中用户也能进行自定义
* Sequence   
Disruptor使用一组Sequence来作为一个手段来标识特定的组件的处理进度( RingBuffer/Consumer )。每个消费者和Disruptor本身都会维护一个Sequence。虽然一个 AtomicLong 也可以用于标识进度，但定义 Sequence 来负责该问题还有另一个目的，那就是防止不同的 Sequence 之间的CPU缓存伪共享(Flase Sharing)问题。
* Sequencer
Sequencer是Disruptor的真正核心。此接口有两个实现类 SingleProducerSequencer、MultiProducerSequencer ，它们定义在生产者和消费者之间快速、正确地传递数据的并发算法。
* Sequence Barrier
保持Sequencer和Consumer依赖的其它Consumer的 Sequence 的引用。除此之外还定义了决定 Consumer 是否还有可处理的事件的逻辑。
* Wait Strategy
Wait Strategy决定了一个消费者怎么等待生产者将事件（Event）放入Disruptor中。
* Event
从生产者到消费者传递的数据叫做Event。它不是一个被 Disruptor 定义的特定类型，而是由 Disruptor 的使用者定义并指定。
* EventProcessor
持有特定的消费者的Sequence，并且拥有一个主事件循环（main event loop）用于处理Disruptor的事件。其中BatchEventProcessor是其具体实现，实现了事件循环（event loop），并且会回调到实现了EventHandler的已使用过的实例中。
* EventHandler
由用户实现的接口，用于处理事件，是 Consumer 的真正实现
* Producer
生产者，只是泛指调用 Disruptor 发布事件的用户代码，Disruptor 没有定义特定接口或类型
##### 示例1-直接使用 
```java
package com.ming;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.Test;

import java.util.concurrent.ThreadFactory;

/**
 * 测试 disruptor
 *
 * @author ming
 * @date 2020-10-27 10:30
 */
public class TestDisruptor {
    /**
     * 基于美团文档的 示例  增加一个消费者
     *
     * @author ming
     * @date 2020-10-27 15:17
     */
    @Test
    public void test() throws InterruptedException {
// 队列中的元素
        class Element {

            private int value;

            public int get() {
                return value;
            }

            public void set(int value) {
                this.value = value;
            }

        }

        // 生产者的线程工厂
        ThreadFactory threadFactory = r -> new Thread(r, "simpleThread");

        // RingBuffer生产工厂,初始化RingBuffer的时候使用
        EventFactory<Element> factory = Element::new;

        // 处理Event的handler
        EventHandler<Element> handler1 = (element, sequence, endOfBatch) -> System.out.println("Element1: " + element.get());
        EventHandler<Element> handler2 = (element, sequence, endOfBatch) -> System.out.println("Element2: " + element.get());

        // 阻塞策略
        BlockingWaitStrategy strategy = new BlockingWaitStrategy();

        // 指定RingBuffer的大小
        int bufferSize = 16;

        // 创建disruptor，采用单生产者模式
        Disruptor<Element> disruptor = new Disruptor(factory, bufferSize, threadFactory, ProducerType.SINGLE, strategy);
        // 设置EventHandler
        disruptor.handleEventsWith(handler1);
        disruptor.handleEventsWith(handler2);

        // 启动disruptor的线程
        disruptor.start();

        RingBuffer<Element> ringBuffer = disruptor.getRingBuffer();

        for (int l = 0; true; l++) {
            // 获取下一个可用位置的下标
            long sequence = ringBuffer.next();
            try {
                // 返回可用位置的元素
                Element event = ringBuffer.get(sequence);
                // 设置该位置元素的值
                event.set(l);
            } finally {
                ringBuffer.publish(sequence);
            }
            Thread.sleep(10);
        }
    }
}

```
##### 示例2-单例使用  
包装成一个单例子 方便其他地方调用   个人感觉强行写成单例 。。。有点丑陋  
```java
package com.ming.core.disruptor;

/**
 * 测试disruptor的element
 *
 * @author ming
 * @date 2020-10-27 16:17
 */
public class Element {

    private Integer value;

    public Integer get() {
        return value;
    }

    public void set(Integer value) {
        this.value = value;
    }

    public void clear() {
        value = null;
    }
}

```
```java
package com.ming.core.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

/**
 * 测试 disruptor单例
 *
 * @author ming
 * @date 2020-10-27 16:16
 */
public class ElementDisruptorSingleton {
    private static volatile Disruptor<Element> elementDisruptor;

    private ElementDisruptorSingleton() {
    }


    /**
     * 单例对象
     *
     * @return elementDisruptor
     * @author ming
     * @date 2020-10-27 16:24
     */
    public static Disruptor<Element> getInstance() {
        if (null == elementDisruptor) {
            synchronized (Disruptor.class) {
                if (null == elementDisruptor) {
                    // 生产者的线程工厂
                    ThreadFactory threadFactory = r -> new Thread(r, "simpleThread");
                    // RingBuffer生产工厂,初始化RingBuffer的时候使用
                    EventFactory<Element> factory = Element::new;
                    // 处理Event的handler
                    EventHandler<Element> handler1 = (element, sequence, endOfBatch) -> System.out.println("Element1: " + element.get());
                    EventHandler<Element> handler2 = (element, sequence, endOfBatch) -> System.out.println("Element2: " + element.get());
                    // 阻塞策略
                    BlockingWaitStrategy strategy = new BlockingWaitStrategy();
                    // 指定RingBuffer的大小
                    int bufferSize = 16;
                    // 创建disruptor，采用单生产者模式
                    Disruptor<Element> disruptor = new Disruptor(factory, bufferSize, threadFactory, ProducerType.SINGLE, strategy);
                    // 设置EventHandler 并且后置清理消费过的数据
                    disruptor.handleEventsWith(handler1);
                    disruptor.handleEventsWith(handler2);
                    elementDisruptor = disruptor;
                    // 启动disruptor的线程
                    elementDisruptor.start();
                }
            }
        }
        return elementDisruptor;
    }
}

```
```java
package com.ming.core.disruptor;

import com.lmax.disruptor.RingBuffer;

/**
 * 测试disruptor
 *
 * @author ming
 * @date 2020-10-27 16:18
 */
public class TestDisruptor {
    public static void main(String[] args) {
        RingBuffer<Element> ringBuffer = ElementDisruptorSingleton.getInstance().getRingBuffer();
        for (int l = 0; true; l++) {
            // 获取下一个可用位置的下标
            long sequence = ringBuffer.next();
            try {
                // 返回可用位置的元素
                Element event = ringBuffer.get(sequence);
                // 设置该位置元素的值
                event.set(l);
            } finally {
                ringBuffer.publish(sequence);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

```
##### 示例3-spring中使用 
spring中 注册到ioc容器中  
```java
package com.ming.core.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ThreadFactory;

@Component
public class ElementDisruptorService {
    private volatile Disruptor<Element> elementDisruptor;

    @PostConstruct
    public void init() {
        //初始化disruptor
        // 生产者的线程工厂
        ThreadFactory threadFactory = r -> new Thread(r, "simpleThread");
        // RingBuffer生产工厂,初始化RingBuffer的时候使用
        EventFactory<Element> factory = Element::new;
        // 处理Event的handler
        EventHandler<Element> handler1 = (element, sequence, endOfBatch) -> System.out.println("Element1: " + element.get());
        EventHandler<Element> handler2 = (element, sequence, endOfBatch) -> System.out.println("Element2: " + element.get());
        // 阻塞策略
        BlockingWaitStrategy strategy = new BlockingWaitStrategy();
        // 指定RingBuffer的大小
        int bufferSize = 16;
        // 创建disruptor，采用单生产者模式
        elementDisruptor = new Disruptor(factory, bufferSize, threadFactory, ProducerType.SINGLE, strategy);
        // 设置EventHandler 并且后置清理消费过的数据
        elementDisruptor.handleEventsWith(handler1);
        elementDisruptor.handleEventsWith(handler2);
        // 启动disruptor的线程
        elementDisruptor.start();
    }

    @PreDestroy
    public void destroy() {
        //销毁 disruptor
        elementDisruptor.shutdown();
    }

    public void sendMessage() {
        RingBuffer<Element> ringBuffer = elementDisruptor.getRingBuffer();
        for (int l = 0; true; l++) {
            // 获取下一个可用位置的下标
            long sequence = ringBuffer.next();
            try {
                // 返回可用位置的元素
                Element event = ringBuffer.get(sequence);
                // 设置该位置元素的值
                event.set(l);
            } finally {
                ringBuffer.publish(sequence);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

```

#### 优化
##### 单生产者和多生产者 
如果确定只有一个线程生产  将disruptor设置为单生产者来提高性能    
ProducerType.SINGLE  
```java
        Disruptor<Element> disruptor = new Disruptor(factory, bufferSize, threadFactory, ProducerType.SINGLE, strategy);
```

##### 等待策略 strategy
* BlockingWaitStrategy
Disruptor的默认策略是BlockingWaitStrategy。在BlockingWaitStrategy内部是使用锁和condition来控制线程的唤醒。BlockingWaitStrategy是最低效的策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现

* SleepingWaitStrategy
SleepingWaitStrategy 的性能表现跟 BlockingWaitStrategy 差不多，对 CPU 的消耗也类似，但其对生产者线程的影响最小，通过使用LockSupport.parkNanos(1)来实现循环等待。一般来说Linux系统会暂停一个线程约60µs，这样做的好处是，生产线程不需要采取任何其他行动就可以增加适当的计数器，也不需要花费时间信号通知条件变量。但是，在生产者线程和使用者线程之间移动事件的平均延迟会更高。它在不需要低延迟并且对生产线程的影响较小的情况最好。一个常见的用例是异步日志记录。

* YieldingWaitStrategy
YieldingWaitStrategy是可以使用在低延迟系统的策略之一。YieldingWaitStrategy将自旋以等待序列增加到适当的值。在循环体内，将调用Thread.yield（），以允许其他排队的线程运行。在要求极高性能且事件处理线数小于 CPU 逻辑核心数的场景中，推荐使用此策略；例如，CPU开启超线程的特性。

* BusySpinWaitStrategy
性能最好，适合用于低延迟的系统。在要求极高性能且事件处理线程数小于CPU逻辑核心树的场景中，推荐使用此策略；例如，CPU开启超线程的特性。

##### 及时清理ring buffer中的对象  
```java
     class Element {

            private int value;

            public int get() {
                return value;
            }

            public void set(int value) {
                this.value = value;
            }

            public void clear() {
                value = 0;
            }
        }
...
        disruptor.handleEventsWith(handler1).then(((event, sequence, endOfBatch) -> event.clear()));

```
#### 总结
disruptor是用来替换jdk 原本的队列 
性能会高出很多 
不过还是基于内存的 只能在单节点中做一些操作  替换原本使用jdk队列的地方  


