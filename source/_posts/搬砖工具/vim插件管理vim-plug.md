---
title: vim插件管理vim-plug
comments: true
date: 2019-03-11 13:46:46
categories: 工具
tags:
 - vim
 - tools
---
```bash
curl -fLo ~/.vim/autoload/plug.vim --create-dirs \
    https://raw.githubusercontent.com/junegunn/vim-plug/master/plug.vim
```


```bash
call plug#begin('~/.vim/plugged')
插件.......
Plug xxxxx
call plug#end()
```
会自动 设置 filetype indent off  和 syntax off  


youCompleteMe
需要tools 
cmake 
make 
gcc or g++ 
python3 


ERROR: msbuild or xbuild is required to build Omnisharp.
没有 Omnisharp  c# 跳过这个组件即可 

ERROR: Unable to find executable 'cargo'. cargo is required for the Rust completer.
安装 cargo  即可  

