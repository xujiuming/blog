---
title: spring resourc笔记
categories: 笔记
tags:
  - spring
abbrlink: d7cd3a26
date: 2017-11-11 00:00:00
---

##1:resource接口
###### Resource是spring 中加载资源的顶级接口 主要方法有:
* boolean exists(); 是否存在资源
* boolean isOpen(); 资源是否打开 是否开启
* URL getURL(); 返回可抽象成url的资源的URL对象
* FIle getFile()；返回底层资源的文件流
* InputStream getInputStream;返回资源的输入流 

   
###### resource接口关系图：
![image.png](http://upload-images.jianshu.io/upload_images/3905525-b6f7babb70039c85.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* WritableResource： 可写资源接口
* ByteArrayResource:二进制数组表示资源
* ClassPathResource:类路径下资源
* FileSystemResource:文件系统中的资源(绝对路径)
* InputStreamResource：用输入流返回表示资源
* ServletContextResource: web容器上下文资源
* UrlResource:能够访问任何能用URL表示的资源(文件系统、http、ftp等 )
* PathResource:java7.0以上 能够访问任何通过URL、Path、系统文件路径表示的资源  


###### spring可以通过资源地址的特殊标识符来访问相应资源如下表 支持Ant风格去描述资源地址
![image.png](http://upload-images.jianshu.io/upload_images/3905525-c959b0858c8911c5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
```
在项目中 会出现 'classpath:*.xml' 和'classpath*:*.xml'  在：之前有个* 这个意思是在所有的目录下面寻找  
举个例子: 一个项目分为 a(web模块)、b(common模块)、core(核心模块)
如果不带*  那么只会加载一个模块的配置  其它模块配置就没加载  
所以 写的时候 带上*   比较合适 
```


###### 资源加载
spring 提供一套加载资源的方法  
如下图：
![image.png](http://upload-images.jianshu.io/upload_images/3905525-3da84466dc8b60e0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 通过ResourceLoader来加载Resource
有一个getResource(String location) 只支持按照特殊字符自动匹配 resource类型 但是不支持Ant风格
* ResourcePatternResolver  扩展ResourceLoader
有一个getResources(String locationPattern) 支持按照特殊字符自动匹配、支持Ant匹配资源
* PathMatchingResourcePatternResolver 是spring提供的实现类 如果有必要 可以自己实现 (我觉得自己实现的可能性很小、但是不排除坑爹的地方需要自己实现)
##总结:
###### 1:特殊字符 classpath、file这样的是spring 提供的 其中classpath 最好使用的时候带上* 避免加载不到资源 
###### 2:Ant风格 其实就是
* ' ? '代表一个任意字符 
*  ' * '代表匹配文件中的任意多个字符  
*  ' ** ' 代表匹配多层路径
###### 3: Resource 系列可以单独抽出来 当作加载资源的utils来用 
###### 4: Resource操作文件的时候 尽量使用getInputStream()  因为getFile()不能加载到jar中的资源
