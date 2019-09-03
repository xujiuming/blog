---
title: redis深度历险-分布式锁笔记
comments: true
categories: redis
tags:
  - redis
  - 读书笔记
  - 分布式锁 
abbrlink: f887a3c7
date: 2019-09-02 11:11:43
---
#### 前言
之前复习和记录了一下redis的基础数据类型   
今天看了一下 这本书对于redis实现分布式锁的内容   
感觉差不多 也是一样的操作 利用redis本身的特性做的单节点分布式锁 和多节点分布式锁的处理 
> 分布式锁:在分布式环境中 对于资源锁定需要一个能共享的变量区域来存储资源的锁定状态 从而处理相关资源  
> 常用实现分布式锁的中间件有 redis、zk、etcd等等 只要是能共享存储的可并发访问使用的都可以用来实现分布式锁  redis只是其中一种方案  
#### 实现分布式锁
一般项目中 单机redis 即可满足使用要求 
但是也存在 分布式情况 这里只考虑官方cluster分布式方案的分布式锁实现  
只需要 使用set 来加锁和设置时间  exists判断是否锁定  del主动删除锁 
##### 指令演示
```bash
#对一个名称为name的值为value的锁进行操作 
#加锁 此处使用redis2.8之后对set的扩展命令    扩展ex nx 使set同时具有 set+ex+nx 功能 
set name value ex 5 nx 
#判断锁是否存在
exists name 
#主动释放锁
del name 
```
##### 单机模式
###### java实现代码
直接使用 redisTemplate来操作 
```java
package com.ming.base;

import com.ming.core.utils.SpringBeanManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * redis 实现单节点分布式锁
 * {@linkplain com.ming.core.utils.SpringBeanManager 操作spring容器中的bean的工具类}
 *
 * @author ming
 * @date 2019-09-02 13:54:42
 */
@Slf4j
public class RedisLock {

    /**
     * 默认超时时间
     */
    private static final long DEFAULT_EXPIRE = 10;

    /**
     * 加锁
     *
     * @author ming
     * @date 2019-09-02 13:57:16
     */
    public static void lock(String key) {
        lock(key, System.currentTimeMillis() + "", DEFAULT_EXPIRE);
    }

    /**
     * 加锁
     *
     * @author ming
     * @date 2019-09-02 13:57:16
     */
    public static void lock(String key, Long expire) {
        lock(key, System.currentTimeMillis() + "", expire);
    }

    /**
     * 加锁
     *
     * @author ming
     * @date 2019-09-02 13:57:16
     */
    public static void lock(String key, String value) {
        lock(key, value, DEFAULT_EXPIRE);
    }

    /**
     * 加锁
     *
     * @author ming
     * @date 2019-09-02 13:57:16
     */
    public static void lock(String key, String value, Long expire) {
        log.debug("加锁,key{},value{},超时时间:{}s", key, value, expire);
        if (!Objects.requireNonNull(getClient().opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS), "加锁返回异常")) {
            throw new RuntimeException("加锁失败、已经锁定");
        }
    }


    /**
     * 释放锁
     *
     * @author ming
     * @date 2019-09-02 13:57:26
     */
    public static void unLock(String key) {
        log.debug("释放锁,key{}", key);
        getClient().delete(key);
    }

    /**
     * 获取 redis client
     *
     * @author ming
     * @date 2019-09-02 14:02:29
     */
    private static StringRedisTemplate getClient() {
        return SpringBeanManager.getBean(StringRedisTemplate.class);
    }
}

```
###### 测试用例
```java
package com.ming;

import com.ming.base.RedisLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * redis lock 测试
 *
 * @author ming
 * @date 2019-09-02 14:15:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
public class RedisLockTest {
    @Test
    public void test(){
        //加锁
        RedisLock.lock("ming");
        //继续加锁
        try{
            RedisLock.lock("ming");
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        //释放锁
        RedisLock.unLock("ming");
        //加锁
        RedisLock.lock("ming");
        RedisLock.unLock("ming");
    }

}

```
##### cluster集群模式
分布式情况下 使用 redisson来实现分布式锁     避免由于集群的同步、故障等问题引起分布式锁异常   
参考文档: https://github.com/redisson/redisson    
大佬解析隐含的坑博客:https://www.jianshu.com/p/b12e1c0b3917 
###### maven配置   
```xml
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.11.0</version>
        </dependency>
```
###### java实现代码 
```java
package com.ming.base;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * redis 通过redisson实现分布式redis集群的分布式锁
 *
 * @author ming
 * @date 2019-09-02 13:54:42
 */
@Slf4j
public class RedissonLock {

    /**
     * 默认超时时间
     */
    public static final long DEFAULT_EXPIRE = 100;

    /**
     * 获取 redis client
     * 此处仅限于测试使用 如果是正式使用 请建立客户端单例 来使用
     *
     * @author ming
     * @date 2019-09-02 14:02:29
     * @see <a href="https://github.com/redisson/redisson/wiki">redisson wiki </a>
     */
    public static RedissonClient getClient() {
        Config config = new Config();
        //cluster 模式集群 配置
//        config.useClusterServers()
//                //扫描间隔时间 毫秒
//                .setScanInterval(1000)
////                .addNodeAddress("redis://127.0.0.1:6379","redis://127.0.0.1:6380","redis://127.0.0.1:6381")
//                .addNodeAddress("redis://192.168.1.179:6379");
        //单实例配置
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);

    }
}

```
###### 测试用例 
```java
package com.ming;

import com.ming.base.RedissonLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * redisson lock 测试
 *
 * @author ming
 * @date 2019-09-02 14:15:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
public class RedissonLockTest {
    @Test
    public void test() throws InterruptedException {
        String key = "ming";
        //加锁
        RLock lock = RedissonLock.getClient().getLock(key);
        lock.lock(RedissonLock.DEFAULT_EXPIRE, TimeUnit.SECONDS);
//        Thread.sleep(10000);
//        lock.lock(RedissonLock.DEFAULT_EXPIRE, TimeUnit.SECONDS);
//        lock.unlock();
//        lock.lock(RedissonLock.DEFAULT_EXPIRE, TimeUnit.SECONDS);

        System.out.println(System.currentTimeMillis());
    }

}

```

#### 注意
##### 超时
 超时时间 一定要根据生产运行情况 进行预估配置  

<font color='red'> 个人习惯配置为:   max(资源执行最大耗时)*130%  </font>

 锁的超时时间:  
 最小值:这个资源最大操作时间 
 >如果预估不准 建议初始设置长一点 根据生产运行情况进行合适的调整    

 最大值:业务允许的最大超时时间或者硬件能支撑的最大时间
 > 一般会有操作此项业务最大超时时间 如果没有可以根据硬件的资源情况估计  


##### 可重入性   
一般来说 锁肯定需要考虑重入性   
但是分布式锁这种本身就比较麻烦的东西 一般建议从业务上考虑避开重入锁    
当然如果无法避开 就只能在key上加上计数器来做重入性锁了    
然后由于 redis本身单个命令是原子操作 但是组合使用就gg了    
如果非要需要  自定义lua脚本 实现redis定制命令来配合实现可重入的锁     
```text
eval "lua脚本"
```
>redis在执行lua脚本的时候 会当成一个整体执行 不存在竞态条件 

或者直接使用redisson 的RedissonLock 来做     
#### 总结
redis 本身性能强劲 不考虑高可用 大多数情况下单机够用 
redis 用来共享数据 做分布式锁也是很适合 性能高、单线程执行、可用lua脚本扩充功能 
分布式锁 本身没有什么特殊性 就是需要一个性能好、可控制并发访问的地方存储就行  
redis、zk、etcd 等等 只要能存数据并且有良好的并发访问控制的中间件都可以实现   
redisson 用起来麻烦点 但是胜在用的人多 考虑的完全  适配单机、分布式各种环境 
手写的话 只适合自嗨  其实并没有什么卵用   