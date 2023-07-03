---
title: linux-ftp命令笔记
comments: true
categories: 笔记
tags:
  - linux
  - ftp
abbrlink: 997a96a2
date: 2019-08-09 15:10:37
---
#### 前言
最近跟工行对接对账相关文件的时候 
采用工行定时向公司的ftp服务器上投递对账文件 
但是公司的ftp服务器 在生产机房 而且只允许指定服务器访问  没得办法 只能用终端使用ftp命令行工具访问 


参考资料:   
https://blog.csdn.net/indexman/article/details/46387561  
https://jingyan.baidu.com/article/b2c186c8ee1116c46ef6ffc8.html    



#### ftp安装 
##### server安装
由于只是做演示使用 直接使用docker 启动一个简单的ftp server  如果没做数据卷映射 不能作为真正的ftp server使用 
```bash
sudo docker run -d  \
-p 20:20 -p 21:21 -p 21100-21110:21100-21110 \
-e FTP_USER=myuser -e FTP_PASS=mypass \
-e PASV_ADDRESS=127.0.0.1 -e PASV_MIN_PORT=21100 -e PASV_MAX_PORT=21110 \
--name vsftpd --restart=always fauria/vsftpd
```
##### client安装
其他linux 版本 直接使用包管理工具安装即可  arch没有单独的ftp包 只能安装 inetutils 
arch：
```bash
sudo pacman -S inetutils
```
#### ftp命令说明
> ftp --help 

##### 直接指定登录 
语法: ftp \[command] ip  -p 端口
进入如下页面:
```text
ftp> 
```
##### 进入ftp控制台 
```bash
ftp 
```
会进入如下的界面:
```text
ftp>
```

#### ftp控制台中常用指令
在ftp控制台中输入 
```text
ftp> help 
```
显示当前ftp终端支持的命令 

##### 常用的指令  
· append -   向远程服务器追加本地文件

· bye -     结束FTP会话并退出

· cd -   改变远程工作目录

· close -   结束FTP会话并返回命令行

· delete -   删除远程单个文件

· disconnect -  从远程断开，重新获得FTP提示框

· get -   复制单个远程文件到本地

· help -  显示FTP命令帮助信息（用法通”?“）

· lcd -   改变本地工作目录

· ls -  显示远程目录文件和子目录的简短列表（只有文件名和目录名）

· mdelete -   删除远程一个或多个文件

· open -  连接到指定的FTP服务器

· prompt -  开关交互提示（默认为ON）

· put -  复制一个本地文件到远程

· pwd -   显示远程当前工作目录（字面意思：打印工作目录）

· quit -   结束FTP会话并退出FTP（功能通bye）

· quote - Sends arguments, verbatim, to the remote FTP server (same as "literal") 发送任意 ftp 命令？？

· recv -  复制远程文件到本地

· remotehelp - 显示远程命令帮助

· rename -   重命名远程文件

· rmdir -  删除远程目录

· send -   复制一个本地文件到远程（功能通put）

· status -  显示当前FTP连接状态

· type -  设置文件传输类型（默认为ASCII）

· user -  发送新用户信息


#### 总结  
ftp 用的地方很多 但是大多数情况下 直接用一些成熟的gui客户端即可     
不过 ftp命令行工具也很厉害 就是比较费键盘  




