---
title: jvm钩子笔记
comments: true
categories: 笔记
tags:
  - jvm
  - 优雅停机
abbrlink: d61be55e
date: 2019-04-16 10:57:53
---
#### 前言
jvm提供关闭时候触发某些操作的钩子 
通过钩子取检测触发某些操作 例如实现优雅停机、通知gc等等操作 
在做无感发布的时候 必须要用的一项功能 
钩子只能在jvm正常关闭 例如System.exit(),或者kill -15 的时候 
如果是 ctrl+c 、kill -9 方式关闭 无法触发钩子  
#### 参考文章
详细介绍钩子的一些细节处理 https://blog.csdn.net/dd864140130/article/details/49155179
kill和信号量的说明 https://www.cnblogs.com/liuhouhou/p/5400540.html

#### 实例
##### hook编写 
直接写一个类或者直接用匿名内部类都行   
```java
package com.ming.core.shutdown;

/**
 * jvm关闭钩子
 *
 * @author ming
 * @date 2019-04-16 11:16:18
 */
public class MingShutdownHook extends Thread {

    @Override
    public void run() {
        System.out.println("shutdown hook start 。。。。。。。。");
        //当触发钩子操作 执行清理操作  这里使用进程休眠10s模拟
        // 真实情况 请处理具体的资源 如数据库连接池是否还存在运行中的线程、各种资源是否处理完毕等
        try {
            Thread.sleep(10* 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("shutdown hook error !"+e.getMessage());
        }


        System.out.println("shutdown hook end 。。。。。。。。");
    }
}
```
##### 注册钩子 
直接通过获取runtime add hook即可 
```java
package com.ming;

import com.ming.core.shutdown.MingShutdownHook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启动类
 * 开启缓存 使用 cglib代理
 *
 * @author ming
 * @date 2019-03-28 10:00:28
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class Start {

    public static void main(String[] args) {
        //注册钩子
        //单独编写一个hook 类
        Runtime.getRuntime().addShutdownHook(new MingShutdownHook());
        //使用匿名内部类
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("two shutdown hook")));
        SpringApplication.run(Start.class, args);
    }

}

```

##### 测试结果 
* kill -9 <pid>
 jcmd |grep com.ming.Start 获取当前jvm的pid 
 kill -9 <pid>
 执行结果如下 
```text
2019-04-16 11:26:31.738  INFO 51137 --- [  restartedMain] com.ming.Start                           : Started Start in 8.513 seconds (JVM running for 10.4)
Disconnected from the target VM, address: '127.0.0.1:37049', transport: 'socket'
```
idea上看到启动成功后 执行 kill -9 直接就关闭jvm 没有触发钩子 
* kill -15 <pid>
 jcmd |grep com.ming.Start 获取当前jvm的pid 
 kill -15 <pid>
 执行结果如下 
```text
2019-04-16 11:31:56.324  INFO 51526 --- [  restartedMain] com.ming.Start                           : Started Start in 10.322 seconds (JVM running for 12.57)
shutdown hook start 。。。。。。。。
shutdown hook start 。。。。。。。。
two shutdown hook
two shutdown hook
2019-04-16 11:32:16.198  INFO 51526 --- [      Thread-14] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2019-04-16 11:32:16.202  INFO 51526 --- [      Thread-14] com.zaxxer.hikari.HikariDataSource       : ming-hikari - Shutdown initiated...
2019-04-16 11:32:16.262  INFO 51526 --- [      Thread-14] com.zaxxer.hikari.HikariDataSource       : ming-hikari - Shutdown completed.
shutdown hook end 。。。。。。。。
shutdown hook end 。。。。。。。。
Disconnected from the target VM, address: '127.0.0.1:39627', transport: 'socket'
```
* System.exit()
调整启动类代码
```java
package com.ming;

import com.ming.core.shutdown.MingShutdownHook;

/**
 * 启动类
 *
 * @author ming
 * @date 2019-03-28 10:00:28
 */
public class Start {

    public static void main(String[] args) {
        //注册钩子
        //单独编写一个hook 类
        Runtime.getRuntime().addShutdownHook(new MingShutdownHook());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("two shutdown hook")));
        System.exit(0);
    }

}

```
```text
two shutdown hook
shutdown hook start 。。。。。。。。
shutdown hook end 。。。。。。。。

Process finished with exit code 0

```

#### 总结
根据jvm提供的添加shutdown hook函数 我们可以根据自己的需要 增加jvm正常关闭时候的资源处理 做到平稳停机 
避免某些任务还在执行中 就强制关闭了  
如果是jvm直接被强制关闭了 那就没办法了  



