---
title: springboot-使用druid连接池笔记
comments: true
categories: 笔记
tags:
  - druid
  - springboot
abbrlink: 86d1c1cf
date: 2022-02-15 10:31:23
---
#### 前言 
druid连接池 没啥好说的 的确好用 虽然极限性能可能跟 hikari低点  但是功能多啊 
各种基本sql监控 、扩展等等  

>  https://github.com/alibaba/druid
> https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98

#### 实例 

> 例子在spring boot项目中引用 

##### 依赖 

```xml
     <druid.starter.version>1.2.8</druid.starter.version>
。。。。
     <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.starter.version}</version>
        </dependency>
```

#####  spring datasource配置 

> web ui地址 http://xxx.xx/spring-boot-druid

```yaml
spring:
  datasource:
    # jdbc classs 名称
    driver-class-name: 
    # jdbc url   
    url: 
    username: 
    password: 
    # 使用druid data source 
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      min-idle: 5
      max-active: 15
      # 配置filter  
      filter:
        stat:
          enabled: true
      #        wall:
      #          enabled: true
      #          dbType: mysql

      web-stat-filter:
        enabled: true
      #      filters: stat,wall
      filters: stat
      stat-view-servlet:
        enabled: true
        url-pattern: /spring-boot-druid/*
        reset-enable: false
        #Sorry, you are not permitted to view this page.
        #deny优先于allow，如果在deny列表中，就算在allow列表中，也会被拒绝。
        #如果allow没有配置或者为空，则允许所有访问
        allow:
```

##### 删除druid监控ui页面的广告 

```java
package com.ming.base.mvc;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import java.io.IOException;


/**
 * https://gblfy.blog.csdn.net/article/details/100619385
 *
 * @author ming
 * @date 2020-11-15 11:30
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
@ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true", matchIfMissing = true)
public class RemoveDruidAdConfig {


    /**
     * 删除druid web监控 底部广告
     *
     * @param properties properties
     * @return FilterRegistrationBean
     * @author ming
     * @date 2020-11-15 11:31
     */
    @Bean
    @SuppressWarnings("unchecked")
    public FilterRegistrationBean removeDruidAdFilterRegistrationBean(DruidStatProperties properties) {
        // 获取web监控页面的参数
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // 提取common.js的配置路径
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");

        final String filePath = "support/http/resources/js/common.js";

        //创建filter进行过滤
        Filter filter = new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                chain.doFilter(request, response);
                // 重置缓冲区，响应头不会被重置
                response.resetBuffer();
                // 获取common.js
                String text = Utils.readFromResource(filePath);
                // 正则替换banner, 除去底部的广告信息
                text = text.replaceAll("<a.*?banner\"></a><br/>", "");
                text = text.replaceAll("powered.*?shrek.wang</a>", "");
                response.getWriter().write(text);
            }

            @Override
            public void destroy() {
            }
        };
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }

}

```

##### 自定义druid filter 

* 继承FilterEventAdapter 实现自定义filter 

> FilterEventAdapter 扩展了一些前置后置操作的函数 一般都继承这个来实现filter   

```java
package com.ming.core.orm;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author ming
 * @date 2022-01-06 10:31:23
 */
@Slf4j
public class JdbcConnectionFilter extends FilterEventAdapter {
    @Override
    public void connection_connectBefore(FilterChain chain, Properties info) {
        log.info("jdbc-connection之前。。。。。。");
        super.connection_connectBefore(chain, info);
    }

    @Override
    public void connection_connectAfter(ConnectionProxy connection) {
        log.info("jdbc-connection之后。。。。。。");
        super.connection_connectAfter(connection);
    }

}

```

* 注册filter  

在META-INF目录下增加 druid-filter.properties  

> 如果是maven管理  直接在resources下增加META-INF/druid-filter.properties   

```properties
# 此处指定一个druid的filter名称为 my  class地址为com.ming.core.orm.JdbcConnectionFilter
druid.filters.my=com.ming.core.orm.JdbcConnectionFilter
```

* 启用名称为my的 filter 

```properties
spring.datasource.druid.filters=my
```


#### 总结 
druid 好用 官方文档也写的很清楚 
这里只是作为速查使用       
