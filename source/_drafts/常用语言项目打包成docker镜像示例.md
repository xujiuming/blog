---
title: 常用语言项目打包成docker镜像示例
comments: true
categories: 实战
tags:
  - dev/ops
  - docker
abbrlink: 73cab05e
date: 2020-09-27 17:10:26
---
####前言
经常需要打包各种语言的项目打包成docker镜像 最近有时间整理一下 各种语言打包docker镜像的实践方案 
#### 示例
##### Java 
此处以openjdk11为基础镜像 调整默认时区
java web项目默认为spring boot 打包的fatjar 直接启动
> openjdk 默认的11版本基础操作系统为debian
```dockerfile
FROM openjdk:11
# 增加启动环境变量MY_ENV 启动的java参数变量JAVA_OPTIONS 配置时区为上海
ENV MY_ENV=dev JAVA_OPTIONS='' TZ=Asia/Shanghai
# 复制jar 
COPY demo.jar /demo.jar
ENTRYPOINT java -jar $JAVA_OPTIONS  /demo.jar --spring.active=$MY_ENV
```

##### Js-前端项目
```dockerfile
FROM nginx
ENV MY_ENV=dev TZ=Asia/Shanghai
COPY dist/* /var/nginx/html/
COPY nginx.conf /var/nginx/conf.d/default.conf 
```
##### NodeJs-后端项目
```dockerfile
FROM pm2
ENV MY_ENV=dev TZ=Asia/Shanghai
COPY dist/* /workspaces
ENTRYPOINT pm2run
```
##### Python
##### go 

#### 总结 
总的来说 分为基础环境镜像+项目编译的文件打包     
注意做好项目编译文件镜像分层就行  
基础镜像只做为基础环境 一般不需要调整    
然后对项目编译文件做好镜像分层的处理 加快分发速度 减少传输大小  