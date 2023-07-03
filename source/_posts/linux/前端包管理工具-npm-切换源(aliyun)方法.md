---
title: 前端包管理工具npm切换国内源
categories: 笔记
tags:
  - linux
abbrlink: 3ff0ec3c
date: 2017-11-11 00:00:00
---

##在学ng2的时候接触到npm管理包、插件的方式 但是npm默认的源是国外的如果懒的科学上网 又想快速使用 只能使用国内的镜像站点 例如aliyun 
####当安装npm 完成 可以通过如下的方法去修改npm的源 
####1.通过config命令
npm config set registry https://registry.npm.taobao.org 
npm info underscore （如果上面配置正确这个命令会有字符串response）
####2.命令行指定
npm --registry https://registry.npm.taobao.org info underscore 
####3.编辑 ~/.npmrc 加入下面内容(linux 在你的用户目录下 没有就创建 windows在c盘下)  
registry = https://registry.npm.taobao.org
