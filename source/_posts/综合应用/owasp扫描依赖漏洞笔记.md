---
title: owasp扫描依赖漏洞笔记
comments: true
categories: 笔记
tags:
  - owasp
  - 漏洞处理
abbrlink: 61a3df4c
date: 2020-08-30 13:22:36
---
#### 前言
。。。最近漏洞频出 。。 大多是各种依赖的漏洞   
例如 fastjson。。  
各种注入。。难受   
所以找了这个owasp 的依赖检查插件 时刻检查项目中依赖的已经公开的漏洞 进行检测并且进行修复   
防患于未然  减少可能的风险  

#### 示例 
##### 增加owasp 依赖检查插件 
> http://www.owasp.org.cn/
```xml
        <!--dependency check cve -->
        <owasp.dependency.check.version>5.3.2</owasp.dependency.check.version>
...

            <plugin>
                <!-- https://mvnrepository.com/artifact/org.owasp/dependency-check-maven -->
                    <groupId>org.owasp</groupId>
                    <artifactId>dependency-check-maven</artifactId>
                    <version>${owasp.dependency.check.version}</version>
            </plugin>
```
* 过滤部分语言依赖检测
```xml
   <!--owasp dependency check maven plugin-->
            <plugin>
                <!-- https://mvnrepository.com/artifact/org.owasp/dependency-check-maven -->
                    <groupId>org.owasp</groupId>
                    <artifactId>dependency-check-maven</artifactId>
                    <version>${owasp.dependency.check.version}</version>
                <configuration>
                    <!--配置忽略指定类型依赖检测-->
                    <retireJsAnalyzerEnabled>false</retireJsAnalyzerEnabled>
                    <golangDepEnabled>false</golangDepEnabled>
                </configuration>
            </plugin>
```
##### 执行检查 
```shell script
mvn -DskipTests dependency-check:check
```
第一次执行的话 会更新漏洞库  耗时较长
##### 查看分析结果 
> 查看target目录下生成的dependency-check-report.html html文件   

![dependency-check-report.html](https://ming-master.oss-cn-shanghai.aliyuncs.com/ming-static/dependency-check-report.html.png)

根据报告提示的漏洞版本去做对应处理即可 一般只需要升级依赖版本     
例如截图中的报告 guava和spring security 存在漏洞 按照详细描述 升级到处理漏洞的fix版本即可   
#### 总结 
java 中经常使用各种第三方依赖 有漏洞 在所难免   
经常检查修复即可  
owasp 官方推荐 最少七天扫描一次    


