---
title: redis深度历险-基础数据结构阅读笔记
comments: true
categories: redis
tags:
  - redis
  - 读书笔记
abbrlink: 4c596b02
date: 2019-08-26 19:06:00
---
#### 前言
最近从公司图书里面找了一本 钱文品大佬写的 《redis深度历险》 准备系统性的看看大佬们是如何使用 redis的 

#### 数据类型
##### 字符串(string)
字符串最常用的数据类型  内部就是一个 字符的数组      
> 当字符长度小于1mb的时候扩容都是加倍当前字符串的大小、当字符串长度超过1mb扩容一次多扩1mb  字符串最大长度为512mb 

* 键值对    
```bash
#设置 name的值为value
set name value
#获取name的值
get name 
#删除name的
del name 
#判断name 是否存在
exists name 
```
* 批量键值对     
```bash
#批量设置 name1的值 value1  name2的值value2
mset name1 value1 name2 value2 
#批量获取 name1 name2的值 
mget name1 name2  
```  
* 过期设置和set命令的扩展操作    
```bash
#分步骤设置name的值为value 过期时间为5s
set name value 
expire name 5 

#set扩展命令设置时间 setex = set+expire  设置name的值为value 过期时间5s 
setex name 5 value 

#set扩展命令 不存在则设置value 存在则不进行操作 setnx  设置name的值为value 如果不存在set创建name=value 如果存在不处理name的值
setnx name value 
```
* 计数 
如果key的value是整数 可以对它进行自增操作 自增范围是Long.minValue～Long.maxValue 超出范围redis报错      
```bash
#设置一个name的值为1 并且进行自增
set name 1
#name +1  
incr name
#name + n
incrby name n  
# name不存在 直接使用incrby 操作 会初始化为0 执行 incr操作
incrby name 
```

##### list(列表)
redis的列表是链表结构 插入和删除快  查询会很慢 
* 右边进左边出(队列)
```bash
# 添加到列表中 
rpush name  value1 value2 value3 
#获取队列深度
llen name 
#从左边获取一个元素 
lpop name 
```
* 右边进右边出(栈)
```bash
#添加到列表中
rpush name value1 value2 value3 
#从右边获取一个元素 
rpop name 
```
* 慢操作

> 
#### 总结 

