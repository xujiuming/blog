---
title: alias笔记
categories: 笔记
tags:
  - linux
abbrlink: d0a7cd2b
date: 2017-11-11 00:00:00
---
#####linux  alias  别名设置 
####有时候有些命令常用 例如 'ls -a'  'ps -aux' 这样的 如果每次输入全部 麻烦的很 通过设置别名 可以快速使用 
1:设定临时 alias  只能在当前shell可用 退出shell 失效
```
alias  asliasNname='命令'
例如: alias psa = 'ps -aux'
```
2:设定永久 alias  
只需要吧 alias 加入 环境变量配置文件中即可 例如加入 全局环境变量/etc/profile 或者当前用户的环境变量配置中 例如～/.bashrc 
```
tee -a /etc/profile
alias psa='ps -aux'


# ctrl +c  结束录入后 应用 环境变量
source /etc/profile
```
注意: psa = 'ps -aux' 这样不行 因为shell语法的格式问题 写python写多了 吗买皮
#####总结: linux别名 可以将一些常用的 但是直接输入 比较长的命令封装起来  向文件追加内容 不一定用tee  echo >> 也是可以的 cat都行 不局限命令 能达成功能即可

