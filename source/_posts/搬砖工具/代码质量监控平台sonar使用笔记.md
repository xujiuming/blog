---
title: 代码质量监控平台sonar使用笔记
comments: true
categories: 实战
tags:
  - 代码质量
  - 持续集成
abbrlink: 8dcd7b21
date: 2018-06-28 14:21:19
---
#### 前言
最近写的代码 太多了 感觉质量有所下降 
之前本地一直使用 alibaba的code插件 但是没有出团队版本的  
干脆搞了一个 sonar 检测一波自己的代码质量 
#### sonar docker 方式启动 
```
#安装中文插件的 sonar 

sudo  docker run -d --name sonarqube -p 9000:9000 -p 9092:9092 jiuming/sonar-qube-zh:1.0.0
#原版 英文版本的
#sudo  docker run -d --name sonarqube -p 9000:9000 -p 9092:9092 sonarqube
```
sonar swarm stack配置
```
version: '3.3'
services:
 sonar:
  image: jiuming/sonar-qube-zh:1.0.0
  ports:
   - 9000:9000
   - 9092:9092
  networks:
    - sonar
  deploy:
   replicas: 1
   update_config:
    parallelism: 1
    delay: 10s
   restart_policy:
    condition: on-failure
networks:
  sonar:
   driver: overlay
```
打开 http://localhost:9000  默认账户:admin  默认密码:admin
* sonar qube 官方docker image 安装中文插件方法  
登录 容器  docker exec -it <containerId> /bin/sh 执行如下命令    
ps: sonar 中文插件版本 请按照https://github.com/SonarQubeCommunity/sonar-l10n-zh 文档去下载安装   
```
#安装 中文插件
wget https://github.com/SonarQubeCommunity/sonar-l10n-zh/releases/download/sonar-l10n-zh-plugin-1.21/sonar-l10n-zh-plugin-1.21.jar  && \
mv /opt/sonarqube/sonar-l10n-zh-plugin-1.21.jar /opt/sonarqube/extensions/plugins
```
或者直接使用已经修改好的镜像jiuming/sonar-qube-zh:1.0.0 这个是基于 7.x的sonar
####  maven 添加sonar 插件
```
            <plugin>
                    <groupId>org.sonarsource.scanner.maven</groupId>
                    <artifactId>sonar-maven-plugin</artifactId>
                    <version>3.4.0.905</version>
            </plugin>
```
#### 执行 sonar 任务 
如果在另外一台机器上 加上-Dsonar.host.url=http://ip:port   
如果使用了密钥 那么需要-Dsonar.login=xxxxxx 
```
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 
#mvn sonar:sonar \
#  -Dsonar.host.url=http://localhost:9000 \
#  -Dsonar.login=5cb26b212d30530db172ff6adbfe5d2887698b8c
```



#### 遇到的问题
* 报svn认证错误(git认证错误和这个差不多处理方法 )
 svn: E170001: Authentication required for '<https://xxxxxx:xxx> VisualSVN Server' -
在配置>通用设置>scm>配置svn的username、password或者配置密钥即可 

#### 总结
大部分检测的规范 都差不多把  本地还是使用alibaba的规范比较和是 直接安装alibaba的code插件也不错
sonar 适合团队使用  统一代码风格、减少明显bug 



