---
title: git-lfs使用笔记
comments: true
date: 2018-07-04 12:47:40
categories: 笔记
tags:
 - git
 - tools
 - github
---
#### 前言
一直想找个能够存储比较大的文件 而且又能像git一样操作方便快捷 
开始准备自建svn来达到需求的
后来看github的一些文档看到了git lfs (Git Large File Storage)功能  
干脆了解了一下  顺便记录下这篇笔记 方便以后查阅
笔记中任何命令都在ubuntu中执行过
#### 实战
##### 安装 git lfs
git lfs支持直接二进制安装、各种包管理工具安装(apt、yum、pacman之类的)
```
#安装 git lfs
 sudo apt install git-lfs
 # 初始化git lfs
 sudo git lfs install 
```
##### 配置lfs 管理的文件
```
git lfs track "ming.png"
```