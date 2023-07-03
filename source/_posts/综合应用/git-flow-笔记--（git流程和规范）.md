---
title: git glow 笔记
categories: 笔记
tags:
  - git
abbrlink: c67df015
date: 2017-11-11 00:00:00
---
###gitflow 代码提交规范  
如下图：
![image.png](http://upload-images.jianshu.io/upload_images/3905525-270696a74d45d27a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

master:线上分支 这个分支 就是线上代码所在的分支 每个点就是软件的一个版本 打上tag 
develop:当代码在开发分支上开发完毕经过基本测试 即可推送合并到develop上 这个是一个比较稳定的一个版本了 合并到develop分支上的功能 代表这个是必须上线的一个功能 
feature:开发分支  这个可以开多个 多个项目组 并行开发 在合适的时机 合并到develop 或者 新的feature分支 进行功能合并
release:测试分支 develop上的代码 分开一个release分支进行测试 当测试阶段性完成 就合并到develop分支 当整个测试完成 就将测试完成的代码合并到develop 和master分支上   
hotfixes:线上bug修复分支 主要处理 master 也就是线上的bug 开的临时分支 当bug处理完成 则将代码推送到develop和master分支 

这几种分支中 develop占用主要地位 几乎所有的分支度基于这个分支  不管是 feature、release、hotfixes、master 度与develop分支有关
