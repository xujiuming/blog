---
title: docker启动jenkins
comments: true
abbrlink: 5e5f86c0
date: 2018-02-09 16:17:08
categories: 笔记
tags:
  - docker
  - jenkins 
---
####在2.x版本jenkins 中 出现了 pipe 所以决定通过较为新的方式去从新部署一套基于jenkins 的docker 项目 自动化部署
https://dzone.com/refcardz/continuous-delivery-with-jenkins-workflow

#### 安装 jenkins docker版本 
1:jenkins 默认密码 
还是jenkins 容器中的安装目录下的老位置 ${jenkins安装位置}/secrets/initialAdminPassword

2:jenkins 容器内部运行 docker相关命令 
方案1:docker-in-docker  就是 在容器内部 安装容器 
这种方案 需要在jenkins 镜像上进行修改 比较麻烦  因为 docker里面是经过高度简化的系统 安装起来麻烦 
遇到错误:
因为 docker主线程 是jenkins  而docker是需要在root权限下运行  
要把jenkins用户加入到docker用户组中 即可用jenkins 用户执行docker 命令 
alpine 没有找到如何切换 用户组  usermod  之类的命令 alpine 没有 

方案2:【jenkins官网方案】docker-out-docker 通过目录影射 把docker.sock 影射到jenkins docker中 
这种方式 安全性很差  容器 能够访问宿主机的功能
如果做好 虚拟机或者物理机级别的隔离 其实还不错
```
sudo docker  run -u root --rm -d -p 20000:8080 -v jenkins-data:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/docker:/var/lib/docker --privileged  jenkinsci/blueocean
```
方案3:【危险方案】把 docker socket暴露出去 调用  
看到这种方案  玩度不想玩 放弃
