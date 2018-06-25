---
title: spring boot  监控模块 actuator笔记
categories: 笔记
tags:
  - spring
abbrlink: 1a1c1aa1
date: 2017-11-11 00:00:00
---
###actuator 是spring boot 提供的一个监控的工具  
直接访问 相应端点(rest 接口)   
端点分为三种:  
* 应用配置类  
/autoconfig 获取应用的自动配置相关参数  
/beans  获取应用的所有bean   
/configprops 应用中配置的属性信息  设置enabled = false 关闭这个端点  
/env  获取应用可以用的环境信息   
/mappings 返回 mvc控制器 映射关系 也就是接口的信息   
/info 返回自定义信息 设置 info.app.name=ming  info返回ming  
* 度量指标类  
/metrics 返回各类重要的度量指标  内存、线程、垃圾回收信息  通过使用 counterServices 来自定义 信息 
/metrics/{name} 获取指定的信息 
/health 获取各类健康的指标 磁盘、数据库链接是否可以用、rabbit、solr、redis是否可用
/dump 项目线程信息
/trace  返回基本http跟踪 保留100条
* 操作控制类
/shutdown 关闭应用   post请求 需要设定开启endpoints.shutdown.enabled: true

