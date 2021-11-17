---
title: tcpdump抓取网络数据包笔记
comments: true
categories: 笔记
tags:
  - tcp
  - udp
abbrlink: 98d70e96
date: 2021-11-17 14:42:35
---
#### 前言
记录下抓包工具和相关的查看工具使用方式 
tcpdump wireshark 
主要记录tcpdump 
>参考文档: 
> https://www.runoob.com/linux/linux-comm-tcpdump.html
> https://www.cnblogs.com/f-ck-need-u/p/7064286.html

#### tcpdump命令详解
```shell
tcpdump [-adeflnNOpqStvx][-c<数据包数目>][-dd][-ddd][-F<表达文件>][-i<网络界面>][-r<数据包文件>][-s<数据包大小>][-tt][-T<数据包类型>][-vv][-w<数据包文件>][输出数据栏位]
```

|参数|功能|备注|   
|:---|:--|:--|   
|-a| 尝试将网络和广播地址转换成名称。||
|-c|\<数据包数目> 收到指定的数据包数目后，就停止进行倾倒操作。||
|-d| 把编译过的数据包编码转换成可阅读的格式，并倾倒到标准输出。||
|-dd| 把编译过的数据包编码转换成C语言的格式，并倾倒到标准输出。||
|-ddd| 把编译过的数据包编码转换成十进制数字的格式，并倾倒到标准输出。||
|-e| 在每列倾倒资料上显示连接层级的文件头。||
|-f| 用数字显示网际网络地址。||
|-F|\<表达文件> 指定内含表达方式的文件。||
|-i|\<网络界面> 使用指定的网络截面送出数据包。||
|-l| 使用标准输出列的缓冲区。||
|-n| 不把主机的网络地址转换成名字。||
|-N| 不列出域名。||
|-O| 不将数据包编码最佳化。||
|-p| 不让网络界面进入混杂模式。||
|-q| 快速输出，仅列出少数的传输协议信息。||
|-r|\<数据包文件> 从指定的文件读取数据包数据。||
|-s|\<数据包大小> 设置每个数据包的大小。||
|-S| 用绝对而非相对数值列出TCP关联数。||
|-t| 在每列倾倒资料上不显示时间戳记。||
|-tt| 在每列倾倒资料上显示未经格式化的时间戳记。||
|-T|\<数据包类型> 强制将表达方式所指定的数据包转译成设置的数据包类型。||
|-v| 详细显示指令执行过程。||
|-vv| 更详细显示指令执行过程。||
|-x| 用十六进制字码列出数据包资料。||
|-w|\<数据包文件> 把数据包数据写入指定的文件。||
#### 常用命令组合 
```shell
# 显示当前网络包信息
tcpdump  
# 指定网卡eth0抓取网络包 
tcpdump -i eth0
# 抓取指定网卡eth0指定20数量包
tcpdump  -i eth0 -c 20 
# 抓取指定网卡eth0指定20数量的包保存为cap文件 
tcpdump -i eth0 -c 20 -w eth0.cap 
#简略显示包内容 
tcpdump -q 
#显示包的16进制和ASCII两种方式同时输出
tcpdump -XX 
#监视指定主机a的包 
tcpdump host a
#打印 主机 a->b 或者a->c的包 
tcpdump host a and \( b or c \)
#打印主机 a和其他主机之间的ip数据包 但是不包含b主机 
tcpdump ip host a and not b
#截获主机a发送的所有数据
tcpdump src host a
#监视所有发送到主机a的数据包
tcpdump dst host a
#监视指定主机a和端口22的数据包
tcpdump tcp port 22 and host a
#对本机的udp 123端口进行监视(123为ntp的服务端口)
tcpdump udp port 123
#监视指定网络的数据包，如本机与192.168网段通信的数据包，"-c 10"表示只抓取10个包
tcpdump -c 10 net 192.168
#打印所有通过网关snup的ftp数据包(注意,表达式被单引号括起来了,这可以防止shell对其中的括号进行错误解析)
tcpdump 'gateway snup and (port ftp or ftp-data)'
#抓取ping包
tcpdump -c 5 -nn -i eth0 icmp 
#抓取ping包 来源于 192.168.1.1
tcpdump -c 5 -nn -i eth0 icmp and src 192.168.1.1
#抓取到本机22端口包 
tcpdump -c 10 -nn -i eth0 tcp dst port 22  
#解析包数据
tcpdump -c 2 -q -XX -vvv -nn -i eth0 tcp dst port 22
```

#### 总结 
在linux下 基本上是没得gui界面的    
简单的问题 可以直接tcpdump抓包现场分析       
麻烦的问题 使用tcpdump 抓包 打包成cap文件 下载下来 用wireshark之类的工具分析        