---
title: java转换音频格式amr到mp3笔记
comments: true
categories: 笔记
tags:
  - ffmpeg
  - jave
abbrlink: 4fae9409
date: 2023-07-18 16:55:26
---
#### 前言    
遇到解析微信的 语音文件    需要将amr格式转换为mp3格式    
然后搜了一下 基本上就是基于ffmpeg来处理就行     
这里记录一下 方便自己速查      
#### 示例 

##### ffmpeg   

```shell
ffmpeg  -i  xxx.amr  -vn  -acodec  libmp3lame  -f  mp3  -y  xxx.mp3
```

##### it.sauronsoftware.jave   
> 使用jave    参考:https://developer.aliyun.com/ask/77316?spm=a2c6h.13159736   

```xml
        <!-- https://mvnrepository.com/artifact/it.sauronsoftware/jave -->
        <dependency>
            <groupId>it.sauronsoftware</groupId>
            <artifactId>jave</artifactId>
            <version>1.0.2</version>
        </dependency>
```

```java
import it.sauronsoftware.jave.AudioAttributes;  
import it.sauronsoftware.jave.Encoder;  
import it.sauronsoftware.jave.EncoderException;  
import it.sauronsoftware.jave.EncodingAttributes;  
import it.sauronsoftware.jave.InputFormatException;  
import java.io.File;  
  
public class ChangeAudioFormat {  
    public static void main(String[] args) throws Exception {  
        String path1 = "E:\\Eclipse_Web\\lbtm\\webapp\\uploadFiles\\1395047224460.amr";  
        String path2 = "E:\\Eclipse_Web\\lbtm\\webapp\\uploadFiles\\1395047224460.mp3";  
        changeToMp3(path1, path2);  
    }  
  
    public static void changeToMp3(String sourcePath, String targetPath) {  
        File source = new File(sourcePath);  
        File target = new File(targetPath);  
        AudioAttributes audio = new AudioAttributes();  
        Encoder encoder = new Encoder();  
  
        audio.setCodec("libmp3lame");  
        EncodingAttributes attrs = new EncodingAttributes();  
        attrs.setFormat("mp3");  
        attrs.setAudioAttributes(audio);  
  
        try {  
            encoder.encode(source, target, attrs);  
        } catch (IllegalArgumentException e) {  
            e.printStackTrace();  
        } catch (InputFormatException e) {  
            e.printStackTrace();  
        } catch (EncoderException e) {  
            e.printStackTrace();  
        }  
    }  
}  
```

#####  dadiyang.jave    

> 使用dadiyang  1.0.6的依赖有点问题 降级1.0.5 即可      

```xml
        <!-- https://mvnrepository.com/artifact/com.github.dadiyang/jave -->
        <dependency>
            <groupId>com.github.dadiyang</groupId>
            <artifactId>jave</artifactId>
            <version>1.0.5</version>
        </dependency>
```

```java
package com.ming.service;

import it.sauronsoftware.jave.AudioUtils;

import java.io.File;

public class AudioUtilsTest {
    public static void main(String[] args) {
        File source = new File("测试语音文件.amr");
        File target = new File("testAudio.mp3");
        AudioUtils.amrToMp3(source, target);
    }
}

```


#### 总结  
都是ffmpeg 处理的 
看看ffmpeg 然后借助一些封装的jar使用就行   
核心还是ffmpeg  




