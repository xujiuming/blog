---
title: spring-data-jpa自定义id生成器笔记
comments: true
categories: 笔记
tags:
  - spring-data-jpa
  - orm
abbrlink: a4a9e9f8
date: 2019-12-12 16:48:17
---
#### 前言
在分布式环境中 一般数据id 都是全局唯一  拥有特定的生成规则   
一般都是从专门的取号中心 取的    
所以jpa中为了全局统一处理 id生成 也提供了扩展方案

#### 示例
##### pom依赖 
这里直接以spring boot starter jpa作为依赖  
```xml
     <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <version>2.2.1.RELEASE</version>
      </dependency>
```

##### 自动生成id
继承 AbstractUUIDGenerator  实现 自定义id生成
```java
package com.ming.base.orm;

import com.ming.common.IdFactory;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.AbstractUUIDGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

/**
 * 自定义id生成器
 *
 * @author ming
 * @date 2019-08-22 11:44:17
 */
@Getter
@ToString
public class MingIdGenerator extends AbstractUUIDGenerator implements Configurable {
    public static final String PREFIX_NAME = "prefix";
    private String prefix;


    /**
     * 生成规则
     *
     * @author ming
     * @date 2019-08-22 11:44:25
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        // 此处修改自己的id生成方法即可 
        return IdFactory.newStringIdByPrefix(o.getClass());
    }

    @Override
    public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry) throws MappingException {
        this.prefix = properties.getProperty(PREFIX_NAME);
    }
}

```

##### 使用自定义id生成器
 @GenericGenerator(name = "mingIdGenerator", strategy = "com.ming.base.orm.MingIdGenerator",parameters = {@Parameter(name = "prefix", value = "inid")})  配置自定义id生成器位置和配置    
 @GeneratedValue(generator = "mingIdGenerator")  指定使用那个id生成器    
```java
package com.ming.base.orm;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 继承映射父类  所有entity 继承这个类
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@Data
@MappedSuperclass
public class InId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "mingIdGenerator")
    @GenericGenerator(name = "mingIdGenerator", strategy = "com.ming.base.orm.MingIdGenerator",
            parameters = {@Parameter(name = "prefix", value = "inid")})
    private String id;
    private LocalDateTime createTime;
    private LocalDateTime lastUpdateTime = LocalDateTime.now();
    @JsonIgnore
    private Boolean isDeleted = Boolean.FALSE;


}

```

> 自定义id生成器配合@MappedSuperclass 可以做到继承映射+自定义生成id  只要引用了InId id都是配置的生成方案 


#### 总结
jpa  我个人很喜欢用  虽然很多人说jpa会比直接写sql慢     
不过我认为jpa如果配置的好  在大多数情况下 并不会慢多少 反而可以利用orm的特性 进行一些优化调整 充分利用硬件资源  
大多数用mybatis的 都是觉得直接写sql简单 但是当项目越来越大的时候mybatis 写的sql调整起来反而更加考验开发的sql功底  而且不同的db 对sql的要求也有所不同  对开发要求就更高了 