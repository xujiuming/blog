---
title: 管理证书-openssl和keytool
comments: true
categories: 笔记
tags:
  - 证书
  - openssl
  - keytool
abbrlink: 7a196489
date: 2021-01-05 13:17:33
---

#### 前言
一直切换证书种类 管理证书的时候都是 百度 google 当场搜索 比较麻烦        
这次有时间 记录一下 个人经常通过openssl 或者keytool去管理证书的命令         
方便自己速查   

>openssl介绍:https://baike.baidu.com/item/openssl/5454803  
>keytool介绍:https://baike.baidu.com/item/keytool   
>常用密钥格式转换:https://blog.csdn.net/achenyuan/article/details/83340179   
>证书中常用名词解释:https://blog.csdn.net/zhangteng22/article/details/70139349   
>证书生成和查看:https://www.jianshu.com/p/5cff7accfd78   

#### openssl    

##### 生成证书    
```shell
# 生成rsa私钥
openssl genrsa -out ca.key 2048
#生成证书申请文件csr  
openssl req -new -key ca.key -out ca.csr 
# 自签名 
openssl x509 -req -days 365 -in ca.csr -signkey ca.key -out ca.crt
```
##### 查看证书      
```shell
#查看rsa密钥
openssl rsa -noout -text -in ca.key 
#查看证书申请 csr文件 
openssl req -noout -text -in ca.csr 
#查看证书信息  
openssl x509 -noout -text -in ca.crt 
#验证证书 
openssl verify -CAfile ca.crt myserver.crt
#祛除证书密码保护
openssl rsa -in ca.key -out server.key.insecure
```
##### 证书格式转换    
>.CRT = CRT扩展用于证书。 证书可以被编码为二进制DER或ASCII PEM。 CER和CRT扩展几乎是同义词。 最常见的于Unix 或类Unix系统   
> CER = .crt的替代形式（Microsoft Convention）您可以在微软系统环境下将.crt转换为.cer（.both DER编码的.cer，或base64 \[PEM]编码的.cer）。   

PEM(.pem) openssl默认生成的是pem文件     
DER(.cer .der)   
PKCS#12文件(.pfx .p12)    
* pem <-> DER    
```shell
openssl x509 -outform der -in ca.crt -out ca.der
openssl x509 -inform der -in ca.der -out ca.pem
```
* pem <-> PKCS12    
```shell
openssl pkcs12 -export -out ca.pfx -inkey ca.key -in ca.crt -certfile ca.crt
openssl pkcs12 -in ca.pfx -out capfx.pem -nodes
# 提取key 
openssl pkcs12 -in ca.pfx -nodes -nocerts -out ca.key -passin pass:<passvalue>
```
* pem -> key     
```shell
openssl rsa -in ca.pem -out ca.key
```

#### keytool      
>keytool 是自签名证书 无法实现 openssl那样生成证书链  https://www.cnblogs.com/zhangshitong/p/9015482.html    

##### 生成证书     
```shell
#生成证书   jks格式   
keytool -genkeypair -keystore ca.jks -alias ming -storetype pkcs12 -keyalg RSA -keysize 2048
#生成证书申请文件csr  
keytool -certreq -keystore ca.jks -alias ming -file ca.csr
#导出 der编码证书 
keytool -exportcert -keystore ca.jks -file ca.cer -alias ming 
#导出 pem编码证书  -rfc 
keytool -exportcert -keystore ca.jks -rfc -file ca.pem -alias ming
#导入证书  
keytool -importcert -alias ming -file ca.cer -keystore ca.jks
```
##### 查看证书     
```shell
#查看证书详细信息 缺省情况下，-list 命令打印证书的 MD5 指纹。而如果指定了 -v 选项，将以可读格式打印证书，如果指定了 -rfc 选项，将以可打印的编码格式输出证书
keytool -list -v -keystore ca.jks 
keytool -list -v -alias ming -keystore ca.jks  
keytool -list  -rfc -keystore ca.jks 
#要删除证书库里面的某个证书，可以使用如下命令： 
keytool -delete -alias ming -keystore ca.jks -storepass 666666 
```


##### 证书格式转换      
>.CRT = CRT扩展用于证书。 证书可以被编码为二进制DER或ASCII PEM。 CER和CRT扩展几乎是同义词。 最常见的于Unix 或类Unix系统    
>.CER = .crt的替代形式（Microsoft Convention）您可以在微软系统环境下将.crt转换为.cer（.both DER编码的.cer，或base64 \[PEM]编码的.cer）。    
  
PEM(.pem) openssl默认生成的是pem文件     
DER(.cer .der)   
PKCS#12文件(.pfx .p12)    
JKS(.jks)  java专属证书格式      
* jks <-> PKCS12    
> p12 = pfx 只是后缀不同     
```shell
keytool -importkeystore -srckeystore ca.jks -destkeystore ca.p12 -deststoretype PKCS12 -srcstorepass <passvalue> -deststorepass <passvalue>
keytool -importkeystore -srckeystore ca.p12 -srcstoretype PKCS12 -deststoretype JKS -destkeystore ca.jks
```

#### 常用命令     
* jks转换 pem+key      
jks提取pem证书 可以把jks -> p12(pfx) -> pem 或者直接提取rfc模式    
```shell
# 查看 有那些 节点  
keytool --list -keystore <keystorefile.jks> -storepass <storepass>
# 提取指定节点  
keytool -export -rfc -alias <alias-name> -file <output-file.pem> -keystore <keystorefile.jks> -storepass <storepass>

# jks -> p12 -> pem 
keytool -importkeystore -srckeystore ca.jks -destkeystore ca.p12 -deststoretype PKCS12 -srcstorepass <passvalue> -deststorepass <passvalue>
openssl pkcs12 -in ca.pfx -out capfx.pem -nodes
# 提取key 
openssl pkcs12 -in ca.pfx -nodes -nocerts -out ca.key -passin pass:<passvalue>
```

* pem转换jks   pem + key -> p12(pfx) -> jks 
```shell
openssl pkcs12 -export -out ca.pfx -inkey ca.key -in ca.crt -certfile ca.crt
keytool -importkeystore -srckeystore ca.pfx -srcstoretype PKCS12 -deststoretype JKS -destkeystore ca.jks
```

![常用证书类型转换图](http://minginfo.xujiuming.com/private-asset/%E5%B8%B8%E7%94%A8%E8%AF%81%E4%B9%A6%E8%BD%AC%E6%8D%A2%E5%9B%BE.png?Expires=1609836626&OSSAccessKeyId=TMP.3KhT9ToPTpSpgEYxwPBKVnfLocz9Sxo5zvmKRSTH7ZGLgeTZWYEgWB8qoaDC5aUBDQbqw9QBd5YqvMczMvYtPJ3zxh2GnS&Signature=7Tp3NK%2Fps7HhumsMqgwjQLiRDNQ%3D)

#### 总结 
证书 用的地方很多     
例如 ssl/tls证书   访问第三方系统等等地方 只要需要加密签名的都可以用    
不过一般来说 做ssl/tls证书居多  其他地方一般直接使用rsa密钥  一般不需要签发证书   









