---
title: win10开启wsl2和基础配置示例
comments: true
categories: 实战
tags:
  - win10
  - wsl
abbrlink: b741fd2
date: 2020-09-29 10:00:58
---
#### 前言  
windows下面 wsl2 算是能还原大多数情况下的linux体验     
由于我喜欢在linux 下开发 但是win+虚拟机linux 对电脑要求太高了 偶尔会出现一些问题 例如卡死之类的   
所以一直在探索    
尝试过    
win+remote linux开发   
win+虚拟机linux 开发   
win+cygwin 开发     
win+wsl 开发    
总的来说   
电脑性能不够又没有其他资源  win+wsl2体验相对来说比较好    
如果电脑性能足够 win+虚拟机linux 比较合适    
或者有资源  win+remote linux 也不错   

#### wsl2安装 
> 参考文档:https://docs.microsoft.com/zh-cn/windows/wsl/install-win10

命令集合  
```shell script
#启用适用于 Linux 的 Windows 子系统
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
# 更新到 WSL 2
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
#下载安装wsl2 内核补丁包
wget https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi
wsl_update_x64.msi
# 将 WSL 2 设置为默认版本
wsl --set-default-version 2
# 在microsoft store中选择合适的linux版本 例如ubuntu 
#https://www.microsoft.com/store/apps/9n6svws3rx71
wsl --list --verbose   
#wsl --set-version <distribution name> <versionNumber>
#wsl --set-default-version 2
```

#### wsl2出现的常规异常处理 
* 使用systemd 管理服务
错误信息:  
```text
System has not been booted with systemd as init system (PID 1). Can't operate.
Failed to connect to bus: Host is down
```
解决办法:
```text
git clone https://github.com/DamionGans/ubuntu-wsl2-systemd-script.git
cd ubuntu-wsl2-systemd-script/
bash ubuntu-wsl2-systemd-script.sh
systemctl 
```

