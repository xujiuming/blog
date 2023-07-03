---
title: postgres基于pgpool读写分离笔记
comments: true
categories: 笔记
tags:
  - postgres
  - db集群
abbrlink: 78706b94
date: 2019-02-25 16:37:49
---
####  前言
db读写分离 是一个优化应用性能 最显著的方式之一  
postgres 本身的 主从同步是很好配置的但是 识别读写语句 java中没有什么特么好的方案 那么就只能采用中间件 来识别postgres的路由做到读写分离 

#### pgpool 介绍
##### 功能
* 连接池    
保持db的链接 当用户名、数据库、协议版本一致的时候 复用链接  
* 复制   
pgpool 管理多个postgres db  当某台db失效 服务不中断 
* 负载均衡
识别select查询 负载均衡到每台db上 
* 限制超过限度的链接
当超过限度 的链接请求的时候 不是立即抛错 而是放入队列中等待执行  
* 并行查询 
当数据库分区之后 可以并行查询 减少总体时间  暂时是实验性功能 无法替代postgres-XC 或者PL/Proxy  
#### 搭建步骤
由于是笔记 只搭建 单节点的pgpool + 一主 一从      
但是事实上  pgpool的ha  和 多主 多从 才是生产标配  
pgpool的ha 由另一篇笔记记录 
多主多从  postgres  你会配置 一主 一丛 那么多主多从 基本上就是改改配置 没啥好说的  
##### 服务器信息

|主机名|ip|角色|数据目录|
|:----|:-|:---|:-----|
|ming-master|192.168.1.211|master+pgpool|/home/postgres/data|
|ming-standby|192.168.1.212|standby|/home/postgres/data|

##### 搭建postgres 主从同步
###### master
修改ming-master机器 postgres配置 
* 配置pg_hba.conf
```
host replication db  0.0.0.0/0 md5
```
* 配置postgresql.conf
```
listen_addresses = '*'
# 流复制必须要设置大于0 
max_wal_senders =5
# 要搭建主从 必须配置为hot_standby
wal_level = hot_standby
```
* 重启master db

###### standby
修改ming-standby机器 postgres配置 
* 使用pg_basebackup 生成基础备份
```bash
pg_basebackup -h 192.168.1.211 -u db -F p -p -x -R -D /home/postgres/data -l db_backup
```
此时 在/home/postgres/data下面有 postgres data相关目录了
* 修改 postgresql.conf
```
hot_standby = on
```
* 启动 standby
```
pg_ctl start -D /home/postgres/data 
```
##### 搭建pgpool  主备模式
* 要求 postgres 节点之间本身有异步流复制 
修改 pgpool.conf
```
listen_addresses = '*'
port = 9999
socket_dir = '/tmp'
pcp_port = 9898
pcp_socket_dir = '/tmp'
enable_pool_bha = on 
pool_passwd ='pool_passwd'
pid_file_name = 'pgpool/run/pgpool.pid'
logdir = '/tmp'


# 第一个节点  
backend_hostname0 = '192.168.1.211'
backend_port0 = 5432
backend_weight0 =1
#第二个节点
backend_hostname1 = '192.168.1.212'
backend_port1 = 5432
backend_weight1 =1

#
replication_mode = off
master_slave_mode =on 
#由于使用的流复制的主从模式  必须设置为 stream
master_slave_sub_mode = 'stream'
# 主备模式可以使用负载均衡  
load_balance_mode = on 
```
* 启动pgpool 
```bash
#后台启动
pgpool
#前台启动
pgpool -n 
```  

#### 总结
postgres 主从和 相关中间件的搭建 还是比较简单方便的  
数据库 一般初级 提升性能 的手段 就是读写分离 
如果 读写分离还不行 那么考虑加上拆库拆表 基本上 就够用了 

