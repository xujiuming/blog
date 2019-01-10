---
title: spring-boot-tomcat容器和nginx配置ca证书笔记
comments: true
categories: 笔记
tags:
  - spring boot
  - nginx
  - ca证书
  - https
abbrlink: d95c5e4e
date: 2019-01-09 14:31:15
---
#### 前言
由于要提升安全性和b格  
有些项目 访问的时候 必须要求 https协议来访问 
一般 情况下 nginx是外层的入口 或者 直接是tomcat 是入口 只要配置nginx和tomcat 即可
#### 证书获得方式 
##### 证书类型介绍
https://www.wosign.com/faq/faq2016-0216-02.htm 
###### 自签证书
1: 使用openssl 自签证书 生成 .key .crt文件
参考: https://www.jianshu.com/p/280de4af8c00
```bash
sudo openssl req -x509 -nodes -days 36500 -newkey rsa:2048 -keyout ./nginx.key -out ./nginx.crt
```
2: 使用jdk的keytool 生成jks 格式的证书
```bash
keytool -genkey -alias was -keyalg RSA -keypass 123456 -storepass 123456 -keysize 2048 -keystore ./ssl.jks
```

###### 购买证书
1: aliyun 提供免费的单域名证书 或者 购买其它类型证书 
都提供 适配 nginx、tomcat 的证书格式下载 
https://cn.aliyun.com/product/cas

#### nginx 配置 
##### 生成 证书
```bash
sudo openssl req -x509 -nodes -days 36500 -newkey rsa:2048 -keyout ./nginx.key -out ./nginx.crt
```
##### 配置nginx 
将 生成的.key、.cer 文件移动到 /etc/nginx/conf.d目录下 
```text
  server {
    #port
    listen       80;
    listen       443 ssl;
    #ssl
  #  ssl on ;
    ssl_certificate /etc/nginx/conf.d/nginx.crt;
    ssl_certificate_key /etc/nginx/conf.d/nginx.key;
    # ssl session 过期配置
    ssl_session_timeout  30m;
    #官方提供的协议配置
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    #官方套件配置
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    ssl_prefer_server_ciphers on;
    # nginx 容器 静态文件目录
    root   /usr/share/nginx/html;
    location / {
      add_header Cache-Control no-cache;
      try_files $uri $uri/ /index.html;
    }
    #error_page
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
      root   /usr/share/nginx/html;
    }
  }
```
这种配置 同时支持 http 和https 访问  
如果要做 http 强制转化 https 访问 参考: https://www.cnblogs.com/kevingrace/p/6187072.html
##### 重启nginx 
```bash
nginx -s reload
```
重启之后访问 http://xxxxx.com  或者 https://xxxxx.com 即可 

####  spring boot 配置jks格式证书  
##### 生成jks证书 
```bash
keytool -genkey -alias was -keyalg RSA -keypass 123456 -storepass 123456 -keysize 2048 -keystore ./ssl.jks
```
##### 增加配置
```yaml
# @see  HttpsConnectorConfig
https:
  ssl:
    #端口
    port: 8443
    #密钥文件地址
    key-store: "ca/ssl.jks"
    #密钥 密码
    key-store-password: "123456"
    # 密钥类型
    key-store-type: JKS
```
##### 增加spring boot 嵌入的tomcat 增加connector
读取 添加的配置  建立 tomcat https connector
```java

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 配置 https  connector
 *
 * @author ming
 * @date 2018-12-25 13:32:21
 */
@Configuration
@DependsOn("environment")
public class HttpsConnectorConfig {
    @Autowired
    private Environment environment;

    /**
     * 创建https  tomcat server
     *
     * @return TomcatServletWebServerFactory
     * @author ming
     * @date 2018-12-25 14:28:37
     */
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createSslConnector());
        return tomcat;
    }


    /**
     * 创建 ssl connector
     *
     * @return Connector
     * @author ming
     * @date 2018-12-25 14:29:05
     */
    private Connector createSslConnector() {
        try {

            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(Integer.valueOf(Objects.requireNonNull(environment.getProperty("https.ssl.port"))));
            protocol.setSSLEnabled(true);
            File keyStore = ResourceUtils.getFile(Objects.requireNonNull(environment.getProperty("https.ssl.key-store")));
            protocol.setKeystoreFile(keyStore.getAbsolutePath());
            protocol.setKeystorePass(Objects.requireNonNull(environment.getProperty("https.ssl.key-store-password")));
            protocol.setKeystoreType(Objects.requireNonNull(environment.getProperty("https.ssl.key-store-type")));
            return connector;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("建立ssl connector失败！！！");
    }
}
```
##### 启动 spring boot 项目 
当出现如下日志 代表配置 https 成功 
```text
2019-01-09 16:44:32.963  INFO 22214 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) 8443 (https) with context path ''
2019-01-09 16:44:32.973  INFO 22214 --- [  restartedMain] com.only.Start                           : Started Start in 60.364 seconds (JVM running for 61.924)
```
访问 http://xxxx.com:8080  或者访问 https://xxxx.com:8443 即可   

#### 证书格式
不同的服务器对证书的格式要求不太一样 例如 tomcat 一般就是要.jks格式  nginx 一般是crt cer格式的 

参考:https://blog.freessl.cn/ssl-cert-format-introduce/

#### 总结
自从 chrome 对http协议的网站进行明显警告后  站点 还是需要搞个证书 提升提升安全性和b格的
配置 证书 相对来说 问题不多 很多文章、博客都有描述  

