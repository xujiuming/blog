---
title: docker配置daemon.json(加速、http访问私服仓库)
categories: 实战
tags:
  - docker
abbrlink: 3022b6f9
date: 2017-11-11 00:00:00
---
#### daemon.json
daemon.json 在linux下呢一般在/etc/docker/daemon.json 目录 
这个配置主要配置 docker守护进程的相关参数 例如代理、私服仓库、相关网络配置
避免直接配置docker.service 
官方文档地址:https://docs.docker.com/engine/reference/commandline/dockerd/#examples 
参考国内博客:http://blog.51cto.com/nosmoking/1881034
####常用配置 
###### 1:aliyun的docker加速是可以个人申请一个加速节点的 以下是我自己的加速点做为实例
当docker版本超过1.10的时候通过修改daemon配置文件/etc/docker/daemon.json来使用加速器：
```
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://o4omo0yw.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```
###### 2：配置私服仓库地址
这个配置 可以避免私服仓库没有https访问导致无法使用的问题 
```
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "insecure-registries":["<private repository url>"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

