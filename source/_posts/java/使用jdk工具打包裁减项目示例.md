---
title: 使用jdk工具打包裁减项目示例
comments: true
categories: 示例
tags:
  - jdk
abbrlink: 99772ea2
date: 2020-05-09 13:47:17
---
#### 前言 
jdk 一直在发展   中间比较有意思的功能 有 模块化、打包成对应平台的安装包  
通过 jmod  jlink  jpackage  等工具 可以对java的项目和运行的环境进行裁减优化 和分发不同平台安装包  
此处使用一个简单的例子 来演示一下  

步骤:    
1. 使用jmod将项目打包🏎成jmod 
2. 使用jlink 将应用模块 和依赖的jre 连接打包成一个裁减过的jre+项目  
3. 使用jpackage 将jlink打包之后的文件 分别打包成不同平台的安装包  如ubuntu的deb包  win的exe 

#### 示例
开发环境和依赖:
* openjdk14 
* 使用自带的建议HttpServer 作一个简单的接口 
* ubuntu 19.10  演示将项目打包成deb包  

##### 代码 


* httpServer 和main函数 
```java
package com;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * 提供一个简易 的http服务
 *
 * @author ming
 * @date 2020-05-09 14:00
 */
public class Ming {
    public static void main(String[] args) throws IOException {
        //创建http服务器，绑定本地8888端口
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        //创建上下文监听,拦截包含/
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                System.out.println("访问服务 [/]"+System.currentTimeMillis());
                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();
                os.write("mmp!".getBytes("UTF-8"));
                os.close();
            }
        });
        System.out.println("开启httpServer服务:8080端口:提供[/]访问");
        httpServer.start();
    }
}

```
* module-info.java 
```java
module TestJPackage {
    requires jdk.httpserver;
}
```
##### 编译成字节码
* 编译源码 
```shell script
javac ./com/Ming.java  
```
* 编译模块配置 
```shell script
javac module-info.java
```
##### 打包成jmod
```shell script
jmod create --class-path .    ming.jmod
```
#####  构建runtime-image

> 使用 jdeps --list-deps .   查看项目依赖模块    

```shell script
# 由于要执行class必须要java.base模块  然后ming.jmod本身依赖jdk.httpserver模块  所以需要link两个模块   并且配置启动类
jlink --module-path . --add-modules java.base,jdk.httpserver,TestJPackage  --output myjre   --launcher mingtest=TestJPackage/com.Ming
#查看裁减之后的jre大小 46m大小  原本jre 所有模块80mb起步  
du -sh ./myjre 
``` 
##### 启动项目
```shell script
# 使用裁runtime-image 运行 
./myjre/bin/mingtest
```
##### 打包成ubuntu下的deb包  
```shell script
# 查看当前平台支持的包 
#生成应用程序映像   --type 按平台打包 例如 app-image ubuntu的deb包 centos的rpm包  mac的 dmg pkg包  win的exe包 具体的可以在不同平台上查看
jpackage --type deb -n mingtest -m TestJPackage/com.Ming --runtime-image myjre 
```
##### 安装、查看、运行deb包 
```shell script
# 安装deb包 
sudo dpkg -i ./mingtest_1.0-1_amd64.deb 
#解压deb包 
sudo dpkg-deb -X  ./mingtest_1.0-1_amd64.deb  ./mingtest
#查看当前是否安装 deb包
sudo dpkg -l mingtest
#通过deb安装之后运行项目  由于没有指定deb包的详细安装配置 所以 会默认安装在/opt目录 而且不会把项目启动的命令放在 path下 
/opt/mingtest/bin/mingtest 
```


#### 总结
jdk8之后的版本 度很有意思    
使用jlink jmod  jpackage 能够将一个java项目 裁减运行环境和打包成不同平台安装包     
之前要想做到打包成不同平台安装包 需要使用各种各样的插件乱七八糟的   
现在终于有了官方的工具   