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
```bash
rpush name value1 value2 value2 
# lindex 获取元素 会遍历链表 随着index增大而变长
lindex  name  2   
# ltrim (lretain) 保留指定区间的数据 区间外全部删除 负数标识倒数元素  例如-1 倒数第一个元素 
ltrim name 1 -1  
# lrange 
```
* 额外的操作
```bash
rpush name value1 value2 value3 
#获取列表长度 
llen name 
# 迭代列表
lrange name  
```
> redis 列表底层是一个 快速链表(quicklist) 在元素较少情况下是一个连续内存的ziplist  当数据量比较多的时候 扩充成一个由ziplist组成的quicklist 减少内存空间使用量、减少内存碎片化 
##### hash(字典)
redis的hash类型 类似java中的 Map<K,V> 但是v只能是字符串    
* redis的hast类型rehash策略   
使用渐进式rehash  在rehash的时候 同时存在新旧两个hash结构  查询的时候同时查询  在后续的定时器中或者hash操作中逐渐将旧hash结构的数据迁移到新hash结构中  当数据迁移完成 删除旧的hash结构    
* 操作   
```bash
#设置hash结构 更新操作
hset  name  k "v"
#读取hash结构
hget name k 
#读取整个hash结构 k和v间隔出现  kvkvkv
hgetall name 
#获取hash字段数量
hlen name 
#批量新增 
hmset name k1 v1 k2 v2 k3 v3 
#对hash的某个键进行自增操作 
hincrby name k 
```
> hash结构获取的时候 可以获取指定key的数据 不一定获取全部数据     hash结构消耗空间会比string多 
##### set(集合)
redis的set相当于java的hashSet 内部键值对无序、唯一  内部实现是一个特殊的hash结构 只不过所有value都是NULL  当最后一个元素删除后 数据结构会删除、回收内存 

```bash
# 添加元素 
sadd name value1 
# 迭代元素
smembers name 
# 判断某个value 是否存在 contains
sismember name value1
# 获取元素长度 count 
scard name
# 获取一个元素
spop name 
```
##### zset(有序列表)
zset 就是一个带权重的set 集合    内部实现为跳跃列表 
权重值为一个double类型   根据从小到大排序  
```bash
#添加元素 添加一个value1 权重为9.0 到name集合中
zadd name 9.0 value1 
# 迭代显示集合  参数为 第一个元素(0位上的) 到最后一个元素(-1位上的) 区间范围
zrange name 0 -1 
# 按照权重逆序列出 参数 参数为 第一个元素(0位上的) 到最后一个元素(-1位上的) 区间范围
zrevrange name 0 -1 
#获取元素长度 count
zcard name 
#获取制定的value的权重值 
zscore name value 
#获取某个元素的排名 
zrank name value 
#根据权重分值区间遍历zset集合  分值区间[负无穷,9.0]   inf=无穷大 
zrnagebyscore name -inf 9.0 withscores 
#删除value
zrem name value 
```

#### 容器数据结构共同特性 
list、set 、hash、zset都属于容器型数据结构 有一些共享的特性
* create if not exists 如果容器不存在 会自动创建一个 再进行操作 
* drop if no elements 如果容器内部没有元素了 会立即删除容器 释放内存  

#### 过期时间规则  
* 过期时间是以对象为单位 例如设置hash结构的过期时间 那么一旦过期整个hash结构全部删除 不是hash结构中的某个key过期 
* 过期时间如果在后续操作中没有设置 那么这个对象就没有过期时间了  例如一个字符串设置了超时时间 后续修改了这个字符串但是没有设置超时时间  那么这个字符串就没有过期时间 
```bash
# 查看某个元素的过期时间 
ttl name
```

#### 总结 
最近在看redis的东西 之前老是瞎用 也不明白啥意思 只知道怎么用 有啥用    
还是要通过一些大佬写的书籍 来协助归纳整理一下 redis的知识   
这份笔记记录了 redis 基础的数据类型 常用的hash  string set zset等 
至于一些 特殊的结构 如HyperLog之类的结构 没有说明  