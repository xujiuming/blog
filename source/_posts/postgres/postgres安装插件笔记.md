---
title: postgres安装插件笔记
comments: true
categories: 笔记
tags:
  - postgres
  - db
abbrlink: 26f8b828
date: 2019-08-01 10:24:22
---
#### 前言
在使用postgres的时候 经常要用一些扩展的东西 例如 fdw  dblink  ltree之类的功能 
postgres 是通过配置来使用这些功能  

#### 安装插件
服务器版本:CentOs7 
postgres版本: postgres11.4
postgres安装方式: yum 安装 
##### 安装 contrib 
yum安装的postgres 默认不带这个插件包  需要手动安装contrib依赖 
```bash
sudo yum install postgresql11-contrib.x86_64
```
##### 使用插件   
链接到数据库服务 执行如下sql   
```sql
create extension  插件名称;
```
##### 查询当前数据库已经在使用的插件 
```sql
select * from pg_extension;
```

#### 总结 
postgres不管是从功能 还是单机性能 还是挺厉害的   
插件也丰富 适合做企业项目 应对各种稀奇古怪的需求  

