---
title: java云原生-Quarkus框架笔记
comments: true
categories: 笔记
tags:
  - 云原生
  - java
  - 框架
abbrlink: e31efaae
date: 2019-11-20 09:40:25
---
#### 前言
在鼓吹云原生的时代 java也在与时俱进  
衍生出适合云原生时代的框架技术    
quarkus是开源的基于vert.x等框架开发的一种应用层框架 可以利用graalvm来做native image 提升执行效率

> 官网: https://quarkus.io/
> graalVM oracle开源的一个强力的跨语言 vm  可以将多种语言编译成native image   

此处记录笔记  方便后续查询使用 

#### 示例
> 此处使用maven手工搭建   可以使用官方cli 或者官网进行搭建 跟spring-boot-cli 和start.spring.io 差不多
##### maven配置
利用quarkus maven插件  来完整的初始化项目 
```shell script
# 创建一个quarkus项目  只有一个path class 
mvn io.quarkus:quarkus-maven-plugin:1.0.0.CR1:create \
    -DprojectGroupId=com.ming \
    -DprojectArtifactId=quarkus \
    -DclassName="com.ming.Hello" \
    -Dpath="/hello"
```
##### 使用
利用quarkus maven插件 启动项目 
```shell script
# 在生成的项目根目录执行  启动当前项目 
mvn io.quarkus:quarkus-maven-plugin:1.0.0.CR1:dev
```
运行日志:
```text
2019-11-20 13:53:14,057 INFO  [io.qua.dep.QuarkusAugmentor] (main) Beginning quarkus augmentation
2019-11-20 13:53:15,194 INFO  [io.qua.dep.QuarkusAugmentor] (main) Quarkus augmentation completed in 1137ms
2019-11-20 13:53:15,505 INFO  [io.quarkus] (main) Quarkus 1.0.0.CR1 started in 1.575s. Listening on: http://0.0.0.0:8080
2019-11-20 13:53:15,507 INFO  [io.quarkus] (main) Profile dev activated. Live Coding activated.
2019-11-20 13:53:15,507 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy]
```
测试使用: 
```shell script
curl http://127.0.0.1:8080/hello
#返回hello 
```
##### 打包
使用quarkus 插件的build功能 
```shell script
# 刷新依赖配置 
mvn -DskipTests clean install 
# 使用插件build 
mvn io.quarkus:quarkus-maven-plugin:1.0.0.CR1:build
```
运行jar:
```shell script
# 示例jar不一定是这个名字 具体的在target文件目录找  
java -jar ./target/quarkus-1.0-SNAPSHOT-runner.jar
```
运行日志:
```text
ming@ming:~/workspaces/tmp/quarkus$ java -jar ./target/quarkus-1.0-SNAPSHOT-runner.jar
2019-11-20 14:05:50,993 INFO  [io.quarkus] (main) quarkus 1.0-SNAPSHOT (running on Quarkus 1.0.0.CR1) started in 0.900s. Listening on: http://0.0.0.0:8080
2019-11-20 14:05:51,013 INFO  [io.quarkus] (main) Profile prod activated. 
2019-11-20 14:05:51,014 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy]
```
##### 使用graalvm 构建native image 
先安装graalvm 
```shell script
sdk install  java  19.2.1-grl
```

使用quarkus插件构建 native image 

```shell script
mvn io.quarkus:quarkus-maven-plugin:1.0.0.CR1:native-image
```

##### 使用容器运行graalvm构建的native image 
查看/src/main/docker/Dockerfile.native dockerfile 打包到镜像的时候 直接用这个dockerfile即可
在项目根目录执行如下shell 演示使用自带的dockerfile 打包和运行 demo 

```shell script
docker build -f src/main/docker/Dockerfile.native -t ming/quarkus:1.0.0 .
dockerk run -d -p 8080:8080 ming/quarkus:1.0.0
```
#### 总结 
quarkus 怎么说呢 把java中的一些比较厉害的框架组合起来形成一种一站式开发框架   从创建项目 到构建镜像 都可以一套插件搞定   
有点不好的地方 就是跟原本熟悉spring全家桶的java开发来说 写法和习惯都有一些不同   
二个上了graalvm的车 说不好后面这个东西 能不能用 




