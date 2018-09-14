---
title: linux-history笔记
comments: true
categories: 笔记
tags:
  - linux
abbrlink: d7a6c3c1
date: 2018-09-12 14:30:20
---
#### 前言 
查看 命令执行记录  重新执行历史命令 应该也是常规操作 
之前都是 cat .bash_history 去看 根本没注意其他信息 现在统一记录一下笔记

#### 历史记录的相关环境变量

|变量名称|变量作用|备注|
|:------|:-----|:---|
|HISTTIMEFORMAT|显示历史命令执行时间|export HISTTIMEFORMAT='%F %T '  配置后才开始记录时间 之前的命令不记录时间|
|HISTSIZE|内存中保存的历史记录个数|export HISTSIZE=100|
|HISTFILESIZE|文件中保存的历史记录个数|export HISTFILESIZE=100|
|HISTFILE|设置本地存储历史记录的文件名|export HISTFILE=~/xxxx|
|HISTCONTROL|设置记录命令的重复条目或者忽略的配置 |export HISTCONTROL=erasedups (erasedups:清除整个命令中历史记录的重复条目,ignoredups:忽略记录命令历史中连续重复的命令,ignorespace:忽略记录空格开头的命令,ignoreboth:等同于ignoredups+ignorespace)|

#### 操作历史执行命令
##### 查看
* history命令
```
history 
```
* 查看历史执行命令的文件
```
cat ~/.bash_history
```
##### 删除
```
#清理内存中的
history -c
#使用内存中的历史记录填充 .bash_history文件的历史记录 或者直接清理这个.bash_history文件 
history -w 
```
#### 执行历史记录中的命令
* 上下方向键  
上下方向选择历史命令 回车执行  
* !!  
直接执行上一次的命令 回车执行   
* !-1  
直接执行上一次的命令 回车执行  
* ctrl+p   
直接执行上一次命令 回车执行 
* !id  
按照history显示的id 执行那一条历史命令    
* ctrl+r  
搜索命令 回车执行 左右方向命令微调     
 
#### 总结 
通过配置和历史命令相关的环境变量可以控制历史命令的生成和记录 在个人电脑上 意义不是很大   
但是在服务器特别是生产机器上 不记录敏感操作的命令 是非常有必要的     
一般是配置HISTCONTROL=ignorespace 然后输入敏感命令的之前 加个空格   来执行不需要记录的历史命令