---
title: linux-shell脚本入参笔记
comments: true
date: 2018-09-14 14:00:44
categories: 笔记
tags: 
 - linux
 - shell 
---
####前言 
linux shell脚本中 获取参数 大致上 有 
* 直接 xxx.sh p1 p2
直接通过获取 脚本后面跟着的参数 进行操作
* getopts
获取 脚本后面的选项 
* getopt 
获取脚本后面的可变选项
* xgrep 
xargs + grep 
* read
从键盘或者文件中读取参数  
这几种方案  
参考地址:   
http://diseng.github.io/2015/04/15/shell-argvs-type
#### 直接接收shell脚本后面的参数 
调用脚本的格式   
```
xxx.sh arg1 arg2 
```
脚本中获取参数的方式  
$1 = arg1 
$2 = arg2 

注意点:
* $0:指脚本本身 
* $(num)脚本后面跟着的参数 num从1开始 
* $#:入参的总数 不包含脚本本身
* $@:入参列表 不包含脚本本身
* $*:和$@相同，但”$” 和 “$@”(加引号)并不同,”$“将所有的参数解释成一个字符串,而”$@”是一个参数数组
例子:   
```
echo '
#!/bin/bash
echo $1
echo $2 
echo $0
echo $#
echo $@
echo $*    
' > test.sh 
chmod +x ./test.sh
./test.sh ming  jiu  
```
#### getopts
带选项方式接收入参  
调用脚本的方式
```
xxx.sh -a -b 'xx'
```
定义选项的方式 
定义ab两个选项  a不带:不接收参数  b带:必须接收参数
```
getopts "ab:" 
```
例子:
```
echo '
#!/bin/bash
while getopts "ab:" arg
do 
  case $arg in 
       a)
          echo "选项$arg"
          ;;
       b)
          echo "选项$arg,参数:$OPTARG"
          ;;
  esac           
done
' > test.sh
chmod +x ./test.sh
./test.sh -a 
./test.sh -b ming
./test.sh -a -b ming 
./test.sh -b 
```
#### getopt 
增强版本的getopts 可以接收长参数、可以指定可选参数  
11


#### xgrep

#### read 

####总结 




