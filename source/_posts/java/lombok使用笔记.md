---
title: lombok使用笔记
comments: true
categories: 笔记
tags:
  - lombok
  - 实用
abbrlink: '3186e368'
date: 2019-05-22 17:31:35
---
#### 前言 
写java 特别是在写业务类功能的时候 会产生大量DTO、VO、PO类似的pojo类  而且还经常容易发生变更  
每次发生变更 都需要调整相关setter、getter、toString、hashCode、eq等相关基础函数 比较麻烦 而且容易出错 
这个时候  lombok就可以来简略大量这种体力工作 
#### lombok介绍
lombok官网:https://projectlombok.org/     
lombok 是一个java类库 配合一些编辑器插件 在编译期间为java一些基础pojo类添加一些公共的代码生成等工作   
##### lombok使用前提 
1: 项目引入lomobkjar 
```xml
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${maven仓库最新的稳定版本即可}</version>
        </dependency>
```
2：编辑器安装lombok插件 
不安装插件 编辑器不识别通过lombok相关注解生成的pojo 无法识别setter、getter等相关功能
idea 直接搜索 lombok插件安装即可 

#### lombok常用注解介绍 
lombok注解很多 功能比较多 此处只介绍 常用的 及其作用 
其实常用的 就@Data @Builder @Slf4j   
非常用的可以取官网查询官方文档 

|注解名称|功能|备注|
|:-----|:---|:--|
|@Data|为pojo类增加setter、getter、toString、Eq、hashCode、和必须的参数的构造函数|一般的pojo类只用这个注解即可|
|@Getter|为pojo类增加getter|-|
|@Setter|为pojo类增加setter|-|
|@ToString|为pojo类增加toString|-|
|@EqualsAndHashCode|为pojo类增加hashCode和equals函数|-|
|@Builder|为pojo类增加一个构建者模式的初始化函数|无法处理继承的属性|
|@Slf4j|为当前类增加一个slf4j的logger属性字段 名称为log|需要打日志的类 用这个注解 、它有类似的注解例如 @Log4j、@Log4j2等等|


#### 代码示例
1:@Data
注解在类上 为pojo类提供标准的常用的函数 
```java
import lombok.Data;

/** 测试 lombok  @Data
 * @author ming
 * @date 2019-05-23 11:19:15
 */
@Data
public class TestData {
    private  String id;
    private Integer age;
}
```
2:@Slf4j
注解在类上 提供 一个logger  名称为log 
```java
import lombok.extern.slf4j.Slf4j;

/** 测试 lombok  slf4j
 * @author ming
 * @date 2019-05-23 11:19:15
 */
@Slf4j
public class TestSfl4j {

    public static void main(String[] args) {
        log.info("lombok-slf4j");
    }
}

```
3：@Builder
注解在类上 提供一个构建者模式的初始化pojo类的函数 
```java
import lombok.Builder;

/**
 * 测试 lombok  Builder
 *
 * @author ming
 * @date 2019-05-23 11:19:15
 */
@Builder
public class TestBuilder {
    private String id;
    private Integer age;

    public static void main(String[] args) {
        TestBuilder t = TestBuilder.builder()
                .id("asdf")
                .age(111)
                .build();
    }
}
```

#### 总结 
通过lombok 可以大量减少 java程序中的必须但是重复的代码 使代码更加美观、整洁
也避免了一些低级错误 例如加字段 忘记重写 setter getter  toString  eq  hashCode等函数 
当然现在 groovy 这种jvm平台上的语言 可以跟java混合编写 也可以利用混合编写的模式减少大量的重复工作  
