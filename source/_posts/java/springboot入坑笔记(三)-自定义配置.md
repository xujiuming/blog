---
title: spring boot (三)笔记
categories: 笔记
tags:
  - java
  - spring
abbrlink: 241784d
date: 2017-11-11 00:00:00
---
###spring boot中 在某些配置的时候肯定是需要自定义配置的  spring boot 默认显示配置覆盖默认配置 所以只需要符合spring boot的配置环境
###能够覆盖默认配置的配置位置
1:命令行参数(如直接命令行启动某个jar  后面附带的命令)
2:jndi配置
3:jvm配置
4:操作系统环境变量
5:随机生成带random.*的属性(并不是很明白 以后看懂了 在更新)
6:应用程序的application.properties 或者 application.yml
7:通过@PropertySource注解标注的bean
######按照顺序覆盖  优先级从1-7 依次降低 就是说 当命令行指定了某个配置 后面在怎么写 也无法改变 因为     命令行的参数优先级最高
######如果 有的配置上述几个位置没有配置则采用spring boot 的默认配置

###application文件位置
1:外置 应用程序的运行目录的/config目录
2:外置 应用程序运行目录
3:内置 jar或者war的/config目录内
4:内置 jar或者war的根目录 
######也是从1-4 优先级依次降低、yml配置 覆盖 properties中的配置

###在程序中使用配置中的参数
@ConfigurationProperties 
这个注解可以去配置中寻找某些前缀的参数 
如 配置中有 
```
my.name=xianyu
my.sex=boy
```
那么可以在某个bean中注入 通过setter注入
···
@ConfigurationProperties(prefix="my")
public class myProperties{
  private String name;
private String sex;
setter.getter 。。。。
}
···










