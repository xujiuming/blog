---
title: springboot依赖代码分离打包笔记
comments: true
categories: 笔记
tags:
  - spring-boot
  - 优化
abbrlink: a2982f5f
date: 2020-04-22 11:07:57
---
#### 前言
spring boot 默认打包方式为fatjar 依赖包+源码一起打包 这种方式简单粗暴 
不过在一些需要减少更新文件大小的地方 还是需要拆分处 lib和源码 
#### 示例
参考网站:
1. https://blog.csdn.net/u013314786/article/details/81120240
2. https://maven.apache.org/plugins/maven-jar-plugin/
3. http://maven.apache.org/plugins/maven-dependency-plugin/plugin-info.html
##### pom.xml配置
```xml
 <!-- 指定启动类，将依赖打成外部jar包 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <!-- 生成的jar中，不要包含pom.xml和pom.properties这两个文件 -->
                        <addMavenDescriptor>false</addMavenDescriptor>
                        <manifest>
                            <!-- 是否要把第三方jar加入到类构建路径 -->
                            <addClasspath>true</addClasspath>
                            <!-- 外部依赖jar包的最终位置 -->
                            <classpathPrefix>lib/</classpathPrefix>
                            <!-- 项目启动类 -->
                            <mainClass>com.ming.Start</mainClass>
                            <!--强制可以使用快照版本-->
                            <useUniqueVersions>false</useUniqueVersions>
                        </manifest>
                        <manifestEntries>
                            <Class-Path>./</Class-Path>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <!--拷贝依赖到jar外面的lib目录-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <executions>
                    <execution>
                        <id>copy-lib</id>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-dependencies</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>target/lib</outputDirectory>
                            <!--不重写lib  如果存在lib不处理-->
                            <overWriteIfNewer>false</overWriteIfNewer>
                            <includeScope>compile</includeScope>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```

> 如果有引用快照版本的jar  必须配置 useUniqueVersions = false 否则生成的MANIFEST.MF中依赖配置会有快照版本的后缀 跟lib中的jar的名字不匹配     
> 需要配置overWriteIfNewer = false  因为docker打包的时候是按照整个文件夹中的文件计算hash值来判断是否命中缓存的 如果复写了lib 那么hash值也变了 无法命中缓存   

这样打包出来的 所有依赖在target/lib包下 代码 还是打包成xxx.jar

##### 启动
```shell script
java -jar ./xxx.jar  
```

##### 附带的MANIFEST.MF 格式 
```text
Manifest-Version: 1.0
Archiver-Version: Plexus Archiver
Created-By: Apache Maven 3.6.2
Built-By: 构建的系统用户名
Build-Jdk: 构建的jdk版本
Class-Path: 依赖包的路径 
Main-Class: 启动类
```



#### 总结
总的来说就是 自定义打包的方式 还是一样的操作
控制MANIFEST.MF 的内容就行了   
如果出现依赖有问题或者无法加载 检查一下路径 和MANIFEST.MF的配置看看 


