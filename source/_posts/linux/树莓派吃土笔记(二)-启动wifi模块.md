---
title: 树莓派链接wifi
categories: 笔记
tags:
  - linux
abbrlink: 8634a6bb
date: 2017-11-11 00:00:00
---

####买回来好久 一直都是被舍友拿去垫电脑去了 今天有时间 拿出来把wifi模块启动起来 以后 就算垫桌子 也能用起来
#####前提条件
1:pi能正常启动
2:能链接pi的shell
3:pi的版本必须由无线网卡  pi 3b版本 带了无线网卡了
#####具体操作
1:登录shell 
2:查看 附近wifi (多个无线网卡的大兄弟 请使用 ifconfig 查看选择一个网卡) 一般一个无线网卡 是wlan0 
```
sudo iwlist wlan0 scan
```
每一个cell都是一个wifi热点 essid 是名字(中文的wifi名字是显示的\xSS这种忽略即可 反正设置的时候直接输入中文即可)
   
注意:
如果由中文的wifi热点名字 会转成其他格式的字符串 会出现\xAs\xDD....   
这样的 如果你需要转换成相应的中文 可以尝试用python来转看看
具体操作如下:
         打开python控制台
         输入
```
 str=b'乱码的字符串'
 print (str.decode('utf-8'))
```
          
  
  
3:编辑 wifi模块的配置文件 /etc/wpa_supplicant/wpa_supoplicant.conf
```
##设置network中的参数
    network={  
        ssid="wifi热点名称(中文直接输入 不需要转码)"  
        psk="wifi密码"  
    }  
```
4:重启
```
#重启
shutdown -r now 
```
5:还是先用线链接pi的shell 然后查看 network的信息 
```
ifconfig
```
```
eth0      Link encap:Ethernet  HWaddr b8:27:eb:43:64:4d  
          UP BROADCAST MULTICAST  MTU:1500  Metric:1
          RX packets:224 errors:0 dropped:0 overruns:0 frame:0
          TX packets:166 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:20583 (20.1 KiB)  TX bytes:23454 (22.9 KiB)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:136 errors:0 dropped:0 overruns:0 frame:0
          TX packets:136 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1 
          RX bytes:11472 (11.2 KiB)  TX bytes:11472 (11.2 KiB)

wlan0     Link encap:Ethernet  HWaddr b8:27:eb:16:31:18  
          inet addr:192.168.3.111  Bcast:192.168.3.255  Mask:255.255.255.0
          inet6 addr: fe80::bd80:706f:310d:a21b/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:89315 errors:0 dropped:31 overruns:0 frame:0
          TX packets:63804 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:125457894 (119.6 MiB)  TX bytes:6884073 (6.5 MiB)

```
无线网卡的ip是 wlan0中的 第二行 inet addr:192.168.3.111
这个时候可以拔掉网线使用 wlan0中的ip链接pi了 
####总结:树莓派 3b版本中已经集成了wifi模块 只需要配置wifi热点信息重启就行了
