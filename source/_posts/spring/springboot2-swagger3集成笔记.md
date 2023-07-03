---
title: springboot2+swagger3集成笔记
comments: true
categories: 笔记
tags:
  - spring boot2
  - webflux
  - swagger3.x
abbrlink: 14d110a5
date: 2019-09-06 11:30:35
---
#### 前言
swagger用起来还是很爽的  直接内嵌代码中 前端老师也能看到最新的文档 也能直接测试访问 减少撕b的机会 
spring boot1.x 集成swagger2.x 就不用说了  
spring boot2.x 由于多了个 webflux swagger在3.x的时候 也做出适配  
分别为使用webflux 和传统servlet 模式提供适配
#### 实例
##### maven配置
```xml
        <!--由于 swagger3 还未放到maven 中央仓库 只能添加私服来获取jar-->
        <repository>
            <id>jcenter-snapshots</id>
            <name>jcenter</name>
            <url>http://oss.jfrog.org/artifactory/oss-snapshot-local/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <releases>
                <enabled>true</enabled>
            </releases>
        </repository>
        ...
                <!--swagger version -->
                <swagger.version>3.0.0-SNAPSHOT</swagger.version>
        ...        
        
             <dependency>
                    <groupId>io.springfox</groupId>
                    <artifactId>springfox-swagger2</artifactId>
                    <version>${swagger.version}</version>
                </dependency>
                <dependency>
                    <groupId>io.springfox</groupId>
                    <artifactId>springfox-spring-webflux</artifactId>
                    <version>${swagger.version}</version>
                </dependency>
<!--            如果是使用servlet容器 需要将webflux替换为webmvc 配合注解 @EnableSwagger2WebMvc-->
<!--        <dependency>-->
<!--            <groupId>io.springfox</groupId>-->
<!--            <artifactId>springfox-spring-webmvc</artifactId>-->
<!--            <version>${swagger.version}</version>-->
<!--        </dependency>-->
                <dependency>
                    <groupId>io.springfox</groupId>
                    <artifactId>springfox-swagger-ui</artifactId>
                    <version>${swagger.version}</version>
                </dependency>
        
```

* 2020-07-22 更新使用springfox.boot.starter来接入swagger
swagger其他参数不变 依赖改为   
> 版本参考maven中央仓库版本即可: https://mvnrepository.com/artifact/io.springfox/springfox-boot-starter

```xml
    <springfox.boot.version>3.0.0</springfox.boot.version>
    ....
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-boot-starter</artifactId>
            <version>${springfox.boot.version}</version>
        </dependency>
```


##### swagger配置
swagger2.x中启动swagger是使用 @EnableSwagger2注解 
在swagger3.x中拆分为 @EnableSwagger2WebMvc(传统servlet模式) @EnableSwagger2WebFlux(webflux模式) 分别适配不同模式    
```java
package com.ming.base.config;

import com.ming.Start;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

/**
 * swagger config
 *
 * @author ming
 * @date 2019-09-04 14:12:49
 */
@Configuration
@EnableSwagger2WebFlux
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(new ApiInfoBuilder()
                .description("ming 工作台api")
                .title("ming-workbench")
                .version("1.0.0")
                .build()
        )
                .select()
                .apis(RequestHandlerSelectors.basePackage(Start.class.getPackageName()))
                .paths(PathSelectors.any())
                .build()
                ;
    }
}

```

##### 访问
由于引入的有 swagger-ui模块 如果spring boot2.x 没有特殊的修改 直接访问/swagger-ui.html即可  
> http://host:port/swagger-ui.html


#### 异常处理
##### 接口、swagger-ui.html 404
```java
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //注册 swagger 相关页面
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
```

##### 出现提示框 

一般是获取相关接口数据异常 主要看看 是不是被拦截、或者被修改了   

* 接口被拦截器处理  
```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor("拦截器").excludePathPatterns("classpath*:/static/**")
                // swagger 排除规则
                .excludePathPatterns("/swagger-ui.html")
                .excludePathPatterns("/swagger-resources/**")
                .excludePathPatterns("/error")
                .excludePathPatterns("/webjars/**");;
    }

```

* 接口被过滤器处理  
```java
        //如果是排除列表的 uri 前缀 不进行任何操作 只增强
        for (String s : 排除uri列表) {
            if (uri.matches(s)) {
                chain.doFilter(result, response);
                return;
            }
        }
```
* 存在全局的返回值处理  
例如 spring 的controllerAdvice 
跟过滤器类似 直接处理即可 
```java
        //如果是排除列表的 uri 前缀 不进行任何操作 只增强
        for (String s : 排除uri列表) {
            if (uri.matches(s)) {
                chain.doFilter(result, response);
                return;
            }
        }
```
##### 无法启动
* spring plugin core版本异常
提示如下:
```log
2020-06-03 14:01:46.323  INFO 35910 --- [  restartedMain] ConditionEvaluationReportLoggingListener : 

Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
2020-06-03 14:01:46.328 ERROR 35910 --- [  restartedMain] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

An attempt was made to call a method that does not exist. The attempt was made from the following location:

    springfox.documentation.spring.web.plugins.DocumentationPluginsManager.createContextBuilder(DocumentationPluginsManager.java:154)

The following method did not exist:

    'org.springframework.plugin.core.Plugin org.springframework.plugin.core.PluginRegistry.getPluginOrDefaultFor(java.lang.Object, org.springframework.plugin.core.Plugin)'

The method's class, org.springframework.plugin.core.PluginRegistry, is available from the following locations:

    jar:file:/home/ming/.m2/repository/org/springframework/plugin/spring-plugin-core/1.2.0.RELEASE/spring-plugin-core-1.2.0.RELEASE.jar!/org/springframework/plugin/core/PluginRegistry.class

It was loaded from the following location:

    file:/home/ming/.m2/repository/org/springframework/plugin/spring-plugin-core/1.2.0.RELEASE/spring-plugin-core-1.2.0.RELEASE.jar


Action:

Correct the classpath of your application so that it contains a single, compatible version of org.springframework.plugin.core.PluginRegistry

```
解决办法:     
根据提升调整 spring plugin core版本 
```xml
        <dependency>
            <groupId>org.springframework.plugin</groupId>
            <artifactId>spring-plugin-core</artifactId>
            <version>2.0.0.RELEASE</version>
        </dependency>
```

#### 总结   
spring 在进步 swagger 也没有落下    
swagger 在小项目中 非常方便 文档会跟着代码走 减小前后端沟通的成本    

