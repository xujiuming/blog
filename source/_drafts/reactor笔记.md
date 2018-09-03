---
title: reactor笔记
comments: true
categories: 笔记
tags:
  - 反应式编程
  - java
abbrlink: c1740fbf
date: 2018-08-30 14:48:22
---
#### 前言
现在对于系统的性能要求越来高  传统的spring 相关的功能 都快被vert.x之类的框架给压垮了    
好在 spring 是个活泛的项目  在最新的版本中 spring 也提供了webflux 这种 响应式的套路 功能 也贴近于最新的需求   
在java中 响应式编程的类库 第一个想到的肯定是rxjava  但是spring 社区 选择了 reactor 这种没有历史包袱  而且更加简单的方式去实现响应式编程   
rxjava 用起来 其实也挺不错的 不过用的多的是在原生安卓上使用 服务端很少使用rxjava的    
现在spring 提供了一种没有历史包袱的新套路 直接使用新套路 毕竟spring的选择值得信赖   

#### 参考资料
https://www.ibm.com/developerworks/cn/java/j-cn-with-reactor-response-encode/index.html 

#### Flux 、Mono对象
* Flux
表示 0->n个元素的异步序列
序列有三种不同类型的消息  包含正常元素的消息(onNext())、序列结束的消息(onComplete())、序列出错的消息(onError()) 

|函数名|作用|备注|  
|:----|:---|:--|   
|just()|指定序列中所有元素 当flux发布所有元素后自动结束||
|fromArray()|从数据创建flux|相似和作用的函数 从迭代器创建flux:fromIterable()、从stream集合创建flux:fromStream()|
|empty()|创建没有元素的序列 只发布结束消息的序列||
|error(Throwable)|创建一个只包含错误消息的序列||
|never()|创建一个不包含任何消息通知的序列||
|range(int start,int count)|创建一个从start开始到count个数量的integer对象序列|
|


* Mono
表示 0或者1个元素的异步序列 
序列有三种不同类型的消息  包含正常元素的消息(onNext())、序列结束的消息(onComplete())、序列出错的消息(onError()) 


Flux 和Mono 可以互相转化  



 
