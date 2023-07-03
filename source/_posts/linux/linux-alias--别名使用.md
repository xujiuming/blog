---
title: alias笔记
categories: 笔记
tags:
  - linux
abbrlink: d0a7cd2b
date: 2018-09-11 00:00:00
---
#### 前言
linux下面 有时候常用的命令太吉尔长了  命名别名是必须的

#### 使用示例  
有时候有些命令常用 例如 'ls -a'  'ps -aux' 这样的 如果每次输入全部 麻烦的很 通过设置别名 可以快速使用 
##### 设定临时 alias  只能在当前shell可用 退出shell 失效
```
alias  asliasNname='命令'
例如: alias psa = 'ps -aux'
```
##### 设定永久 alias  
只需要吧 alias 加入 环境变量配置文件中即可 例如加入 全局环境变量/etc/profile 或者当前用户的环境变量配置中 例如～/.bashrc 
```
tee -a /etc/profile
alias psa='ps -aux'


# ctrl +c  结束录入后 应用 环境变量
source /etc/profile
```
##### 查看别名 
查看所有的别名
```
alias 
```
查看指定的别名 
```
alias 别名 
```
##### 取消别名 
```
unalias 别名 
```
##### 强制执行命令本身   
* 使用绝对路径执行命令 /xxx  
* 切换到命令目录 ./xxx  
* 使用反斜线 \ xxx   
#### 总结 
linux别名 可以将一些常用的 但是直接输入 比较长的命令封装起来  向文件追加内容 不一定用tee  echo >> 也是可以的 cat都行 不局限命令 能达成功能即可   
alias 一定要使用 单引号''   
