---
title: redis深度历险-消息队列笔记
comments: true
categories: 笔记
tags:
  - redis
  - 读书笔记
  - 消息队列
abbrlink: aaa3b624
date: 2019-09-02 18:23:07
---
#### 前言 
上一个redis应用是做分布式锁  
这次看到那个用redis做队列  
其实我觉得用处不是很大  毕竟市面上有非常成熟的mq  要求性能的 kafka 要求稳定性的 rabbit    
redis的mq实在是没办法比 只能在做一些无关紧要的地方做队列玩玩 
例如: 
通过redis 队列 异步存日志 
通过redis 队列  做一些提示性的推送之类的   
#### list 实现简单的队列 （点对点）
参考之前的文章:   {% post_link redis/redis深度历险-基础数据结构阅读笔记 %}
> 添加元素 rpush/lpush  
##### 非阻塞模式 
> lpop/rpop 

演示队列使用rpush/lpop 右进左出队列   
```bash
#添加进队列
rpush name value1 #。。。。
#获取元素
lpop name 
```
###### 队列操作类实现 
```java
package com.ming.base.queue;

import com.ming.core.utils.SpringBeanManager;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis list 实现的queue
 * 使用 rpush lpop 右进左出
 *
 * @author ming
 * @date 2019-09-03 14:56:07
 */
public class RedisListQueue {

    /**
     * push 元素到指定队列的右边
     *
     * @param queueName 队列名称
     * @param value     元素内容
     * @author ming
     * @date 2019-09-03 14:57:30
     */
    public static void push(String queueName, String value) {
        getClient().opsForList().rightPush(queueName, value);
    }

    /**
     * 从左边获取一个元素
     *
     * @param queueName 队列名称
     * @return String 元素
     * @author ming
     * @date 2019-09-03 14:58:30
     */
    public static String pop(String queueName) {
        return getClient().opsForList().leftPop(queueName);
    }

    /**
     * 获取客户端
     *
     * @return StringRedisTemplate
     * @author ming
     * @date 2019-09-03 15:03:32
     */
    private static StringRedisTemplate getClient() {
        return SpringBeanManager.getBean(StringRedisTemplate.class);
    }
}

```
###### 队列测试用例  
```java
package com.ming.base.queue;

import com.ming.Start;
import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * redis list queue测试
 *
 * @author ming
 * @date 2019-09-03 15:10:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class RedisListQueueTest {
    private static final String QUEUE_NAME = "ming";

    /**
     * 这里测试推送队列  发送100个元素到队列中
     *
     * @author ming
     * @date 2019-09-03 15:12:57
     */
    @Test
    public void aTestPush() {
        //推100个元素 到队列中
        for (int i = 0; i < 10000; i++) {
            RedisListQueue.push(QUEUE_NAME, "value" + i);
        }
    }

    /**
     * 这里测试论询获取队列
     * 启动两个线程 分别获取队列
     *
     * @author ming
     * @date 2019-09-03 15:13:10
     */
    @Test
    public void bTestPop() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                String tmp = RedisListQueue.pop(QUEUE_NAME);
                System.out.println("线程t1:" + i + "::" + tmp);
                if (StringUtils.isEmpty(tmp)) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                String tmp = RedisListQueue.pop(QUEUE_NAME);
                System.out.println("线程t2:" + i + "::" + tmp);
                if (StringUtils.isEmpty(tmp)) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        });
        //设置t2 优先级别高
        t2.setPriority(6);

        t1.start();
        t2.start();

        Thread.sleep(10000L);
    }
}
```

##### 阻塞模式 
> block阻塞获取list元素  blpop/brpop  指令格式 blpop name timeout  

演示队列使用 rpush/blpop 方法做队列 
```bash
#添加进队列
rpush name value1 #。。。。
#阻塞10s获取元素
blpop name  10 
```
###### 队列操作类实现
```java
package com.ming.base.queue;

import com.ming.core.utils.SpringBeanManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * redis list 实现的阻塞queue
 * 使用 rpush lpop 右进左出
 *
 * @author ming
 * @date 2019-09-03 14:56:07
 */
public class RedisListBlockQueue {

    /**
     * push 元素到指定队列的右边
     *
     * @param queueName 队列名称
     * @param value     元素内容
     * @author ming
     * @date 2019-09-03 14:57:30
     */
    public static void push(String queueName, String value) {
        getClient().opsForList().rightPush(queueName, value);
    }

    /**
     * 从左边获取一个元素 阻塞   超时之后还是需要重试操作
     *
     * @param queueName 队列名称
     * @param timeout   超时时间
     * @param timeUnit  时间单元
     * @return String 元素
     * @author ming
     * @date 2019-09-03 14:58:30
     */
    public static String blockPop(String queueName, long timeout, TimeUnit timeUnit) {
        return getClient().opsForList().leftPop(queueName, timeout, timeUnit);
    }

    /**
     * 获取客户端
     *
     * @return StringRedisTemplate
     * @author ming
     * @date 2019-09-03 15:03:32
     */
    private static StringRedisTemplate getClient() {
        return SpringBeanManager.getBean(StringRedisTemplate.class);
    }
}

```
###### 队列测试用例
```java
package com.ming.base.queue;

import com.ming.Start;
import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * redis list block  queue测试
 *
 * @author ming
 * @date 2019-09-03 15:10:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class RedisListBlockQueueTest {
    private static final String QUEUE_NAME = "ming";

    /**
     * 这里测试推送队列  发送100个元素到队列中
     *
     * @author ming
     * @date 2019-09-03 15:12:57
     */
    @Test
    public void aTestPush() {
        //推100个元素 到队列中
        for (int i = 0; i < 5; i++) {
            RedisListQueue.push(QUEUE_NAME, "value" + i);
        }
    }

    /**
     * 这里测试论询获取队列
     * 启动两个线程 分别获取队列
     *
     * @author ming
     * @date 2019-09-03 15:13:10
     */
    @Test
    public void bTestBlockPop() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            while (true){
                //获取元素 做多阻塞10s
                String tmp = RedisListBlockQueue.blockPop(QUEUE_NAME, 10, TimeUnit.SECONDS);
                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"线程t1:" + "::" + tmp);
            }
        });
        Thread t2 = new Thread(() -> {
            while (true){
                String tmp = RedisListBlockQueue.blockPop(QUEUE_NAME, 10, TimeUnit.SECONDS);
                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"线程t2:" + "::" + tmp);
            }
        });
        //设置t2 优先级别高
        t2.setPriority(6);

        t1.start();
        t2.start();

        Thread.sleep(1000000L);
    }
}
```
#### redis的publish 和subscribe （点对多 ）
redis提供一套简单的发布订阅系统 用来弥补使用list做队列无法快速做到发布订阅模式       

|命令|格式|备注|
|:--------|:---|:---|
|publish|publish channelName message |返回接受到消息的订阅者数量|
|subscribe|subscribe channelName |订阅channel|
|unsubscribe|unsubscribe channelName|退订channel|
|psubscribe|psubscribe channelPattern |按照channelPattern 正则订阅符合规则的channel|
|punsubscribe|punsubscribe channelPattern|按照channelPattern规则取消订阅符合规则的channel|
|pubsub| pubsub subcommand  arg|查看 发布订阅系统状态|


* pubsub 详解     

|subcommand|arg|说明|
|:--------|:---|:---|
|CHANNELS|\[pattern]|返回指定模式pattern的活跃的频道,指定返回由SUBSCRIBE订阅的频道|
|NUMSUB|channel channel2 ...|返回指定频道的订阅数量|   
|NUMPAT|-|返回订阅模式的数量，注意：这个命令返回的不是订阅模式的客户端的数量， 而是客户端订阅的所有模式的数量总和 |


#### java代码示例
```java
package com.ming.base.queue;

import com.ming.Start;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;

/**
 * redis pub sub测试
 *
 * @author ming
 * @date 2019-09-03 15:10:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class RedisPubSubTest {
    private static final String CHANNEL_NAME = "ming";

    @Autowired
    private RedisTemplate stringRedisTemplate;

    /**
     * 这里测试发布
     *
     * @author ming
     * @date 2019-09-03 15:12:57
     */
    @Test
    public void bTestPub() {
        for (int i = 0; i < 10; i++) {
            stringRedisTemplate.convertAndSend(CHANNEL_NAME, "mingtest" + i + "::" + System.currentTimeMillis());
        }
    }

    /**
     * 这里测试 订阅
     * 启动两个线程 分别订阅
     * 先启动本测试用例   任何启动bTestPub 发布消息
     *
     * @author ming
     * @date 2019-09-03 15:13:10
     */
    @Test
    public void aTestSub() throws InterruptedException {
        Thread t1 = new Thread(() -> stringRedisTemplate.getConnectionFactory().getConnection().subscribe((message, pattern) -> System.out.println("t1-message:" + message), CHANNEL_NAME.getBytes(StandardCharsets.UTF_8)));
        Thread t2 = new Thread(() -> stringRedisTemplate.getConnectionFactory().getConnection().subscribe((message, pattern) -> System.out.println("t2-message:" + message), CHANNEL_NAME.getBytes(StandardCharsets.UTF_8)));
        t1.start();
        t2.start();
        Thread.sleep(Integer.MAX_VALUE);
    }
}
```

#### 总结 
书上还有一种用zset实现的 队列 不过我感觉没啥必要     
redis的队列做一些允许误差的快方案 还是可以的   
但是对队列有要求 例如速度、稳定性 有还是直接使用成熟的mq kafka或者rabbit就行   
