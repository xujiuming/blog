---
title: mapstruct笔记
comments: true
categories: 笔记
tags:
  - mapstruct
abbrlink: '1815854'
date: 2022-02-11 16:26:07
---
#### 前言
对象属性转换 方式有很多 
例如 各种beanUtils或者dozer 但是mapstruct 是类似lombok一样 在编译器直接生成性能最高的 直接调用set的方式      
在编译器能够提示大多数的映射异常       

#### 示例 
##### 依赖和搭配lombok使用配置 

```xml
        <lombok.version>1.18.20</lombok.version>
        <mapstruct.version>1.5.0.Beta1</mapstruct.version>
        <lombok-mapstruct-binding.version>0.1.0</lombok-mapstruct-binding.version>
    。。。
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <optional>true</optional>
            </dependency>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>

    。。。 
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <encoding>${encoding}</encoding>
                    <compilerArgs>--enable-preview</compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>${lombok-mapstruct-binding.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
```

##### 相关代码

* 公用类型转换实现类

```java
package com.ming.mapstuct;

import com.ming.core.utils.JSONSingleton;
import com.ming.core.utils.TimeUtils;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Named(GlobalTypeConvertMapping.GLOBAL_TYPE_CONVERT_MAPPING)
public class GlobalTypeConvertMapping {
    public static final String GLOBAL_TYPE_CONVERT_MAPPING = "GlobalTypeConvertMapping";
    public static final String CONVERT_JSON_TO_STRING = "jsonToString";
    public static final String CONVERT_OBJECT_TO_STRING = "objectToString";
    public static final String CONVERT_URI_TO_URL_STRING = "uriToUrlString";
    public static final String CONVERT_TIME_MILLIS_TO_LOCAL_DATE = "timeMillisToLocalDate";
    public static final String CONVERT_LOCAL_DATE_TO_TIME_MILLIS = "localDateToTimeMillis";

    @Named(CONVERT_JSON_TO_STRING)
    public String jsonToString(Object object) {
        return JSONSingleton.writeString(object);
    }

    @Named(CONVERT_OBJECT_TO_STRING)
    public String objectToString(Object object) {
        return object.toString();
    }

    @Named(CONVERT_URI_TO_URL_STRING)
    public String uriToUrlString(URI uri) {
        return uri.getPath();
    }

    @Named(CONVERT_TIME_MILLIS_TO_LOCAL_DATE)
    public LocalDate timeMillisToLocalDate(long timeMillis) {
        return TimeUtils.toLocalDate(timeMillis);
    }

    @Named(CONVERT_LOCAL_DATE_TO_TIME_MILLIS)
    public Long localDateTimeToMillis(LocalDateTime localDateTime) {
        return 20000L;
    }

}
```

* mapper和相关pojo

```java
package com.ming.mapstuct;

import lombok.Data;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * componentModel参数:
 * default: 这是默认的情况，mapstruct 不使用任何组件类型, 可以通过Mappers.getMapper(Class)方式获取自动生成的实例对象。
 * cdi: the generated mapper is an application-scoped CDI bean and can be retrieved via @Inject
 * spring: 生成的实现类上面会自动添加一个@Component注解，可以通过Spring的 @Autowired方式进行注入
 * jsr330: 生成的实现类上会添加@javax.inject.Named 和@Singleton注解，可以通过 @Inject注解获取
 *
 * @author ming
 * @date 2021-11-30 15:32:32
 */
@Mapper(componentModel = MappingConstants.ComponentModel.DEFAULT, uses = GlobalTypeConvertMapping.class)
public interface TestMapper {
    /**
     * 演示类型自动转换 +不同字段映射
     *
     * @author ming
     * @date 2022-02-11 16:57:12
     */
    @Mappings({
            @Mapping(source = "createTime", target = "createTimeStr")
    })
    Demo12 convertDemo12(Demo11 demo11);

    /**
     * 演示没有原始数据的初始化方式
     *
     * @author ming
     * @date 2022-02-11 16:57:44
     */
    @Mappings({
            @Mapping(target = "timeMillis", expression = ("java(System.currentTimeMillis())")),
            @Mapping(target = "flag", constant = "false"),
    })
    Demo22 convertDemo21(Demo21 demo21);

    /**
     * 演示自定义转换方式的字段
     * 有两种方式 隐式和显示
     * 隐式就是直接在当前mapper定义一个类型转换器  mapstruct会自动识别
     * 不过建议显示处理 会更加清晰和明了
     *
     * <p>
     * 此函数显示为隐式 转换
     * {@linkplain this#implicitLocalDateTimeToMillis(LocalDateTime) 隐式转换的函数}
     *
     * </p>
     *
     * @author ming
     * @date 2022-02-11 17:01:02
     */
    @Mapping(source = "dateTime", target = "dateTimeMillis")
    Demo32 implicitConvertDemo31(Demo31 demo31);

    /**
     * 定义localDateTime 转换milli s的函数
     * 提供给当前mapper中的函数使用
     *
     * @author ming
     * @date 2022-02-11 17:07:09
     */
    default Long implicitLocalDateTimeToMillis(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }


    /**
     * 演示自定义转换方式的字段
     * 有两种方式 隐式和显示
     * 隐式就是直接在当前mapper定义一个类型转换器  mapstruct会自动识别
     * 不过建议显示处理 会更加清晰和明了
     *
     * <p>
     * 此函数显示为显示指定
     * {@linkplain this#displayLocalDateTimeToMillis(LocalDateTime) 隐式转换的函数}
     *
     * </p>
     *
     * @author ming
     * @date 2022-02-11 17:01:02
     */
    @Mapping(source = "dateTime", target = "dateTimeMillis", qualifiedByName = "displayLocalDateTimeToMillis")
    Demo32 displayConvertDemo31(Demo31 demo31);

    /**
     * 显示指定转换
     *
     * @author ming
     * @date 2022-02-11 17:12:46
     */
    @Named("displayLocalDateTimeToMillis")
    default Long displayLocalDateTimeToMillis(LocalDateTime localDateTime) {
        return 10000L;
    }

    /**
     * 演示从其他类定义的转换函数
     * 例如项目中统一定义的对某些类型的转换方式
     * 要求:
     * 1: @mapper 配置uses=  GlobalTypeConvertMapping.class
     * 2:使用qualifiedByName 显示引用
     *
     * @author ming
     * @date 2022-02-11 17:13:27
     */
    @Mapping(source = "dateTime", target = "dateTimeMillis", qualifiedByName = GlobalTypeConvertMapping.CONVERT_LOCAL_DATE_TO_TIME_MILLIS)
    Demo32 superExtendsConvertDemo31(Demo31 demo31);
}

@Data
class Demo11 {
    private String name;
    private Integer age;
    private LocalDateTime createTime;
}

@Data
class Demo12 {
    private String name;
    private String age;
    private String createTimeStr;
}

@Data
class Demo21 {
    private String name;
}

@Data
class Demo22 {
    private String name;
    private Long timeMillis;
    private Boolean flag;
}

@Data
class Demo31 {
    private LocalDateTime dateTime;
}

@Data
class Demo32 {
    private Long dateTimeMillis;
}

```

* mapper编译后生成的mapperImpl classes 

> 此代码为mapper编译后生成的 用idea反编译出来看的
> 可以清晰的看到 按照mapping定义的方式 编译成对应的代码  

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.ming.mapstuct;

import java.time.format.DateTimeFormatter;

public class TestMapperImpl implements TestMapper {
    private final GlobalTypeConvertMapping globalTypeConvertMapping = new GlobalTypeConvertMapping();

    public TestMapperImpl() {
    }

    public Demo12 convertDemo12(Demo11 demo11) {
        if (demo11 == null) {
            return null;
        } else {
            Demo12 demo12 = new Demo12();
            if (demo11.getCreateTime() != null) {
                demo12.setCreateTimeStr(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(demo11.getCreateTime()));
            }

            demo12.setName(demo11.getName());
            if (demo11.getAge() != null) {
                demo12.setAge(String.valueOf(demo11.getAge()));
            }

            return demo12;
        }
    }

    public Demo22 convertDemo21(Demo21 demo21) {
        if (demo21 == null) {
            return null;
        } else {
            Demo22 demo22 = new Demo22();
            demo22.setName(demo21.getName());
            demo22.setTimeMillis(System.currentTimeMillis());
            demo22.setFlag(false);
            return demo22;
        }
    }

    public Demo32 implicitConvertDemo31(Demo31 demo31) {
        if (demo31 == null) {
            return null;
        } else {
            Demo32 demo32 = new Demo32();
            demo32.setDateTimeMillis(this.implicitLocalDateTimeToMillis(demo31.getDateTime()));
            return demo32;
        }
    }

    public Demo32 displayConvertDemo31(Demo31 demo31) {
        if (demo31 == null) {
            return null;
        } else {
            Demo32 demo32 = new Demo32();
            demo32.setDateTimeMillis(this.displayLocalDateTimeToMillis(demo31.getDateTime()));
            return demo32;
        }
    }

    public Demo32 superExtendsConvertDemo31(Demo31 demo31) {
        if (demo31 == null) {
            return null;
        } else {
            Demo32 demo32 = new Demo32();
            demo32.setDateTimeMillis(this.globalTypeConvertMapping.localDateTimeToMillis(demo31.getDateTime()));
            return demo32;
        }
    }
}

```


* 测试用例

```java
package com.ming.mapstuct;

import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 测试 mapstruct
 *
 * @author ming
 * @date 2022-02-11 17:25:39
 */
public class Test {
    /**
     * 获取mapper
     * 这里是default模式 直接通过mappers#getMapper获取
     *
     * @author ming
     * @date 2022-02-11 17:26:42
     */
    private TestMapper getMapper() {
        return Mappers.getMapper(TestMapper.class);
    }

    @org.junit.jupiter.api.Test
    public void test1() {
        Demo11 demo11 = new Demo11();
        demo11.setName("ming");
        demo11.setAge(200);
        demo11.setCreateTime(LocalDateTime.now());
        System.out.println("demo11:" + demo11);
        System.out.println("demo12:" + getMapper().convertDemo12(demo11));
    }

    @org.junit.jupiter.api.Test
    public void test2() {
        Demo21 demo21 = new Demo21();
        demo21.setName("ming");
        System.out.println("demo21:" + demo21);
        System.out.println("demo22:" + getMapper().convertDemo21(demo21));
    }

    @org.junit.jupiter.api.Test
    public void test3() {
        Demo31 demo31 = new Demo31();
        demo31.setDateTime(LocalDateTime.now());
        System.out.println("demo31:" + demo31);
        System.out.println("隐式函数转换为时间戳-demo32:" + getMapper().implicitConvertDemo31(demo31));
        System.out.println("显示函数转换固定值为10000-demo32:" + getMapper().displayConvertDemo31(demo31));
        System.out.println("公共uses引用函数转换固定值为20000-demo32:" + getMapper().superExtendsConvertDemo31(demo31));

    }
}
```

* 测试用例执行结果  

```log
demo11:Demo11(name=ming, age=200, createTime=2022-02-11T17:57:09.729450200)
demo12:Demo12(name=ming, age=200, createTimeStr=2022-02-11T17:57:09.7294502)
demo21:Demo21(name=ming)
demo22:Demo22(name=ming, timeMillis=1644573429759, flag=false)
demo31:Demo31(dateTime=2022-02-11T17:57:09.762451900)
隐式函数转换为时间戳-demo32:Demo32(dateTimeMillis=1644573429762)
显示函数转换固定值为10000-demo32:Demo32(dateTimeMillis=10000)
公共uses引用函数转换固定值为20000-demo32:Demo32(dateTimeMillis=20000)
```

#### 总结  
其实使用cglib的BeanCopier 性能也不低  不过一些特殊情况就不好处理     
mapstruct 定义比较清晰 而且编译期间可以提示大多数情况的错误      
就是这玩意跟lombok一样 要影响编译    
如果项目使用了静态增强的aop  配置起来会比较麻烦      






