#!/usr/bin/env bash
#安装npm
yay -Syyu npm
#安装
sudo npm install --registry=https://registry.npm.taobao.org
sudo npm install -g hexo-cli --registry=https://registry.npm.taobao.org

########################################################################################################
npm install npm@latest -g
node -v
npm cache clean -f
npm install -g n
n stable


##########################################################################################
npm install hexo-abbrlink  -S   --registry=https://registry.npm.taobao.org
npm install hexo-filter-mermaid-diagrams -S   --registry=https://registry.npm.taobao.org
npm install hexo-generator-feed -S   --registry=https://registry.npm.taobao.org
npm install hexo-generator-search -S   --registry=https://registry.npm.taobao.org
npm install hexo-generator-searchdb -S   --registry=https://registry.npm.taobao.org
npm install hexo-generator-seo-friendly-sitemap -S   --registry=https://registry.npm.taobao.org
npm install hexo-helper-live2d -S   --registry=https://registry.npm.taobao.org
npm install live2d-widget-model-wanko -S   --registry=https://registry.npm.taobao.org





