---
title: aop和spel实现动态注解操作日志
comments: true
categories: 实战
tags:
  - aop
  - spel
abbrlink: 4d536e6a
date: 2021-09-24 10:55:07
---
#### 前言
懒的硬编码操作日志  然后大多数操作日志 也比较固定 
看了看网上别人的实现方案  
发现大多数用 aop + spel 来实现动态的注解记录操作日志
不过大多数没有讲到
aop如何增强private 或者当前class中的函数 和相关完整的代码  
这里自己也写了一套 做一个记录 方便自己速查  

#### 实战
##### 注解 
> 如果是静态增强模式 注解可以加 LOCAL_VARIABLE类型 不过在实现aop切面要调整一下 

```java
package com.ming.base.aop.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志 注解
 * content 支持spel 表达式
 * 两种用法实例
 * 1:直接注解在函数上
 * 2:新建立一个无返回值的空函数加上注解  然后在局部调用
 *
 * @author ming
 * @date 2021-09-22 11:35:49
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface OperatorLog {

    String name();

    String content() default "";
}

```

##### 切面实现 
```java
package com.ming.base.aop;

import com.ming.base.aop.annotation.OperatorLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作日志aop
 *
 * @author ming
 * @date 2021-09-22 13:44:48
 */
@Component
@Aspect()
@Slf4j
public class OperatorLogAop {

    /**
     * spel 表达式解析器
     */
    private final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
    /**
     * 参数名发现器
     */
    private final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 拦截  OperatorLog注解
     * execution(* *(..)) + anno 模式 来避免生成的字节码 重复增强
     *
     * @author ming
     * @date 2021-09-22 13:46:19
     */
    @Pointcut("execution(* *(..)) && @annotation(com.ming.base.aop.annotation.OperatorLog)")
    public void pointcut() {

    }


    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        Method method = methodSignature.getMethod();
        OperatorLog operatorLog = method.getAnnotation(OperatorLog.class);
        String content = "";
        if (StringUtils.isNotBlank(operatorLog.content())) {
            content = parseSpel(method, joinPoint.getArgs(), operatorLog.content());
        }
        log.info("操作日志AOP--->name:{},content:{}", operatorLog.name(), content);
    }


    /**
     * 解析 spel 表达式
     *
     * @param method    方法
     * @param arguments 参数
     * @param spel      表达式
     * @return 执行spel表达式后的结果
     */
    private String parseSpel(Method method, Object[] arguments, String spel) {
        String[] params = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], arguments[len]);
        }
        try {
            Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(spel);
            return expression.getValue(context).toString();
        } catch (Exception e) {
            log.error("解析spel失败!", e);
        }
        return "";
    }
}

```
##### aspectj静态增强

>静态增强 使用ajc编译器 不是javac  就是在编译器直接生成相关字节码来进行增强 这种方式 可以增强private 或者当前class中的切面

```xml


    <properties>
        。。。
        <aspectj.maven.plugin.version>1.14.0</aspectj.maven.plugin.version>
        <aspectjrt.version>1.9.8.M1</aspectjrt.version>
        <aspectjtools.version>1.9.8.M1</aspectjtools.version>

    </properties>


    <dependencies>
        。。。
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>${aspectjrt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>${aspectjtools.version}</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
    。。。

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>${aspectj.maven.plugin.version}</version>
                <dependencies>
                    <dependency>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjrt</artifactId>
                        <version>${aspectjrt.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjtools</artifactId>
                        <version>${aspectjtools.version}</version>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>compile</goal>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <showWeaveInfo/>
                    <forceAjcCompile>true</forceAjcCompile>
                    <sources/>
                    <weaveDirectories>
                        <weaveDirectory>${project.build.directory}/classes</weaveDirectory>
                    </weaveDirectories>
                    <aspectLibraries>
                    </aspectLibraries>
                    <complianceLevel>${java.version}</complianceLevel>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
```

##### 使用示例  
* 源码 
```java
    @Override
    @OperatorLog(name = "登录", content = "'fff' + #loginRequest.username")
    public String login(LoginRequest loginRequest) {
        operatorLog(loginRequest.getUsername());
        return null;
    }

    @OperatorLog(name = "test", content = "'test' + #name ")
    private void operatorLog(String name) {
    }
```
* 编译之后的字节码反编译的内容  
```java
    @OperatorLog(
        name = "登录",
        content = "'fff' + #loginRequest.username"
    )
    public String login(LoginRequest loginRequest) {
        JoinPoint var2 = Factory.makeJP(ajc$tjp_0, this, this, loginRequest);
        OperatorLogAop.aspectOf().before(var2);
        this.operatorLog(loginRequest.getUsername());
        return null;
    }

    @OperatorLog(
        name = "test",
        content = "'test' + #name "
    )
    private void operatorLog(String name) {
        JoinPoint var2 = Factory.makeJP(ajc$tjp_1, this, this, name);
        OperatorLogAop.aspectOf().before(var2);
    }
```

#### 总结   
使用aop + Spel 实现动态的增强的时候 一定要考虑 是否存在其他方式的切面 是否要切入当前class中的函数 是否会调用私有函数 等等aop中的问题     
还有就是构建spel 的context的时候 是否能获取到所有切入点的相关参数 等问题      
aop动态代理进行增强的时候 必然会遇到 调用当前class 或者私有函数无效的情况 那么就要适当考虑其他hack方式处理了    
例如静态增强 或者重写相关字节码代理增强的方式  
