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
基础镜像选择要求:   
1: 尽量选择合适的基础系统 如centos  debian ubuntu  alpine    一般来说选择debian这种基础镜像总体体积会比较大 但是有分层缓存这个优化 只会在第一次的时候会传输基础镜像层      
2: 指定相关sdk版本 不要使用latest  保证各个环境的镜像版本一致      
##### Java    
此处以openjdk11为基础镜像 调整默认时区
java web项目默认为spring boot 打包的fatjar 直接启动   
```dockerfile
FROM openjdk:11
MAINTAINER ming
# 增加启动环境变量MY_ENV 启动的java参数变量JAVA_OPTIONS 配置时区为上海
ENV MY_ENV=dev JAVA_OPTIONS='' TZ=Asia/Shanghai
#设置时区
RUN echo "${TZ}" > /etc/timezone \ 
    && ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime 
# 复制jar 
COPY demo.jar /demo.jar
EXPOSE 8080 8443
ENTRYPOINT java $JAVA_OPTIONS -jar /demo.jar --server.port=8080 --https.ssl.port=8443 --spring.cloud.config.profile=$MY_ENV
```

##### Js-前端项目
```dockerfile
FROM nginx
MAINTAINER ming
ENV MY_ENV=dev TZ=Asia/Shanghai
#设置时区
RUN echo "${TZ}" > /etc/timezone \ 
    && ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime 
COPY nginx.conf /var/nginx/conf.d/default.conf 
COPY dist/* /usr/share/nginx/html
#COPY 证书 /var/nginx/conf.d/
EXPOSE 80 443 
#如果没有特殊的要求 不用重写 nginx默认的启动参数 
```

#### 常见问题
* 时区
大多数镜像为默认0时区 
需要按需调整为对应时区  如上海时区 +8时区 
* 时间 
在某些服务中 对时间要求比较严格 需要保持一致  这个时候 需要把镜像链接到ntp授时服务器 保证时间的准确 
* 开放服务端口 EXPOSE
基础容器 大多数情况下只开启默认端口 一般需要扩充为需要的端口 例如 443 8443 等  
* 数据卷 VOLUME
按照业务需要 考虑是否需要开放数据卷  例如打印出来的log文件   内存快照之类的 需要保存到文件系统中   

#### 总结 
总的来说 分为基础环境镜像+项目编译的文件打包       
注意做好项目编译文件镜像分层就行    
基础镜像只做为基础环境 一般不需要调整      
然后对项目编译文件做好镜像分层的处理 加快分发速度 减少传输大小    