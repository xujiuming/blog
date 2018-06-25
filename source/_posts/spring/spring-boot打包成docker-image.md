---
title: spring boot 项目打包docker image 插件方式
categories: 笔记
tags:
  - spring
  - docker
abbrlink: dbafeaad
date: 2017-11-11 00:00:00
---
####spring boot 打包成docker image 会更加方便使用 
1:配置编译jar选项
2:配置maven docker 插件
3:上传到私服 
4:自动化脚本
####配置编译jar
如果是继承spring boot 的pom 直接如下配置即可
```
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```
然后  mvn package 即可  这个时候 打包的jar 里面带有main-class的默认配置 也会有需要的依赖包
可以直接 java -jar ming.jar 方式运行
####配置 maven docker 插件
这个插件略坑  在windows上 docker build比较操蛋  linux下docker又需要root权限运行 略坑
```
            <plugin>
                <groupId>com.spotify</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>${maven.docker.version}</version>
                <configuration>
                    <!--镜像名称-->
                    <imageName>${docker.image.prefix}/${project.artifactId}</imageName>
                    <!--docker file位置-->
                    <dockerDirectory>src/main/docker</dockerDirectory>
                    <resources>
                        <resource>
                            <!-- 编译jar路径 -->
                            <targetPath>/</targetPath>
                            <!--编译jar 所在文件夹-->
                            <directory>${project.build.directory}</directory>
                            <!--编译的jar 名称-->
                            <include>${project.build.finalName}.jar</include>
                        </resource>
                    </resources>
                </configuration>
            </plugin>
```
然后使用 mvn package docker:build 编译构建即可  最好是在linux环境下 使用root 权限
####上传到私服
我这里选择的是上传到 aliyun 提供的镜像仓库  
```
$ sudo docker login --username=18120580001@163.com registry.cn-hangzhou.aliyuncs.com
$ sudo docker tag [ImageId] registry.cn-hangzhou.aliyuncs.com/mingimages/ming:[镜像版本号]
$ sudo docker push registry.cn-hangzhou.aliyuncs.com/mingimages/ming:[镜像版本号]
```
####参考地址:https://github.com/xuxianyu/ming
