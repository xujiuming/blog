---
title: postgres使用pg_upgrade升级大版本笔记
comments: true
abbrlink: 872cea1b
date: 2019-07-30 15:41:03
categories: 笔记
tags:
 - linux
 - postgres 
---
#### 前言
。。。 准备在线上测试环境搞个postgres主从的时候 发现版本竟然是10.3的 果断升级为最新的11.4版本的  
之前一直都是直接 dump数据然后卸载重新安装 然后把数据导入进去  不过postgres 提供一个现成的工具 刚好尝试一下    

> 参考文档: https://www.postgresql.org/docs/current/pgupgrade.html  
> 参考文档:https://www.postgresql.org/download/


#### 准备工作
* 备份原10.3版本的postgres上的数据 
* 安装11.4版本的postgres

#### 实际操作
> 服务器操作系统是CentOs7 
##### 备份数据
利用pg_dump备份 
```bash
pg_dump -h 数据库访问地址  -w -U 用户名 -T  -p 数据库端口 数据库名 > 备份文件
```
从备份还原数据
```bash
psql  -h 数据库访问地址  -p 数据库端口 -w -d 数据库名 -U 用户名 < 备份文件
```

##### 安装postgres11.4 
```bash
yum install https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm

yum install postgresql11

yum install postgresql11-server
# 默认数据目录为 /var/lib/pgsql/11/data/
/usr/pgsql-11/bin/postgresql-11-setup initdb
systemctl enable postgresql-11
#先不启动 原postgres10 服务还占用5432端口 如果要启动 请修改 /var/lib/pgsql/11/data/ 中的 postgres.conf 调整启动端口  
# systemctl start postgresql-11
```
##### 关闭所有postgres
升级时候 原postgres10 要关闭     
```bash
ps -aux |grep postgres 
# 关闭那个版本的 postgres 使用那个版本的pg_ctl  
pg_ctl stop -D 数据目录  
```
##### 使用pg_upgrade升级 
* pg_upgrade升级有两种模式   

|名称|功能|备注|  
|:---|:--|:--|
|link模式|新数据数据目录中建立链接到老数据目录|此种模式老数据库和新数据库无法同时启动使用|
|copy模式|完全复制老数据目录到信数据目录|此目录调整端口后不影响老数据库运行 新旧数据库可以同时运行|

> link模式 只是在新数据目录上建立一个链接到旧数据目录上 升级会非常快   、但是copy模式会跟原库脱离关系 独立存在 如果数据量不大 建议 copy模式  

* 使用新数据库的pg_upgrade
 由于此时系统中存在 postgres10和postgres11 两个都存在postgres相关的工具 如pg_upgrade 请使用postgres11的pg_upgrade 
 
 执行如下命令:
 ```bash
#只校验 不真实执行 升级操作 
/usr/pgsql-11/bin/pg_upgrade --check -b /usr/pgsql-10/bin -B /usr/pgsql-11/bin   -d /var/lib/pgsql/10/data -D /var/lib/pgsql/11/data  

# copy模式升级   
/usr/pgsql-11/bin/pg_upgrade -b /usr/pgsql-10/bin -B /usr/pgsql-11/bin   -d /var/lib/pgsql/10/data -D /var/lib/pgsql/11/data  

# link模式升级   
#/usr/pgsql-11/bin/pg_upgrade  --link -b /usr/pgsql-10/bin -B /usr/pgsql-11/bin   -d /var/lib/pgsql/10/data -D /var/lib/pgsql/11/data  
```

pg_upgrade命令使用详情直接参考:
```bash
pg_upgrade --help 
```
##### 收尾工作
* 按需调整pg_hba.conf 和postgresql.conf 
例如有一些配置和设定 需要调整 的 即可调整 
*  删除或者清理 postgres10的环境变量 

##### 启动postgres11

```bash
#使用 postgres11的pg_ctl 
pg_ctl start  -D /var/lib/pgsql/11/data 
```

#### 问题处理
* 老postgres安装了一些插件  
需要在新的postgres上安装同样的插件  
```text
无法加载库 "$libdir/ltree": 错误:  无法访问文件 "$libdir/ltree": 没有那个文件或目录
无法加载库 "$libdir/dblink": 错误:  无法访问文件 "$libdir/dblink": 没有那个文件或目录
无法加载库 "$libdir/file_fdw": 错误:  无法访问文件 "$libdir/file_fdw": 没有那个文件或目录
无法加载库 "$libdir/postgres_fdw": 错误:  无法访问文件 "$libdir/postgres_fdw": 没有那个文件或目录
无法加载库 "$libdir/pg_stat_statements": 错误:  无法访问文件 "$libdir/pg_stat_statements": 没有那个文件或目录
```
参考   

{% post_link postgres/postgres安装插件笔记 %}     

安装插件即可 

#### 总结 
postgres 官方提供升级的方案 还是挺不错的    
用pg_upgrade 或者用复制数据的方式 都差不多   
只要在开始升级之前 做好备份 一切都好说     
备份+停机升级  那种方案也不会出毛病   

