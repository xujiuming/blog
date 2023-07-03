---
title: spring-boot-cache使用实战
comments: true
categories: 实战
tags:
  - spring boot
  - cache
  - redis
abbrlink: a29d9e52
date: 2018-07-11 16:26:32
---
#### 前言
cache可以说是后端提高响应速度、承载能力的标准套路了
spring boot中提供spring boot starter cache 组件 配合spring boot starter redis 或者其他缓存组件 可以很简单的使用缓存 
#### spring cache 介绍
一套基于spring aop的方式 为函数添加缓存的 框架 

##### 支持的缓存类型 
* Generic   
* JCache (JSR-107)  
* EhCache 2.x  
* Hazelcast  
* Infinispan  
* Redis  
* Guava  
* Simple  
如果不满足上述的缓存方案  可以自实现 cacheManager   
##### 注解介绍
* @Cacheable 
获取缓存  如果有缓存 直接返回 


|属性|类型|功能|
|:--|:---|:--|
|value|String[]|缓存的名称 和cacheNames功能一样|
|cacheNames|String[]|缓存的名称和value功能一样|
|key|String|缓存key的值、默认是以所有的参数作为key、也可以直接配置keyGenerator|
|keyGenerator|String|缓存key的生成器|
|cacheManager|String|配置使用那个缓存管理器、和cacheResolver排斥|
|cacheResolver|String|定义使用那个拦截器、和cacheManager互斥|
|condition|String|根据spel表达式来可以配置什么条件下进行缓存 默认全部缓存|
|unless|String|和condition相反 |
|sync|boolean|是否开启同步功能、默认不开启|

* @CachePut
执行并且更新缓存相关  不管如何 肯定会执行方法 然后返回 这样可以更新缓存的内容 

|属性|类型|功能|
|:--|:---|:--|
|value|String[]|缓存的名称 和cacheNames功能一样|
|cacheNames|String[]|缓存的名称和value功能一样|
|key|String|缓存key的值、默认是以所有的参数作为key、也可以直接配置keyGenerator|
|keyGenerator|String|缓存key的生成器|
|cacheManager|String|配置使用那个缓存管理器、和cacheResolver排斥|
|cacheResolver|String|定义使用那个拦截器、和cacheManager互斥|
|condition|String|根据spel表达式来可以配置什么条件下进行缓存 默认全部缓存|
|unless|String|和condition相反 |

* @CacheEvict
删除缓存相关

|属性|类型|功能|
|:--|:---|:--|
|value|String[]|缓存的名称 和cacheNames功能一样|
|cacheNames|String[]|缓存的名称和value功能一样|
|key|String|缓存key的值、默认是以所有的参数作为key、也可以直接配置keyGenerator|
|keyGenerator|String|缓存key的生成器|
|cacheManager|String|配置使用那个缓存管理器、和cacheResolver排斥|
|cacheResolver|String|定义使用那个拦截器、和cacheManager互斥|
|condition|String|根据spel表达式来可以配置什么条件下进行缓存 默认全部缓存|
|allEntries|boolean|是否删除所有键的缓存 默认不删除|
|beforeInvocation|boolean|是否在调用此方法前 删除缓存 |


* @CacheConfig
在类级别统一的配置缓存公共配置 

|属性|类型|功能|
|:--|:---|:--|
|cacheNames|String[]|缓存的名称和value功能一样|
|keyGenerator|String|缓存key的生成器|
|cacheManager|String|配置使用那个缓存管理器、和cacheResolver排斥|
|cacheResolver|String|定义使用那个拦截器、和cacheManager互斥|

* @EnableCaching
开启缓存以及缓存的全局配置 


|属性|类型|功能|
|:--|:---|:--|
|proxyTargetClass|boolean|是否要基于cglib生成代理去实现缓存|
|mode|AdviceMode|配置那种模式去实现缓存、默认是AdviceMode.PROXY  可以切换为 AdviceMode#ASPECTJ|
|order|int|设置缓存管理器执行的顺序|


* @Caching
对多个缓存组的配置   

|属性|类型|功能|
|:--|:---|:--|
|cacheable|Cacheable|配置获取缓存相关的配置|
|put|CachePut|配置如何更新缓存的相关配置|
|evict|CacheEvict|配置如何删除缓存的相关配置|

#### 实战
##### gradle 依赖
```
compile('org.springframework.boot:spring-boot-starter-web')
compile('org.springframework.boot:spring-boot-starter-data-redis')
compile('org.springframework.boot:spring-boot-starter-cache')
testCompile('org.springframework.boot:spring-boot-starter-test')
```
##### 启动引导开启缓存 
```
package com.ming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//开启缓存 
@EnableCaching
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class,args);
    }
}
```
##### 配置 
```
spring:
  redis:
    host: <ip>
    port: <port>
    password: <password>
  cache:
    # spring cache 缓存类型为redis  也可以是其他的实现 
    type: redis
```
##### 使用cache
###### 模拟带缓存的service
```
package com.ming;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
//公共配置  可以在类上注释 注释本类的 缓存相关公共配置
//@CacheConfig(cacheNames = TestCacheService.CACHE_KEY)
public class TestCacheService {

    public static final String CACHE_KEY = "test-cache";

    /**
     * 获取信息  第二次访问会取缓存
     *
     * @author ming
     * @date 2018-07-11 17:41:47
     */
    @Cacheable(cacheNames = CACHE_KEY)
    public String testCache(String id) {
        return getString(id);
    }


    /**
     * 更新信息   更新缓存
     *
     * @author ming
     * @date 2018-07-12 09:50:53
     */
    @CachePut(cacheNames = CACHE_KEY)
    public String testCachePut(String id) {
        return getString(id + "update");
    }

    /**
     * 清除缓存
     *
     * @author ming
     * @date 2018-07-12 09:51:22
     */
    @CacheEvict(cacheNames = CACHE_KEY)
    public void removeCache(String id) {
        System.out.println("删除缓存 ");
    }


    /**
     * 获取string 模拟调用方法
     *
     * @author ming
     * @date 2018-07-11 17:41:58
     */
    private String getString(String id) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return id + "load";
    }


}

```
###### 测试用例
```
package com.ming;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Start.class)
public class TestCache {

    @Autowired
    private TestCacheService testCacheService;

    @Test
    public void test() {
        String id = "ming";
        System.out.println("第一次访问没有缓存--------");
        long oneNow = System.currentTimeMillis();
        System.out.println(testCacheService.testCache(id));
        System.out.println("耗时:" + (System.currentTimeMillis() - oneNow) + "ms");


        System.out.println("第二次访问有缓存--------");
        long twoNow = System.currentTimeMillis();
        System.out.println(testCacheService.testCache(id));
        System.out.println("耗时:" + (System.currentTimeMillis() - twoNow) + "ms");


        System.out.println("更新缓存信息--------");
        long threeNow = System.currentTimeMillis();
        System.out.println(testCacheService.testCachePut(id));
        System.out.println("耗时:" + (System.currentTimeMillis() - threeNow) + "ms");


        System.out.println("获取更新后的缓存信息-------");
        long fourNow = System.currentTimeMillis();
        System.out.println(testCacheService.testCache(id));
        System.out.println("耗时:" + (System.currentTimeMillis() - fourNow) + "ms");


        System.out.println("移除缓存------并且调用testCache方法");
        testCacheService.removeCache(id);
        long fiveNow = System.currentTimeMillis();
        System.out.println(testCacheService.testCache(id));
        System.out.println("耗时:" + (System.currentTimeMillis() - fiveNow) + "ms");
    }
}

```

#### 坑  
* @Cacheable 、@CachePut、@CacheEvict 必须要有 cacheNames  
* 注解必须放在public修饰的方法上   
* 如果只是获取缓存使用@Cacheable即可 如果要更新数据库并且更新缓存一定要使用@CachePut 否则@Cacheable会出现脏读   

#### 总结 
spring cache 为缓存提供了一套简单快捷的方案 可以在旧的功能上很快速添加上缓存 
具体缓存的实现 也有更多的选择 也可以自己实现spring cache的缓存管理器皿 来实现自定义的缓存 
本身提供的有很多 例如Generic、JCache (JSR-107)、EhCache 2.x、Hazelcast、Infinispan、Redis、Guava、Simple
按需选择 如果没有 可以自己实现cacheManager去做 

