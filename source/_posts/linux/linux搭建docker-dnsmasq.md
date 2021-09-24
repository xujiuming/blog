---
title: linux搭建docker-dnsmasq
comments: true
categories: 笔记
tags:
  - dnsmasq
abbrlink: d45e0e82
date: 2021-09-13 10:14:08
---

#### 前言

搭建私有化dns 服务 避免dns污染

#### 实战

选择使用容器部署

#### 部署

> https://hub.docker.com/r/jpillora/dnsmasq

```shell
 docker run \
    --name dnsmasq \
    -d \
    -p 53:53/udp \
    -p 5380:18888 \
    -v /home/ubuntu/dnsmasq/dnsmasq.conf:/etc/dnsmasq.conf \
    --log-opt "max-size=100m" \
    -e "HTTP_USER=ming" \
    -e "HTTP_PASS=ming" \
    --restart always \
    jpillora/dnsmasq
```

#### 遇到问题
##### 53端口占用
> lsof -i:53
> netstat -tlunp|grep 53
* bind9服务占用
可能服务器已经安装类似的dns server  例如bind
此时只需要停止即可
```shell
# 停止bind9
sudo systemctl stop bind9
# 关闭bind9 
sudo systemctl disable bind9 
```
* system-resolve服务占用 
>https://blog.csdn.net/qq_24924187/article/details/109197505

停止服务  
```shell
sudo systemctl stop systemd-resolved

```

编辑配置  
```shell
vim /etc/systemd/resolved.conf
```
```text
[Resolve]
#配置阿里云的dns解析    
DNS=223.5.5.5
#FallbackDNS=
#Domains=
#LLMNR=no
#MulticastDNS=no
#DNSSEC=no
#DNSOverTLS=no
#Cache=no-negative
#DNSStubListener=yes
#关闭dns 根服务器监听 
DNSStubListener=no
#ReadEtcHosts=yes
```

覆盖引用     
```shell
sudo ln -sf /run/systemd/resolve/resolv.conf /etc/resolv.conf
```

重启服务
```shell
sudo systemctl restart systemd-resolved 
```

#### 总结
启动自己的dnsmasq  大多数情况就是端口被占用  
看看 端口被那些屌进程占用 依次处理就行 
一般也就是同类型dns server  或者 systemd-resolved服务 