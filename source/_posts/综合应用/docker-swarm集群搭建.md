---
title: docker-swarm集群搭建
comments: true
categories: docker
tags:
  - docker
  - swarm
  - linux
abbrlink: e855d436
date: 2018-03-19 10:25:17
---
swarm编排吧  首先肯定比不上k8s犀利 但是呢 胜在 使用简单 部署简单
docker直接集成   可以使用docker、docker-compose 原生api 
 
我个人认为 没有十来台服务器 部署k8s集群  完全是浪费  毕竟k8s 是非常需要 master 高可用的   服务器少了 部署k8s 浪费资源太多了  so  开发环境直接使用 swarm 来编排docker 

### 环境:
* centos 7.x
* docker  版本新点   至少 要集成swarm 模块  
#### 1： 安装基本docker 环境 
{% post_link 综合应用/centos-docker环境搭建 %}

使用 如下命令测试是否有swarm模块
```
docker swarm 
```
swarm 默认集成到docker中的
#### 2：基于 docker swarm  配置portainer管理页面
```
# 初始化 swarm 集群  
sudo docker swarm init 


# 创建 portainer 挂载目录
sudo mkdir -p /opt/portainer


# 以 swarm service 建立 portainer 管理 
sudo  docker service create \
--name portainer \
--publish 9000:9000 \
--replicas=1 \
--constraint 'node.role == manager' \
--mount type=bind,src=//var/run/docker.sock,dst=/var/run/docker.sock \
--mount type=bind,src=//opt/portainer,dst=/data \
portainer/portainer \
-H unix:///var/run/docker.sock
```
http://<ip>:9000 访问即可 
#### 3:遇到问题
参考地址:http://blog.51cto.com/zpf666/1908067   
1:无法通过服务名访问服务 
需要建立一个overlay网络  
swarm上默认已有一个名为ingress的overlay 网络, 可以直接使用
也可以从新建立     

2:遗忘swarm 添加节点时候的token
```
sudo docker swarm join-token master
sudo docker swarm join-token worker 
```
#### 总结
swarm 一两台服务器的docker编排 还是很轻松的  而且也不需要 k8s那么麻烦  
虽然k8s 一统天下  但是个人电脑用个swarm管理管理 还是简简单单 轻轻松松的 

