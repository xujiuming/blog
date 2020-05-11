---
title: keytool使用笔记
comments: true
date: 2020-05-11 11:00:52
categories: 笔记
tags:
 - keytool
 - 密钥管理
---
#### 前言 
jdk中自带的密钥和证书管理工具    
密钥和证书 在一些对安全有要求的地方 经常会用    
例如数据加密签名、ssl/tls传输加密等       
当然 管理证书的工具keytool只是其中一种 更多的是使用openssl 去管理   
不过不管那种工具 只要生成的证书符合规范即可 

> 公钥、密钥和数字证书说明 http://www.youdzone.com/signature.html

#### 生成
```shell script
keytool -genkeypair -alias golove -keysize 2048 -keyalg RSA -validity 3650 -keystore teststore.jks -storetype JKS
#- genkeypair：生成公私钥对条目，私钥不可见，公钥会以证书格式保存在keystore中。
#- alias: 指定别名，区分不同条目，默认mykey
#- keysize: 密钥长度
#- keyalg: 公私钥算法
#- validity: 证书过期时间
#- keystore: 指定存储密钥库，若不存在会创建，若指定则在当前文件夹下生成。默认密钥库为用户目录下.keystore文件
#- storetype: 密钥库类型  JKS PKCS等
```
> 输入密钥库密码和本条目密码都为123456，以及其他主体信息会生成密钥对保存在teststore.jks中。公钥以证书格式保存，带有主体信息。此时证书库中可以看到公钥信息（私钥无法打印)
> 如果是给网站使用的第一条主体信息：您的姓名与姓氏中填入服务器域名的完整信息而非name，如：www.golove.com。
#### 管理 
