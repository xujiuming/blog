---
title: iptables笔记
categories: 笔记
tags:
  - linux
abbrlink: 5bcc860
date: 2017-11-11 00:00:00
---
##iptables linux上常用防火墙 
参考地址：http://www.cnblogs.com/kevingrace/p/6265113.html
#### iptables 和netfilter关系
netfilter 是linux内核中实现的包过滤的函数相关的东西
iptables 是一个管理防火墙的工具 真正实现防火墙的还是netfilter
#### 语法
iptables (选项) (参数)
#### 选项
man iptables 

-t<表>：指定要操纵的表；  
-A：向规则链中添加条目；   
-D：从规则链中删除条目；   
-I：向规则链中插入条目；   
-R：替换规则链中的条目；   
-L：显示规则链中已有的条目；   
-F：清楚规则链中已有的条目；   
-Z：清空规则链中的数据包计算器和字节计数器；   
-X:删除规则链  
-N：创建新的用户自定义规则链；   
-P：定义规则链中的默认目标；   
-h：显示帮助信息；   
-p：指定要匹配的数据包协议类型；   
-s：指定要匹配的数据包源ip地址；   
-j<目标>：指定要跳转的目标；   
-i<网络接口>：指定数据包进入本机的网络接口；   
-o<网络接口>：指定数据包要离开本机所使用的网络接口。  

```
iptables -t 表名 <-A/I/D/R> 规则链名 [规则号] <-i/o 网卡名> -p 协议名 <-s 源IP/源子网> --sport 源端口 <-d 目标IP/目标子网> --dport 目标端口 -j 动作

```
![iptables用法](http://upload-images.jianshu.io/upload_images/3905525-b576da7a2c520fbc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![iptables 参数](http://upload-images.jianshu.io/upload_images/3905525-7e8ee8a4829024de.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

####表名
提供特定的功能 
|名称|功能|备注| 
|:--|:--|:--|
|raw|数据跟踪处理|网址过滤|
|mangle|数据包修改，包重构|qds|
|nat|地址转换,网络地址转换|网关路由|
|filter|包过滤|防火墙规则|

#### 链 名

|名称|功能|备注|
|:--|:--|:--|
|INPUT|输入数据包|对输入数据包的规则|
|OUTPUT|输出数据包|对输出数据包的规则|
|FORWARD|转发数据包|对转发数据的规则|
|PREROUTING|目标地址转换数据包|DANT|
|POSTOUTING|源地址转换数据包|SNAT|

#### 表 和 链的关系图
![四表五链](http://upload-images.jianshu.io/upload_images/3905525-6ca6c7de7205562d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![请求流转流程图](http://upload-images.jianshu.io/upload_images/3905525-fc2f411c47c80d5a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![请求流转详情流程图](http://upload-images.jianshu.io/upload_images/3905525-d28fca015fec26d2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

####动作

|名称|功能|备注|
|:--|:--|:--|
|ACCEPT|接受数据包|常用  打开某个端口接受数据|
|DROP|丢弃数据包| 常用  禁止某个端口访问|
|MATCH|匹配|符合某个ip 或者端口 |
|REJECT|丢弃数据包 |可以向发送这个包的源主机发送错误消息|
|TARGET| 指定动作 |说明如何处理这个数据包 接受 丢弃 拒绝|
|JUMP|标识跳转到那个链上||
|RULE|一个或者多个匹配及其对应目标||
|REDIRECT|重定向、映射、透明代理||
|SNAT|源地址转换||
|DNAT|目标地址转换||
|MASQUERADE|ip伪装|nat 用于adsl|
|LOG|日志记录||

####实例
* 查看已有的iptables 规则
```
//查看iptables 列表 带数字 
iptables -L -n -v 
//查看iptables 以序号标记
iptables -L -n --line-numbers
```
* 清除已有的iptables 规则
```
iptables -F   --flush ：清楚规则链中已有的条目；
iptables -X    --delete-chain  删除 规则链
iptables -Z    --flush ：清空规则链中的数据包计算器和字节计数器；   
```
*删除 INPUT中的序号为num的规则
```
iptables -D INPUT num
```
*开放指定端口 就是接受数据包 
```
# 开启本地回环接口 运行本机访问本机
iptables -A INPUT -s 127.0.0.1 -j ACCEPT 
# 允许已建立或相关连的通行
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
#允许本机所有向外访问
iptables -A OUTPUT -j ACCEPT
#允许 访问某个端口
iptables -A INPUT -p tcp  --dport 22 -j ACCEPT  允许访问 22端口
# 禁止其他未允许的规则访问
iptables -A INPUT -j reject
# 禁止其他未允许的转发数据包访问
iptables -A FORWARD -j reject
```
* 屏蔽ip 
```
# 屏蔽单个ip
iptables -I INPUT -s 123.1.1.1 -j DROP
# 屏蔽整个网段 123.0.0.0 - 123.255.255.254
iptables -I INPUT -s 123.0.0.0/8 -j DROP
# 屏蔽123.1.0.0 - 123.1.255.254网段
iptables -I INPUT -s 123.1.0.0/16 -j DROP
# 屏蔽 123.1.1.0 - 123.1.1.254网段
iptables -i INPUT -s 123.1.1.0/24 -j DROP 
```
*
*
*
