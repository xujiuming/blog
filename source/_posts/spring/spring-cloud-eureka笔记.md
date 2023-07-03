---
title: spring cloud 注册中心模块笔记
categories: 笔记
tags:
  - spring
abbrlink: 4c352ff0
date: 2017-11-11 00:00:00
---

eureka客户端:
* 服务注册相关配置 注册的中心、心跳、注册信息缓存时间 org.springframework.cloud.netflix.eureka.EurekaClientConfigBean    
* 服务实列配置信息 服务实列的名称、地址、健康检查路径 org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean    
eureka服务端:
* 配置相关检测 eureka.server中配置 org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean  服务端相关配置 


|配置名称|说明|备注|
|:---|:-----|:------|
|eureka.client.register-with-eureka=true |是否向注册中心注册自己  |true=注册，false=不注册|
|eureka.instance.lease-renewal-interval-in-seconds=30 |续约服务间隔  心跳间隔  |心跳间隔 30s|
|eureka.instance.lease-expiration-duration-in-seconds=90|服务失效时间，当90s后没有成功续约，服务失效  |默认失效时间 90s|
|eureka.client.registry-fetch-interval-seconds=30|注册信息缓存时间|默认30s|
|eureka.instance.hostname=name|eureka服务地址|域名或者ip|
|eureka.server.enable-self-preservation=false|是否开启自我保护 默认true |eureka server自我保护机制: 十五分钟内心跳失败低于85% 出现 自我保护机制|
|eureka.client.serviceUrl.defaultZone|指定默认zone(可用区)|如果没有为region(区域)配置zone(可用区)那么使用默认zone(可用区) 多个用,分割,带安全校验的格式(http://<username>:<password>@localhost:10000/eureka)|
|eureka.client.availability-zones|zone(可用区)列表|region一个对应多个zone(可用区)  一个区域可以有多个可用区|
|eureka.instance.instanceId=${spring.application.name}:${random.int}|设定 客户端的明名规则防止冲突|和server.port=0结合使用|
|management.context-path|设置 上下文前缀|如果设置了这个  需要再eurekaclient相应设置/health 和/info端点  否则eureka server端无法获取 客户端的健康情况|
|eureka.instance.statusPageUrlPath=${management.context-path}/info|设置info端点|如果客户端修改了端点相应信息 必须要设置这个|
|eureka.instance.healthCheckUrlPath=${}/health|设置health端点|如果客户端修改了端点相应信息 必须要设置这个|

