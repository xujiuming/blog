---
title: docker镜像打包优化
comments: true
categories: 实战
tags:
  - docker
  - 优化
abbrlink: b182d6c9
date: 2020-04-22 14:48:46
---
#### 前言
很多时候 看到别人写的dockerfile 根本不考虑 打包的效率和分发的速度 
反正只是打包完成了就算了  
一个好的docker 镜像 应该是合理分层 减少变动次数 从而加快打包的速度 和分发的速度
如   
打包java的项目 一般写法 就是直接打包一个fatjar 直接塞进jdk镜像
这样每次更新最少也有50mb+
这样第一个问题 build的时候 构建内容过大 build任务计算打包空间和复制相关内容的时候慢   
第二个问题 每次递交仓库 或者其它机器上的时候 jar这一层镜像 总是要重新传输 大量浪费带宽等资源

#### 示例
> 使用一个java项目进行示例 
##### 优化前
此处就直接打包一个fatjar 直接塞到 openjdk镜像里面  
dockerfile如下:
```dockerfile
FROM openjdk:14-jdk-alpine
# 作者  app名称  是否可快速清理
LABEL author=ming app=ming-workbench
# 默认环境为dev     默认开启 zgc  关闭字节码校验 开启分层编译 关闭jmx
ENV MY_ENV='dev' JAVA_OPTIONS='-XX:+UnlockExperimentalVMOptions -XX:+UseZGC    -XX:TieredStopAtLevel=1 -Dspring.jmx.enabled=false   '
#切换时区
#RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

RUN mkdir /workspace
#创建 log目录
RUN mkdir /workspace/logs
#复制jar
COPY target/workbench.jar /workspace/
#健康检测配置  间隔10s  超时1s  超过三次异常     启动30s后开始进行检查 使用health.sh脚本检测
#HEALTHCHECK --interval=10s --timeout=1s --retries=3 --start-period=60s CMD /workspace/bin/health.sh

EXPOSE 8080
ENTRYPOINT java -jar $JAVA_OPTIONS  /workspace/workbench.jar --spring.profiles.active=$MY_ENV  --server.port=8080
```
##### 优化方案 
* 增加build时候忽略无关文件 减少build执行时候的工作空间大小 
* java项目 依赖和代码分开  拆分为两层镜像  lib 和代码打包分开  需要项目支持

##### 增加.dockerignore 文件配置忽略无关文件 
```text
# docker build context 忽略文件 减少构建context
src
frontend
gradle
.gradle
.idea
ming.mv.db
ming.trace.db
deploy.sh
gradle
gradlew.bat
Jenkinsfile.groovy
ming.mv.db
README.md
build.gradle
docker-compose.yml
gradlew
initTools.sh
LICENSE
settings.gradle
```
##### 依赖和代码分开处理 
> 此步骤需要项目支持  项目打包的时候 将依赖和代码分开打包 

java spring boot项目分离依赖打包笔记: {% post_link 综合应用/springboot依赖代码分离打包笔记 %}


```dockerfile
FROM openjdk:14-jdk-alpine
# 作者  app名称  是否可快速清理
LABEL author=ming app=ming-workbench
# 默认环境为dev     默认开启 zgc  关闭字节码校验 开启分层编译 关闭jmx
ENV MY_ENV='dev' JAVA_OPTIONS='-XX:+UnlockExperimentalVMOptions -XX:+UseZGC    -XX:TieredStopAtLevel=1 -Dspring.jmx.enabled=false   '
#切换时区
#RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

RUN mkdir /workspace
RUN mkdir /workspace/lib
#复制lib
COPY target/lib  /workspace/lib
#复制jar
COPY target/workbench.jar /workspace/
#健康检测配置  间隔10s  超时1s  超过三次异常     启动30s后开始进行检查 使用health.sh脚本检测
#HEALTHCHECK --interval=10s --timeout=1s --retries=3 --start-period=60s CMD /workspace/bin/health.sh

EXPOSE 8080
ENTRYPOINT java -jar $JAVA_OPTIONS  /workspace/workbench.jar --spring.profiles.active=$MY_ENV  --server.port=8080

```

> 主要就是 单独复制lib那一步  单独建立一层镜像 这样docker打包镜像的时候 如果依赖没有变 就不会重新打包该层镜像 

#### 总结
其实就是利用了 docker 镜像分层存储的效果 这样将依赖这种 几乎固定的东西固化 只需要传递一次 后续只需要更新code那一层就行 每次传输的数据就非常小    
通过.dockerignore 减少docker打包的工作空间的资源  是因为docker 构建的时候 会吧文件复制到工作空间进行打包处理 一些不需要的资源过滤即可 