---
title: springboot2增强request、response笔记
comments: true
categories: 笔记
tags:
  - spring boot2.x
  - http
  - webflux
abbrlink: 5c640346
date: 2019-10-10 09:31:22
---
#### 前言 
spring boot升级到2.x之后 对于request和response的增强方式发生了变化  
由于spring boot2.x 不仅仅有servlet标准容器 还有一些其他类型的容器  如netty 

#### 实例
##### 增强request
继承ServerHttpRequestDecorator  实现对于 serverHttpRequest的增强 
```java

import com.ming.base.GlobalConstant;
import com.ming.core.utils.DataBufferUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.stream.Collectors;

import static reactor.core.scheduler.Schedulers.single;

/**
 * request 增强
 *
 * @author ming
 * @date 2019-09-04 16:51:41
 */
@Slf4j
@Getter
public class RequestWrapper extends ServerHttpRequestDecorator {


    /**
     * 需要日志的 http访问类型
     */
    public static final ImmutableList<MediaType> NEED_LOG_MEDIA_TYPES = ImmutableList.<MediaType>builder()
            .add(MediaType.TEXT_XML)
            .add(MediaType.APPLICATION_XML)
            .add(MediaType.APPLICATION_JSON)
            .add(MediaType.TEXT_PLAIN)
            .add(MediaType.TEXT_XML)
            .build();
    
    
    private Flux<DataBuffer> body;

    public RequestWrapper(ServerHttpRequest delegate) {
        super(delegate);
        final String path = delegate.getURI().getPath();
        final String query = delegate.getURI().getQuery();
        final String method = Optional.ofNullable(delegate.getMethod()).orElse(HttpMethod.GET).name();
        final String headers = delegate.getHeaders().entrySet()
                .stream()
                .map(entry -> "" + entry.getKey() + ": [" + String.join(";", entry.getValue()) + "]")
                .collect(Collectors.joining("\n"));
        final MediaType contentType = delegate.getHeaders().getContentType();
        log.info("HttpMethod:{},Uri:{},Headers:{}", method, path + (StringUtils.isEmpty(query) ? "" : "?" + query), headers);
        Flux<DataBuffer> flux = super.getBody();
        //指定类型的body才需要打印
        if (NEED_LOG_MEDIA_TYPES.contains(contentType)) {
            body = flux.publishOn(single()).map(dataBuffer -> {
                byte[] bodyByteArr = DataBufferUtils.readDataBuffer(dataBuffer);
                log.info("requestBody:{}", com.ming.core.utils.StringUtils.valueOfByUtf8(bodyByteArr));
                return DataBufferUtils.refillDataBuffer(dataBuffer, bodyByteArr);
            });
        } else {
            body = flux;
        }
    }
}

```
##### 增强response 
继承ServerHttpResponseDecorator 实现增强serverHttpResponse
```java

import com.ming.base.GlobalConstant;
import com.ming.core.utils.DataBufferUtils;
import com.ming.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.scheduler.Schedulers.single;

/**
 * @author ming
 * @date 2019-09-04 17:17:28
 */
@Slf4j
public class ResponseWrapper extends ServerHttpResponseDecorator {
    ResponseWrapper(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        final MediaType contentType = super.getHeaders().getContentType();
        if (GlobalConstant.NEED_LOG_MEDIA_TYPES.contains(contentType)) {
            if (body instanceof Mono) {
                final Mono<DataBuffer> monoBody = (Mono<DataBuffer>) body;
                return super.writeWith(monoBody.publishOn(single()).map(dataBuffer -> {
                    byte[] bodyByteArr = DataBufferUtils.readDataBuffer(dataBuffer);
                    log.info("mono->responseBody:{}", StringUtils.valueOfByUtf8(bodyByteArr));
                    return DataBufferUtils.refillDataBuffer(dataBuffer, bodyByteArr);
                }));
            } else if (body instanceof Flux) {
                final Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;
                return super.writeWith(fluxBody.publishOn(single()).map(dataBuffer -> {
                    byte[] bodyByteArr = DataBufferUtils.readDataBuffer(dataBuffer);
                    log.info("flux->responseBody:{}", StringUtils.valueOfByUtf8(bodyByteArr));
                    return DataBufferUtils.refillDataBuffer(dataBuffer, bodyByteArr);
                }));
            }
        }
        return super.writeWith(body);
    }


}
```
##### 增强exchange
继承 ServerWebExchangeDecorator 实现增强exchange 引用增强的request、response
```java

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * exchange 增强
 *
 * @author ming
 * @date 2019-09-04 17:29:11
 */
public class ExchangeWrapper extends ServerWebExchangeDecorator {
    private RequestWrapper requestDecorator;

    private ResponseWrapper responseDecorator;

    public ExchangeWrapper(ServerWebExchange delegate) {
        super(delegate);
        requestDecorator = new RequestWrapper(delegate.getRequest());
        responseDecorator = new ResponseWrapper(delegate.getResponse());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return requestDecorator;
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}

```
##### 在filter中使用增强的request，response，exchange
实现spring抽象的webFilter 来对请求的exchange request response 进行增强处理 
```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 请求拦截器
 *
 * @author ming
 * @date 2019-04-03 10:57:36
 */
@Configuration
@Slf4j
public class Filter implements WebFilter {
    /**
     * Process the Web request and (optionally) delegate to the next
     * {@code WebFilter} through the given {@link WebFilterChain}.
     *
     * @param serverWebExchange the current server exchange
     * @param chain             provides a way to delegate to the next filter
     * @return {@code Mono<Void>} to indicate when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain chain) {
        //使用 增强 exchange  request response  
        return chain.filter(new ExchangeWrapper(serverWebExchange));
    }

}

```
#### 总结
java web中 spring 框架提供对http的请求响应的包装 默认的功能不是很多    
大部分时候 需要借用spring 框架预留的口子 进行增强来处理一些对http请求的操作   
例如 读取requestBody responseBody 等     