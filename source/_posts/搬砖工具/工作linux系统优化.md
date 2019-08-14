---
title: 工作linux系统初始化
comments: true
categories: 实战
tags: 
  -linux 
abbrlink: fbab8e0
date: 2019-01-29 10:45:26
---
#### 前言
由于强迫症 在工作和学习的时候 必须要用linux 但是linux桌面真的令人为难     
用了n多版本的linux 什么arch、manjaro、centos系列的、debian系列的等等   
最后还是觉得manjaro用来做工作的系统最简单合适 而且少折腾    
#### 初始化常用软件及工具

##### 初始化额外的工具
```
#安装tmux
sudo pacman -Syu tmux net-tools traceroute vim 
```

##### 初始化shadowsocks
科学上网必备软件  不解释 
```
#安装pip
sudo pacman -Syu python3-pip
#安装sslocal
sudo pip3 install shadowsocks
#ss 配置
echo '{
    "server":"地址",
    "server_port":端口,
    "local_address":"127.0.0.1",
    "local_port":1080,
    "password":"密码",
    "timeout":3000,
    "method":"aes-256-cfb"
}' > ss.json
```
##### 初始化docker
docker 安装一些 数据库啊之类的软件 还是很方便的  而且docker技术 现在应该是每个工程师必备的技能了 不会简直说不过去
```
#安装docker
sudo pacman -Syu docker 
#初始化docker 加速配置
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://o4omo0yw.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
sudo systemctl enable docker
```
##### 初始化java相关环境
```
#安装sdk man  安装 jvm相关工具 
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
sdk install java
sdk install maven 
sdk install groovy 
sdk install gradle 
```
##### 初始化wps
ubuntu 的liboffice 其实用起来还行就是有点丑  但是有更加好的选择 wps  良心软件 对linux支持很不错
就是安装过程略麻烦  后面有时间 可以考虑打包成snap的格式使用
###### 下载wps官方deb包    

资源tar包中包含
  
WPS官方网站 ：http://linux.wps.cn/ （区分64位、32位）   
###### 安装 libpng12依赖   
这个依赖是libpng-12.0。不过这个在默认的apt 仓库里没有。所以需要手动下载一下。   

资源tar包中包含 

或者取官方下载 地址：https://packages.debian.org/zh-cn/wheezy/amd64/libpng12-0/download  
```
sudo dpkg -i libpng12-0_1.2.49-1+deb7u2_amd64.deb
```
###### 安装wps   
```
sudo dpkg -i <wps>.deb
```
###### 安装wps 需要的字体  

资源tar包中包含  

下载该字体，解压后将整个wps_symbol_fonts目录拷贝到 /usr/share/fonts/ 目录下   
```
#1.权限设置,执行命令如下 
sudo cd /usr/share/fonts/ 
sudo chmod 755 wps_symbol_fonts 
sudo cd /usr/share/fonts/wps_symbol_fonts 
sudo chmod 644 * 
#2.生成缓存配置信息 
sudo cd /usr/share/fonts/wps_symbol_fonts 
sudo mkfontdir 
sudo mkfontscale 
sudo fc-cache
```
##### 输入法初始化
这里使用sogou的deb包来安装 当然也可以根据arch的wiki上 安装sunpinyin或者googlepinyin之类的   
###### 安装fcitx输入法框架   
```
#安装fcitx 组件 如果不行那么在商店中把所有呆fcitx的软件安装
sudo apt install fcitx fcitx-configtool 
#设置 fcitx相关配置
echo '
#fcitx
export GTK_IM_MODULE=fcitx 
export QT_IM_MODULE=fcitx 
export XMODIFIERS="@im=fcitx"
' >> ~/.xprofile
```
###### 安装sogou输入法  

资源tar包中包含

搜狗官方地址: https://pinyin.sogou.com/linux/?r=pinyin

```
sudo dpkg -i sogoupinyin_2.2.0.0108_amd64.deb
```

##### 初始化 oss-browser   
下载oss-browser压缩包

资源tar包中包含

```
# 安装 依赖 
sudo apt install libgconf2-4
```

##### 初始化oss-ftp
下载oss-ftp压缩包

资源tar包中包含

```
#安装gtk 依赖
sudo  apt install -y python-gtk2
```

#####  无法使用脚本初始化的软件  
###### 开发工具 
使用idea系列的全家桶   
安装 toolbox来管理idea系列的全家桶
   
   
#### 常用可选工具   
|名称|功能|使用方式|
|:---|:--|:------
|tldr|查看某个命令的常规用法| tldr commandName|
|nmon|查看当前机器的性能指标|    nmon|
|tmux|终端分屏工具| tmux （ctrl+b）|
|thefuck|fuck工具 输入指令发生错误直接使用fuck提示| fuck|
|jq|json文件格式化 高亮|jq .  *.json|
####  总结  
由于我是java开发 我的工作系统肯定最主要就是java以及相关的如maven、gradle、groovy、springbootcli之类的  
其实把如果真的想折腾linux 建议还是arch  毕竟瞎折腾    

