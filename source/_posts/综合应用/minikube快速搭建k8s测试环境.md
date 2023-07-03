---
title: minikube快速搭建k8s测试环境
comments: true
categories: 实战
tags:
  - docker
  - k8s
abbrlink: ba1e0f81
date: 2018-02-11 14:30:12
---
 有朋友问我 k8s测试环境如何快速搭建 官方推荐的是minikube 
其实吧 阿里云上这种文档一抓一大把 
参考文档:https://yq.aliyun.com/articles/221687 
#### 运行环境
* ubuntu 16.04 至少这个版本  低了不行 没有snap
* 可以访问互联网   
#### 安装步骤
0:更新系统apt包相关资源
需要手动调整到国内的镜像源  不然要慢死人了
安装snap snapd 方便后续安装kubectl
```
sudo apt update && sudo apt upgrade 
sudo apt install snap  snapd
```
1：安装kubectl 
比较懒  采用 snap(类似apt的东西) 安装kubectl 
```
sudo snap install kubectl 
```
2：安装golang
使用apt 安装golang 
```
sudo apt install golang
```
3: 安装minikube 
```
curl -Lo minikube http://kubernetes.oss-cn-hangzhou.aliyuncs.com/minikube/releases/v0.25.0/minikube-linux-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
```
4:启动minikube
```
minikube start --registry-mirror=https://registry.docker-cn.com
```
5:尝试kubectl是否可用
在启动minikube的时候 它会自动覆盖kubectl之前的链接配置 直接使用就是链接到minikube创建的k8s集群中
```
kubectl get all
```
6:打开k8s  web ui
```
minikube dashboard
```
执行完毕后会自动弹出浏览器 如果没有 请尝试 http://192.168.99.100:30000 
7:现在就可以在web ui中或者终端中使用kubectl操控k8s集群了
此集群k8s版本为1.9 

#### 总结: 这个方法只能在本地快速搭建起来一套测试环境 如果是真实运行环境还是要手动的去安装  高版本的k8s 安装基本上对于一个熟悉linux的用户来说没啥难度 总的就是安装各个组件、配置各个组件 即可 

