---
title: github-actionsCI尝鲜笔记
comments: true
categories: github
tags:
  - github apps
  - CI
abbrlink: 5181baaf
date: 2019-08-28 16:17:25
---
#### 前言
github 前段时间出了个 actions 官方嵌入的 CI工具   
很幸运 在初期就给我开通了 actions 工具的 内测资格  
今天实在心痒难耐  决定试试水 

参考文档:
https://help.github.com/en/articles/workflow-syntax-for-github-actions
#### 实际操作步骤
##### 添加action 初始化配置 
进入项目源码首页 选择 actions选项  选择一个 java ci模板  
会在项目的根目录生成一个 .github/workflows/maven.yml 内容如下 
```yaml
name: Java CI

on: [push]

jobs:
  build:

    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v1
    - name: Set up JDK 1.8
      uses: actions/setup-java@v1
      with:
        java-version: 1.8
    - name: Build with Maven
      run: mvn package --file pom.xml
```
##### 根据项目调整脚本配置  
由于我测试用的项目是jdk11 默认生成的action配置是jdk8  
这里按需调整脚本  
```bash
name: 编译ci

on: [push]

jobs:
  build:

    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v1
    - name: 使用jdk11插件 
      uses: actions/setup-java@v1
      with:
      # 调整jdk版本 这里参考 actions/setup-java插件文档 
        java-version: 11
    - name: 构建编译jar 
      run: mvn package --file pom.xml
```

##### 运行
由于脚本中配置的是 监听push 操作  那么只需要push 一下项目就会触发上面配置的脚本  在actions管理页面可以点进去看日志 没啥好说的 

#### 脚本配置说明 
官方文档地址:https://help.github.com/en/articles/workflow-syntax-for-github-actions#jobsjob_idstepsuses  
##### 对上面脚本的解读    
name: 工作流名称 
on: 监听那些github事件 可以提供多种不同的方式触发  
jobs: 定义一个任务  一个工作流是一个或者多个任务组成  
jobs.build  定义任务的名称 为build 
jobs.build.runs-on： 每个job必须制定的 运行环境 
jobs.build.steps：定义任务步骤   定义使用那些插件 插件的配置 
jobs.build.steps.name：名称
jobs.build.steps.uses：使用的公共插件 
jobs.build.steps.with：插件的参数配置 
jobs.build.steps.run：使用操作系统的shell运行的命令  

其它未说明的 直接看官方文档即可  github的文档还算不错  简单明了     
#### 总结
github 自从被微软收购后 对这种免费性质的开源支持 越来越到位了 不管是开放个人免费私有库 还是这次的 actions ci  都挺方便的    

