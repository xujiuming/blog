---
title: linux常用工具
categories: 笔记
tags:
  - linux
abbrlink: 5e1b0665
date: 2017-11-11 00:00:00
---
|linux版本|命令|功能描述|实例|
|------------|-------|------------|-----|
|ubuntu | nautilus | ubuntu下面 从终端打开文件夹 | nautilus .|
|*|traceroute|跟踪域名解析|traceroute 域名|
|*|hexdump|将文件转换成hex码| hexdump 文件|
|*|tcpdump、wireshark|抓包工具| |
|*|sl |一个会动的火车||
|*|nohup|忽略退出终端信号|nohup renwu &|
|*|tmux|将单个控制台页面切分的工具| tmux 启动即可 功能巨强大 适合熟悉linux平台并且有一定的精分症的孩子使用 边编译源码边安装二进制包边解压 简直无敌|
|*|netstat|查看端口占用情况 配合grep可以查看指定端口的占用情况|sudo netstat -apn |
|*|top|动态查看系统的相关状态如cpu占用等| top|
|*|grep|正则选取 | |
|*|cat|输出某个文件内容 或者往某个文件写入内容| cat /etc/profile|
|*|tee|往某个文件中追加内容 以指定字符结尾 未指定 则是EOF结尾| tee ./a.txt |
|*|alias|命名别名|alias  psa=‘ps -aux' |
|*|sed|按行操作文本|sed '$d' ./file|
|ubuntu| gnome-screenshot -a |局域截图|
|*|time|统计命令的在每个硬件中的执行时间| \time -v ls |
|*|sleep|休眠|sleep 30s 单位(s:秒,m:分,h:小时,d:天) 休眠ms sleep 0.03s 有10ms左右误差|
|*|expr|计算器|expr 10 + 10| 
|*|file|查看文件信息|file xxx,(-z:查看压缩文件的信息只能查看gzip的压缩包其他的压缩包无法查看,-i查看mime,-F 设置分隔符,-L查看软连接,-f根据文件的命长查看每一个文件)|
|*|dig| 查看dns解析信息的工具|dig 域名|
|*|watch |定时重复执行某个命令 | watch -n 1 命令|
|*|sdkman |安装java 开发环境的工具|  sdkman.io|
|*|tldr |查看一些常用命令用法的工具| tldr 命令|
|*|fzf|文本增强工具，我主要就是ctrl+R 搜索|-|