---
title: linux 解压windows文件乱码处理
categories: 坑
tags:
  - linux
abbrlink: 5c7769e6
date: 2017-11-11 00:00:00
---
1. 通过unzip行命令解压，指定字符集
unzip -O CP936 xxx.zip (用GBK, GB18030也可以)
有趣的是unzip的manual中并无这个选项的说明, unzip --help对这个参数有一行简单的说明。
2. 在环境变量中，指定unzip参数，总是以指定的字符集显示和解压文件
/etc/environment中加入2行
[?](http://www.jb51.net/article/113961.htm#)
1
2

UNZIP="-O CP936"

ZIPINFO="-O CP936"

这样Gnome桌面的归档文件管理器(file-roller)可以正常使用unzip解压中文，但是file-roller本身并不能设置编码传递给unzip。
