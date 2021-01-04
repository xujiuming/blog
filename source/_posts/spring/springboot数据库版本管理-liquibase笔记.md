---
title: springboot数据库版本管理-liquibase笔记
comments: true
categories: 笔记
tags:
  - db版本管理
  - liquibase
abbrlink: 489cdd4e
date: 2021-01-04 14:32:42
---
#### 前言
数据版本管理 很多工具  java主要有 liquibase flyway   python的SQLAlchemy 
liquibase 和flyway 各有侧重点  flyway胜在简单粗暴  直接管理sql脚本  
liquibase 胜在可以通过配置去管理db版本  可以不用写单一平台的sql脚本  如果项目用jpa 之类的  orm框架 那么更加适合liquibase 去管理  
今天有时间  顺手记录一下 方便自己速查      
#### 示例 

基于spring boot 2.x     

##### 增加liquibase依赖 

* gradle  
```text
implementation 'org.liquibase:liquibase-core'
```

* maven 
```xml
   <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-core</artifactId>
   </dependency>
```

##### 配置liquibase  

* 直接通过yaml配置   
```yaml
spring:
  # 所有配置参考 org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties  
  liquibase:
    # 开启liquibase 启动加载
    enabled: true
    # liquibase db变更配置地址 
    change-log: 'change/master.xml'
```

* 自定义liquibase配置
```yaml
spring:
  # 所有配置参考 org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties  
  liquibase:
    # 关闭spring boot 默认的 liquibase配置  
    enabled: false
```
```java
package com.ming.base.orm;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;

import javax.sql.DataSource;

/**
 * 配置 liquibase
 *
 * @author ming
 * @date 2020-12-29 15:18
 */
@Configuration
public class SpringLiquibaseConfig {

    @Bean
    public SpringLiquibase configLiquibase(DataSource dataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setChangeLog("classpath*:liquibase/master.xml");
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setShouldRun(true);
        springLiquibase.setResourceLoader(new DefaultResourceLoader());
        springLiquibase.setDatabaseChangeLogTable("database_change_log");
        springLiquibase.setDatabaseChangeLogLockTable("database_change_log_lock");
        return springLiquibase;
    }
}
```

##### 定义liquibase 数据库变更配置
在 liquibase目录下建立 master.xml 内容如下    
```xml
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
    <changeSet id="5" author="ming">
        <insert tableName="staff">
            <column name="id" value="1"/>
            <column name="name" value="ming"/>
            <column name="password" value="ming"/>
            <column name="title_image_url" value="https://xujiuming.com/img/logo.jpeg"/>
            <column name="create_time" value="2020-03-10 12:00:00"/>
            <column name="last_update_time" value="2020-03-10 12:00:00"/>
            <column name="deleted" value="false"/>
        </insert>
    </changeSet>
</databaseChangeLog>
```

> liquibase 支持 xml yaml  json sql 等等配置格式 具体可以自由选择   
> https://www.liquibase.org/

##### 启动项目
当包含liquibase相关任务执行的日志 则代表liquibase 已经接入完成 可以直接使用了  
```text
2021-01-04 14:48:20.928 [main] INFO  liquibase.lockservice.StandardLockService- Successfully acquired change log lock
2021-01-04 14:48:21.125 [main] INFO  l.changelog.StandardChangeLogHistoryService- Creating database history table with name: PUBLIC.database_change_log
2021-01-04 14:48:21.128 [main] INFO  l.changelog.StandardChangeLogHistoryService- Reading from PUBLIC.database_change_log
2021-01-04 14:48:21.149 [main] INFO  liquibase.changelog.ChangeSet- New row inserted into staff
2021-01-04 14:48:21.150 [main] INFO  liquibase.changelog.ChangeSet- ChangeSet classpath*:liquibase/master.xml::5::ming ran successfully in 3ms
2021-01-04 14:48:21.156 [main] INFO  liquibase.lockservice.StandardLockService- Successfully released change log lock
```

#### 总结 
liquibase 我个人喜欢用 使用起来会比flyway繁琐      
胜在可以通过配置 去变更不同db的结构  当管理多种数据库的时候不用写不同方言的sql     
flyway 有时间我也会记录一下 那个比较简单 就是按照规则编写sql的文件名 即可    


