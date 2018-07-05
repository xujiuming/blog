---
title: git-lfs使用笔记
comments: true
categories: 笔记
tags:
  - git lfs
  - tools
  - github
abbrlink: 68597cc5
date: 2018-07-04 12:47:40
---
#### 前言
一直想找个能够存储比较大的文件 而且又能像git一样操作方便快捷   
开始准备自建svn来达到需求的   
后来看github的一些文档看到了git lfs (Git Large File Storage)功能    
干脆了解了一下  顺便记录下这篇笔记 方便以后查阅  
常见的git 仓库网站都提供 git lfs 存储  例如github 
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
初始化 lfs的配置文件
```
git lfs track "<目录>/*"
```
执行上面的命令会在项目的根目录生成 .gitattributes
内容大致如下 
```
<目录>/* filter=lfs diff=lfs merge=lfs -text
```

##### 添加lfs .gitattributes配置到仓库 
```
sudo git add .gitattributes 
sudo commit -m "init  git lfs config"
```

##### 添加并且上传大文件
将大文件复制到 <目录>
```
sudo git add <目录>/xxx
sudo git commit -m "update xxx"
sudo git push 
```
基于git lfs 管理的文件更新会显示成
```
Git LFS: (1 of 1 files) 9.18 KB / 9.18 KB                                                                                                                      
对象计数中: 10, 完成.
。。。。。。。。
```

#### 总结
git 的功能越来越吊了  现在能够好好的管理大文件了  
git只是存储大文件的指针   不负责存储 还是保持了git的高效 易用 但是有能够管理大文件  6的一批
