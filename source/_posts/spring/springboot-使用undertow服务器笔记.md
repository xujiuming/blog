---
title: springboot-使用undertow服务器笔记
comments: true
abbrlink: 44144cfb
date: 2022-02-11 18:22:46
categories: 笔记 
tags:
 - undertow 
 - springboot 
---
#### 前言 
spring boot 默认内嵌是tomcat
如果只是想最简单提升一下性能  可以把tomcat 换成 undertow   
至于哪里强 可以看看一些web服务器对比  undertow还是很能打的   

> https://undertow.io/ 


#### 示例  

##### 依赖 

```xml
        <spring-boot.version>2.5.6</spring-boot.version>
。。。
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <!-- Import dependency management from Spring Boot -->
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-dependencies</artifactId>
                    <version>${spring-boot.version}</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>

        <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                    <exclusions>
                        <!--排除默认依赖的tomcat -->
                        <exclusion>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-tomcat</artifactId>
                        </exclusion>
                    </exclusions>
                </dependency>
                <!--引用undertow依赖-->
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-undertow</artifactId>
                </dependency>
```

#####  application.yml配置

```yaml
server:
  # 提供默认的80 然后实现访问http自动跳转到https  
  http:
    port: 80
  port: 443
  ssl:
    key-store-type: JKS
    key-store: "classpath:{证书名字}.jks"
    key-password: 证书密码
    enabled: true
    # access 日志  
    accesslog:
      enabled: true
      dir: .
      pattern: combined
  # 服务器配置    
  undertow:
    threads:
      # 默认为处理器数量 按需调整  一般为逻辑核心数就行   
      io: 4
      # 一般配置为 io*8  这里是手动调整了一下        
      worker: 128
  # 压缩配置     
  compression:
    enabled: true
```

##### 配置undertow 

```java
package com.ming.base.config;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.SecurityInfo;
import io.undertow.servlet.api.TransportGuaranteeType;
import io.undertow.servlet.api.WebResourceCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true", matchIfMissing = false)
public class UndertowConfig {

    @Autowired
    private Environment environment;

    @Bean
    public ServletWebServerFactory embeddedServletContainerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        // 这段就可以可以转换为http2
        factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true));
        //这段可以增加http重定向，如果只需要http2的话下面的代码可以去掉
        factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
            @Override
            public void customize(Undertow.Builder builder) {
                builder.addHttpListener(getHttpPort(), "0.0.0.0");
            }
        });
        //下面这段是将http的8080端口重定向到https的8443端口上
        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            deploymentInfo.addSecurityConstraint(new SecurityConstraint()
                            .addWebResourceCollection(new WebResourceCollection()
                                    .addUrlPattern("/*")).setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL)
                            .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT))
                    .setConfidentialPortManager(exchange -> getHttpsPort());
        });
        log.info("deploy https {}!", getHttpsPort());
        return factory;

    }

    private int getHttpPort() {
        return environment.getProperty("server.http.port", int.class, 8080);
    }

    private int getHttpsPort() {
        return environment.getProperty("server.port", int.class, 8443);
    }

}

```


##### 启动spring  boot 

```java
package com.ming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动
 * 开启全局缓存
 *
 * @author ming
 * @date 2021-11-12 11:30:50
 */
@SpringBootApplication()
public class Start {
    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }
}
```

##### 启动成功的日志 开启了 443 和80

```log
。。。
:::2022-02-14 10:41:59.693 [main] INFO  o.s.boot.web.embedded.undertow.UndertowWebServer- Undertow started on port(s) 443 (https) 80 (http)
```

#### 总结
undertow jboss出品 
没啥毛病   
