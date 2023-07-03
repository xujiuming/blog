---
title: spring-boot定时器Schedule模块笔记
comments: true
categories: 笔记
tags:
  - spring boot
  - 定时器
abbrlink: 93aa77f
date: 2018-11-09 09:54:05
---
#### 前言
系统中 定时任务肯定会有 根据项目大小 去正确的选择定时器的相关实现 是非常有必要的
在大型项目中 一般会选择 quartz、elastic job之类的分布式定时框架 或者基于一些环境上的定时功能去实现 如k8s的定时任务、linux系统的cron、timer之类的功能 
但是在一些单体应用中 对定时任务有需求 但是不需要这么强大的功能的时候 一般会选择 基于jdk的相关功能去实现定时器 
或者选择 spring 全家桶中的 schedule模块来实现定时器功能 

#### 相关注解说明
和spring schedule 模块相关的注解 在 org.springframework.scheduling.annotation下 
##### EnableScheduling
* 作用 
标记在启动类上 表示 开启spring schedule功能

##### Scheduled
* 作用 
标记一个函数 并且按照参数去定时调用这个函数 
* 参数

|名称|作用|备注|
|:---|:--|:--|
|cron|使用cron表达式定时执行标记的函数| linux标准格式的cron表达式 |
|zone|配合cron使用配置时区信息|默认为"" 将使用服务器时区||
|fixedDelay|指定固定时长周期执行任务|单位ms 默认 -1 当上一个任务结束开始计算间隔时间  |
|fixedDelayString|指定固定时长周期执行任务 字符串格式|作用跟fixedDelay差不多|
|fixedRate|指定固定时长周期执行|单位ms 默认 -1 当上一个任务开始就开始计算时间间隔|
|fixedRateString|指定固定时长周期执行 字符串格式|作用跟fixedRate差不多 也是从上一个任务开始就开始计算时间间隔|
|initialDelay|设定在第一次执行前延迟的ms时间|单位ms 默认-1|
|initialDelayString|设定在第一次执行前延迟的ms时间 字符串格式|作用和initialDelay 一样|

* 注意   
cron和zone 参数配合使用    
fixedDelay 和fixedRate 要区分是从哪里开始计算时间间隔 例如任务需要在上一个任务执行完成之后间隔n秒执行 就选择fixedDelay 如果是在上一个任务一开始启动间隔n秒再次启动第二个任务那么就要用fixedRate   
有些任务可能需要等待项目启动后延迟执行 那么通过initialDelay 配置 第一次执行间隔即可   

##### Schedules
* 作用
标记一个函数 内部可以配置多个@Scheduled注解 让这个函数使用多种调度策略
* 参数
value @Scheduled[]  
* 使用实例 
```
  @Schedules(value = {@Scheduled(fixedDelay = 10000),@Scheduled(fixedDelay = 13000)})
    public void test(){
        System.out.println("----------------------");
        System.out.println(System.currentTimeMillis());
    }
```
#### 实例
##### 检查依赖
spring boot 项目中 只要引入的 spring-boot-starter-web 就可以类
```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
```
##### 启动类添加注解 
在spring boot 的启动类上 加上@EnableScheduling 表示启动schedule模块 
```

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 * @author ming
 * @date 2018-09-25 15:41:31
 */
@SpringBootApplication
@EnableScheduling
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }
}

```
##### 编写定时器类 
将class注册为spring bean  使用@Scheduled注解 标明改任务执行时间策略
```

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 测试任务
 *
 * @author ming
 * @date 2018-11-09 10:25:09
 */
@Component
@Slf4j
public class TestJob {

    @Scheduled(fixedRate = 1000)
    public void test() {
        System.out.println("测试schedule模块定时器。。。。。");
    }
}

```

#### 总结
spring 的schedule模块 功能是挺简单的
这种简单的任务 又不需要异步执行 而且任务又不是很耗时的 可以用spring 的schedule模块实现 
这个模块还支持 异步执行定时任务等等功能 但是不推荐用 因为你需要这种异步执行的任务的时候 更多的时候 选择更加完善的定时器技术比较好 
例如 quartz 、elastic job这种 毕竟异步执行需要考虑的地方更多 例如并发、任务分片等等问题

