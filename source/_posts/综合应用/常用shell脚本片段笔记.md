---
title: 常用shell脚本片段笔记
comments: true
date: 2021-11-18 15:00:10
categories: 笔记
tags:
 - shell
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

#### 总结 
记录一下常用的shell 片段   
免得要写的时候 一直找     
