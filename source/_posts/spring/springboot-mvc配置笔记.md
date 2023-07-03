---
title: spring boot mvc配置笔记
categories: 笔记
tags:
  - spring
abbrlink: cdea8691
date: 2017-11-11 00:00:00
---
#### 配置mvc相关参数的类
常用的 也就是如下三种方法 如果需要使用spring boot 中默认实现 请选则第三种方式 避免覆盖spring boot 实现
* @EnableWebMvc+extends WebMvcConfigurationAdapter，在扩展的类中重写父类的方法即可，这种方式会屏蔽springboot的@EnableAutoConfiguration中的设置
* extends WebMvcConfigurationSupport，在扩展的类中重写父类的方法即可，这种方式会屏蔽springboot的@EnableAutoConfiguration中的设置
* extends WebMvcConfigurationAdapter，在扩展的类中重写父类的方法即可，这种方式依旧使用springboot的@EnableAutoConfiguration中的设置
以上资料参考博客:http://www.voidcn.com/article/p-fpvxvkbi-bpb.html
####常用方式实现
1:拦截器实现
```

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一设定返回包头
 *
 * @author ming
 * @date 2017-11-06 18:15
 */
public class ResponseHandlerInterceptot extends HandlerInterceptorAdapter {

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统一设定 返回编码集
        response.setCharacterEncoding("UTF-8");
    }
}

```
2:注册到拦截器链中
```


/**
 * 拦截器配置
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@Configuration
public class WebInterceptors extends WebMvcConfigurerAdapter {
    public WebInterceptors() {
        super();
    }


    /**
     * 添加拦截器
     *
     * @author ming
     * @date 2017-11-07 10:08
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册统一处理响应包头拦截器
        registry.addInterceptor(new ResponseHandlerInterceptot());
        super.addInterceptors(registry);
    }

    /**
     * 添加允许跨域的请求
     *
     * @author ming
     * @date 2017-11-10 17:07
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //允许所有请求跨域
        registry.addMapping("/**");
        super.addCorsMappings(registry);
    }
  }
```
####总结:追求高度自定义 继承 WebMvcConfigurationSupport  如果想偷懒 那就继承 WebMvcConfigurationAdapter 可以拥有spring boot 默认的配置
