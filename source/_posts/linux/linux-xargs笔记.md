---
title: linux-xargs笔记
comments: true
categories: 笔记
tags:
  - linux
abbrlink: 1f5f60e8
date: 2018-09-12 15:22:09
---
#### 前言
在需要使用复杂的长命令去解决一些事情的事情 很多时候 需要将标准输出作为参数去使用 而不是当做标准输入去使用  
那这个时候 就需要xargs 将标准输出的值重定向到某个命令的参数上去 并且执行这个命令 

#### 例子
* xargs 会把标准输出中的换行符、空格、制表符当成空格去分割传递给后面执行的命令 
```
echo "aaaaaaaaaaaa" > a.txt
echo "bbbbbbbbbbbb" > b.txt
echo "a.txt\nb.txt" | xargs cat 
#结果应该是  aaaaaaa  bbbbbbb  
```
* 通过 xargs -d 指定标准输出分隔符
```
echo "aaaaaaaaaaaa" > a a.txt
echo "bbbbbbbbbbbb" > b.txt
echo "a a.txtXb.txt"|xargs -dX cat 
```
* 使用 -p 来确定参数是否要继续执行 
```
echo "aaaaaaaaaaaa" > a.txt
echo "bbbbbbbbbbbb" > b.txt
echo "a.txt\nb.txt" | xargs -p cat 
```
* 使用 -p -n 对每隔参数都进行确认 是否要继续执行 
```
echo "aaaaaaaaaaaa" > a.txt
echo "bbbbbbbbbbbb" > b.txt
echo "a.txt\nb.txt" | xargs -p -n1 cat 
```
* 使用 -E 'xxx' 遇到xxx 就停止执行  
注意 当使用 -0 或者-d参数 -E无效  
```
echo "aaaaaaaaaaaa" > a.txt
echo "bbbbbbbbbbbb" > b.txt
echo "a.txt xx b.txt" | xargs -E 'xx' -p -n1 cat 
```
#### 总结
当需要将标准输出定向到某个命令的参数的时候 使用 xargs 去使用 通过 -d指定分隔符  使用 -p -n 一一确定是否执行  通过-E来设定遇到什么符号停止执行   
xargs配合 | 重定向 想把标准输出 当成参数还是当成内容 都随便操作   
