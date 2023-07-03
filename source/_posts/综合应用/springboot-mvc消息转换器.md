---
title: springboot-mvc的messageConverter处理
comments: true
categories: 实战
tags:
  - spring boot
  - mvc
  - 实战
  - messageConverter
abbrlink: '3174607'
date: 2018-05-22 17:30:48
---
#### 前言
最近项目中通过feign调用内部服务和其他系统服务猛然增多  
在messageConverter这一环节出现n多问题   
什么xml解析gg
什么json 时间格式解析gg 
什么乱七八糟的骚格式的数据解析gg  
导致最近一直在作在messageConverter这一块的处理 
但是feign 的在messageConverter  有一部分是从spring mvc 的在messageConverter列表中获取的
所以说总的来说 是要处理spring mvc中在messageConverter的维护   
#### spring boot消息转换器配置
spring boot 的mvc配置 优先使用 继承WebMvcConfigurerAdapter 方式 
这样既可以拥有 大量的默认配置 也可以在一定程度上自定义配置
##### 重写configureMessageConverters
* 无法保证顺序 也不能清理其他的消息转换器 
重写这个方法 可以添加自己编写的 messageConverter  到 默认的messageConverter列表中 
messageConverter如何编写 直接参考 AbstractHttpMessageConverter的下层实现即可 
这里使用MappingJackson2HttpMessageConverter作为演示
```
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.core.utils.SpringBeanManagerUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 拦截器配置
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper mapper = SpringBeanManagerUtils.getBeanByType(Jackson2ObjectMapperBuilder.class).build();
        // ObjectMapper为了保障线程安全性，里面的配置类都是一个不可变的对象
        // 所以这里的setDateFormat的内部原理其实是创建了一个新的配置类
        DateFormat dateFormat = mapper.getDateFormat();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        MappingJackson2HttpMessageConverter mappingJsonHttpMessageConverter = new MappingJackson2HttpMessageConverter(
                mapper);
        converters.add(mappingJsonHttpMessageConverter);
    }
}
```
##### 重写extendMessageConverters
* 可以操控完整的 messageConverter列表 增加、删除等等
重写这个方法 方便控制到整个messageConverter 列表的顺序内容  
这里示例删除所有的 messageConverter列表   
增加一个简单的简单的FastJsonHttpMessageConverter作为mvc中 唯一一个转换器  
保证messageConverter顺序 调整list的先后顺序 即可
```

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.core.utils.SpringBeanManagerUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 拦截器配置
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        converters.add(fastJsonHttpMessageConverter);        
    }
```
#### messageConverter 详细规则
spring boot 中 所有的消息转换器 需要继承 AbstractHttpMessageConverter<T>抽象类 
根据其中的方法进行修改 达到转换效果 
* canRead()
判断转换器能不能将请求内容转换成java对象
* canWrite()
判断转换器能不能将java对象转换成返回内容
* read()
读取请求内容转换成java对象
* write()
将返回的java对象写入到返回内容
* getSupportedMediaTypes()
获取这个转换器支持的MediaType类型



#### 总结  
控制spring mvc 对于http请求的内容和响应的内容的转换 直接通过重写 messageConverter即可  
如果只是为了增加特殊情况的处理可以直接重写configureMessageConverters在原有的messageConverter 列表中添加一个接口   
如果需要完整控制 messageConverter的数量和顺序 那么需要重写extendMessageConverters 来达到控制messageConverter的数量和顺序  

feign会从spring mvc的messageConverter的列表中获取对应的messageConverter列表   
那么这样来说 其实可以通过严格控制spring mvc的messageConverter列表来达到控制feign的messageConverter列表  

###### MediaType解释
```
spring 中对于http contentType的一些枚举类型 类的全限定名org.springframework.http.MediaType
```