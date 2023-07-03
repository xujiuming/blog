---
title: manjaro更新之后中文乱码
comments: true
categories: 实战
tags:
  - 坑
  - linux
abbrlink: b9f58837
date: 2019-07-24 11:50:20
---
#### 前言
今天更新manjaro的时候 更新完之后 idea中中文竟然是"□"形状的乱码 
#### 问题引起原因和解决方案
* 系统编码不是utf8
```bash
#查看系统编码 
locale 
#编辑系统编码配置
vim /etc/sysconfig/i18n
#修改为zh_CN.UTF-8。。。。

#配置生效 
source /etc/sysconfig/i18n 

```
*  字体库缺少中文的字体  
```bash
#安装一个待中文字体的字体库   
sudo pacman -S wqy-microhei
```


#### 总结 
arch 更新快的一批   有时候出点小问题 正常的  反正 arch wiki 都会有大哥踩坑  

