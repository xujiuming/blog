---
title: spring-boot2全局异常捕获webfluxFilter级别笔记
comments: true
categories: 笔记
tags:
  - webflux
  - spring-boot2
abbrlink: 62d81c3
date: 2020-04-27 15:52:25
---
#### 前言
在spring mvc中 使用@ControllerAdvice + @ExceptionHandler 可以做到mvc级别的异常拦截  
但是涉及到容器级别 如tomcat  或者netty构建web容器中 无法拦截filter级别的异常 

mvc级别的全局异常拦截笔记: {% post_link spring/springboot-mvc返回全局处理异常 %}     

#### 示例 
参考文档：   
https://www.xttblog.com/?p=3592  此文档在新版本中存在异常 没有注册messageWriters 

##### 重写异常处理 
跟参考文档一致 但是不注册到spring 中  

```java
package com.ming.base;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

/**
 * 全局异常处理器
 * handler级别
 *
 * @author ming
 * @date 2020-04-27 16:01
 */
@Slf4j
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {


    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), request -> this.renderErrorResponse(request, errorAttributes.getError(request)));
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request, Throwable e) {
        //输出异常堆栈信息
        e.printStackTrace();
        log.error("全局异常拦截:{}", e.getMessage());
        //为了避免 网络供应商、dns解析商 拦截  此处全部返回200    后续上https之后这里可以正常返回
        return ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ImmutableMap.builder()
                        .put("code", -1)
                        .put("msg", e.getMessage())
                        .build()));
    }

}
```
   

##### 注册全局异常处理器     


```java
 /**
     * 注册异常处理器
     * <p>
     * Order注解的值必须小于-1  因为ErrorWebFluxAutoConfiguration#errorWebExceptionHandler order为-1
     *
     * @author ming
     * @date 2020-04-27 16:05
     */
    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                             ResourceProperties resourceProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        GlobalErrorWebExceptionHandler exceptionHandler = new GlobalErrorWebExceptionHandler(errorAttributes,
                resourceProperties, applicationContext);
        //必须手动设置 下面三项配置 特别是messageWriters 
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }
```

#### 总结    
异常的处理 要分清是mvc层级 还是web server层级   
如果只需要处理 mvc级别的异常 使用@ControllerAdvice+@ExceptionHandler即可   
如果要处理server级别 需要去捕获更高范围的异常 进行处理    