---
title: java加载so、dll笔记
comments: true
categories: 笔记
tags:
  - java
  - jni
  - dll
  - so
abbrlink: e3f6bc96
date: 2023-07-11 16:42:35
---
#### 前言  
在一些比较不同的公司中 没有提供java的sdk   需要调用dll  so库等 需要用java-jni来调用     
需要手动加载对应的lib到 内存中
#### 示例   

> 例如linux下加载  xxx.so 库    

##### os中so、dll直接加载   
> 直接在os目录中的 直接加载就是的    

```java
//当前 库已经加入到系统环境变量中 直接按照名称加载就行
System.loadLibrary("xxx");
//指定绝对路径导入 
System.load("/home/ming/xxx.so");
```

##### jar中的so、dll加载    
既然是在java中  肯定是想打包进jar中 方便分发           
这里以 maven管理项目的方式来演示        
将 xxx.so 放进resources目录下  也就是 classLoader.getResource\("")      
然后在启动加载的时候 直接从jar中将库文件复制到os目录中 然后加载   

```java
package com.ming.test;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 加载linux下的so库
 * <a href="https://stackoverflow.com/questions/1611357/how-to-make-a-jar-file-that-includes-dll-files">加载jar中的so库</a>
 *
 * @author ming
 * @date 2023-07-11 16:04:52
 */
public class TestLoad {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLoad.class);
    private static final String SDK_NAME = "xxx";

    static {
        LOGGER.info("load xxx.so....");
        try {
            System.loadLibrary(SDK_NAME);
        } catch (Throwable e) {
            loadFromJar();
        }
    }

    private static void loadFromJar() {
        //linux 下是so文件
        String name = SDK_NAME + ".so";
        String path = "lib_" + System.currentTimeMillis() + "/";
        try {
            // 放在classPath下 需要从classLoader的目录下获取   如果是放在当前文件的包下 需要 TestLoad.class.getResourceAsStream(name); 即可
            InputStream in = TestLoad.class.getClassLoader().getResourceAsStream(name);
            File fileOut = new File(System.getProperty("java.io.tmpdir") + "/" + path + name);
            LOGGER.info("Writing so to: " + fileOut.getAbsolutePath());
            OutputStream out = FileUtils.openOutputStream(fileOut);
            IOUtils.copy(in, out);
            in.close();
            out.close();
            System.load(fileOut.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load required so", e);
        }
    }
}
```

#### 总结    
简单记录一下 加载dll so库的方式     
基本上就是直接通过环境变量+库名加载 或者直接通过绝对路径加载     
如果是在jar中 就加载class的时候 复制到os的目录然后在加载     


