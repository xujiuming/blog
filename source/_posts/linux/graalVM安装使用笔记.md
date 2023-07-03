---
title: graalVM安装使用笔记
comments: true
categories: 笔记
tags:
  - graalVM
  - java
  - linux
abbrlink: a0a359da
date: 2019-11-20 14:32:44
---
#### 前言 
graalVM oracle搞得 看起来很犀利的VM 
作为java 开发 比较喜欢其中的native image  虽然jdk自带有aot编译(jaotc)   
但是不怎么好用 很多东西都无法通过jaotc正常使用   
> 官网:https://www.graalvm.org/

由于我只在linux下开发 这里示例也是只说linux怎么安装使用    
其它平台安装方式也差不多 区别不大   
#### 安装graalVM
还是老办法 sdkman 安装即可   
```shell script
# 查看 graalvm 版本  
sdk  list java  
# 当前稳定版本 为19.2.1-grl  
sdk install  java  19.2.1-grl
```
> 如果是想临时体验或者嫌麻烦 也可以利用 graalvm docker镜像 来使用  docker run -it oracle/graalvm-ce:19.3.0 bash
#### 安装插件 
由于graal vm 默认支持的语言不多  如果要安装其它语言支持需要手工安装 
至于支持那些语言 请在官网上查找   
```shell script
gu install python 
```
#### 总结
graal vm  oracle 搞得东西  虽然很叼    
因为是oracle 不敢随便上车  还需要观望一段时间  
不过这个vm的功能很强  能够直接编译成native image 提升java 程序的速度 不过感觉微乎其微 并不是很明显   而且对于spring全家桶的支持约等于0          
还能把其它语言一样的搞定  如果真的做起来了  那么基本上要通过虚拟机的或者解释性语言 都可以在同一个平台上共同工作了  
