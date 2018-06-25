---
title: 分屏工具tmux使用笔记
categories: 笔记
tags:
  - linux
abbrlink: 885dd8c4
date: 2017-11-11 00:00:00
---
linux中 经常使用 终端 一次打开多个终端 很麻烦 不好用  有个很强悍的工具 tmux  很出名的分屏工具
ubuntu安装
```
sudo apt install tmux
```
然后 在终端输入
```
tmux
```
这个时候 终端已经启动tmux  默认是ctrl+b 是快捷键 就是进入tmux切换操作的命令中 例如
将当前终端水平分屏  进入 tmux操作 按下双引号 
```
ctrl+b ---->“
```
垂直分屏幕 进入tmux 按下%
```
ctrl+b---->%
```
可以启动多个不同的窗口 每个窗口可以按照水平垂直分成不同的窗体
窗体之间  切换
```
ctrl+b ---->方向键
```
改变窗体的大小 
```
ctrl+b---->按住ctrl 然后方向键改变
```
#####其他操作 请查阅 man tmux 
