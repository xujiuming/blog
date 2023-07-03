---
title: 使用jdk工具查看jvm笔记
comments: true
categories: 笔记
tags:
  - jdk
abbrlink: 216c3be8
date: 2020-05-11 11:40:07
---
#### 前言 
有时候线上出问题或者需要对java项目进行优化的时候  需要一些工具对jvm运行状态进行一些查看和分析 
jdk自带了很多工具  
这里对jvm参数查看分析写一篇笔记 方便查阅   至于在线debug 另外的笔记在记录

> 此文档用的工具 以openjdk14版本为准  其他版本大同小异   

查看和分析jvm 主要就是 内存信息、线程信息、gc信息、vm信息这几样  

涉及到的工具:
jps、jmap、jstack 、jstat  
#### 实操  
此处主要以命令行工具为准  至于gui工具 可以直接使用 jconsole 即可  
基本上jconsole 能查看大多数的参数 如vm配置 内存 线程  gc等等   
但是大多数情况下 jconsole无法直接连接服务器 
所以只能用命令行工具来进行查看和分析 


> 使用jps之类的工具可以快速获取jvm进程id（pid）

##### jmap

###### 打印Java类加载器的智能统计信息
对于每个类加载器而言，对于每个类加载器而言，它的名称，活跃度，地址，父类加载器，它所加载的类的数量和大小都会被打印。此外，包含的字符串数量和大小也会被打印。　　 

```shell script
jmap  -clstats  pid
```
![20200511130613](https://xujiuming.com/ming-static/vscode/773dbfd521c794956d1309a15bb2670b.png)

###### 查看等待回收的对象信息
```shell script
jmap -finalizerinfo  pid
```

###### 查看堆的对象统计  
* 查看存活的 live  jvm会gc之后统计  会统计对象数、大小
 ```shell script
  jmap  -histo:live  pid
 ```
  ![20200511132905](https://xujiuming.com/ming-static/vscode/a1b34803a56a79c7bac3a9c49d1a95f3.png)
* 查看所有的 all  
 ```shell script
 jmap -histo:all  pid
 ```
  ![20200511133021](https://xujiuming.com/ming-static/vscode/7922dc0099e997a13e5034c65af2cd4a.png)
* 导出成文件
 ```shell script
   # 存活对象
   jmap -histo:live,file=./histo.data pid 
   # 所有对象 
   jmap -histo:all,file=./histo.data pid 
 ```

###### dump堆到文件 

 ```shell script
 # 存活对象 格式化导出
 jmap -dump:live,format=b,file=./heap.dump 15414
# 所有对象  格式化导出  
 jmap -dump:all,format=b,file=./heap.dump 15414
 ```
 ![20200511133423](https://xujiuming.com/ming-static/vscode/661e4e8325ff8c1babea2b59217c3e52.png)


##### jstat
查看jvm状态的小工具  
> 可查看的维度  jstat -options    

|options|功能|备注|
|:------|:--|:---|
|-class|显示加载class的数量，及所占空间等信息。|| 
|-compiler|显示VM实时编译的数量等信息。|| 
|-gc|显示VM gc的信息。|看gc的次数、耗时等信息| 
|-gccapacity|gc的容量|| 
|-gccause|gc原因|| 
|-gcmetacapacity|gc元数据容量|| 
|-gcnew|新生代gc信息|| 
|-gcnewcapacity|新生代gc容量|| 
|-gcold|老年代gc信息|| 
|-gcoldcapacity|老年代gc容量|| 
|-gcutil|gc工具|| 
|-printcompilation|输出编译信息|| 

```shell script
jstat <options> <pid> 
```
##### jstack 
* 获取当前堆栈  
```shell script
jstack -l pid 
```
> 参考文档 https://blog.csdn.net/zhaozheng7758/article/details/8623535

##### jcmd+jfr 
jfr java运行记录日志  飞行记录器  
利用jfr来检测一段时间内的java程序运行状态  
```shell script
jcmd pid JFR.start duration=60s filename=my.jfr 
jcmd pid JFR.check duration=60s filename=my.jfr 
jcmd pid JFR.stop duration=60s filename=my.jfr 
jcmd pid JFR.dump duration=60s filename=my.jfr
```

> my.jfr文件使用jdk自带的jfr工具打开查看即可   


#### 总结 
jdk自身提供了很多工具来查看 jvm当前的信息 如内存、线程、gc信息  可以辅助查询优化jvm   




