---
title: linux使用ss笔记
categories: 笔记
tags:
  - linux
abbrlink: 5624ab1d
date: 2017-11-11 00:00:00
---

由于有个朋友对于在linux上使用ss翻墙不会 特别写个简单的笔记、
#######基于ubunut17.04、python3、
####1:安装pip3
```
sudo apt install pip3
```
####2:通过pip3安装shadowsocks
```
sudo pip3 install shadowsocks
```
####3:设置shadowsocksServer属性配置
```
{
	"server":"ss服务器ip",
	"server_port":端口,
	"local_address":"127.0.0.1",(本地socks5地址)
	"local_port":1080,(本地socks5端口)
	"password":"ss服务器密码",
	"timeout":300,(超时时间 ms),
	“method”:"aes-256-cfb"(加密方式)尽量选择这个 
}
```
####4:启动本地ss
```
sudo sslocal -c 配置文件地址
```
####5:设置代理
这里设置代理有很多方式  先说两种常用的
1:直接设置整个系统的代理 、通过gfwlist生成ip表 去判别国内国外网站选择性加速 
2:对某部分程序设置代理、如chrome浏览器 下载个switch代理工具 可以自己设定路由规则也可以通过gfwlist生成相应的ip表
######总结:ss翻墙 总的来说就是 发起访问--->从本地服务器代理访问、判断是否需要代理--->需要代理的连接远程ssServer 进行代理---->访问gfw之外的内容

