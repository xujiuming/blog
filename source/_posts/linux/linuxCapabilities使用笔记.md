---
title: linuxCapabilities使用笔记
comments: true
categories: 笔记
tags:
  - linux
  - capabilities
abbrlink: bb4b85ad
date: 2021-11-02 16:57:50
---
#### 前言
今天突然有个服务 不能是root用户但是要绑定 80端口   
看到了capabilities这个功能    
顺便记录一下 方便自己速查    

#### 描述 
>参考文档:       
> https://www.cnblogs.com/nf01/articles/10418141.html    
> https://blog.csdn.net/jing_flower/article/details/82692585    
> https://www.cnblogs.com/chenxueqiang/p/13653623.html  setcap java报错解决    


从内核 2.2 开始，Linux 将传统上与超级用户 root 关联的特权划分为不同的单元，称为 capabilites。Capabilites 作为线程(Linux 并不真正区分进程和线程)的属性存在，每个单元可以独立启用和禁用。如此一来，权限检查的过程就变成了：在执行特权操作时，如果进程的有效身份不是 root，就去检查是否具有该特权操作所对应的 capabilites，并以此决定是否可以进行该特权操作。

>详细capabilities的列表: http://man7.org/linux/man-pages/man7/capabilities.7.html

*  选取的部分功能

|capability名称|描述|  
|:---------|:--|
|CAP_AUDIT_CONTROL|启用和禁用内核审计；改变审计过滤规则；检索审计状态和过滤规则|
|CAP_AUDIT_READ|允许通过 multicast netlink 套接字读取审计日志|
|CAP_AUDIT_WRITE|将记录写入内核审计日志|
|CAP_BLOCK_SUSPEND|使用可以阻止系统挂起的特性|
|CAP_CHOWN|修改文件所有者的权限|
|CAP_DAC_OVERRIDE|忽略文件的 DAC 访问限制|
|CAP_DAC_READ_SEARCH|忽略文件读及目录搜索的 DAC 访问限制|
|CAP_FOWNER|忽略文件属主 ID 必须和进程用户 ID 相匹配的限制|
|CAP_FSETID|允许设置文件的 setuid 位|
|CAP_IPC_LOCK|允许锁定共享内存片段|
|CAP_IPC_OWNER|忽略 IPC 所有权检查|
|CAP_KILL|允许对不属于自己的进程发送信号|
|CAP_LEASE|允许修改文件锁的 FL_LEASE 标志|
|CAP_LINUX_IMMUTABLE|允许修改文件的 IMMUTABLE 和 APPEND 属性标志|
|CAP_MAC_ADMIN|允许 MAC 配置或状态更改|
|CAP_MAC_OVERRIDE|覆盖 MAC\(Mandatory Access Control\)|
|CAP_MKNOD|允许使用 mknod\(\) 系统调用|
|CAP_NET_ADMIN|允许执行网络管理任务|
|CAP_NET_BIND_SERVICE|允许绑定到小于 1024 的端口|
|CAP_NET_BROADCAST|允许网络广播和多播访问|
|CAP_NET_RAW|允许使用原始套接字|
|CAP_SETGID|允许改变进程的 GID|
|CAP_SETFCAP|允许为文件设置任意的 capabilities|
|CAP_SETPCAP|参考 capabilities man page|
|CAP_SETUID|允许改变进程的 UID|
|CAP_SYS_ADMIN|允许执行系统管理任务，如加载或卸载文件系统、设置磁盘配额等|
|CAP_SYS_BOOT|允许重新启动系统|
|CAP_SYS_CHROOT|允许使用 chroot\(\) 系统调用|
|CAP_SYS_MODULE|允许插入和删除内核模块|
|CAP_SYS_NICE|允许提升优先级及设置其他进程的优先级|
|CAP_SYS_PACCT|允许执行进程的 BSD 式审计|
|CAP_SYS_PTRACE|允许跟踪任何进程|
|CAP_SYS_RAWIO|允许直接访问 /devport、/dev/mem、/dev/kmem 及原始块设备|
|CAP_SYS_RESOURCE|忽略资源限制|
|CAP_SYS_TIME|允许改变系统时钟|
|CAP_SYS_TTY_CONFIG|允许配置 TTY 设备|
|CAP_SYSLOG|允许使用 syslog\(\) 系统调用|
|CAP_WAKE_ALARM|允许触发一些能唤醒系统的东西\(比如 CLOCK_BOOTTIME_ALARM 计时器\)|

* 常用方式     
```shell
# 设置cap属性
sudo setcap [cap名称]=+eip [命令地址]  
# 查看当前命令的cap
getcap [命令地址]
# 删除命令的cap属性 
sudo setcap -r [命令地址]
```

#### 实践    
##### 授权非root用户java项目可以开启1024下的端口   
直接授权非root用户的java可以绑定1024一下的端口的cap_net_bind_service属性     

> java本身有个bug   授权java cap属性会导致无法连接libjli.so 库  所以需要重写这个库的配置    
> 参考: https://www.cnblogs.com/chenxueqiang/p/13653623.html  setcap java报错解决   
```shell
# 重写java依赖的libjli.so的地址     
sudo  cat '/home/ubuntu/.sdkman/candidates/java/17.0.1-open/lib' >  /etc/ld.so.conf.d/java.conf
# 重新加载ldconfig   
ldconfig -p | grep libjli
# 给java指令添加  cap_net_bind_service属性  
sudo setcap cap_net_bind_service=+eip  .sdkman/candidates/java/17.0.1-open/bin/java
# 查看java指令的 cap属性  
getcap .sdkman/candidates/java/17.0.1-open/bin/java
# sudo   setcap -r .sdkman/candidates/java/17.0.1-open/bin/java
```

#### 总结  
