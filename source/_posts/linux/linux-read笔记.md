---
title: linux-read笔记
comments: true
categories: 笔记
tags:
  - linux
abbrlink: 9e17b242
date: 2018-09-12 16:46:08
---
#### 前言
read 接收输入的数据 写脚本经常用的命令 
之前都是瞎鸡儿用 从来没考虑仔细看看 文档  这次有时间 看看 记录下这篇笔记
#### 实例
##### 读取键盘输入 
```
echo  '
#!/bin/bash
echo "请输入名字:"
read name
echo "你的名字:$name"
exit 0 
' > testRead.sh 
chmod +x ./testRead.sh 
./testRead.sh 
```
1:带提示的read
```
echo '
#!/bin/bash
read -p "请输入名字:" name 
echo "你的名字:$name"
exit 0
' > testRead.sh 
chmod +x ./testRead.sh
./testRead.sh
```
2:多个参数的read
经过空格分割参数数量等于指定的变量数量
```
echo '
#!/bin/bash
read -p "请输入多个名字 空格分割:" name1 name2
echo "姓名1:$name1"
echo "姓名2:$name2"
' > testRead.sh
chmod +x ./testRead.sh
./testRead.sh
```
经过空格分割的参数小于指定的变量数量   
当经过空格分割的参数小于指定的变量数量的时候 多余的变量为空  
提示输入的时候 输入 'ming'   
```
echo '
#!/bin/bash
read -p "请输入多个名字:" name1 name2
echo "姓名1:$name1"
echo "姓名2:$name2"
' > testRead.sh
chmod +x ./testRead.sh
./testRead.sh
```
经过空格分割的参数大于指定变量的数量   
当经过空格分割的参数大于指定变量的数量  超出部分的数据会赋值给最后一个变量   
提示输入的时候 输入 'ming jiu xu'    
```
echo '
#!/bin/bash
read -p "请输入多个名字:" name1  name2
echo "姓名1:$name1"
echo "姓名2:$name2"
' > testRead.sh
chmod +x ./testRead.sh
./testRead.sh
```
3:使用内置的环境变量调用输入的数据   
使用REPLY环境变量获取此次输入的数据  
```
echo '
#!/bin/bash
read -p "请输入多个名字:" 
echo "姓名1:$REPLY"
' > testRead.sh
chmod +x ./testRead.sh
./testRead.sh
```
4:限制时间的read   
使用 read 的-t参数来设定read等待时间    
执行不进行操作等待5s后超时  
```
echo '
#!/bin/bash 
if read -t 5 -p "请输入名字5s超时:" name
then 
  echo "姓名:$name"
else
  echo "time out !!!!"  
fi 
exit 0   
' > testRead.sh 
chmod +x ./testRead.sh
./testRead.sh
```
5:输入敏感字符不显示在终端
使用-s参数 不在终端显示输入的数据 
```
echo '
#!/bin/bash
read -s -p "请输入密码:" pwd
echo "$pwd"
exit 0 
' > testRead.sh
chmod +x ./testRead.sh
./testRead.sh
```
##### 读取文件
准备文件  
```
echo 'nihao1
nihao2
nihao3' > test.txt
```
* 使用-u选项读取文件 
```
echo '
#!/bin/bash 
exec 3< test.txt 
count = 0 
while read -u 3 var 
do 
  let count=$count+1 
  echo "test.txt第$count行:$var"
done 
echo "结束"
exec 3<&-   
' > readFile.sh
chmod +x ./readFile.sh
./readFile.sh 
```
* 使用管道read 文件  
需要注意 管道是新开启一个进程执行  那么 只有在管道的进程中 count =行数  
```
echo '
#!/bin/bash
count=0
cat test.txt | while read var 
do 
  let count=$count+1
  echo "第$count行:$var"
done 
echo "结束"  
exit 0 
' > readFile.sh 
chmod +x ./readFile.sh
./readFile.sh 
```
* 使用重定向
```
echo '
#!/bin/bash
count=0
while read var 
do 
  let count=$count+1
  echo "第$count行:$var"
done  < test.txt 
echo "结束"
exit 0 
' > readFile.sh
chmod +x ./readFile.sh 
./readFile.sh 
```
#### 注意   
\在linux常规操作中没有做任何特殊处理的话 它是表示续行符 read也是一样的 默认处理\续行符 如果需要read 把所有的特殊符号都不进行处理 加上 -r选项  
#### 总结  
通过read 读取键盘输入或者文件 中的信息 是经常的操作     
读取键盘的输入主要就考虑 是不是要有提示(-p) 是不是要设置超时时间(-t) 终端打不打印明文(-s)   
读取文本的时候 主要考虑 是直接使用read读取 还是使用重定向还是管道去处理 当使用管道读取的时候 一定要注意 管道是新建一个进程去执行 变量不共享   
不官是键盘还是读文本 如果不需要处理特殊字符 加上-r参数  