---
title: shell捕获信号量处理trap笔记
comments: true
categories: 笔记
tags:
  - linux
  - shell
abbrlink: b3da6d14
date: 2019-08-14 15:33:28
---
#### 前言  
写shell的时候  经常要求执行异常或者中断脚本之后 需要清理现场或者提示用户  
之前一直是用if判断命令执行结果来做的 比较麻烦 而且繁琐 小脚本可以 稍微复杂一点的脚本就麻烦了 
这个时候 trap 就可以发挥作用了 

参考文档:  
http://linux.51yip.com/search/trap  

#### 常用方式 
> trap --help  查看文档    

```bash
trap 当接受信号执行的命令  接受那些信号  
```

##### 监听信号量 
监听指定信号量  触发命令  
脚本内容如下:
```bash
#!/usr/bin/env bash
#监听 SIGINT  使用 ctrl+c 终端脚本的时候会出现 print SIGINT  
trap "echo 'print SIGINT'" SIGINT
#监听  SIGKILL 9  无法处理 和阻塞  通知-9信号量的时候 不会有任何处理      
trap "echo  'print SIGKILL '" SIGKILL
#监听 SIGTERM  向进程发送15信号量的时候  会输出 print SIGTERM
trap "echo ' print  SIGTERM'" SIGTERM
sleep 30
```
使用 ps获取这个脚本的pid  使用kill 信号量  pid 向进程发送信号量 
即可 

##### 对脚本执行情况处理  
###### EXIT
脚本退出的时候 触发    
```bash
tee test.sh <<- 'EOF' 
#!/usr/bin/env bash
trap "echo '脚本关闭'" EXIT
sleep 10 
exit
EOF
chmod +x ./test.sh 
./test.sh
```
######  DEBUG
脚本每条命令执行的时候 都会触发一次    
```bash
tee test.sh <<- 'EOF' 
#!/usr/bin/env bash
trap  "echo '脚本调试'$?" DEBUG
echo ming1
echo ming2
echo ming3
echo ming4
EOF
chmod +x ./test.sh 
./test.sh
```
######  ERR
脚本执行异常的时候 触发    
```bash
tee test.sh <<- 'EOF' 
#!/usr/bin/env bash
trap "echo '脚本执行异常'" ERR
top  sssx
EOF
chmod +x ./test.sh 
./test.sh
```
###### RETURN
当存在函数返回或者source 执行其他脚本的时候 触发     
```bash
tee test.sh <<- 'EOF'
#!/usr/bin/env  bash
trap "echo '函数返回或者source执行其他脚本'" RETURN
source ~/.bash_profile 
EOF
chmod +x ./test.sh 
./test.sh
```
#### 总结 
一个好的脚本 应该是不管成功失败 都应该做好相关处理     
使用 trap 能够更好的处理脚本执行过程中各种结果  使脚本更加好用      