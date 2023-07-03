---
title: wsl2安装arch笔记
comments: true
categories: 笔记
tags:
  - wsl2
  - arch
abbrlink: de6fce4f
date: 2023-02-15 13:39:39
---

#### 前言

一直在用wsl2-ubuntu 感觉没劲 干脆重装arch 记录一下使用的指令 方便速查

> wsl常用命令: https://learn.microsoft.com/zh-cn/windows/wsl/basic-commands?source=recommendations  

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

5. 安装一些基础工具 
```shell
sudo pacman -Syyu gcc git make unzip zip  vim python python-pip screenfetch tree  openssh vi wget tmux 
```

7. 安装yay   
> makepkg 必须在非root用户执行  

```shell
# clone yay代码
git clone https://aur.archlinux.org/yay.git
cd yay
# 增加golang的镜像  go 1.13以上版本  
export GO111MODULE=on
export GOPROXY=https://goproxy.cn
# build yay   
makepkg -si
```

8. 常用wsl命令 
```shell
#查看当前wsl镜像列表 
wsl -l -v 
#更改镜像wsl版本
wsl --set-default-version 2  ${name}
#停机
wsl --shutdown ${name}
#注销镜像
wsl --unregister ${name}
```

9. 常用组件安装
```shell
# 安装oh my zsh 
wget https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh
chmod +x ./install.sh 
./install.sh 
# 安装zsh 命令提示插件
git clone git://github.com/zsh-users/zsh-autosuggestions $ZSH_CUSTOM/plugins/zsh-autosuggestions
# 配置zsh的内容
plugins=(git extract sudo  zsh-autosuggestions)

# 安装sdkman  
#安装sdk man  安装 jvm相关工具 
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
sdk install java 
sdk install maven 
sdk install mvnd 
sdk install groovy 
```

#### wsl高级配置
>参考资料:      
> https://devblogs.microsoft.com/commandline/systemd-support-is-now-available-in-wsl/#set-the-systemd-flag-set-in-your-wsl-distro-settings     
> https://learn.microsoft.com/zh-cn/windows/wsl/wsl-config#wslconf    

.wslconfig ，用于在 WSL 2 上运行的所有已安装分发版 全局 配置设置。
wsl.conf 为 WSL 1 或 WSL 2 上运行的 Linux 发行版配置 每个分发 版的设置。

##### 开启systemd 
* 当前linux版本生效   
在linux内部输入
```shell
echo '[boot]
systemd=true' > /etc/wsl.conf
```
* 所有wsl 子系统生效 新增 c://user/【用户名】/.wslconfig 文件
```text
[boot]
systemd=true
```

> 重启wsl 在powershell中输入 【 wsl --shutdown】 关机之后等8s    
> 查看启动的systemd情况 【systemctl】

##### wsl2开启GPU加速 
> https://devblogs.microsoft.com/commandline/d3d12-gpu-video-acceleration-in-the-windows-subsystem-for-linux-now-available/

* 前置要求
```text
wsl version >= 1.1.0   
安装支持linux gpu的linux 版本  尽量使用微软商店的版本  
wsl 启动systemd  
```
* ubuntu下需要安装的组件 
```shell
sudo apt update 
# 检查mesa 
sudo apt list mesa-va-drivers -a  
# 安装ppa  
sudo add-apt-repository ppa:oibaf/graphics-drivers
sudo apt-get update && sudo apt-get upgrade
sudo apt-get install ppa-purge
sudo ppa-purge ppa:oibaf/graphics-drivers
sudo apt-get update && sudo apt-get upgrade
# wslg设置视频加速  
#安装 vainfo（和 libva 依赖项）
sudo apt-get install vainfo
#安装台面库 *（如果从台面源代码构建，请跳过此操作）
sudo apt-get mesa-va-drivers
#配置 libva 环境。你可能希望在 ~/.bashrc 文件中添加它，因为它在每个新的 WSL 控制台会话上都是必需的。
export LIBVA_DRIVER_NAME=d3d12
#枚举当前硬件的 libva 功能
vainfo --display drm --device /dev/dri/card0
```

> 使用wsl gpu加速 要去查看和验证当前显卡和驱动 是否支持  

#### 总结
wsl 安装arch 各种大神 已经打包了很多方式了 
安装完成之后 还需要做一些调整适配 安装zsh、开发环境之类的 