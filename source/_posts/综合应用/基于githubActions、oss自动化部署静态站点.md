---
title: 基于githubActions、oss自动化部署静态站点
comments: true
categories: 实战
tags:
  - github actions
  - oss
  - CI/CD
abbrlink: 24bfd827
date: 2019-12-06 15:57:57
---
#### 前言
自己的博客、主站 之前老是手工或者利用本地的插件 同步到aliyun oss上 麻烦的很  而且每次换电脑还都要配置 
最近有时间 通过github 的actions 来做一些简单的同步静态网站到oss上 

#### 示例    
阅读要求:   
* 熟悉阿里云 管理oss    
* 熟悉一般前端项目编译打包   
* 熟悉github常用功能和配置  
* 熟悉git、ci\cd 等基础用法和原理  

此处以本人 hexo博客为示例  https://github.com/xujiuming/blog   

大致思路:    
通过actions构建node编译环境->安装依赖和hexo-cli -> 使用ossutil 将hexo生成的静态文件复制到oss上  

##### 阿里云 oss 配置 
此处不再赘述  如何用阿里云oss 搭建静态网站 和cdn加速的工作  阿里云官网的操作手册已经说的够详细了    
建立一个子账号 配置oss 读写权限 并且分配accessKey 和accessSecret    

##### github secret配置 
进入 github->对应的项目->settings->侧边栏中选择secrets 新增secret 

新增 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET 值分别是刚刚建立的oss key和secret 
如下图 

![github配置actionsSecrets截图](https://www.xujiuming.com/ming-static/github%E9%85%8D%E7%BD%AEactionsSecrets%E6%88%AA%E5%9B%BE.png)

> 使用 '${{ secrets.新增的secretName }}' 访问配置的变量

##### 编写github actions配置 
在项目根目录建立 .github/workflows/sync-oss.yml
> 由于是我自己的博客部署  我添加了钉钉的消息提示  设立 DINGTALK_ACCESS_TOKEN secret 

以hexo构建的博客 自动化部署yml内容如下:
```yaml
# 参考: https://www.jianshu.com/p/99952652b2dd
name: sync-oss
# 初期监听push事件  后续按情况调整
on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      # 检出代码
      - uses: actions/checkout@v1
      # 安装node 指定版本
      - uses: actions/setup-node@v1
        with:
          node-version: "12.x"
      - name: 开始部署ming-blog
        # 个人的开始部署通知 可以不要         
        run: |
          curl 'https://oapi.dingtalk.com/robot/send?access_token=${{ secrets.DINGTALK_ACCESS_TOKEN }}' -H 'Content-Type: application/json' -d '{"msgtype": "text",
                          "text": {
                               "content": "ming-blog开始部署"
                          }
                        }'
      # 构建
      - name: 构建hexo博客
        run: |
          npm install hexo-cli -g
          npm install
          hexo clean
          hexo generate
      # 使用oss util 复制dist目录到oss
      - uses: manyuanrong/setup-ossutil@v1.0
        with:
          # endpoint 可以去oss控制台上查看
          endpoint: "oss-cn-shanghai.aliyuncs.com"
          # 使用我们之前配置在secrets里面的accesskeys来配置ossutil
          access-key-id: ${{ secrets.OSS_ACCESS_KEY_ID }}
          access-key-secret: ${{ secrets.OSS_ACCESS_KEY_SECRET }}
      - name: 同步dist文件到oss上
        run: ossutil cp public/ oss://mingblog/ -rf
      # 个人的部署结果通知 可以不要             
      - name: 成功通知
        if: success()
        run: |
          curl 'https://oapi.dingtalk.com/robot/send?access_token=${{ secrets.DINGTALK_ACCESS_TOKEN }}' -H 'Content-Type: application/json' -d '{"msgtype": "text",
                                      "text": {
                                           "content": "ming-blog部署成功"
                                      }
                                            }'
      - name: 失败通知
        if: failure()
        run: |
          curl 'https://oapi.dingtalk.com/robot/send?access_token=${{ secrets.DINGTALK_ACCESS_TOKEN }}' -H 'Content-Type: application/json' -d '{"msgtype": "text",
                                      "text": {
                                           "content": "ming-blog部署失败!请登录github查看"
                                      }
                                            }'

```


#### 总结
github actions 对开源仓库免费使用 真的是给力     
不过在部署国内的资源的时候 需要考虑一下gfw是否会影响    
如果是个人 使用github actions 没啥毛病 方便的一批   
例如我的博客 基本上从push 到部署 到刷新cdn  2分钟多就生效了  
