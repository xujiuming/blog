---
title: redis-module笔记
comments: true
categories: 笔记
tags:
  - redis
abbrlink: e0fd20e
date: 2022-02-08 10:24:03
---
####  前言
记录一下redis-module的安装和使用      

> 示例在ubuntu20.04版本上操作  

####  安装redis
>https://redis.io/download

* apt安装    

```shell
sudo add-apt-repository ppa:redislabs/redis
sudo apt-get update
sudo apt-get install redis
```

* 常用配置 

```text
# 配置监听的ip  
bind * -::*
# 配置redis端口 
port 16379
# 密码 
masterauth ming
# 日志文件地址 
logfile /var/log/redis/redis-server.log  
# 文件地址 例如rdb、aof之类的文件目录配置
dir /var/lib/redis  
```

* 管理redis服务

```shell
# 启动
systemctl start redis-server 
# 关闭  
systemctl stop redis-server 
# 重启   
systemctl restart redis-server
```

#### 安装redis module

> https://redis.io/topics/modules-intro

```shell
# 编辑 redis.conf 永久加载模块 
loadmodule /path/to/mymodule.so

# redis console中临时加载模块
MODULE LOAD /path/to/mymodule.so
# 列出当前加载的模块列表  
MODULE LIST
# 卸载模块    
MODULE UNLOAD mymodule
```

#### 安装模块   

> 源码安装redisearch模块作为示例   
> https://www.cnblogs.com/lina-2159/p/14335919.html

* 打包编译模块  
```shell
git clone  https://github.com/RediSearch/RediSearch.git
cd RediSearch 
make setup 
make all  
# 进入编译的目录  
cd ./bin/linux-x64-release/search/
# 复制redisearch.so 到 redis工作目录下 apt安装的复制到/etc/redis下   
mv ./redisearch.so  /etc/redis/      
```

* 配置加载模块

```shell
# 编辑/etc/redis/redis.conf 
# loadmoudle /etc/redis/redisearch.so   
# 临时加载  
module load /etc/redis/redisearch.so   
# 查看安装的模块   
module list 
```

* 测试使用redisearch模块 

> 在redis console上操作  

```shell
# 创建索引并且定义格式 
FT.CREATE myIdx ON HASH PREFIX 1 doc: SCHEMA title TEXT WEIGHT 5.0 body TEXT url TEXT
# 添加索引记录 
hset doc:1 title "hello world" body "lorem ipsum" url "http://redis.io"
# 搜索索引    
FT.SEARCH myIdx "hello" LIMIT 0 10
```

#### 总结
在ubuntu 20.04上安装了一下redisearch模块   
安装还是很方便的  有了module这个功能 各种常用的操作都可以借助redis module实现了  
例如 全文检索、图数据库、限流器、布隆过滤器、分布式锁、等等 具体的可以看看 官网的模块    

