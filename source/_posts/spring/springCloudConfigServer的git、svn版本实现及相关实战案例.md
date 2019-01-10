---
title: springCloudConfigServer的git、svn版本实现及相关实战案例
comments: true
categories: 实例
tags:
  - 实用
  - spring cloud
  - 分布式
abbrlink: 7ced3fa5
date: 2018-02-27 15:01:39
---
### 配置中心
在分布式 环境中 配置中心是必不可少的一个组件 不管是国内的 disconf 、Apollo之类 还是spring cloud的config server 都是一个套路 
将配置统一管理 通过namespace、项目名称之类的 进行区分 加入一些操作审计、配置加密之类的功能  有的是基于数据库、有的是基于文件+版本管理
由于技术栈和个人习惯  我个人更加喜欢spring 社区的config server  基于文件和版本管理来实现的配置 可以很方便的管理配置的版本和内容 

个人觉得 在整个集群中管踏马是什么配置 都可以丢进配置中心进行统一管理 例如app的打包配置、集群的参数配置等等 反正只要是配置都可以放进去 
#### spring cloud config server 
这个是属于spring cloud 项目中的一个组件  
它基于 svn或者git 去管理配置文件 提供各种各样的配置文件的管理 、提供敏感配置加密(对称、非对称都支持)、访问控制等等  
java中使用 config server client 客户端读取配置直接提供不停机更新配置功能 等等  
非java项目 可以通过http接口获取配置  
#### 需要技能
* git、svn熟练 
* java相关技能熟练 例如maven之类的
* docker 熟练 涉及到项目打包成docker image 

#### git版本
git版本就简单了 毕竟git好用 大部分书籍、博客都是介绍git版本的 
1：新建maven java项目
pom配置
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>parent</artifactId>
        <groupId>ming</groupId>
        <version>1.0.0</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>configServer</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <!-- <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-security</artifactId>
         </dependency>-->
    </dependencies>
</project>
```
2:application.yml配置
```
spring:
  application:
    name: config
  cloud:
    config:
      server:
        git:
          uri: "https://github.com/xuxianyu/springcloud.git"
          #仓库的搜索路径 这里可以指定 git的目录  config/spring-repo/{目录} 这种方式就可以按照项目区分配置目录了 
          search-paths: config/spring-repo/ming
#config server 访问账户密码
security:
  user:
    name: ming
    password: ming
#设定 对称加密密钥
#encrypt:
#  key: ming
          #username: 18120580001@163.com
          #password:
```
3：配置启动类
```
package com.ming;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * config server 启动类
 *
 * @author ming
 * @date 2017-10-30 11:18
 */
//开启config 服务
@EnableConfigServer
//注册到注册中心
@EnableDiscoveryClient
@SpringBootApplication
public class StartConfig {

    public static void main(String[] args) {
        new SpringApplicationBuilder(StartConfig.class).web(true).run(args);
    }
}
```
4：访问配置 
在git仓库中建立ming/config-dev.yaml  master分支
```
name: ming
```
浏览器访问 http://localhost:8888/master/config-dev.yaml

实例地址: https://github.com/xuxianyu/springcloud/tree/master/configServer
#### svn版本
1:新建maven java项目 
pom配置
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ming</groupId>
    <artifactId>configServer</artifactId>
    <version>1.0-SNAPSHOT</version>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.1.RELEASE</version>
    </parent>
    <properties>
        <!-- spring cloud version -->
        <spring.cloud.version>Camden.SR5</spring.cloud.version>
        <!-- svnkit version -->
        <svnkit.version>1.9.0</svnkit.version>
    </properties>

    <!--仓库-->
    <repositories>
        <!--aliyun repository-->
        <repository>
            <id>aliyun</id>
            <name>aliyun nexus repository</name>
            <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
        </repository>


    </repositories>


    <dependencies>
        <!-- spring cloud  config -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <!-- spring cloud config svn repository -->
        <dependency>
            <groupId>org.tmatesoft.svnkit</groupId>
            <artifactId>svnkit</artifactId>
            <version>${svnkit.version}</version>
        </dependency>
        <!-- spring  boot security 访问控制 -->
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-security</artifactId>
         </dependency>
    </dependencies>


    <dependencyManagement>
        <!--spring cloud parent pom-->
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring.cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>


    <!--构建插件-->
    <build>

        <plugins>
            <!--docker 插件-->
            <!--<plugin>
                <groupId>com.spotify</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>${maven.docker.version}</version>
                <configuration>
                    &lt;!&ndash;镜像名称&ndash;&gt;
                    <imageName>${docker.image.prefix}/${project.artifactId}</imageName>
                    &lt;!&ndash;docker file位置&ndash;&gt;
                    <dockerDirectory>src/main/docker</dockerDirectory>
                    <resources>
                        <resource>
                            &lt;!&ndash; 编译jar路径 &ndash;&gt;
                            <targetPath>/</targetPath>
                            &lt;!&ndash;编译jar 所在文件夹&ndash;&gt;
                            <directory>${project.build.directory}</directory>
                            &lt;!&ndash;编译的jar 名称&ndash;&gt;
                            <include>${project.build.finalName}.jar</include>
                        </resource>
                    </resources>
                </configuration>
            </plugin>-->
            <!--指定编译jdk版本 默认为1.5-->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- 解决资源文件的编码问题 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <configuration>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>


        </plugins>
    </build>
</project>
```
2：配置 application.yml
```
# default port
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        svn:
          # svn 配置仓库地址
          uri: https://repo.xujiuming.com/svn/ming/trunk/confDir
          # svn 访问账户
          username: ming
          # svn 访问密码
          password: mingpwd
          # 这个设置是访问时候没有带label默认的label  用处不大
          default-label: trunk
  # 使用 svn 作仓库  必须要填写
  profiles:
    active: subversion

# 访问控制
security:
  user:
    name: ming
    password: ming
encrypt:
  # rsa 密钥 设定 使用resources 下的 configServer.keystore
  key-store:
    location: configServer.keystore
    alias: configServer
    #  生成 密钥时候的密码
    password: ming2
    # 生成 密钥时候的签名
    secret: ming1
```
3：生成敏感数据密钥包
必须替换jce相关jar 或者直接使用我选择的docker 镜像 那个镜像是替换好jce相关jar包的

因为 config server 使用的是aes256 加密 所以必须替换jce 相关jar 
就算使用rsa 加密方式 也是需要jce相关jar 的  因为他还是用的aes256加密
rsa 加密方式配置
* 生成 rsa 密钥包  直接到控制台执行即可  使用的是jdk 的keytool 
```
configserver rsa 秘钥生成命令 有效时间 10000天
keytool -genkeypair -alias configServer -keyalg RSA \
-dname "CN=ming,OU=ming,O=ming,L=ming,ST=ming,C=ming" \
-keypass ming1 \
-keystore configServer.keystore \
-storepass ming2 \
-validity 10000 \
```
配置中使用 数据加密
* 使用加密字符串替换 原始字符串
```
访问 configServer的 {[/encrypt],methods=[POST]} 接口  参数为要加密的字符串  返回加密字符串
例如 加密前 字符串a   通过接口获取加密后字符串 b
在a配置的地方 使用 {cipher}b  替换a  即可 
加密前
passwd=a
加密后
passwd={cipher}b  
```
将生成的configServer.keystore 文件复制到resources目录中去
4:配置spring boot 项目启动类
```
package com.ming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/** config server start class
 *
 * @author  ming
 * @date  2017-12-06 10:08
 * */
@EnableConfigServer
@SpringBootApplication
public class StartConfigServer {

    public static void main(String[] args) {
        SpringApplication.run(StartConfigServer.class,args);
    }
}

```
5:配置dockerfile
```
FROM docker.io/fabric8/java-alpine-openjdk8-jdk
MAINTAINER "ming"

# create  workspace
RUN  mkdir /workspace
# copy app.jar
COPY target/configServer-1.0-SNAPSHOT.jar /workspace
# open port
EXPOSE 8888
# 运行jar
CMD  java -jar /workspace/configServer-1.0-SNAPSHOT.jar
```
6:打包成docker
```
mvn package 
sudo docker build -t config-server-ming:1.0.0 .
```
7：启动容器
```
sudo docker run -d -p 8888:8888 --name mingconfigserver config-server-ming:1.0.0
```
8：访问配置 
在svn仓库中建立ming/config-dev.yaml 
```
name: ming
```
浏览器访问 http://localhost:8888/ming/config-dev.yaml

实例地址: https://github.com/xuxianyu/springcloud/tree/master/configServerSvn
#### 访问配置文件url 详细解释
参数解释:
1:git
在git仓库中目录 配置文件的目录/配置文件前缀-配置文件后缀.配置文件类型  如在svn仓库目录下的ming/app-dev.yaml master分支  那么 master就是label app就是name  profiles就是dev  path就是app-dev.yaml git配置可以通过配置 区分文件夹 不是跟svn一样 把文件夹当作label 

```
name: 前缀
profiles: 后缀
label: 分支
path： 完整文件名
```
2:svn
在svn仓库中目录 配置文件的目录/配置文件前缀-配置文件后缀.配置文件类型  如在svn仓库目录下的ming/app-dev.yaml  那么 ming就是label app就是name  profiles就是dev  path就是app-dev.yaml
```
name: 配置文件前缀
profiles: 配置文件后缀
label: 文件夹目录名称
path:配置文件全称包含.后面的类型  
```

访问properties、yaml、json格式 访问方式  
/{name}/{profiles:.\*\[^-].\*}   
/{name}/{profiles}/{label:.*}   
/{name}-{profiles}.properties  
/{label}/{name}   
/{profiles}.properties  
/{name}-{profiles}.json   
/{label}/{name}-{profiles}.json   


访问非 java适配的配置文件 只能以文本模式 读取
参考博客地址:http://www.voidcn.com/article/p-yhrhfyla-bqr.html
/{name}/{profile}/{label}/{path}


#### 实际案例  
1：java web项目启动拉取配置中心配置  
在spring cloud 接入config server的项目中   
bootstrap.yml 优先级最高 在一启动就会加载 在这里配置 链接config server的配置、链接配置中心的配置 等等 
application.yml是在config client 拉取配置后才加载 如果在这里配置链接config server配置不会生效

增加pom依赖
```
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```
配置bootstrap.yml
```
spring:
 cloud:
     config:
       # 拉取后缀为dev的配置
       profile: dev
       # 拉取master分支的配置
       label: master
       username: ming
       password: ming
       uri: "http://localhost:8888"
       #开启 以服务方式访问配置中心
       #discovery:
       #  enabled: true
       #  service-id: CONFIG
       #开启预检
       #fail-fast: true
       #重试参数
       #retry:
         #重试间隔
         #multiplier: 2000
         #下一个间隔
         #initial-interval: 2000
         #最大间隔
         #max-interval: 5000
         #最大重试次数
         #max-attempts: 3

 application:
   # 拉取前缀为ming的配置
   name: ming
       #fail-fast: true
```
在配置仓库配置相关配置
然后启动项目 打个断点 查询spring env bean 即可查看是否加载到配置中心的配置



2: docker启动 时候拉取容器环境配置
思路: 重写镜像的 启动命令 在启动的时候拉取配置中心的配置覆盖容器中的配置文件 
如果配置中心设置的账户密码 需要获取配置中心 访问接口的token 
浏览器访问一下配置输入账户密码  抓http请求包 报头中有个 Authorization: Basic xxxxxxx 报头 
使用curl命令获取配置
```
curl -H "Authorization: Basic xxxxxxx" http://configServer地址//{name}/{profile}/{label}/{path} > /etc/nginx/conf.d/default.conf
```
调整dockerfile中的默认启动命令
CMD 或者ENTRYPOINT  一定要用&&  不能使用& 这个是并行执行 但是大部分容器是需要先加载完毕配置才能启动的
```
ENTRYPOINT curl -H "Authorization: Basic xxxxxxx" http://configServer地址//{name}/{profile}/{label}/{path} > /etc/nginx/conf.d/default.conf && run.sh
```
#### 总结: 
总的来说 spring cloud config server基本满足需求   
如果需要什么配置操作审计 完全可以在svn或者git操作上进行    
如果是java项目可以通过引入 spring cloud config client jar来使用  
非java项目 可以通过http接口获取配置信息   
关于高可用 就是多启动几台config server 注册到注册中心就行 客户端通过注册中心去访问http接口 

