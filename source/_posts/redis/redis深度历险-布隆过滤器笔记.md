---
title: redis深度历险-布隆过滤器笔记
comments: true
categories: 笔记
tags:
  - redis
  - 读书笔记
abbrlink: a5fab8eb
date: 2019-09-16 14:27:19
---
#### 前言
看了利用HyperLogLog 来做一些大量数据粗略统计的方案 
然后终于看到 自己之前用过 但是并不熟悉的东西 布隆过滤器 

#### 布隆过滤器
##### 介绍
布隆过滤器 在\[允许错误]的情况下 判断某个元素是否在指定大量数据集中
参考文档: https://www.cnblogs.com/aspnethot/articles/3442813.html
##### 常规用途 
允许误差判断某个元素是否在指定大量数据集中的操作都适合用 布隆过滤器  
* 爬虫 判断是否url是否爬过   
* 缓存 判断缓存key是否存在 避免缓存穿透    
* 垃圾邮件判断   

> 布隆过滤器判断存在的时候 这个值可能不存在 判断某个值不存在的时候这个值肯定不存在  
#### redis-布隆过滤器 
redis在4.0之后作为插件可以添加到redis-server中  

安装教程参考:   
https://github.com/RedisLabsModules/redablooms   
https://oss.redislabs.com/redisbloom/   

##### 添加元素 
```bash
bf.add name value 
bf.madd name value1 value2 ...
```
##### 判断元素是否存在
```bash
bf.exists name  value
bf.mexists name value1 value2 ...

```

##### 自定义参数布隆过滤器
> bf.reserve  key error_rate initial_size      
默认error_rate(错误率)=0.01 百分之一 error_rate设置越小需要的空间越大 需要综合对准确率的要求和实际的硬件资源进行估算        
默认initial_size=100  initial_size 设置大了浪费空间 小了影响准确率 需要计算预估的数据量+预留部分 计算这个initial_size    

```bash
#创建一个名字为name的 错误率在千分之一以下的  初始化为1000的布隆过滤器
bf.reserve name error_rate=0.001 initial_size=1000
```
#### 实际使用空间占用预估
预估元素数量:n
错误率:f
最佳hash函数数量:k
位数组相对:(1/n)
```text
k=0.7*(1/n)
f=0.6185^(1/n)
```

* 当位数组相对越长 错误率越低 
* 当位数组相对越长 hash函数最佳数量越多 计算效率越慢 
* 当一个元素平均需要一个字节(8bit)的指纹空间(1/n=8)，错误率大约为2%
* 错误率为10% 一个元素需要的平均指纹空间为 4.792bit
* 错误率为1% 一个元素需要的平均指纹空间为 9.585bit
* 错误率为0.1% 一个元素需要的平均指纹空间为 14.377bit

> 布隆在线计算器: http://krisives.github.io/bloom-calculator/ 

#### 当实际元素超出之后错误率计算
实际元素与预计元素数量的倍数:t

```text
f=(1-0.5^t)^k
```

当元素超出两倍多的时候 错误率就变得非常大 所以这里需要准确的对元素进行合理预估  

#### 扩展 Cuckoo Filters 
参考文档: https://www.cnblogs.com/chuxiuhong/p/8215719.html
布谷过滤器在错误率小于3% 的时候空间性能是比布隆过滤器好  
```bash
#添加元素 
cf.add name value 
cf.exists name value 
```

#### 总结
布隆过滤器 用处很多  我经常用的就是来做缓存穿透处理的    
总的来说 使用布隆过滤器 需要在准确度、耗费时间、耗费空间之间做判断   
如果需要高准确度 那么 时间和空间肯定会有上升    
在使用 布隆过滤器的时候 需要根据真实的数据量、业务需求进行判断 调整 最好能够动态调整布隆过滤器数据   
至于布谷过滤器 没用过 只是看布隆过滤器的时候恰好看到了 看起来应该是个布隆过滤器某方面的优化版本   
