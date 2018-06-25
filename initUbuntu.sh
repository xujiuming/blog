#!/usr/bin/env bash
#安装npm
sudo apt install npm  -y
# 使用npm 安装  n
sudo npm install -g n
# 使用n  更新 npm  node
sudo n latest
#安装 hexo
sudo npm install hexo -g
# 安装压缩优化
sudo npm install gulp -g

#安装依赖
sudo npm install