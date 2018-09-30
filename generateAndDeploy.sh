#!/usr/bin/env bash
# author ming
#  生成静态文件 并且部署
chown -R ming:ming ./*
chmod -R 777 ./*

# 编译
hexo generate
# 压缩
gulp
# 同步到ftp
hexo deploy

#刷新cdn
./deploy.groovy