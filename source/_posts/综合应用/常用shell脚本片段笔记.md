---
title: 常用shell脚本片段笔记
comments: true
categories: 笔记
tags:
  - shell
abbrlink: 5b91377c
date: 2021-11-18 15:00:10
---
#### 前言
记录一下常用的shell片段 方便自己速查 

#### 示例
>默认基本为bash脚本 其他解释器会特别说明   

##### 获取指定名称的进程并且处理

```shell
NAME="xxxxxx"
ID=`ps -ef | grep "$NAME" | grep -v "grep" | awk '{print $2}'`
echo The process pid is $ID
for id in $ID
do
    # kill process  
    kill -9 $id
    echo killed $id
done

```

##### 备份文件带时间后缀 

```shell
NAME="xxxx"
mv $NAME $NAME'.'`date '+%Y%m%d%H%M%S'`
```

##### nohup启动并且重定向输出 

```shell
# 重定向到指定文件  
nohup echo 'nihao'   > echo.out 2>&1 &
# 不输出nohup日志
nohup echo 'nihao' >/dev/null 2>&1 &
```

##### 常用单机部署java的脚本

* deploy.sh

```shell
#!/bin/bash
#author ming
# date 20211118


# upload package name
OLDJARNAME='xx-latest.jar'
NAMEDEPLOY='xx.jar'

# stop the process before
ID=`ps -ef | grep "$OLDJARNAME" | grep -v "grep" | awk '{print $2}'`
echo The process pid is $ID
for id in $ID
do
    kill -9 $id
    echo killed $id
done

# 备份当前上个版本的jar
mv $OLDJARNAME $OLDJARNAME'.'`date '+%Y%m%d%H%M%S'`
# 将刚刚上传的jar 更名为执行jar
mv $NAMEDEPLOY $OLDJARNAME
nohup  java -jar -Xmx1g $OLDJARNAME --spring.profiles.active=test  > nohup.out 2>&1 &
```

* upload.sh
 
```shell
#!/bin/bash
#author ming
# date 20211118

SSH_HOST='user@host'
WORKER_PATH='/data/xx'

# 复制jar 到服务器
scp  ./target/socket-server.jar $SSH_HOST':'$WORKER_PATH
# 复制部署shell脚本到服务器
scp  ./deploy.sh $SSH_HOST':'$WORKER_PATH
# 授权
ssh $SSH_HOST "cd ${WORKER_PATH} && chmod +x ./deploy.sh  && ./deploy.sh && tail -f ./nohup.out"

```

#### 总结 
记录一下常用的shell 片段   
免得要写的时候 一直找     
