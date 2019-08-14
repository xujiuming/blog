#!/usr/bin/env bash
# author ming
#  生成静态文件 并且部署

sudo rm -rf ./public/*
# 编译
sudo hexo generate
# 压缩
#sudo gulp

sudo chown -R ming:ming ./*
sudo chmod -R 777 ./*
# 同步到ftp
#sudo hexo deploy

#刷新cdn
#./deploy.groovy