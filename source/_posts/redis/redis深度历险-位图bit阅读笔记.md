---
title: redis深度历险-位图bit阅读笔记
comments: true
categories: redis
tags:
  - redis
  - 读书笔记
abbrlink: 674b6797
date: 2019-09-16 13:16:40
---
#### 前言 
刚刚把redis 当作队列的几页看完 
觉得没啥意思  mq 这个东西 很多成熟的中间件 没必要用redis 
看到位图 bitMap 觉得终于到了一个还不错的章节了    
虽然我没用过这种 位图 但是在很多地方都看到过有用 只是没深入研究  这次借着redis的位图结构 看看redis的位图做啥

#### 位图说明 
如果有大量的boolean值需要存储处理 例如用户一年打卡保存  如果用常规的存储方式 会消耗大量的空间并且查询效率什么的也会非常地下 
这个时候 可以利用位图来保存是否打卡的数据 
每个bit位 只能是0或者1  假设打卡=1 未打卡=0  那么一个用户 一年365天 也就365bit  一个汉字在utf-8编码集中一般也就3个Byte   那么一个用户一年的打开记录用位图存储可能就十来个汉字需要的存储空间       

> 位图本质上也是普通的字符串  就是byte数组  将字符串换算成ascii码  用setbit指令设置 最后用get获取 就可以获取字符串  
#### 基本用法
位图 直接设置某个bit位的值 默认为0  假设需要设置位图 为 01 那么只需要将第二个元素设置为1即可 0会自动填充 
##### setbit,getbit
* 零存零取
```bash
#设置一个名称为b  值为 011的位图  setbit 名称  数组位置 值(只能为0或者1)
setbit b 1 1
setbit b 2 1
#获取 名称为b 的某一位的bit值 getbit 名称  数组位置 
getbit b 2 
```
* 整存零取 
```bash
# 整存  存储一个名称为b 值为m的 字符串 
set b m
# 获取b字符串的指定bit位的值
getbit b 3

```
* 零存整取
```bash
# 零存  
setbit b 1 1
setbit b 2 1
#整取
get b 
```

##### bitcount
指定\[字节]长度 统计 1出现的次数 
> 命令格式 bitcount \[name] 开始字节位置 结束字节位置
```bash
# bitcount统计 位图  统计0-0字节中的bit值为1的数量  
bitcount 0 0 
```

##### bitpos
查找指定\[字节]访问出现的第一个1或者0 
> 命令格式  bitpos \[name] 0or1 开始字节位置  结束字节位置 
```bash
#获取指定字节位置中第一次出现1
bitpos 1  1 1
```
##### bitfield 
redis 3.2之后增加bitfield操作 对一段位片段进行处理
> 最大64个连续的位 超过64 必须使用多个子命令  bitfiled 允许多个子命令一次执行
###### get 
获取指定位置的值 
> bitfiled name get  u/i数量  位置  #u:无符号数 i:有符号
```bash
# 获取m的 从第1位开始的四个位 不带符号 
bitfield name get u4 0  
# 获取m的 从第1位开始的四个位 带符号 
bitfield name get i4 0  
#批量执行 
bitfield name get i4 0 get u4 0 ...
```
###### set 
> bitfiled name set u/i数量  值
```bash
# 设置name的n位开始不带符号连续8位的值 
bitfield name set u8 n value 
```
###### incrby 
对指定位置的位进行自增操作  如果出现溢出 增加的是正数就向上溢出 负数向下溢出 
> 提供 overflow子命令 提供 折返(wrap) 失败(fail) 饱和截断(sat)

*  overflow子命令属性    

|名称|功能|备注|
|:--|:--|:--|
|折返(wrap)|出现溢出 就将溢出的符号位丢弃|redis 默认为折返|
|失败(fail)|报错不执行|-|
|饱和截断(sat)|超过范围就停留在最大值或者最小值|-|


```bash
# 使用默认溢出策略 自增  bitfield  name u/i数量  位置  自增数 
# 对name 从第三个元素开始 不带符号四个位进行自增+1操作 
bitfield name u4 2  1
# 对name 指定溢出策略 从第三个元素开始不带符号位连续四个位 进行自增+1操作 bitfield name overflow 策略 u/i数量 位置  自增数 
bitfield name overflow sat/wrap/fail u4 2 1 
```

#### 总结 
位图存储  我个人感觉 只有在某些特殊情况下可以 例如书中的例子 用户打卡数 
不过大部分打卡可能不仅仅只记录是否打开的信息 肯定会携带更多附加属性 那这个位图作用就只能说做快速查看是否打卡的功能 




