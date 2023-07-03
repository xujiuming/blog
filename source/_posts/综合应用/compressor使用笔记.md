---
title: compressor使用笔记 
comments: true 
categories: 笔记 
tags:
  - compressor
  - apache commons

abbrlink: 46b68068 
date: 2022-04-01 15:21:48
---

#### 前言

之前老是用别人的压缩工具类 感觉不太方便 各种各样花式实现    
干脆自己完整的了解下 apache commons compressor 工具包 自己封装下

> https://commons.apache.org/proper/commons-compress/examples.html
> https://www.jianshu.com/p/14af3aeb6db9

#### 模块笔记

这个模块主要功能就是提供归档和压缩功能

> 归档就是常见的 .zip .tar .jar .7z之类的 可以打包多个文件 可以压缩 也可以不压缩   
> 压缩主要是单纯的压缩数据 例如 .gz .xz .bz2 .lz4 .lzma .sz .zstd 等等

##### 依赖

```xml
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-compress</artifactId>
            <version>1.21</version>
        </dependency>
        <!--压缩解压xz必须的包-->
        <dependency>
            <groupId>org.tukaani</groupId>
            <artifactId>xz</artifactId>
            <version>1.8</version>
        </dependency>

```

##### 归档

```java
package com.ming;

import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 压缩测试
 * 使用 apache commons compress
 * https://commons.apache.org/proper/commons-compress/examples.html
 *
 * @author ming
 * @date 2022-03-29 11:33:01
 */
public class CompressTest {

    File rootFile = new File("./logs");
    String encoding = "UTF-8";

    /**
     * 归档
     * <p>
     * 使用 zip 做示例
     * 所有继承 ArchiveOutputStream 的 zip  tar  jar 都可以做归档  有的带压缩
     *
     * @author ming
     * @date 2022-03-29 16:01:35
     */
    @SneakyThrows
    @Test
    public void archive() {
        //归档zip
        ArchiveOutputStream archiveOutputStream = new ArchiveStreamFactory()
                .createArchiveOutputStream(ArchiveStreamFactory.ZIP, Files.newOutputStream(Path.of("./test.zip")), encoding);
        addEntry("", rootFile, archiveOutputStream);
        archiveOutputStream.finish();
        archiveOutputStream.close();

        //解开归档
        ArchiveInputStream inputStream = new ArchiveStreamFactory().
                createArchiveInputStream(ArchiveStreamFactory.ZIP, Files.newInputStream(Path.of("./test.zip")), encoding);
        ArchiveEntry entry;
        String dirPath = "./test";
        while ((entry = inputStream.getNextEntry()) != null) {
            System.out.println(entry.getName());
            //当前流是否合一读取entry  不能读取直接跳过
            if (!inputStream.canReadEntryData(entry)) {
                continue;
            }
            String name = entry.getName();
            File file = new File(dirPath, name);
            System.out.println("创建资源:" + file.getAbsolutePath());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                //https://stackoverflow.com/questions/53754387/java-read-vs-readnbytes-of-the-inputstream-instance
                //read()说它试图读取"最多len字节...但可以读取较小的数字。此方法会一直阻塞，直到输入数据可用、检测到文件末尾或引发异常。
                //readNBytes()表示"在读取了输入数据的 len 字节、检测到流结束或引发异常之前，才会出现块。

                //处理父级文件夹
                File parentFile = file.getParentFile();
                if (parentFile == null || !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                //copyToFile try-resource 关闭了inputStream   不能拆分多文件的归档
                //FileUtils.copyToFile(inputStream,file);
                FileOutputStream outputStream = new FileOutputStream(file);
                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();
                outputStream.close();
                //FileUtils.writeByteArrayToFile(file,inputStream.readAllBytes());
                file.setLastModified(entry.getLastModifiedDate().getTime());
            }
        }
        inputStream.close();
    }

    private void addEntry(String basePath, File rootFile, ArchiveOutputStream outputStream) throws IOException {
        if (rootFile.isDirectory()) {
            basePath = basePath + rootFile.getName() + "/";
            File[] files = rootFile.listFiles();
            if (files == null || files.length == 0) {
                ArchiveEntry entry = outputStream.createArchiveEntry(rootFile, basePath);
                outputStream.putArchiveEntry(entry);
                outputStream.closeArchiveEntry();
                return;
            }
            for (File file : files) {
                addEntry(basePath, file, outputStream);
            }
        } else {
            ArchiveEntry entry = outputStream.createArchiveEntry(rootFile, basePath + rootFile.getName());
            outputStream.putArchiveEntry(entry);
            IOUtils.copy(Files.newInputStream(rootFile.toPath()), outputStream);
            outputStream.closeArchiveEntry();
        }
    }
}
```
归档: 打开outputStream -> addArchiveEntry -> closeArchiveEntry -> finish    
解开归档: 打开inputStream -> getNextEntry -> copy  -> write     


##### 压缩

```java
package com.ming;

import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 压缩测试
 * 使用 apache commons compress
 * https://commons.apache.org/proper/commons-compress/examples.html
 *
 * @author ming
 * @date 2022-03-29 11:33:01
 */
public class CompressTest {

    /**
     * java.lang.NoClassDefFoundError: org/tukaani/xz/FilterOptions  配置xz的pom依赖
     *
     * @author ming
     * @date 2022-03-29 16:33:54
     */
    @SneakyThrows
    @Test
    public void compress() {
        //压缩xz
        CompressorOutputStream compressorOutputStream = new CompressorStreamFactory()
                .createCompressorOutputStream(CompressorStreamFactory.XZ, Files.newOutputStream(Path.of("./test.xz")));
        IOUtils.copy(Files.newInputStream(Path.of("./logs/output.log")), compressorOutputStream);
        compressorOutputStream.flush();
        compressorOutputStream.close();

        //解压xz
        CompressorInputStream inputStream = new CompressorStreamFactory()
                .createCompressorInputStream(CompressorStreamFactory.XZ, Files.newInputStream(Path.of("./test.xz")));
        FileUtils.copyToFile(inputStream, new File("./output.log"));

        //压缩gz
//        CompressorOutputStream outputStream = new CompressorStreamFactory()
//                .createCompressorOutputStream(CompressorStreamFactory.GZIP,Files.newOutputStream(Path.of("./test.gz")));
//        IOUtils.copy(Files.newInputStream(Path.of("./logs/output.log")),outputStream);
//        outputStream.flush();
//        outputStream.close();

    }
}
```
压缩: 获取对应的outputStream -> copy -> flush -> close  
解压缩: 获取对应的inputStream -> copy -> write 


> 大致看出 总的来说就是CompressorInputStream解压  CompressorOutputStream压缩  ArchiveInputStream+ArchiveEntry打开归档 ArchiveOuputStream+ArchiveEntry 来打包归档    
> 然后初始化归档或者压缩的inputStream 和outputStream 除了用构造函数外 也提供了CompressorStreamFactory ArchiveStreamFactor 工厂函数     
> CompressorInputStream构建的时候 可以指定最大允许使用的内存大小 memoryLimitInKb          

##### 归档+压缩    
说到归档+压缩 就不得不说 java io的装饰器设计真的牛叉   

```java
package com.ming;

import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 压缩测试
 * 使用 apache commons compress
 * https://commons.apache.org/proper/commons-compress/examples.html
 *
 * @author ming
 * @date 2022-03-29 11:33:01
 */
public class CompressTest {

    File rootFile = new File("./logs");

    private void addEntry(String basePath, File rootFile, ArchiveOutputStream outputStream) throws IOException {
        if (rootFile.isDirectory()) {
            basePath = basePath + rootFile.getName() + "/";
            File[] files = rootFile.listFiles();
            if (files == null || files.length == 0) {
                ArchiveEntry entry = outputStream.createArchiveEntry(rootFile, basePath);
                outputStream.putArchiveEntry(entry);
                outputStream.closeArchiveEntry();
                return;
            }
            for (File file : files) {
                addEntry(basePath, file, outputStream);
            }
        } else {
            ArchiveEntry entry = outputStream.createArchiveEntry(rootFile, basePath + rootFile.getName());
            outputStream.putArchiveEntry(entry);
            IOUtils.copy(Files.newInputStream(rootFile.toPath()), outputStream);
            outputStream.closeArchiveEntry();
        }
    }


    @SneakyThrows
    @Test
    public void archiveAndCompress() {
        //获取压缩流
        CompressorOutputStream compressorOutputStream = new CompressorStreamFactory()
                .createCompressorOutputStream(CompressorStreamFactory.GZIP, Files.newOutputStream(Path.of("./test.tar.gz")));
        //将压缩流装饰城归档流
        ArchiveOutputStream archiveOutputStream = new ArchiveStreamFactory()
                .createArchiveOutputStream(ArchiveStreamFactory.TAR, compressorOutputStream);
        addEntry("", rootFile, archiveOutputStream);
        archiveOutputStream.finish();
        archiveOutputStream.close();
        compressorOutputStream.flush();
        compressorOutputStream.close();

        //解压 + 还原归档
        CompressorInputStream compressorInputStream = new CompressorStreamFactory()
                .createCompressorInputStream(CompressorStreamFactory.GZIP, Files.newInputStream(Path.of("./test.tar.gz")));
        //装饰为归档流
        ArchiveInputStream archiveInputStream = new ArchiveStreamFactory()
                .createArchiveInputStream(ArchiveStreamFactory.TAR, compressorInputStream);
        ArchiveEntry entry;
        String dirPath = "./test";
        while ((entry = archiveInputStream.getNextEntry()) != null) {
            System.out.println(entry.getName());
            //当前流是否合一读取entry  不能读取直接跳过
            if (!archiveInputStream.canReadEntryData(entry)) {
                continue;
            }
            String name = entry.getName();
            File file = new File(dirPath, name);
            System.out.println("创建资源:" + file.getAbsolutePath());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                //https://stackoverflow.com/questions/53754387/java-read-vs-readnbytes-of-the-inputstream-instance
                //read()说它试图读取"最多len字节...但可以读取较小的数字。此方法会一直阻塞，直到输入数据可用、检测到文件末尾或引发异常。
                //readNBytes()表示"在读取了输入数据的 len 字节、检测到流结束或引发异常之前，才会出现块。

                //处理父级文件夹
                File parentFile = file.getParentFile();
                if (parentFile == null || !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                //copyToFile try-resource 关闭了inputStream   不能拆分多文件的归档
                //FileUtils.copyToFile(inputStream,file);
                FileOutputStream outputStream = new FileOutputStream(file);
                IOUtils.copy(archiveInputStream, outputStream);
                outputStream.flush();
                outputStream.close();
                //FileUtils.writeByteArrayToFile(file,inputStream.readAllBytes());
                file.setLastModified(entry.getLastModifiedDate().getTime());
            }
        }
        archiveInputStream.close();
        compressorOutputStream.close();
    }
}
```

归档+压缩: 获取CompressorOutputStream -> 把CompressorOutputStream装饰成 ArchiveOutputStream -> addEntry -> finish归档 ->刷新和关闭ArchiveOutputStream和CompressorOutputStream    
解压+还原归档: 获取CompressorInputStream  -> 把CompressorInputStream装饰成ArchiveInputStream -> 解析还原entry -> 关闭CompressorInputStream和ArchiveInputStream

##### 7z  

> 由于7z过于复杂  commons compressor 是另外实现的  SevenZFile  SevenZArchiveEntry SevenZOutputFile  

```java
  /**
     * 7z压缩 存在很多的参数和情况 这里只做最简单的 压缩
     * 例如密码、是否固实 等等
     * 实际在程序中用7z  也实在很少遇到     基本上 xz gz zstd 足够用了
     *
     * @param rootFile   压缩的 file
     * @param sevenZFile 7z的file
     * @throws IOException io异常
     * @author ming
     * @date 2022-03-31 17:26:06
     */
    @Beta
    public static void sevenZ(File rootFile, File sevenZFile) throws IOException {
        //ArchiveStreamFactor 不支持创建7z的流  所以不能使用archive()
        try (SevenZOutputFile outputFile = new SevenZOutputFile(sevenZFile)) {
            addSevenEntry(outputFile, rootFile, "");
        }
    }

    /**
     * 添加7z条目
     *
     * @param sevenZFile 7z file
     * @param rootFile   压缩的file
     * @param basePath   压缩的基础路径
     * @throws IOException io异常
     * @author ming
     * @date 2022-03-31 18:13:31
     */
    @Beta
    private static void addSevenEntry(SevenZOutputFile sevenZFile, File rootFile, String basePath) throws IOException {
        if (rootFile.isDirectory()) {
            basePath = basePath + rootFile.getName() + "/";
            File[] files = rootFile.listFiles();
            if (files == null || files.length == 0) {
                ArchiveEntry entry = sevenZFile.createArchiveEntry(rootFile, basePath);
                sevenZFile.putArchiveEntry(entry);
                sevenZFile.closeArchiveEntry();
                return;
            }
            for (File file : files) {
                addSevenEntry(sevenZFile, file, basePath);
            }
        } else {
            ArchiveEntry entry = sevenZFile.createArchiveEntry(rootFile, basePath + rootFile.getName());
            sevenZFile.putArchiveEntry(entry);
            byte[] bs = FileUtils.readFileToByteArray(rootFile);
            sevenZFile.write(bs);
            sevenZFile.closeArchiveEntry();
        }

    }

    /**
     * 7z解压 存在很多的参数和情况 这里只做最简单的 解压
     * 例如密码、是否固实 等等
     * 实际在程序中用7z  也实在很少遇到     基本上 xz gz zstd 足够用了
     *
     * @param dirPath    解压的路径
     * @param sevenZFile 7z file
     * @throws IOException io异常
     * @author ming
     * @date 2022-03-31 17:26:06
     */
    @Beta
    public static void unSevenZ(String dirPath, File sevenZFile) throws IOException {
        //ArchiveStreamFactor 不支持创建7z的流  所以不能使用unArchive()
        try (SevenZFile archive = new SevenZFile(sevenZFile)) {
            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                File file = new File(dirPath, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                }
                if (entry.hasStream()) {
                    final byte[] buf = new byte[2048];
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int len = 0; (len = archive.read(buf)) > 0; ) {
                        baos.write(buf, 0, len);
                    }
                    FileUtils.writeByteArrayToFile(file, baos.toByteArray());
                }
            }
        }
    }
    
```


#### 封装utils

* CompressorUtils 

```java
package com.ming.core.utils;

import com.google.common.annotations.Beta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;

/**
 * 压缩工具类
 * 基于 commons-compress commons-io等工具包
 * https://commons.apache.org/proper/commons-compress/examples.html
 * https://www.jianshu.com/p/14af3aeb6db9
 * <p>
 * 提供常见的归档 压缩功能
 * zip  .zip
 * tar .tar
 * tar+gzip .tar.gz
 * tar+xz  .tar.xz
 * tar+Z  .tar.Z  只提供解压 
 * tar+zstd  .tar.zstd
 * 7z .7z
 *
 * @author ming
 * @date 2022-03-31 11:21:04
 */
@Slf4j
public class CompressUtils {

    /**
     * 归档工厂
     * 设置默认编码
     */
    private static final ArchiveStreamFactory ARCHIVE_STREAM_FACTORY = new ArchiveStreamFactory("UTF-8");

    /**
     * 压缩流工厂
     * decompressUntilEOF 设置为true 一直持续到解压结束
     * memoryLimitInKb 设置为100MB  = 100* 1024KB
     */
    private static final CompressorStreamFactory COMPRESSOR_STREAM_FACTORY = new CompressorStreamFactory(Boolean.TRUE, 100 * 1024);


    /**
     * zip归档
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void zip(File rootFile, OutputStream outputStream) throws ArchiveException, IOException {
        archive(ArchiveStreamFactory.ZIP, rootFile, outputStream);
    }

    /**
     * 解压zip
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unZip(String dirPath, InputStream inputStream) throws IOException, ArchiveException {
        unArchive(ArchiveStreamFactory.ZIP, dirPath, inputStream);
    }


    /**
     * tar归档
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tar(File rootFile, OutputStream outputStream) throws IOException, ArchiveException {
        archive(ArchiveStreamFactory.TAR, rootFile, outputStream);
    }

    /**
     * 解压tar
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTar(String dirPath, InputStream inputStream) throws IOException, ArchiveException {
        unArchive(ArchiveStreamFactory.TAR, dirPath, inputStream);
    }

    /**
     * tar归档+gzip压缩
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarGz(File rootFile, OutputStream outputStream) throws CompressorException, ArchiveException, IOException {
        archiveAndCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP, rootFile, outputStream);
    }

    /**
     * 解压tar+gz
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarGz(String dirPath, InputStream inputStream) throws IOException, ArchiveException, CompressorException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP, dirPath, inputStream);
    }


    /**
     * tar归档+xz
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarXz(File rootFile, OutputStream outputStream) throws CompressorException, IOException, ArchiveException {
        archiveAndCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.XZ, rootFile, outputStream);
    }

    /**
     * 解压tar+xz
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarXz(String dirPath, InputStream inputStream) throws CompressorException, IOException, ArchiveException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.XZ, dirPath, inputStream);
    }

    /**
     * 暂未实现
     * tar归档+z
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarZ(File rootFile, OutputStream outputStream) throws CompressorException, IOException, ArchiveException {
        //因为 apache commons compressor 不支持 。。
        throw new UnsupportedOperationException("暂时不支持.Z文件压缩!");
    }

    /**
     * 解压tar+Z
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarZ(String dirPath, InputStream inputStream) throws CompressorException, IOException, ArchiveException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.Z, dirPath, inputStream);
    }

    /**
     * tar归档+zstd压缩
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarZstd(File rootFile, OutputStream outputStream) throws CompressorException, IOException, ArchiveException {
        archiveAndCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.ZSTANDARD, rootFile, outputStream);
    }

    /**
     * 解压tar+zstd
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarZstd(String dirPath, InputStream inputStream) throws CompressorException, IOException, ArchiveException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.ZSTANDARD, dirPath, inputStream);
    }

    /**
     * tar归档+bz2压缩
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarBz2(File rootFile, OutputStream outputStream) throws CompressorException, ArchiveException, IOException {
        archiveAndCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2, rootFile, outputStream);
    }

    /**
     * 解压tar+bz2
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarBz2(String dirPath, InputStream inputStream) throws IOException, ArchiveException, CompressorException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2, dirPath, inputStream);
    }

    /**
     * tar归档+lz4压缩
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarLz4(File rootFile, OutputStream outputStream) throws CompressorException, ArchiveException, IOException {
        archiveAndCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.LZ4_FRAMED, rootFile, outputStream);
    }

    /**
     * 解压tar+lz4
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarLz4(String dirPath, InputStream inputStream) throws IOException, ArchiveException, CompressorException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.LZ4_FRAMED, dirPath, inputStream);
    }

    /**
     * tar归档+lzma压缩
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarLzma(File rootFile, OutputStream outputStream) throws CompressorException, ArchiveException, IOException {
        archiveAndCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.LZMA, rootFile, outputStream);
    }

    /**
     * 解压tar+lzma
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarLzma(String dirPath, InputStream inputStream) throws IOException, ArchiveException, CompressorException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.LZMA, dirPath, inputStream);
    }

    /**
     * tar归档+sz压缩
     *
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:49:47
     */
    public static void tarSz(File rootFile, OutputStream outputStream) throws CompressorException, ArchiveException, IOException {
        archiveAndCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.SNAPPY_FRAMED, rootFile, outputStream);
    }

    /**
     * 解压tar+sz
     *
     * @param dirPath     解压目录
     * @param inputStream 文件流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:27:02
     */
    public static void unTarSz(String dirPath, InputStream inputStream) throws IOException, ArchiveException, CompressorException {
        unArchiveAndUnCompressor(ArchiveStreamFactory.TAR, CompressorStreamFactory.SNAPPY_FRAMED, dirPath, inputStream);
    }


    /**
     * 7z压缩 存在很多的参数和情况 这里只做最简单的 压缩
     * 例如密码、是否固实 等等
     * 实际在程序中用7z  也实在很少遇到     基本上 xz gz zstd 足够用了
     *
     * @param rootFile   压缩的 file
     * @param sevenZFile 7z的file
     * @throws IOException io异常
     * @author ming
     * @date 2022-03-31 17:26:06
     */
    @Beta
    public static void sevenZ(File rootFile, File sevenZFile) throws IOException {
        //ArchiveStreamFactor 不支持创建7z的流  所以不能使用archive()
        try (SevenZOutputFile outputFile = new SevenZOutputFile(sevenZFile)) {
            addSevenEntry(outputFile, rootFile, "");
        }
    }

    /**
     * 添加7z条目
     *
     * @param sevenZFile 7z file
     * @param rootFile   压缩的file
     * @param basePath   压缩的基础路径
     * @throws IOException io异常
     * @author ming
     * @date 2022-03-31 18:13:31
     */
    @Beta
    private static void addSevenEntry(SevenZOutputFile sevenZFile, File rootFile, String basePath) throws IOException {
        if (rootFile.isDirectory()) {
            basePath = basePath + rootFile.getName() + "/";
            File[] files = rootFile.listFiles();
            if (files == null || files.length == 0) {
                ArchiveEntry entry = sevenZFile.createArchiveEntry(rootFile, basePath);
                sevenZFile.putArchiveEntry(entry);
                sevenZFile.closeArchiveEntry();
                return;
            }
            for (File file : files) {
                addSevenEntry(sevenZFile, file, basePath);
            }
        } else {
            ArchiveEntry entry = sevenZFile.createArchiveEntry(rootFile, basePath + rootFile.getName());
            sevenZFile.putArchiveEntry(entry);
            byte[] bs = FileUtils.readFileToByteArray(rootFile);
            sevenZFile.write(bs);
            sevenZFile.closeArchiveEntry();
        }

    }

    /**
     * 7z解压 存在很多的参数和情况 这里只做最简单的 解压
     * 例如密码、是否固实 等等
     * 实际在程序中用7z  也实在很少遇到     基本上 xz gz zstd 足够用了
     *
     * @param dirPath    解压的路径
     * @param sevenZFile 7z file
     * @throws IOException io异常
     * @author ming
     * @date 2022-03-31 17:26:06
     */
    @Beta
    public static void unSevenZ(String dirPath, File sevenZFile) throws IOException {
        //ArchiveStreamFactor 不支持创建7z的流  所以不能使用unArchive()
        try (SevenZFile archive = new SevenZFile(sevenZFile)) {
            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                File file = new File(dirPath, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                }
                if (entry.hasStream()) {
                    final byte[] buf = new byte[2048];
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int len = 0; (len = archive.read(buf)) > 0; ) {
                        baos.write(buf, 0, len);
                    }
                    FileUtils.writeByteArrayToFile(file, baos.toByteArray());
                }
            }
        }
    }

    /**
     * 归档
     *
     * @param archiverName 归档名称 参考 ArchiveStreamFactory中的枚举值
     * @param rootFile     需要归档的目录or文件
     * @param outputStream 归档的文件输出流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 13:47:40
     */
    private static void archive(String archiverName, File rootFile, OutputStream outputStream) throws ArchiveException, IOException {
        //归档zip
        try (ArchiveOutputStream archiveOutputStream = ARCHIVE_STREAM_FACTORY.createArchiveOutputStream(archiverName, outputStream)) {
            addEntry("", rootFile, archiveOutputStream);
            archiveOutputStream.finish();
        }
    }

    /**
     * 递归添加归档条目
     *
     * @param basePath     基本路径
     * @param rootFile     根文件
     * @param outputStream 输出流  添加条目的时候 不关闭 需要自行处理
     * @throws IOException io异常
     * @author ming
     * @date 2022-03-31 13:41:06
     */
    private static void addEntry(String basePath, File rootFile, ArchiveOutputStream outputStream) throws IOException {
        if (rootFile.isDirectory()) {
            basePath = basePath + rootFile.getName() + "/";
            File[] files = rootFile.listFiles();
            if (files == null || files.length == 0) {
                ArchiveEntry entry = outputStream.createArchiveEntry(rootFile, basePath);
                outputStream.putArchiveEntry(entry);
                outputStream.closeArchiveEntry();
                return;
            }
            for (File file : files) {
                addEntry(basePath, file, outputStream);
            }
        } else {
            ArchiveEntry entry = outputStream.createArchiveEntry(rootFile, basePath + rootFile.getName());
            outputStream.putArchiveEntry(entry);
            try (InputStream inputStream = Files.newInputStream(rootFile.toPath())) {
                IOUtils.copy(inputStream, outputStream);
            }
            outputStream.closeArchiveEntry();
        }
    }


    /**
     * 解开归档
     *
     * @param archiveName 归档名称 参考ArchiveStreamFactory枚举
     * @param dirPath     解开文件存放的目录
     * @param inputStream 归档文件输入流
     * @throws ArchiveException 归档异常
     * @throws IOException      io异常
     * @author ming
     * @date 2022-03-31 14:19:42
     */
    private static void unArchive(String archiveName, String dirPath, InputStream inputStream) throws ArchiveException, IOException {
        //解开归档
        try (ArchiveInputStream archiveInputStream = ARCHIVE_STREAM_FACTORY.createArchiveInputStream(archiveName, inputStream)) {
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                //当前流是否合一读取entry  不能读取直接跳过
                if (!archiveInputStream.canReadEntryData(entry)) {
                    continue;
                }
                String name = entry.getName();
                File file = new File(dirPath, name);
                if (log.isDebugEnabled()) {
                    log.debug("释放归档资源:{}", file.getAbsolutePath());
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    //https://stackoverflow.com/questions/53754387/java-read-vs-readnbytes-of-the-inputstream-instance
                    //read()说它试图读取"最多len字节...但可以读取较小的数字。此方法会一直阻塞，直到输入数据可用、检测到文件末尾或引发异常。
                    //readNBytes()表示"在读取了输入数据的 len 字节、检测到流结束或引发异常之前，才会出现块。
                    //处理父级文件夹
                    File parentFile = file.getParentFile();
                    if (parentFile != null || !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    //copyToFile try-resource 关闭了inputStream   不能拆分多文件的归档
                    //FileUtils.copyToFile(inputStream,file);
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        IOUtils.copy(archiveInputStream, outputStream);
                        outputStream.flush();
                    }
                    file.setLastModified(entry.getLastModifiedDate().getTime());
                }
            }
        }
    }

    /**
     * 归档并且压缩
     *
     * @param archiveName    归档类型名称
     * @param compressorName 压缩类型名称
     * @param rootFile       归档压缩的file
     * @param outputStream   归档压缩的输出流
     * @throws CompressorException 压缩异常
     * @throws ArchiveException    归档异常
     * @throws IOException         io异常
     * @author ming
     * @date 2022-03-31 18:11:43
     */
    private static void archiveAndCompressor(String archiveName, String compressorName, File rootFile, OutputStream outputStream) throws CompressorException, IOException, ArchiveException {
        //获取压缩流
        try (CompressorOutputStream compressorOutputStream = COMPRESSOR_STREAM_FACTORY.createCompressorOutputStream(compressorName, outputStream)) {
            archive(archiveName, rootFile, compressorOutputStream);
        }
    }

    /**
     * 解压并且还原
     *
     * @param archiveName    归档类型名称
     * @param compressorName 压缩类型名称
     * @param dirPath        解压还原的路径
     * @param inputStream    归档压缩输入流
     * @throws CompressorException 压缩异常
     * @throws ArchiveException    归档异常
     * @throws IOException         io异常
     * @author ming
     * @date 2022-03-31 18:11:43
     */
    private static void unArchiveAndUnCompressor(String archiveName, String compressorName, String dirPath, InputStream inputStream) throws CompressorException, IOException, ArchiveException {
        try (CompressorInputStream compressorInputStream = COMPRESSOR_STREAM_FACTORY.createCompressorInputStream(compressorName, inputStream)) {
            unArchive(archiveName, dirPath, compressorInputStream);
        }
    }

}
```

* CompressorUtilsTest

> 提供CompressorUtils测试用例  

```java
package com.ming;

import com.ming.core.utils.CompressUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 压缩测试
 * 使用 apache commons compress
 * https://commons.apache.org/proper/commons-compress/examples.html
 *
 * @author ming
 * @date 2022-03-29 11:33:01
 */
public class CompressUtilsTest {

    @Test
    public void testZip() {
        String path = "./logs.zip";
        //zip归档
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.zip(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }

        //解压zip
        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unZip("./zipoutput", inputStream);
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test7z() {
        String path = "./logs.7z";
        try {
            CompressUtils.sevenZ(new File("./logs"), new File(path));
        } catch (IOException  e) {
            e.printStackTrace();
        }

        try {
            CompressUtils.unSevenZ("./7zoutput", new File(path));
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testTar() {
        String path = "./logs.tar";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tar(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTar("./taroutput", inputStream);
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTarGz() {
        String path = "./logs.tar.gz";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarGz(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarGz("./targzoutput", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTarXz() {
        String path = "./logs.tar.xz";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarXz(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarXz("./tarxzoutput", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void testTarZ() {
        String path = "./logs.tar.Z";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarZ(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarZ("./tarZoutput", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTarZstd() {
        String path = "./logs.tar.zstd";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarZstd(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarZstd("./tarzstdoutput", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTarBz2() {
        String path = "./logs.tar.bz2";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarBz2(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarBz2("./tarbz2output", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testTarLz4() {
        String path = "./logs.tar.lz4";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarLz4(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarLz4("./tarlz4output", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testTarLzma() {
        String path = "./logs.tar.lzma";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarLzma(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarLzma("./tarlzmaoutput", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testTarSz() {
        String path = "./logs.tar.sz";
        try (OutputStream outputStream = Files.newOutputStream(Path.of(path))) {
            CompressUtils.tarSz(new File("./logs"), outputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
            CompressUtils.unTarSz("./tarszoutput", inputStream);
        } catch (IOException | ArchiveException | CompressorException e) {
            e.printStackTrace();
        }
    }

}

```

#### 总结   
apache commons compressor 提供的常见的压缩和归档算法的实现  
在实际使用中 zip 或者 tar配合其他压缩算法用的多 例如 .tar.gz  .tar.xz  .tar.zstd 等      
xz压缩和解压要注意 要引用xz的jar     













