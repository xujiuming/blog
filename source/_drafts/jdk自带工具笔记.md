---
title: jdk自带工具笔记
comments: true
date: 2020-04-30 11:32:29
categories: 笔记
tags:
  - java-tools
  - jdk 
---
#### 前言
jdk自带的一些工具 经常用但是老是现查 很烦  
这次找个机会自己记录下 方便自己速查  
此处只大概记录 各个工具是干什么的  详细用法 另起笔记记录  

> 此处以openjdk-14为例  其他版本jdk可能有自己独有的工具 如oracle jdk 

|名称|功能|备注|
|:--|:--|:---|
|jaotc      |java aot 编译|将java文件编译成二进制|
|jar        |管理jar的工具|管理jar的工具 创建、调整等|
|jarsigner  |jar签名工具|给jar签名|
|java       |运行java字节码,二进制包||
|javac      |java将源码编译成字节码||
|javadoc    |生成javadoc||
|javap      |查看一个java类的反汇编、常量池、变量表、指令代码行号等信息|用来解析class文件|
|jcmd       |给指定java进程执行命令||
|jconsole   |基于jmx监控jvm进程的gui工具||
|jdb        |java在线debug|在线debug工具|
|jdeprscan  |扫描@Deprecated信息||
|jdeps      |显示java类、文件的依赖||
|jfr        |jvm运行记录器 |配合 jcmd使用|
|jhsdb      |连接jvm 进行调试、分析工具||
|jimage     |将java程序打包成image|
|jinfo      |查看当前java程序的扩展参数|
|jjs        |java js终端、引擎||
|jlink      |创建特定于平台的运行时映像||
|jmap       |查看jvm中的内存信息||
|jmod       |创建jomd文件工具||
|jpackage   |打包特定平台的运行文件|win的exe,msi  linux的deb,rpm mac的pkg，dmg |
|jps        |显示当前所有java进程pid的命令||
|jrunscript |java运行脚本的解释器|默认nashorn js引擎|
|jshell     |java的shell|偶尔用来实验一些功能不错|
|jstack     |查看jvm的 stack和native stack||
|jstat      |查看堆内存各部分的使用量，以及加载类的数量。||
|jstatd     |是一个RMI服务器应用程序，主要用于监控HotSpot Java 虚拟机的创建与终止，并提供一个接口以允许远程监控工具附加到本地主机上运行的JVM上。||
|keytool    |ca证书工具||
|rmic       |为使用 Java 远程方法协议 （JRMP） 或 Internet Orb 协议 （IIOP） 的远程对象生成存根、骨架和绑定类。还生成对象管理组 （OMG） 接口定义语言 （IDL）||
|rmid       |启动激活系统守护进程，使对象能够在 Java 虚拟机 （JVM） 中注册和激活。||
|rmiregistry|在当前主机上的指定端口上启动远程对象注册表。||
|serialver  |返回指定类的 serialUID。||

#### 使用示例

> 所有tools 详细的请直接看 help   此示例只做简单的常用的示例 

示例java文件内容: 

```java
/**
 * 测试类
 *
 * @author ming
 * @date 2020-04-30 14:43
 */
public class Test {
    /**
     * 主函数
     *
     * @param args 参数列表
     * @author ming
     * @date 2020-04-30 14:43
     */
    public static void main(String[] args) {
        System.out.println("nihao ming!");
    }
}
```

##### jaotc 
>参考文档: http://openjdk.java.net/jeps/295

把.class文件编译成二进制的文件 
```shell script
#编译成 class文件 
javac ./Test.java
#编译成二进制文件 
jaotc --output Test.so Test.class
#执行文件 
java -XX:+UnlockExperimentalVMOptions -XX:AOTLibrary=./Test.so Test
```
##### jar
管理jar的工具  创建、更新、查看 等
```shell script
#根据class文件创建jar  没有指定清单文件内容 不能直接启动 只能单纯的当个jar依赖使用 
jar cvf ./Test.jar ./Test.class  
```
##### jarsigner
> 参考文档:  https://docs.oracle.com/javase/9/tools/jarsigner.htm#JSWOR-GUID-925E7A1B-B3F3-44D2-8B49-0B3FA2C54864

用来给jar签名和验签的工具 
```shell script
# 生成密钥包  要记住密钥库密码
keytool -genkey -alias ming -keystore ming.keystore -keyalg RSA 
#对jar进行签名 需要输入密钥库的密码
jarsigner -verbose -keystore ming.keystore ./Test.jar ming
# 验签
jarsigner -verify ./Test.jar
```
##### java
运行java的class文件 二进制文件
```shell script
# 编译成class文件
javac Test.java
#执行class文件
java Test
#编译成二进制文件 
jaotc --output Test.so Test.class
#执行文件 
java -XX:+UnlockExperimentalVMOptions -XX:AOTLibrary=./Test.so Test
```
##### javac
编译java源码成为class文件
```shell script
javac Test.java
```
##### javadoc
提取代码中java doc 生成doc 
```shell script
# 生成一系列 html 基于javadoc格式生成的doc
javadoc ./Test.java
```
##### javap
>参考文档: https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javap.html

反编译字节码 
```shell script
javap ./Test.class
```

##### jcmd
> 参考文档: 
>https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jcmd.html
>http://www.manongjc.com/article/7558.html
```shell script
# 列出本机 java进程
jcmd -l 
# 列出指定jvm进程能够执行的操作指令
jcmd pid help
# 输出堆栈信息 
jcmd pid Thread.print
```
##### jconsole
java管理 jvm进程的gui工具  直接打开连接即可  
##### jdb
> 在线debug工具  https://www.cnblogs.com/rocedu/p/6371262.html
```shell script
# 进入 jdb终端
jdb 
> help
```
##### jdeprscan
> 参考文档: https://docs.oracle.com/en/java/javase/11/tools/jdeprscan.html

扫描@Deprecated信息
```shell script
jdeparscan ./Test.class
```

##### jdeps
> 参考文档: https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html

查看java 依赖
```shell script
jdeps ./Test.class 
```

##### jfr
> 参考文档: https://docs.oracle.com/javacomponents/jmc-5-4/jfr-runtime-guide/run.htm#JFRUH178

java 飞行记录器
* 启动jvm时候 使用  
```shell script
java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=my.jfr  ./Test.class 
```
* 使用jcmd 启动关闭、停止、dump文件
```shell script
jcmd pid JFR.start duration=60s filename=my.jfr 
jcmd pid JFR.check duration=60s filename=my.jfr 
jcmd pid JFR.stop duration=60s filename=my.jfr 
jcmd pid JFR.dump duration=60s filename=my.jfr 
```
##### jhsdb
> 参考文档： 
>https://www.jianshu.com/p/92931e6466b3
>https://docs.oracle.com/en/java/javase/12/tools/jhsdb.html

debug工具 
```shell script
#打开gui工具 
jhsdb hsdb 
```
##### jimage

##### jinfo
##### jjs
##### jlink
##### jmap
##### jmod
##### jpackage
##### jps
##### jrunscript
##### jshell
##### jstack
##### jstat
##### jstatd
##### keytool
##### rmic
##### rmid
##### rmiregistry
##### serialver



#### 总结
没啥说的  都是要用的  
