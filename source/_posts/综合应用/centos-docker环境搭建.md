---
title: centos-docker环境搭建
comments: true
categories: docker
tags:
  - docker
  - linux
abbrlink: aef2102a
date: 2018-03-19 10:22:33
---

### 环境:
* centos 7.x
* docker  
#### 1:安装docker
```
#安装 一些组件
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
# 拉取阿里云中 docker-ce的repo
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
# 刷新 yum 缓存
sudo yum makecache fast
# 安装 docker-ce
sudo yum -y install docker-ce
# 启动 docker service
sudo systemctl start docker 
```
#### 2：配置阿里云加速
```
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://7vm1yv9c.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
# 设置开机自启动
sudo systemctl enable docker 
```
#### 3:关闭SELinux  firewalld  
```

Redhat系列使用了SELinux来增强安全，关闭的办法为：
1. 永久有效
修改 vi /etc/selinux/config 文件中的 SELINUX="" 为 disabled ，然后重启。
2. 即时生效
#setenforce 0
#3:查看是否关闭
sestatus

#### 关闭 firewalld
systemctl stop firewalld
### 禁止开机自动启动firewalld
systemctl disable firewalld 
```