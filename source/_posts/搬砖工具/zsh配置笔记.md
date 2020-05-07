---
title: zsh配置笔记
comments: true
categories: 笔记
tags:
  - zsh
abbrlink: 609f122f
date: 2020-04-13 10:33:46
---
#### 前言
zsh 的确挺好用的  这里记录一下 方便速查 

#### 安装
##### 安装zsh 
ubuntu:  
```shell script
sudo apt install zsh -y 
```

##### 查看/切换shell
查看当前所有安装过的shell
```shell script
cat /etc/shells
```
查看当前shell
```shell script
echo $SHELL
```

切换shell  
```shell script
chsh -s /bin/zsh 
```


##### 安装oh my zsh 
curl方式:
```shell script
sh -c "$(curl -fsSL https://raw.githubusercontent.com/robbyrussell/oh-my-zsh/master/tools/install.sh)"
```
wget方式:
```shell script
sh -c "$(wget https://raw.githubusercontent.com/robbyrussell/oh-my-zsh/master/tools/install.sh -O -)"
```
#### 配置
编辑 ~/.zshrc文件
* 个人常用配置 
>需要安装 zsh-autosuggestions   和 source ~/.zshrc
```text
ZSH_THEME="agnoster"

plugins=(git sudo z web-search extract zsh-autosuggestions sdk)
```
##### 主题
> 参考地址:https://github.com/ohmyzsh/ohmyzsh/wiki/Themes

```text
ZSH_THEME="agnoster"
```
##### 常用插件
###### sudo 
需要输入sudo的语句 连续按两次 Esc 自动在前面加sudo 
* 启用
```shell script
plugins=(git sudo)
```
###### z
跳转目录插件  ‘z xxx’  之前输入过的目录 
* 启用
```shell script
plugins=(git z)
```
* 使用示例
```shell script
z workspaces
```
###### web-search
终端中直接唤起浏览器搜索 
* 启用 
```shell script
plugins=(git web-search)
```
* 使用示例
```shell script
baidu xxx
google xxx
```
###### zsh-autosuggestions 
命令提示插件
* 安装
```shell script
git clone git://github.com/zsh-users/zsh-autosuggestions $ZSH_CUSTOM/plugins/zsh-autosuggestions
```
* 启用
```text
plugins=(git zsh-autosuggestions)
```
###### extract 
使用x 根据后缀快速解压大多数压缩包 如tar.gz tar.xz 等等
* 启用 
```text
plugins=(git extract)
```
* 使用示例
```shell script
x xxx.tar.gz
```
###### sdk
sdkman 管理java相关sdk tools的工具
```shell script
sdk list java 
```
#### 总结
zsh 用的人蛮多的 没啥好说的  就是用  

