---
title: linux进入单用户模式和救援模式
comments: true
categories: 实战
tags:
  - linux
abbrlink: f37a09c1
date: 2019-09-04 09:35:04
---
#### 前言 
之前遇到过本地环境由于mount错磁盘重启之后无法启动  
就使用单用户模式恢复了  
就此记录一篇笔记  方便后续出现问题快速查询
#### 单用户模式
单用户模式，只有一个用户可以访问某个资源的状态。类Unix系统上工作时的一种拥有超级用户权限的模式。


* 由于公司大部分机器都是安装的cent os7 这里以cent os7作为示例 
##### 编辑grub 进入单用户模式
- 1.进入grub界面   
  在选择内核的界面上按\[e]进入grub 
- 2.编辑grub     
  找到\[linux16] 开头的行 把其中的 \[ro]只读 改成\[rw  init=/sysroot/bin/sh] 读写、并且启动指定shell   
- 3.启动系统    
  按下 ctrl+x 启动  

> 切换到操作系统环境 chroot /sysroot/ 
##### 输入1或者s进入单用户模式
- 1.进入选择启动用户界面
   在内核选择页面 输入\[a] 
- 2.输入1或者s进入单用户 
  
#### 救援模式       
所谓的救援模式。。。就是做个u盘或者光盘的启动盘 利用制作镜像时候自带的功能取维护调整系统    

- 进入引导页面选择 Troubleshooting 
- 选择 rescue a cent os system 



#### 总结
linux 系统 修复起来还是比较简单的     
一般来说 单用户模式已经能救活绝大多数的问题了    
如果到了一定需要救援模式解决  那么我认为应该找个机会重装 将服务器上的应用切换移走  


