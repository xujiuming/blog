---
title: spring boot (四)笔记
categories: 笔记
tags:
  - java
  - spring
abbrlink: 68619b43
date: 2017-11-11 00:00:00
---

###在使用spring boot的时候 spring本身会根据springboot的版本引入依赖包 跟maven一样有隐藏依赖会自动引入这个时候如果不需要其中一些工具包 或者需要指定某些包的版本 可以通过maven的功能去排除依赖和覆盖依赖
##排除依赖 通过maven的语法排除依赖包   exclusions
例如 为项目大小瘦身的时候 排除不需要的jar 例如jackson
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
 <exclusions>
  <exclusion>
        <groupId>com.fasterxml.jackson.core</groupId>
    </exclusion>
  </exclusions>
</dependency>
```  
##覆盖依赖 通过maven的最短路径寻包的特性 去覆盖已有依赖
例如 需要指定某个包的版本 直接在spring boot所在的pom中添加这个包的依赖即可 
例如 spring boot 的版本依赖的jackson是2.3.3 但是我们需要2.4.3版本 直接在spring boot 的pom下面添加2.4.3 的jackson即可
```
<dependency>
<groupId>com.fasterxml.jackson.core</groupId>
<artifactId>jackson-databind</artifactId>
<version>2.4.3</version>
</dependency>
```
