---
title: jenkinsfile常用功能代码示例
comments: true
categories: 笔记
tags:
  - jenkins
  - dev-ops
abbrlink: 7cb86a98
date: 2019-08-09 14:38:20
---
#### 前言 
jenkins 的 pipe模式的任务 也用了好久了 
脚本也写了一堆了 一直没时间写个笔记  
今天刚刚忙好了线下集群的脚本更新和调整 顺手记录一下常用的几种功能  

由于 jenkinsfile标准模式 比较僵硬 不太适合复杂多变的要求  
所以这里不处理jenkinsfile标准模式的写法 直接使用 script模式写法 利用 groovy语言来编写打包相关脚本 

> groovy 是一门jvm平台上很犀利胶水语言 
适合做各种脚本啊之类的工作 
配合上自带的Garpe包管理 脚本就很厉害了  
而且语法很简单 完全可以当作java写 对于java开发来说没有门槛 


#### 实例
大部分语法都可以在jenkins中的pipeline-syntax 中查询使用  但是jenkins一贯的操作就是文档少 看得头疼 
pipeline-syntax中的示例大部分也是简单的实例 有很多隐含的用法 需要自行研究
##### 检出代码及其获取相关仓库基本属性 
一般写jenkinsfile都应该独立管理   在需要的时候才去拉取代码 
根据pipeline-syntax中的实例  使用checkout来检出代码  

```groovy

```
