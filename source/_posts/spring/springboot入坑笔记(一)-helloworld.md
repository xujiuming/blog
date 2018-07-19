---
title: spring boot (一)笔记
categories: 笔记
tags:
  - java
  - spring
abbrlink: 1d0e6366
date: 2017-11-11 00:00:00
---
##步骤
1:安装spring boot cli  
2:利用spring boot cli初始化项目
3:写一个helloworld控制器
##1:安装spring boot cli
spring boot cli是spring boot的一个操作工具 可以直接利用这个工具去生成 管理spribg boot项目
在这里去寻找合适的版本下载解压 添加到系统路径
http://repo.spring.io/release/org/springframework/boot/spring-boot-cli/
安装方式有两种 
1:类似jdk安装方法 下载spring boot cli 解压包 将目录下的bin添加到系统路径中
2:利用gvm(Groovy的工具去管理):
安装gvm 之后
```
gvm install springboot
spring --version
```
##2:利用spring boot cli 初始化项目 
spring boot cli 初始化项目 是从网络上下载一个基本demo来初始化的
```
spring init -dweb,data-jpa,h2,thymeleaf 
```
会初始化spring mvc jpa  h2 等  组成一个基本的spring +mvc+jpa+h2数据库的一个基本项目   默认是maven 如果需要gradle来进行项目管理 在语句后加上 --build gradle即可
##3:hellwworld控制器 
就是写个spring mvc的控制器
```
@RestController
public class TestController {

    @RequestMapping("/test")
    public String test(){
        return "test springboot";
    }
}
```
控制器访问地址为:http://localhost:8080/test
###总结:spring boot 就是spring 等相关框架一个快速版  默认设置已经够用 自定义配置覆盖默认配置 
