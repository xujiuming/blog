---
title: wsl2安装arch笔记 
comments: true 
date: 2022-01-05 13:39:39 
categories: 笔记 
tags:
 - wsl2
 - arch

---

#### 前言

一直在用wsl2-ubuntu 感觉没劲 干脆重装arch 记录一下使用的指令 方便速查

#### 安装

> wsl2 安装arch大致有两种做法 1-直接下载打包好的wsl2-arch 2-使用LxRunOffline安装
> 此处偷懒 直接使用打包好的 不使用LxRunOffline

0. 开启wsl2

> 管理员权限下执行  
> 参考文档:https://docs.microsoft.com/zh-cn/windows/wsl/install

```shell
# 开启wsl 
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
# 开启虚拟机支持  
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
# 下载wsl对linux的支持包 并且安装  
wget https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi
```

1. 下载  
   在 https://github.com/yuk7/ArchWSL/releases 页面中下载最新Arch.zip
2. 安装  
   解压 arch.zip 到一个有权限的目录 例如d:/workspaces/   
   启动 Arch.exe

```shell
# 进入arch  
wsl -d Arch 
```

3. 初始化pacman

```shell
pacman-key --init
pacman-key --populate
```

4. 初始化用户

```shell
#初始化root的密码
passwd 
#新增ming用户
useradd -m  -s /bin/bash ming
#初始化ming的密码
passwd ming 
# 编辑sudoers  增加  ming ALL=(ALL) ALL 
vi /etc/sudoers 
# 设置arch wsl2默认登录用户   
Arch.exe config --default-user {username}
```

5. 安装yay   
> makepkg 必须在非root用户执行  

```shell
# 安装一些工具  
sudo pacman -Syyu gcc  git make    
# clone yay代码
git clone https://aur.archlinux.org/yay.git
cd yay
# build yay   
makepkg -si
```
