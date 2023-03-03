---
title: Redis客户端二级缓存功能笔记 
comments: true 
categories: 笔记 
tags:
  - redis
  - client side caching tracking
  - 多级缓存
date: 2023-03-03 11:00:00
---

#### 前言

redis+内存做二级缓存是很常见的套路了 但是一直有不太好处理的地方 例如 多客户端下 本地缓存和服务端缓存一致性问题  
redis在6.x之后 增加了一个功能 client side caching tracking

> https://lettuce.io/core/6.0.0.RC1/api/io/lettuce/core/support/caching/ClientSideCaching.html lettuce客户端接入实例    
> https://redis.io/docs/manual/client-side-caching/#other-hints-for-implementing-client-libraries redis client side caching tracking 官方文档    
> https://stackoverflow.com/questions/64885694/how-to-configure-client-side-caching-in-lettuce-6-spring-boot-2-4

#### 官方文档 重要功能记录

> https://redis.io/docs/manual/client-side-caching/#other-hints-for-implementing-client-libraries

##### 跟踪模式

###### 默认模式

在默认模式下，服务器会记住给定客户端访问的密钥，并在修改相同的密钥时发送失效消息。这会消耗服务器端的内存，但仅针对客户端可能在内存中拥有的密钥集发送失效消息。

* 交互步骤

```text
1. 客户端启用tracking  
2. 服务器记住每个客户端的的请求过的key 
3. 当key被修改后 所有启用tracking 和 可能缓存的key的客户端都会接受到失效消息通知 
4. 当客户端接受失效消息  删除本地的key 
大概示例:   
client1->server: client tracking on 
client1->server: get foo 
client2->server: del foo 
server->client1: invalidate "foo"
```

* 服务端记录客户端和使用过的key原理

```text
1. 服务器会记住可能在单个全局表中缓存给定密钥的客户端列表。此表称为失效表。失效表可以包含最大条目数。如果插入了新密钥，服务器可能会通过假装该密钥已被修改（即使未被修改）并向客户端发送失效消息来逐出较旧的条目。这样做，它可以回收用于此密钥的内存，即使这会强制具有密钥本地副本的客户端逐出它。    
2. 在失效表中，我们实际上不需要存储指向客户端结构的指针，这将在客户端断开连接时强制执行垃圾回收过程：相反，我们所做的只是存储客户端 ID（每个 Redis 客户端都有一个唯一的数字 ID）。如果客户端断开连接，则随着缓存槽失效，将增量对信息进行垃圾回收。      
3. 有一个 keys 命名空间，不按数据库编号划分。因此，如果客户端正在数据库 2 中缓存密钥，并且其他某个客户端更改了数据库 3 中的密钥值，则仍将发送失效消息。这样，我们可以忽略数据库数量，从而减少内存使用量和实现复杂性。foofoo       
```

* redis协议实现tracking区别     
  resp2: 使用两个连接 connection1 使用pub/sub 订阅 'SUBSCRIBE \_\_redis__:invalidate'消息来清理本地cache, connection2用来 开启tracking 'CLIENT TRACKING on REDIRECT 4'      
  resp3: 在一个连接中 跟踪和接受失效消息 使用push 类型来处理 resp3协议新增的     
  使用hello 可以升级当前redis协议版本      

> https://redis.io/docs/reference/protocol-spec/
> https://github.com/redis/redis-doc/blob/master/docs/reference/protocol-spec.md
> https://blog.csdn.net/LZH984294471/article/details/114233835

* 跟踪内容    
  默认情况下，客户端不需要告诉服务器哪些密钥 他们正在缓存。在只读上下文中提到的每个键 命令由服务器跟踪，因为它可以被缓存。      
  使用【OPTIN】 模式 默认不跟踪查询类的key 使用 【CLIENT CACHING YES】命令之后下一个key会被跟踪      
  使用【OPTOUT】 模式 默认跟踪查询类的key 使用 【CLIENT CACHING NO】命令之后下一个key不会被跟踪    
  使用【NOLOOP】 模式 不发送当前连接本身关联key的变更通知    

###### 广播模式

在广播模式下，服务器不会尝试记住给定客户端访问的密钥，因此此模式在服务器端根本不会消耗内存。相反，客户端订阅键前缀（如 或 ），并在每次触摸与订阅前缀匹配的键时收到通知消息。object:user:  
如果没有指定 前缀 则发送所有修改的key消息 如果使用 N个前缀 会匹配对应的前缀的key修改消息发送 注意是前缀匹配 如果存在前缀覆盖了 也会匹配上 例如 foo 和food前缀       
如果不想接受key失效消息 使用 【NOLOOP】模式

广播模式客户端实现的时候 要注意在 断开连接、心跳异常等情况清理本地缓存    
和使用比较合理的本地缓存实现 例如使用guava caffeine 之类的 不要直接使用简易map

> 使用 BCAST模式 几乎不消耗内存 非广播模式使用的内存和跟踪的key数量和客户端数量成正比

#### 示例

> https://www.lanmper.cn/redis/t9524 client tracking 命令

```shell
# CLIENT TRACKING ON|OFF[REDIRECT client-id][PREFIX prefix[PREFIX prefix ...]][BCAST][OPTIN][OPTOUT][NOLOOP]
# 停止跟踪 
CLIENT TRACKING OFF   
# 开启默认模式
CLIENT TRACKING  ON  
# 开启默认模式 指定id的连接
CLIENT TRACKING  ON  REDIRECT 【id】 
# 开启读取模式不跟踪 
CLIENT TRACKING  ON  REDIRECT 【id】 OPTIN 
# 读取模式不跟踪情况下  跟踪指定key 下一个key会被跟踪
CLIENT CACHING YES 
GET 【key】
# 开启读取模式跟踪   
CLIENT TRACKING  ON  REDIRECT 【id】 OPTOUT
# 读取模式跟踪情况下  不跟踪指定key 下一个key不会会被跟踪
CLIENT CACHING NO 
GET 【key】
# 开启广播模式 不带前缀 
BCASTPREFIXCLIENT TRACKING on REDIRECT 【id】 BCAST 
# 开启广播模式 带前缀 
BCASTPREFIXCLIENT TRACKING on REDIRECT 【id】 BCAST PREFIX 【前綴1】 PREFIX 【前綴2】
```

##### lettuce客户端demo 
使用lettuce接入 client side cache tracking     
使用map或者自定义的cache缓存实现   

```java
package com.ming.admin.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 测试演示  redis 两级缓存demo
 * https://lettuce.io/core/6.0.0.RC1/api/io/lettuce/core/support/caching/ClientSideCaching.html
 * https://redis.io/docs/manual/client-side-caching/#other-hints-for-implementing-client-libraries
 * https://stackoverflow.com/questions/64885694/how-to-configure-client-side-caching-in-lettuce-6-spring-boot-2-4
 * <p>
 * redis+本地缓存 痛点是 redis和本地数据同步问题
 * 在新的redis中  使用tracking 方式来进行通精致和同步
 * @author ming
 * @date 2023-03-02 11:25:25
 */
@Slf4j
public class RedisClientSideCachingDemoTest {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final String PASSWORD = "";

    @Test
    public void testClient() {
        CacheInfo cache = getCacheInfo("m-client");
        String key = "ming";
        String value = "fffff";
        //清理历史的key
        cache.getConnection().sync().del(key);
        printlnCacheData(cache);
        log.info("获取不存在的key:{}", cache.getCacheFrontend().get(key));
        printlnCacheData(cache);
        log.info("获取key如果不存在则加载value:{}", cache.getCacheFrontend().get(key, () -> key + ":" + value + "-" + System.currentTimeMillis()));
        printlnCacheData(cache);
        log.info("获取key:{}", cache.getCacheFrontend().get(key));
        printlnCacheData(cache);
    }

    @SneakyThrows
    @Test
    public void testMultiClient() {
        CacheInfo cacheInfo1 = getCacheInfo("client-1");
        CacheInfo cacheInfo2 = getCacheInfo("client-2");
        new Thread(() -> {
            for (; ; ) {
                cacheInfo1.getCacheFrontend().get("ming" + System.currentTimeMillis(), () -> "value" + System.currentTimeMillis());
                printlnCacheData(cacheInfo1);
            }
        }).start();

        new Thread(() -> {
            for (; ; ) {
                cacheInfo2.getCacheFrontend().get("ming2" + System.currentTimeMillis(), () -> "value2" + System.currentTimeMillis());
                printlnCacheData(cacheInfo2);
            }
        }).start();

        cacheInfo2.getConnection().sync().keys("*")
                .forEach(f -> cacheInfo2.getConnection().sync().del(f));
        Thread.sleep(100000L);
    }

    @Test
    public void testCustomLocalCache() {
        StatefulRedisConnection<String, String> connection = RedisClient.create(RedisURI.builder()
                .withHost(HOST)
                .withPort(PORT)
                .withPassword(PASSWORD.toCharArray())
                .build()).connect();
        MyCacheAccessor myCacheAccessor = new MyCacheAccessor();
        CacheFrontend<String, String> cacheFrontend = ClientSideCaching.enable(myCacheAccessor,
                connection,
                TrackingArgs.Builder.enabled());

        log.info(cacheFrontend.get("ming", () -> "value" + System.currentTimeMillis()));
        log.info(cacheFrontend.get("ming"));
        log.info(myCacheAccessor.toString());
    }

    public static class MyCacheAccessor implements CacheAccessor<String, String> {
        private static Cache<String, String> CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();

        @Override
        public String get(String key) {
            return CACHE.getIfPresent(key);
        }


        @Override
        public void put(String key, String value) {
            CACHE.put(key, value);
        }


        @Override
        public void evict(String key) {
            CACHE.invalidate(key);
        }

        @Override
        public String toString() {
            return CACHE.stats().toString();
        }
    }


    private void printlnCacheData(CacheInfo cacheInfo) {
        log.info("{}-本地缓存数量:{},当前redis的缓存数量:{},\r\n---------------------------\r\n",
                cacheInfo.getName(),
                cacheInfo.getLocalMap().size(),
                cacheInfo.getConnection().sync().dbsize());
    }


    private CacheInfo getCacheInfo(String name) {
        StatefulRedisConnection<String, String> connection = RedisClient.create(RedisURI.builder()
                .withHost(HOST)
                .withPort(PORT)
                .withPassword(PASSWORD.toCharArray())
                .build()).connect();
        Map<String, String> map = new ConcurrentHashMap<>();
        CacheFrontend<String, String> cacheFrontend = ClientSideCaching.enable(CacheAccessor.forMap(map),
                connection,
                TrackingArgs.Builder.enabled());

        return new CacheInfo(name, cacheFrontend, connection, map);
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheInfo {
        private String name;
        private CacheFrontend<String, String> cacheFrontend;
        private StatefulRedisConnection<String, String> connection;
        private Map<String, String> localMap;
    }
}
```

#### 总结  
多级缓存 很多情况下都用 就是有时候实现很纠结   
redis在6.x之后增加了相关支持  用redis+内存实现两级缓存变的更加简单方便了  