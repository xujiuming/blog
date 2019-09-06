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
                <dependency>
                    <groupId>io.springfox</groupId>
                    <artifactId>springfox-swagger-ui</artifactId>
                    <version>${swagger.version}</version>
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

#### 总结   
spring 在进步 swagger 也没有落下    
swagger 在小项目中 非常方便 文档会跟着代码走 减小前后端沟通的成本    

