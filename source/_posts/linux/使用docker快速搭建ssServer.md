---
title: 使用docker搭建ssServer
categories: 笔记
tags:
  - linux
abbrlink: 409db0cf
date: 2017-11-11 00:00:00
---
#### 前言 
由于gfw的存在 经常需要翻墙 自己手动搭建 ssServer又麻烦  使用docker 使用现成的image简单方便快捷
 docker只能在3.10以上的内核的linux系统或者差不多版本的其他系统中运行 所以在购买vps的时候要看清楚是什么架构 内核是否支持docker
 docker 下载ssServer镜像 (无法翻墙的请参考 另一篇笔记 docker使用aliyun加速器)
 
```shell script
docker pull mritd/shadowsocks
```

> 这个镜像是我认识的一个大佬写的、在dockerhub上也是排名前面的shadowsocksServer容器image

#### 启动 server端 

> 镜像文档地址:https://hub.docker.com/r/mritd/shadowsocks/

```shell script
docker run -dt --name ss -p 6443:6443 mritd/shadowsocks -s "-s 0.0.0.0 -p 6443 -m aes-256-cfb -k test123 --fast-open"
```

本地的ssClient设置  
ip是你vpsip  
端口 6443   
加密方式是aes-256-cfb   
密码是test123   

#### 总结
用已经有的docker容器来做ssServer能够快速的搭建起一个配置好的ssServer 避免自己手工安装配置的繁琐 

感觉这篇要被查水表 