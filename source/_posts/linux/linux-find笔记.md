---
title: linux-find笔记
comments: true
date: 2018-09-13 14:31:28
categories: 笔记
tags:
 - linux 
---
#### 前言
find 搜索查询linux系统中的文件 

#### 实例
##### 根据名称搜索
表达式格式
```
find [path ....] -name [pattern]
```
##### 指定搜索的文件类型 
表达式格式
```
find [path...] -type [type类型枚举] -name [pattern]
```
type类型枚举
|枚举值|类型名称|
|:----|:-----|
|d|文件夹|
|f|普通文件|
|l|符号链接|
|b|块设备|
|c|字符设备|
|p|管道文件|
|s|socket套接字|
##### 使用正则匹配文件名 查询
表达式
```
#简单模式的正则
find [path...] -name [pattern]
#复杂的正则
find [path...] -regex [pattern]
```
##### 根据用户用户组查询 
```
find [path...] -user [用户名] -group [用户组名]
```
##### 按照权限搜索
```
find [path...] -perm [权限数字组例如 777 ]
```
##### -exec使用 

