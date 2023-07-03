---
title: dockerDesktop开启k8s笔记 
comments: true 
date: 2021-11-18 15:22:29 
categories: 笔记 
tags:
    - k8s
    - docker
---

#### 前言

最近想复习一下k8s 本来准备 miniKube搭建的  
但是看到windows 下面的dockerDesktop 也支持一个单机的k8s 就干脆玩玩

> 参考文档:
> https://www.cnblogs.com/danvic712/p/enable-k8s-in-docker-desktop.html
> https://github.com/AliyunContainerService/k8s-for-docker-desktop

#### 部署
> 环境: wsl2 ubuntu20.04    
 
* 配置镜像加速

```json
{
  "registry-mirrors": [
    "https://o4omo0yw.mirror.aliyuncs.com"
  ],
  "insecure-registries": [],
  "debug": false,
  "experimental": false,
  "features": {
    "buildkit": true
  },
  "builder": {
    "gc": {
      "enabled": true,
      "defaultKeepStorage": "20GB"
    }
  }
}
```
* 复制阿里云的容器服务的镜像脚本  

```shell
git clone git@github.com:AliyunContainerService/k8s-for-docker-desktop.git
#切换到对应版本     例如我当前是1.21.4   
git checkout v1.21.4
#管理员启动 powershell  
Set-ExecutionPolicy RemoteSigned
.\load_images.ps1
```

* 启动和配置kubectl、dashboard
```shell
# 创建 dashboard 资源
kubectl apply -f kubernetes-dashboard.yaml

# 查看 Deployment 的运行状态 
kubectl get deployment -n kuberenetes-dashboard

# 查看 Pod 的运行状态
kubectl get pods -n kuberenetes-dashboard

# 通过代理的方式访问 dashboard
kubectl proxy
# 生成 dashboard使用的token   
$TOKEN=((kubectl -n kube-system describe secret default | Select-String "token:") -split " +")[1]
kubectl config set-credentials docker-for-desktop --token="${TOKEN}"
echo $TOKEN

```

* 配置dashboard直接访问 
