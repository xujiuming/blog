---
title: spring-boot增强request，response实现在请求前后处理数据笔记
comments: true
categories: 笔记
tags:
  - spring boot
  - servlet
abbrlink: a6660bdf
date: 2019-01-10 11:19:13
---
#### 前言 
在spring boot web项目中 会有一些需求 要求 对 请求之前的信息和返回给请求方信息进行预处理
如 参数、响应信息加密 解密操作 , 记录请求参数和响应信息日志 等操作 
原本的httpServletRequest,httpServletResponse  对这些操作不支持 只能通过 servlet 预留的wrapper进行增强 然后进行预处理 

#### 具体实现
##### 原理
通过 servlet 预留的wrapper类 对原始的 request、response对象进行装饰 
##### 装饰 request示例  
```java

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * request 增强
 *
 * @author ming
 * @date 2018-10-23 01:36:23
 */
@Slf4j
public class MyRequestWrapper extends HttpServletRequestWrapper {


    private byte[] body;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public MyRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            body = IOUtils.toByteArray(request.getInputStream());
        } catch (IOException e) {
            log.error("增强request io异常:{}", request.getRequestURI());
            e.printStackTrace();
        }
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new SignWrapperInputStream(body);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

  
    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    private class SignWrapperInputStream extends ServletInputStream {

        private ByteArrayInputStream buffer;

        public SignWrapperInputStream(byte[] body) {
            body = (body == null) ? new byte[0] : body;
            this.buffer = new ByteArrayInputStream(body);
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new RuntimeException("Not implemented");

        }
    }
}
```
##### 装饰 response示例  
```java

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * 增强 response
 *
 * @author ming
 * @date 2019-01-10 16:23:20
 */
@Slf4j
public class MyResponseWrapper extends HttpServletResponseWrapper {

    private final WrapperServletOutputStream wrapperServletOutputStream = new WrapperServletOutputStream();

    public MyResponseWrapper(HttpServletResponse response) {
        super(response);
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return wrapperServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(wrapperServletOutputStream);
    }


    /**
     * 获取 数组body
     *
     * @author ming
     * @date 2019-01-10 16:23:42
     */
    public byte[] getBodyBytes() {
        return wrapperServletOutputStream.out.toByteArray();
    }

    /**
     * 获取 字符串body    utf-8 编码
     *
     * @author ming
     * @date 2019-01-10 16:23:54
     */
    public String getBodyString() {
        try {
            return wrapperServletOutputStream.out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "[UNSUPPORTED ENCODING]";
        }
    }

    /**
     * 将 body内容 重新赋值到 response 中
     * 由于stream 只读一次  需要重写到response中
     *
     * @author ming
     * @date 2019-01-10 16:24:25
     */
    public void copyToResponse() {
        try {
            getResponse().getOutputStream().write(getBodyBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class WrapperServletOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }


        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }


        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
    }
}
```
##### 在servlet filter中增强 原始的request 和response 
```java

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 签名拦截器
 *
 * @author ming
 * @date 2018-10-23 01:36:06
 */
@Component
@WebFilter(urlPatterns = "/*")
@Slf4j
public class MyFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MyRequestWrapper requestWrapper = new MyRequestWrapper((HttpServletRequest) request);
        //增强 response
        MyResponseWrapper responseWrapper = new MyResponseWrapper((HttpServletResponse) response);
        chain.doFilter(requestWrapper, responseWrapper);
        log.info("请求地址:{},请求的body{}，返回信息:{}", ((HttpServletRequest) request).getRequestURI(), new String(requestWrapper.getBody(), StandardCharsets.UTF_8), responseWrapper.getBodyString());
        //输出 response stream
        responseWrapper.copyToResponse();
    }

    @Override
    public void destroy() {

    }
}

```
##### 启动项目 
启动项目 访问接口 即可打印出info级别的记录日志  
如：
```log
2019-01-10 17:08:22.570  INFO 22029 --- [nio-8080-exec-7] com.only.base.filter.MyFilter            : 请求地址:/api/user,请求的body，返回信息:{"code":-1,"msg":"未知异常"}
```

#### 总结
这个只能在servlet 容器中使用  如果使用的spring boot 2.x 一定要注意 是servlet容器 还是webflux模式的   
本身spring 提供了一些 增强的方式  直接查看  HttpServletRequestWrapper、HttpServletResponseWrapper 这两个类的子类即可 

