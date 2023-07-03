
---
title: docker-compose配置笔记
categories: 笔记
tags:
  - docker
abbrlink: 5f628b10
date: 2017-11-11 00:00:00
---
####docker-compose 是docker的一个工具 是用来根据配置联合启动多个docker的工具  特别是在启动一些复杂应用的时候 适合用docker-compose来启动  当然 也可以自己写shell脚本启动 
####docker-compose配置文件 部分字段解释
#####实例 yaml格式
```
version: '3'
services: 
  mysql: 
    image: mysql
    ports:
      - '3306:3306'
    networks:
      - network1
  redis: 
    image: redis
    ports:
      - '6379:6379'
    networks: 
      - network1
  nginx:
    image: nginx
    prots:
      - '80:80'
    networks:
      - network1
networks:
  network1:
    driver: bridge
```
标准的docker-compose配置需要version、service、networks三部分
* version 配置文件版本
```
不能填写1  
```
* service 容器相关启动配置
```
可以指定 image相关的启动参数 例如 -v  --name  -p   等启动信息
在挂在数据卷的时候 也就是 指定-v参数的时候  windows指定的目录无权限 坑的一匹  
```
* networks 容器使用的网卡模式
```
主要是设定容器使用的网卡模式  
```

####docker-compose 用法 
用法和docker 的用法差不多 
例如
    up 根据这个yaml启动相应的容器  加上 -f  指定yaml文件  不指定 默认是当前目录的./docker-compose.yaml   -d是后台运行 不指定-d前台运行  
    build  可以根据dockerfile 去构建生成启动容器 
```
sudo docker-compose -f ./xxx.yaml up 
```
其他用法 参考man docker-compose 

#####学习博客地址:http://www.jianshu.com/p/2217cfed29d7
