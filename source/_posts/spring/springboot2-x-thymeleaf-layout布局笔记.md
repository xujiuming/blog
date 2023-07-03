---
title: springboot2.x+thymeleaf+layout布局笔记
comments: true
categories: 笔记
tags:
  - spring boot
  - thymeleaf
  - layout
abbrlink: dc044e7c
date: 2019-06-18 14:26:08
---
#### 前言    
身为闲不住的人，总是想搞事 但是又懒得学vue之类的前端框架技术   
只好利用学学后端的一些模板引擎技术 来过过手瘾   
在远古时代 可以直接用jsp来做    
但是spring boot 2.x 引入webflux 这种非标准java servlet容器   
jsp基本gg  官方又比较推荐thymeleaf   
那就只好用用thymeleaf 玩玩   
jsp拥有sitemesh 这种神器   
 由于 sitemesh是利用 servlet的filter来进行装饰的    
thymeleaf 也有用layout 这种协助快速布局的工具 
#### 使用thymeleaf + layout布局 
##### 依赖 
这里只描述核心依赖 
```xml
        <!-- spring boot 默认配置的 thymeleaf 组件-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <!-- 使用thymeleaf layout布局的必要依赖-->
        <dependency>
            <groupId>nz.net.ultraq.thymeleaf</groupId>
            <artifactId>thymeleaf-layout-dialect</artifactId>
        </dependency>
        
         <dependencyManagement>
                <dependencies>
                    <!-- spring boot -->
                    <dependency>
                        <!-- Import dependency management from Spring Boot -->
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-dependencies</artifactId>
                        <version>${spring.boot.version}</version>
                        <type>pom</type>
                        <scope>import</scope>
                    </dependency>
                    <!-- spring  io platform -->
                    <dependency>
                        <groupId>io.spring.platform</groupId>
                        <artifactId>platform-bom</artifactId>
                        <version>${spring.io.platform.version}</version>
                        <type>pom</type>
                        <scope>import</scope>
                    </dependency>
                </dependencies>
            </dependencyManagement>
```
##### layout文件和示例html 
* 在templates/layout目录下建立default.html
```html
<!DOCTYPE html>
<html lang="zh-CN"
      xmlns:layout="http://www.ultraq.net.nz/web/thymeleaf/layout"
>
<head>

    <meta charset="utf-8"/>
    <title>ming</title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
</head>

<body>
<div>
    <h1>nav</h1>
</div>

<div layout:fragment="content"></div>


<div>
    <h1>footer</h1>
</div>

</body>
</html>

```
*  在templates目录下建立 content.html
两个必须要注意的地方 
1: layout:fragment="content"  
2: layout:decorate="~{layout/default}" 
```html
<div class="row" layout:fragment="content" 
     xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
     layout:decorate="~{layout/default}">
        <h1>content by content.html</h1>
</div>
```

##### 编写控制器
```java
 package com.ming;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.GetMapping;
 import reactor.core.publisher.Mono;
 
 /**
  * 首页控制器
  *
  * @author ming
  * @date 2019-06-18 14:38:51
  */
 @Controller
 public class IndexControoler {
 
     @GetMapping("/content")
     public Mono<String> getContent() {
         return Mono.just("content");
     }
 }

``` 
##### 访问
访问 http://ip:port/content 

#### 总结
利用layout 配合thymeleaf 达到装饰效果 跟远古时代的jsp使用sitemesh一个套路 
个人练手的时候 用这个 还是很方便的 
