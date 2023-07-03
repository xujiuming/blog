---
title: yml格式笔记
categories: 笔记
tags:
  - yml、yaml
abbrlink: 958a34fe
date: 2017-11-11 00:00:00
---
####yaml 在学习docker-compose工具中需要用到yaml来做配置  
####yaml语法清晰简洁 适合写配置 但是不适合做数据传输
####在比较新的技术中 大量应用了yaml去配置 如spring cloud   docker-compose等
######语法规则
* 大小写敏感  
* 缩进表示层级关系  
* 缩进不允许tab 只允许空格  
* 空格数量不重要。相同层级左边对齐即可  
* #表示注释
######支持的数据结构
* 对象 
```
name: xu
```
* 数组
```
 name: 
  - xu
  - xu1
```
* 纯量
```
数字
number: 123
boolean 
flag: true or false
null值
parent:~
 !!强转类型
 
字符串
1:默认不使用引号   str: xu
2:包含特殊字符串需要引号 str: 'xu'
3:双引号不对特殊字符转义 s1: 'xu'   s2: "xu"
4:字符串可以写成多行 必须缩进一个空格 换行符转为空格  
str: 多行
 字符
 串
5:多行字符串 可以用| 保留换行符 或者> 折叠
6: + 保留文字末尾换行 -删除文字末尾换行
```
######引用
允许用&设定锚点 用*引用 用<<合并到当前数据

######学习博客地址:http://www.ruanyifeng.com/blog/2016/07/yaml.html?f=tt
