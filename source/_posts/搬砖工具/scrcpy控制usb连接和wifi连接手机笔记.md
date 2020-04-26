---
title: scrcpy控制usb连接和wifi连接手机笔记
comments: true
categories: 笔记
tags:
  - scrcpy
abbrlink: 7d94a3fb
date: 2020-04-26 17:54:27
---
#### 前言
用电脑控制安卓手机 很多工具 如收费向日葵等  
在同局域网或者干脆可以线连的情况下 使用scrcpy连接也是蛮方便的 

> 官网:https://github.com/Genymobile/scrcpy

#### scrcpy安装
只记录debian系列的安装 其他平台请查询官网 

* debian系列安装  
```shell script
#由于scrcpy以来adb  需要先安装adb
sudo apt install -y android-tools-adb
#安装 scrcpy 
sudo apt install -y scrcpy
# 如果安装不了 可以尝试使用snap安装
sudo snap install scrcpy 
```

#### 环境要求 
* 安卓版本 >5.0
* 开启adb调试 (开启开发模式)


#### 连接手机 

##### 线连(usb连接)

1. 插上usb线
2. 查看设备
```shell script
adb usb 
```
3. 打开 scrcpy 
```shell script
scrcpy 
```
##### wifi连接
> 手机和电脑必须在同局域网下   

1. 插上usb线
2. adb启用 tcp/ip
```shell script
adb tcpip 5555
```
3. 拔下usb线
4. 查看手机ip 
5. adb连接设备
```shell script
adb connect 设备ip:5555
```
6. 运行 scrcpy
```shell script
scrcpy 
```
#### scrcpy启动配置 
##### 调整码率和最大分辨率
```shell script
scrcpy --bit-rate 2M --max-size 800
scrcpy -b2M -m800
```
##### 窗口配置
```shell script
# 调整窗口标题 
scrcpy --window-title 'ming设备'
# 调整位置和大小 
scrcpy --window-x 100 --window-y 100 --window-width 800 --window-height 600
# 无边界
scrcpy --window-borderless
# 固定顶部
scrcpy --window-borderless
# 全屏 
scrcpy --fullscreen
scrcpy -f
```
##### 显示触摸
```shell script
scrcpy --show-touches
scrcpy -t
```

#### 窗口快捷键 

|功能|快捷键|备注|
|:----|:--|:---|
|切换全屏模式|Ctrl+f||
|将窗口调整到1：1（像素完美）|Ctrl+g||
|调整窗口大小以删除黑色边框|	Ctrl+x 双击1||	
|点击HOME|Ctrl+h中间点击||
|点击BACK|Ctrl+b右键单击2||
|点击APP_SWITCH|	Ctrl+s||
|点击MENU|Ctrl+m||
|点击VOLUME_UP|Ctrl+↑ （向上）||
|点击VOLUME_DOWN|Ctrl+↓ （向下）||
|点击POWER|Ctrl+p||
|打开电源|右键单击2||
|关闭设备屏幕（保持镜像）|Ctrl+o||
|旋转设备屏幕|Ctrl+r||
|展开通知面板|Ctrl+n||
|折叠通知面板|Ctrl+Shift+n||
|将设备剪贴板复制到计算机|Ctrl+c||
|将计算机剪贴板粘贴到设备|Ctrl+v||
|将计算机剪贴板复制到设备|Ctrl+Shift+v||
|启用/禁用 FPS 计数器（在斯特点上）|Ctrl+i||

#### 总结
scrcpy 使用adb 来远控安卓机器  主要是简单 不卡    