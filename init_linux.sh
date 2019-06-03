#!/usr/bin/env bash
#安装npm
sudo pacman -Syu npm
#安装 cnpm
sudo npm install -g cnpm --registry=https://registry.npm.taobao.org
# 使用npm 安装  n
sudo cnpm install -g n
# 使用n  更新 npm  node
sudo n latest
#安装 hexo
sudo cnpm install hexo -g
# 安装压缩优化
sudo cnpm install gulp -g

#安装依赖
sudo cnpm install


########################################################################################################




