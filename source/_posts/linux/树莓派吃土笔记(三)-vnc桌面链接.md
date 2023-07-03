---
title: 树莓派vnc链接
categories: 笔记
tags:
  - linux
abbrlink: 52f5e4f0
date: 2017-11-11 00:00:00
---

###树莓派开启wifi模块后 想看看树莓派的桌面 然后就开启了vnc服务 玩玩 
####安装vnc服务
```
sudo apt-get install tightvncserver
```
####设置vnc链接的密码（最长只能8位 超过的截取前8位）
```
vncpasswd
```
####添加开机启动
```
sudo vim /etc/init.d/vncserver
```
内容: 
```
#!/bin/sh
#如果不是 pi用户 请 改这里的user  登录的时候 也用这里的user
export USER='pi' 
eval cd ~$USER
 
case "$1" in
  start)
    # 启动命令行。此处自定义分辨率、控制台号码或其它参数。
    su $USER -c '/usr/bin/vncserver -depth 16 -geometry 1024x768 :1'
    echo "Starting VNC server for $USER "
    ;;
  stop)
    # 终止命令行。此处控制台号码与启动一致。
    su $USER -c '/usr/bin/vncserver -kill :1'
    echo "vncserver stopped"
    ;;
  *)
    echo "Usage: /etc/init.d/vncserver {start|stop}"
    exit 1
    ;;
esac
exit 0
```
添加执行权限 更新开机启动
```
sudo chmod 755 /etc/init.d/vncserver
sudo update-rc.d vncserver defaults
```
###登录 
通过vnc工具登录 
账户 pi(脚本中的user)
密码 设置的vnc密码 不是ssh密码

###多个桌面开启 
默认是端口:5900  如果第一个窗口推荐在5900+1。。。以此类推 不要用默认的 有坑    
####总结: vnc链接树莓派的桌面 感觉卡卡的 玛格及
