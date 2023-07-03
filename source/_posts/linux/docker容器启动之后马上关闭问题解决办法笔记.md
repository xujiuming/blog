---
title: docker 运行秒退出解决办法
categories: 实战
tags:
  - docker
abbrlink: 52ab603
date: 2017-11-11 00:00:00
---

###在学习docker的时候 遇到一个坑  就是当docker容器中没有前台运行的程序的时候 docker会认为这个容器没有工作内容会自动关闭这个容器 
解决方法:
   在写dockerfile的时候 记得留一个在前台执行的任务  特别是在做那种一般在后台运行的服务 如mysql这样的 需要让他在前台运行 mysqld.service
