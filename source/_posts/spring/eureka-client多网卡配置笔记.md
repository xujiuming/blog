---
title: eureka-client多网卡配置笔记
comments: true
categories: 笔记
tags:
  - srping cloud
  - eureka
  - swarm
  - docker
abbrlink: 17f39e95
date: 2018-08-23 10:49:21
---
#### 前言
在使用swarm编排docker的时候  由于swarm 需要暴露端口需要ingress网络 但是 eureka client 注册的时候 不需要使用 ingress这种网络的网卡 
那么这个时候 就需要配置 eureka client 偏向的网络和排除的网卡之类的配置   
#### 相关源码解析  
* InetUtils  获取网络工具类   
这个工具类主要就是从InetUtilsProperties中的配置中 去查询、指定偏向网络、排除网络等相关功能   
主要函数:    

|函数名称|函数作用|备注|    
|:------|:-----|:--|  
|findFirstNonLoopbackHostInfo|获取第一个非本地回环的主机信息|通过获取第一个非本地回环的网络地址来解析成为需要的hostInfo|  
|findFirstNonLoopbackAddress|获取第一个非本地回环的网络地址|根据配置的忽略网卡、倾向网络等参数 来选择第一个有效的网卡|  
|ignoreAddress|是否排除这个地址|配合配置和判断是否本地回环地址来判断|  
|ignoreInterface|是否排除这个网卡|配合配置来判断是否排除|   

* InetUtilsProperties 获取网络工具类依赖的配置    
这个类主要就是接收配置文件中的spring.cloud.inetutils前缀的配置 提供给InetUtils使用  

主要配置:   

|配置属性|作用|备注|
|:------|:--|:---|   
|defaultHostname|默认主机姓名|默认是localhost|  
|defaultIpAddress|默认网络地址|默认是127.0.0.1|  
|timeoutSeconds|超时时间|默认1|  
|ignoredInterfaces|排除的网卡|接收正则表达式|  
|useOnlySiteLocalInterfaces|是否启用本地回环网卡|  
|preferredNetworks|倾向网络ip地址|接收正则表达式、或者前缀匹配|  

* UtilAutoConfiguration InetUtils相关自动配置  
这个类是为了提供一个InetUtils相关的默认配置   
默认开启这个配置 但是都是使用的InetUtilsProperties中的默认配置  

#### 配置 
```
spring:
  cloud:
    inetutils:
      #默认host 名称
      default-hostname: xxx
      # 默认ip地址
      default-ip-address: 192.168.1.11
      #超时时间
      timeout-seconds: 2
      #排除的网卡
      ignored-interfaces: eth0
      #是否读取本地回环网络
      use-only-site-local-interfaces: false
      #倾向网络ip地址 可匹配正则 也可以匹配前缀 
      preferred-networks: 192.168
```
#### 总结 
eureka client 提供了这些配置 直接看源码  在InetUtils 、InetUtilsProperties 、 UtilAutoConfiguration 
看看 就知道那些配置可以配置了

