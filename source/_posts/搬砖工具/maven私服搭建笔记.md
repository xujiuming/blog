---
title: maven私服搭建笔记
comments: true
categories: 笔记
tags:
  - maven
  - 私有化部署
  - 工具
  - CI/CD
abbrlink: fb79c020
date: 2019-07-11 10:51:29
---
#### 前言 
maven gradle java常用的依赖、项目管理工具     
在公司 总是需要一个私服 来提供给大家搬砖   
nexus 就是一个用的人贼多的 仓库管理工具 
#### 安装部署
由于个人比较懒惰 懒得折腾环境  直接采用docker 部署 
利用 docker 默认携带的swarm 集群 来管理 
运行服务器要求: 内核 3.1以上  能运行docker即可 
##### docker-compose配置
利用docker-compose配置来配置swarm 如何启动 这个nexus服务  
nexus.yml:
```yaml
#author ming  
# 部署 maven 私服 的docker compose 配置 
version: '3.3'
services:
  sonatype-nexus3:
    image: sonatype/nexus3
    ports:
      - 28888:8081
    networks:
      - nexus
  volumes:
    - type: bind
      source: /本地目录
      target: /nexus-data
    deploy:
      replicas: 1
      update_config:
        parallelism: 1
        delay: 10s
      restart_policy:
        condition: on-failure
networks:
  nexus:
    driver: overlay
    ipam:
      driver: default
      config:
      # 如果有冲突 请修改 这个 子网网段配置 
        - subnet: 10.88.0.0/16
```
##### 根据配置启动 
* 必须先初始化swarm集群 参考:  {% post_link 综合应用/docker-swarm集群搭建 %}
根据配置启动 一个叫做 nexus的stack     
```bash
sudo docker stack deploy -c ./nexus.yml  nexus --with-registry-auth
```
注意: --with-registry-auth 使用仓库认证  


##### 访问
浏览器打开 http://服务器ip:28888  
默认管理员账户: admin
默认管理员密码: 密码在启动容器中的/nexus-data/admin.password 中  登录之后 会提示设置信密码  
* 建议登录之后 先关闭其他权限、修改管理员密码、 新增自定义权限和账户分配出去  

#### 总结
由于docker的出现 使得大部分依赖复杂 安装麻烦的软件 都变得很好安装了 
直接启动一个容器即可   
nexus 不仅可以用来做maven私服   做其他的仓库也是可以的 例如 docker 镜像仓库  具体的看怎么配置仓库即可   

