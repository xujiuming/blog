---
title: rsocket笔记
comments: true
categories: 笔记
tags:
  - tcp
  - websocket
  - rsocket
abbrlink: f14ec8f7
date: 2022-01-26 14:02:15
---
#### 前言
最近看websocket 和tcp的一些应用协议定义的时候 看到有个rsocket协议  
spring社区、阿里等 都在试水和研究扩展这个协议     
干脆自己玩玩  记录一下 方便自己后续查阅  

#### 介绍
> 参考文档:
> https://docs.spring.io/spring-framework/docs/5.3.15/reference/html/web-reactive.html#rsocket-spring
> https://rsocket.io/

rsocket是一个上层协议 可以基于tcp、udp\(Aeron\)、websocket、http/2-stream等方式做传输    
提供统一的双向通信方式  提供request-response\(请求一次响应一次\)、Request Fire-n-Forget\(只请求不要求响应\)、request-stream\(请求一次-多次响应\)、Request-channel\(打开一个双向通道\)几种方式请求
提供常规的响应式操作 例如背压、会话恢复、租赁等等操作   
理论上rsocket协议是一个响应式的传输协议有很多优点  具体的参考官网即可    


#### 实战 
> rsocket 应该是两个都算server端  这里方便区分  server作为被动端  client作为主动端 
> 为了方便 使用spring boot rsocket + 测试用例方式来演示    


##### 依赖和配置  
> 实例使用tcp作为rsocket底层协议    使用spring boot rsocket框架  

```xml
    。。。 version 直接在继承最新的   当前使用的版本是: spring boot 2.5.7
     <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-rsocket</artifactId>
        </dependency>
        <dependency>
            <groupId>io.rsocket</groupId>
            <artifactId>rsocket-transport-local</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.rsocket.broker</groupId>
            <artifactId>rsocket-broker-common-spring</artifactId>
            <version>0.3.0</version>
        </dependency>
```

##### server端实现
* 启动类
  就是一个常规的spring boot 项目启动类  

```java
package com.ming.rsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StartRsocket {

    public static void main(String[] args) {
        SpringApplication.run(StartRsocket.class, args);
    }
}
```

* rsocket端点实现

```java
package com.ming.rsocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Controller
@Slf4j
public class TestRsocketController {

    @MessageMapping("test-rsocket-req-res")
    public Mono<String> requestResponse(Mono<String> message) {
        log.info("测试rsocket req-res模式:{}", message);
        return Mono.just("测试rsocket req-res模式," + message);
    }

    @MessageMapping("test-rsocket-req-stream")
    public Flux<String> requestStream(Mono<String> message) {
        log.info("测试req-stream模式:{}", message);
        return Flux.interval(Duration.ofSeconds(1)).map(m -> "测试req-stream模式" + m + ":" + message + ":" + LocalDateTime.now());
    }

    @MessageMapping("test-rsocket-fire-and-forget")
    public Mono<Void> fireAndForget(Mono<String> message) {
        log.info("测试fire-and-forget模式:{}", message);
        return Mono.empty();
    }

    @MessageMapping("test-rsocket-channel")
    public Flux<String> channel(Flux<String> message) {
        return message.map(m -> "测试channel模式:" + m + ":" + LocalDateTime.now());
    }

    @MessageExceptionHandler
    public Mono<String> handlerException(Exception e) {
        return Mono.just(e.getMessage());
    }
}
```

* rsocket配置 

```yaml
spring:
  rsocket:
    server:
      # rsocket 端口 
      port: 20000
      # rsocket 底层协议选择  
      transport: tcp
```

##### client端实现

```java
package com.ming.rsocket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class TestRsocketClient {
    RSocketRequester requester = RSocketRequester.builder().tcp("localhost", 20000);

    @Test
    public void testReqRes() {
        String str = requester.route("test-rsocket-req-res")
                .data("fffffffffffff")
                .retrieveMono(String.class)
                .block();
        System.out.println(str);
    }

    @Test
    public void testReqStream() {
        requester.route("test-rsocket-req-stream")
                .data("fffffffffffffffffffffffffff")
                .retrieveFlux(String.class)
                .map(m -> {
                    System.out.println(m);
                    return m;
                })
                .blockLast();

    }

    @Test
    public void testFireAndForget() {
        requester.route("test-rsocket-fire-and-forget")
                .data("fffffffffffffffffffffffffffffffffffff")
                .send()
                .block();
    }


    @Test
    public void testChannel() {
        Flux flux = Flux.interval(Duration.ofSeconds(1)).map(String::valueOf);
        requester.route("test-rsocket-channel")
                .data(flux)
                .retrieveFlux(String.class)
                .log()
                .blockLast();
    }
}
```

##### rsocket报文样式

```text
Frame => Stream ID: 1 Type: REQUEST_FNF Flags: 0b100000000 Length: 79
Metadata:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| fe 00 00 1d 1c 74 65 73 74 2d 72 73 6f 63 6b 65 |.....test-rsocke|
|00000010| 74 2d 66 69 72 65 2d 61 6e 64 2d 66 6f 72 67 65 |t-fire-and-forge|
|00000020| 74                                              |t               |
+--------+-------------------------------------------------+----------------+
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 66 66 66 66 66 66 66 66 66 66 66 66 66 66 66 66 |ffffffffffffffff|
|00000010| 66 66 66 66 66 66 66 66 66 66 66 66 66 66 66 66 |ffffffffffffffff|
|00000020| 66 66 66 66 66                                  |fffff           |
+--------+-------------------------------------------------+----------------+
```

#### 开启另一个端口的rsocket服务

> 因为roscket server 需要指定 实现协议内容 如果要同时支持 tcp  websocket之类的操作 
> 那么必然要手动开启一个端口来处理  

```java
package com.ming.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.rsocket.context.RSocketServerBootstrap;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

/**
 * rsocket server配置
 *
 * @author ming
 * @date 2021-12-27 13:51:44
 */
@Configuration
@Slf4j
public class RSocketServerTcpConfig implements CommandLineRunner {

    @Autowired
    private ReactorResourceFactory resourceFactory;
    @Autowired
    private ObjectProvider<RSocketServerCustomizer> customizers;
    @Autowired
    private RSocketMessageHandler rSocketMessageHandler;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        NettyRSocketServerFactory factory = new NettyRSocketServerFactory();
        factory.setResourceFactory(resourceFactory);
        factory.setTransport(RSocketServer.Transport.TCP);
        factory.setPort(30000);
        factory.setRSocketServerCustomizers(customizers.orderedStream().toList());
        log.info("start tcp rsocket server ...........");
        RSocketServerBootstrap rSocketServerBootstrap = new RSocketServerBootstrap(factory, rSocketMessageHandler.responder());
        rSocketServerBootstrap.setApplicationEventPublisher(applicationEventPublisher);
        rSocketServerBootstrap.start();
    }


}
```

#### 总结
暂时只随便玩玩  深入的背压、租赁什么的 要用的时候 再深入看看  

