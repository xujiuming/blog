---
title: spring bean 加载顺序解决办法
categories: 实战
tags:
  - spring
abbrlink: 50d1ed2b
date: 2017-11-11 00:00:00
---

在使用SpringBeanManager工具类的时候 发现 spring boot  是根据目录取扫描装配bean 的
由于我把 这个工具类放在com.ming.core.utils下 导致 这个加载顺序在一些初始化服务之后
看了一下相关资料 
有四种解决方法
* 在配置中调整顺序 
先加载的 写在最上面  但是呢 我懒的写配置文件
```
  <!--bean声明-->
    <bean class="com.ming.core.utils.SpringBeanManager"/>
    <bean class="com.ming.base.init.SystemInit" init-method="init" destroy-method="destroy" />
```
* 使用@Autowired 注入到所需的服务中
其实就是跟在配置中写 ref差不多的功能  spring 会解析到这个会依赖springBeanManager 所以会先加载springBeanManager
这种方式 有点丑 但是可以解决问题
```
   @Component
   public class SystemInit {
   
       /**
        *  使 SpringBeanManager在SystemInit之前初始化
        *
        * @author ming
        * @date 2017-11-09 17:52
        */
      @Autowired
       private SpringBeanManager springBeanManager;
   
       @PostConstruct
       public void init() {
           //初始化 script job  bean
           GroovyBeanInit.InitScriptJob();
       }
   }
```
* 使用DependsOn 
这个是要配置一个或者多个 注册的bean的名称  而不是类的名称
```

// 等待springBeanManager 装载完毕  初始化本类
@DependsOn(value = "springBeanManager")
@Component
public class SystemInit {

    @PostConstruct
    public void init() {
        //初始化 script job  bean
        GroovyBeanInit.InitScriptJob();
    }
}
```
* 使用@Order注解
此方式 在spring boot 中暂时无法使用 等找到相关文档 再看看是什么原因 无法使用 
####总结：一般来说 对于加载顺序有强烈要求的地方 不是很多 如果有 使用DependsOn 基本能解决了 
