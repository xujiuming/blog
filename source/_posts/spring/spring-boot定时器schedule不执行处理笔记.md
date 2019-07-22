---
title: spring-boot定时器schedule不执行处理笔记
comments: true
categories: 笔记
tags:
  - spring-boot
  - 定时器
  - 坑
abbrlink: 73301d57
date: 2019-07-22 13:36:21
---
####  前言
公司有个使用spring-boot schedule模块的定时任务偶尔不执行  
查看类一些文档和部分源码 发现spring boot schedule模块还是有点坑的 
1:如果是单线程执行 当错过执行时间 就不会在执行任务    并不是想象中的阻塞等待执行     
2:如果有延迟加载  会出现不执行的情况  (未验证)   

#### schedule模块使用 
搭建基础项目参考: {% post_link spring/spring-boot定时器Schedule模块笔记 %}
##### 引起未执行原因
由于 多个任务都定时在凌晨1点执行 
单线程情况下 如果有一个任务执行时间过长 导致其他任务到时间点无法触发 那么这些任务 当天就错过执行时间点  不会在执行
##### 解决办法
将定时任务 多线程执行即可  
###### 实现SchedulingConfigurer 配置定时器线程池
将下面的class 添加进项目中即可  
```java

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 定时器配置
 *
 * @author ming
 * @date 2019-07-22 10:45:56
 */
@Configuration
@Slf4j
public class ScheduleConfig implements SchedulingConfigurer {

    /**
     * 配置 定时器的 线程池
     *
     * @param taskRegistrar ScheduledTaskRegistrar
     * @author ming
     * @date 2019-07-22 11:37:34
     */
    @Override
    
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(new ScheduledThreadPoolExecutor(10,
                new BasicThreadFactory.Builder().namingPattern("Scheduling-%d").daemon(true).build()));
    }


    @Scheduled(fixedRate = 100)
    public void te(){
        log.info(""+System.currentTimeMillis());
    }



    @Scheduled(fixedRate = 10)
    public void te1(){
        log.info(""+System.currentTimeMillis());
    }
}
```
###### 使用 @Async 将定时任务异步化 
代码实例如下: 
```java

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 测试定时任务异步化
 *
 * @author ming
 * @date 2019-07-22 14:11:28
 */
@Component
@Slf4j
public class TestScheduleAsync {

    /**
     * 注册一个 名字为:SchedulePool 线程池
     *
     * @author ming
     * @date 2019-07-22 14:16:44
     */
    @Bean("SchedulePool")
    public ThreadPoolExecutor schedulePool() {
        return new ScheduledThreadPoolExecutor(10,
                new BasicThreadFactory.Builder().namingPattern("Scheduling-%d").daemon(true).build());
    }


    /**
     * 使用 @Async(value = "SchedulePool") 将任务异步化
     *
     * @author ming
     * @date 2019-07-22 14:17:02
     */
    @Async(value = "SchedulePool")
    @Scheduled(fixedRate = 100)
    public void te() {
        log.info("" + System.currentTimeMillis());
    }

    /**
     * 使用 @Async(value = "SchedulePool") 将任务异步化
     *
     * @author ming
     * @date 2019-07-22 14:17:02
     */
    @Async(value = "SchedulePool")
    @Scheduled(fixedRate = 10)
    public void te1() {
        log.info("" + System.currentTimeMillis());
    }

}


```

#### 总结 
在简单的项目中 用定时器 还是 spring boot schedule模块简单好用 
但是它也有一些隐含的点 需要注意一下 例如这个单线程下的坑  
如果项目会有大量的定时任务 还是建议使用成熟的调度中间件 如 xxl-job 、quartz或者 elastic-job
毕竟 成熟的中间件 在并发、重复执行、ha等方面会笔记成熟稳定  


