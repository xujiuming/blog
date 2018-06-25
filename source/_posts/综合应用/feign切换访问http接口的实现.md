---
title: feign切换访问http接口的实现
comments: true
categories: 实战
tags: 
  -spring cloud 
  - feign 
  - http
abbrlink: 47a3a555
date: 2018-06-25 13:31:04
---
#### 前言
由于feign底层默认使用 jdk的UrlConnection来发送http请求   
没有连接池之类的 很麻烦 而且bug很多 性能也差    
  
经过查看文档和网络上大佬们的博客  发现可以使用 常用的http工具包去替换 如apache的httpClient  、okHttp等   
Spring Cloud从Brixtion.SR5版本开始支持这种切换feign底层访问http的实现  
#### apache httpClient
使用apache 的http client 作为feign底层发起http请求的实现   
#####  增加pom 中feign httpClient依赖

```
            <!--使用 feign httpclient -->
            <dependency>
                <groupId>com.netflix.feign</groupId>
                <artifactId>feign-httpclient</artifactId>
                <version>8.17.0</version>
            </dependency>
```

##### 开启 feign的httpClient
配置bootstrap.yaml 
```
# feign 设置
feign:
  httpclient:
    enabled: true
```
#### okHttp
使用okHttp来作为feign发起请求的实现 
##### 增加pom中 feign okHttp的依赖
```
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-okhttp</artifactId>
    <version>9.7.0</version>
</dependency>

```

##### 开启feign的okHttp

配置bootstrap.yaml 
```
# feign 设置
feign:
  okhttp:
    enabled: true
```


#### 总结
feign 坑还是很多的  这个使用默认的jdk的urlConnection 实现 会出现一些不好理解的异常  而且性能很低 
切换成httpClient 或者okHttp 可以使用一些优化来增加性能 如连接池之类的 
至于选择httpClient还是okHttp 看情况吧  没有测试过 
spring io platform 里面都有 依赖的版本  看项目原本是啥 就用啥  



