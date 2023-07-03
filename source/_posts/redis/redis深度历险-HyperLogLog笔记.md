---
title: redis深度历险-HyperLogLog笔记
comments: true
abbrlink: 1b3e547f
date: 2019-09-16 13:35:12
categories: 笔记
tags:
  - redis
  - 读书笔记
---
#### 前言
前面看了一些位图结构 用处有是有 对于后端来说用处不是很多  这篇 HyperLogLog  看起来做一些粗略统计的时候 还是很合适的 
#### HyperLogLog
##### 介绍
* hyperLogLog 是一种近似去重算法 使用少量空间判断数据中不重复元素的个数 redis的HyperLogLog 12kb存储     
* hyperLogLog 会完整遍历所有元素   
* hyperLogLog 无法判断每个元素出现的次数或者是一个元素在之前是否出现过    
* 多个hyperLogLog 可以融合    
> 参考文档: https://blog.csdn.net/redenval/article/details/85205453
##### 常规用途 
主要用在一些 不精确去重计算处理上 
* 统计pv uv

#### redis-HyperLogLog 使用
> pf就是HyperLogLog结构的发明人Philippe Flajolet  
##### pfadd
添加元素 
```bash
pfadd name value ... 
```
##### pfcount
统计长度 
```bash
pfcount name 
```
##### pfmerge
合并 hyperLogLog
```bash
pfmerge name1 name2 ....
```
> 低于阀值的时候使用稀疏矩阵存储  当达到阀值使用稠密矩阵存储才回占用12kb

* 为什么占用是12kb存储
redis中HyperLogLog使用的是16384个桶  也就是2^14 每个桶需要6bit 最大maxbits=63 
总共占用内存\[(2^14) * 6/8] =12kb 

#### 总结
hyperLogLog 统计去重大量数据 是非常实用的 不仅是redis有这个算法的实现 很多大数据处理工具都会有这个算法的相关实现   




