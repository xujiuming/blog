---
title: rancher搭建k8s集群-国内加速
comments: true
categories: 实战
tags:
  - k8s
  - rancher
  - docker
abbrlink: b52bf00b
date: 2018-03-01 11:04:01
---
通过rancher部署k8s如果不考虑gfw 是非常简单的 但是在国内部署 不得不考虑gfw的问题  
默认有个 组件镜像是从 google上下载的 所以需要修改一些参数配置提供国内加速 

### 环境:
* centos 7.x
* rancher：1.6.x
* docker  支持rancher:1.6.x的版本即可  
#### 1： 安装基本docker 环境 
{% post_link 综合应用/centos-docker环境搭建 %}

#### 2：启动rancher server
```
# 建立存放 rancher 相关数据文件存储
mkdir -p /root/rancher
# 启动 rancher server 容器 
sudo docker run -d -v /root/rancher:/var/lib/mysql --restart=unless-stopped -p 40000:8080 --name rancher-server rancher/server:stable 
```
#### 3:登陆rancher
访问 http://<ip>:40000 
#### 4:自定义 k8s模板 使用 国内可用镜像 
按照 https://www.cnrancher.com/kubernetes-installation/ 文档进行修改 
注意：  
文档中标记红线的值 必须如下 设置 否则 无法启动 共四个值需要修改
Private Registry for Add-Ons and Pod Infra Container Image = registry.cn-shenzhen.aliyuncs.com
Image namespace for  Add-Ons and Pod Infra Container Image = rancher_cn
Image namespace for kubernetes-helm Image = rancher_cn
Pod Infra Container Image =  rancher_cn/pause-amd64:3.0
#### 5:创建 环境
根据刚刚创建的 k8s模板创建环境 即可  
#### 总结
部署rancher 很简单 注意是有时候是gfw导致组件无法创建 略坑    
rancher 部署的k8s集群 有两个dns  一个是k8s中 kube-system 中的dns服务 一个是rancher的network 应用  两个都可以用 
部署rancher 如果资源相对充足 rancher server和rancher agent分开部署  
在rancher1.x版本中 rancher部署的k8s集群 是在rancher agent 上部署的 扩展起来很困难 
而且由于 rancher也提供一套cni网络  这样就导致 k8s的网络和rancher的网络复合起来  显得较为复杂 不好处理







