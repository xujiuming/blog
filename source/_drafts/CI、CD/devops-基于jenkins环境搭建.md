---
title: devops-基于jenkins环境搭建
comments: true
categories: devops
tags:
  - devops
abbrlink: ffe107ab
date: 2020-05-25 10:01:18
---
#### 前言
在持续集成过程中 通常要使用一系列的工具来达成效果 
自己也在昂立这边实践过一些工具  
对此 写一些笔记 来记录分享以下 

#### 需要的工具

|名称|功能|是否需要安装|备注|
|:---|:--|:--------|：---|
|k8s|应用、工具的工作的集群编排工具|是|单机使用kubeadm部署即可|
|jenkins|编译打包分发等自动化功能的工具|是||
|nexus|软件包仓库、镜像仓库等等|是|较新版本支持存储docker image|
|docker|容器|是||
|禅道 or jira|是|项目管理工具||
|gogs|git仓库|是|由于是基于jenkins演示 这里不使用gitlab 采用轻量级的gogs|
|postman or  jmeter or  py容器|否|自动化测试相关工具|具体的看测试的选择 也可以使用py脚本什么的|

#### 安装
##### k8s集群
使用kubeadm 构建一个简单的单机k8s集群 用来承载 各种工具和应用 

参考笔记: {% post_link k8s/ubuntu2004上使用kubeadm初始化单机k8s笔记 %}

##### git仓库
此处使用gogs来部署  减少资源损耗 

