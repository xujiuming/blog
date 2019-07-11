---
title: maven私服搭建笔记
comments: true
date: 2019-07-11 10:51:29
categories: 笔记
tags:
 - maven 
 - 私有化部署 
 - 工具 
 - ci/cd
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
    environment:
      # nexus 启动jvm配置    
      -  INSTALL4J_ADD_VM_PARAMS="-Xms2g -Xmx4g"
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
        - subnet: 10.88.0.0/16
```
##### 根据配置启动 
* 必须先初始化 swarm 集群 参考:  


{% post_link  %}
