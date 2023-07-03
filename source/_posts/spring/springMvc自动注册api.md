---
title: springMvc自动注册api
comments: true
categories: 实战
tags:
  - spring
abbrlink: d0321adc
date: 2022-05-11 15:18:19
---
#### 前言   
懒得自己手写api      
干脆让他根据包名、类名 函数名自动生成算了     

#### 思路    
* 自定义注解 注册到spring ioc容器中     
* 借助原本requestMapping及其衍生的注解来为接口除了地址以外的属性做处理  如method  consumer 等参数   
* 使用RequestMappingHandlerMapping来搭配 相关注解 来构建 api      

#### 实战   
##### 注解     
```java
package com.ming.base.mvc.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动控制器
 *
 * @author ming
 * @date 2021-10-09 09:45:12
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@ResponseBody
public @interface AutoController {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
```
##### 自动注册    
```java
package com.ming.base.mvc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.ming.base.mvc.annotation.AutoController;
import com.ming.core.utils.JSONSingleton;
import com.ming.core.utils.MyStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 自动初始化controller
 * 只在指定包名 && RestController注解下的 bean才自动注册
 *
 * @author ming
 * @date 2021-09-29 15:49:47
 */
@Component
@Slf4j
public class ControllerAutoRegister implements ApplicationContextAware {

    /**
     * 包名前缀
     */
    private static String PREFIX_PACKAGE_NAME = "com.ming.controller";
    private static ImmutableList<String> DEFAULT_METHOD_NAME_LIST = ImmutableList.<String>builder()
            .add("view")
            .add("delete")
            .add("save")
            .build();
    /**
     * spring bean上下文
     *
     * @author ming
     * @date 11:00
     */
    private ApplicationContext applicationContext;
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @PostConstruct
    public void init() {
        log.info("auto register api......");
        //扫描所有的restController
        List<String> beanNameList = Lists.newArrayList(applicationContext.getBeanNamesForAnnotation(AutoController.class));
        log.info("scan rest controller number:{}", beanNameList.size());
        //删除非指定前缀的 bean
        beanNameList.removeIf(r -> !applicationContext.getBean(r).getClass().getPackageName().startsWith(PREFIX_PACKAGE_NAME));
        log.info("register controller number:{}", beanNameList.size());
        for (String beanName : beanNameList) {
            registerMapping(beanName);
        }
    }

    private boolean registerMapping(String beanName) {
        Object obj = applicationContext.getBean(beanName);
        Class<?> objClass = obj.getClass();
        List<Method> methodList = Lists.newArrayList(objClass.getDeclaredMethods());
        //删除静态函数
        methodList.removeIf(r -> Modifier.isStatic(r.getModifiers()));
        //删除私有函数
        methodList.removeIf(r -> Modifier.isPrivate(r.getModifiers()));

        String classSimpleName = objClass.getSimpleName().replace("Controller", "").replace("Entity", "");
        //构建包路径
        String prefix = "/api" + objClass.getPackageName().replace(PREFIX_PACKAGE_NAME, "").replace(".", "/");
        //构建class路径
        prefix = prefix + "/" + MyStringUtils.toLowerCaseJoiner(classSimpleName, "-");
        for (Method method : methodList) {
            //构建mapping
            RequestMethod[] requestMethods = buildRequestMethod(method);
            String path = prefix;
            if (!DEFAULT_METHOD_NAME_LIST.contains(method.getName())) {
                path = path + "/" + MyStringUtils.toLowerCaseJoiner(method.getName(), "-");
            }
            //按需实现对应的 mapping 注册即可 这里可以自由调整
            RequestMappingInfo.Builder requestMappingInfoBuilder = RequestMappingInfo
                    .paths(path)
                    .produces(MediaType.APPLICATION_JSON_VALUE)
                    .methods(requestMethods);

            RequestMappingInfo requestMappingInfo = requestMappingInfoBuilder.build();
            requestMappingHandlerMapping.registerMapping(requestMappingInfo, beanName, method);
            log.info("register api :【{}】{},{}", StringUtils.join(requestMethods, ","), JSONSingleton.writeString(requestMappingInfo.getDirectPaths()), objClass.getName() + "#" + method.getName());
        }
        return true;
    }

    /**
     * 构建请求参数
     *
     * @param method
     * @author ming
     * @date 2021-10-09 15:03:43
     */
    private RequestMethod[] buildRequestMethod(Method method) {
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (requestMapping != null) {
            return requestMapping.method();
        } else {
            return new RequestMethod[]{RequestMethod.GET};
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

```
##### 使用方式      
```java
package com.ming.controller;

import com.ming.base.mvc.annotation.AutoController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@AutoController
public class TestController {

    public String get() {
        return "xxxxxxxxxxxxxxxxx";
    }


    @PutMapping
    public String put() {
        return "nnnnnn";
    }

    @PostMapping
    public String post() {
        return "nnnnnn";
    }

    @PatchMapping
    public String patch() {
        return "nnnn";
    }

    @DeleteMapping
    public String delete() {
        return "nnnn";
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public String options() {
        return "nnnnn";
    }
}
```

##### 查看注册api列表    
直接看输出日志       
```text
2021-10-11 11:12:35.433 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- auto register api......
2021-10-11 11:12:35.439 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- scan rest controller number:1
2021-10-11 11:12:36.257 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- register controller number:1
2021-10-11 11:12:36.284 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- register api :【GET】["/api/test/get"],com.ming.controller.TestController#get
2021-10-11 11:12:36.284 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- register api :【PUT】["/api/test/put"],com.ming.controller.TestController#put
2021-10-11 11:12:36.285 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- register api :【DELETE】["/api/test"],com.ming.controller.TestController#delete
2021-10-11 11:12:36.285 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- register api :【OPTIONS】["/api/test/options"],com.ming.controller.TestController#options
2021-10-11 11:12:36.285 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- register api :【PATCH】["/api/test/patch"],com.ming.controller.TestController#patch
2021-10-11 11:12:36.286 [restartedMain] INFO  com.ming.base.mvc.ControllerAutoRegister- register api :【POST】["/api/test/post"],com.ming.controller.TestController#post
```   

#### 总结
为了偷懒 直接依托于class的 包名 类名  函数名来避免重复 构建api    
免得自己去自定义使用   
要用自动构建就用指定注解 要用mvc的标准注解 也可以接着使用 互不影响     
方便开发        

