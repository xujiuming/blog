---
title: devops-工具选型 
comments: true 
abbrlink: 6d40f3e 
date: 2020-12-30 14:25:55 
categories: 实战 
tags:
- devops
---

#### 前言

上一篇写的 一般devops中 研发部分的流程  
这一篇记录一下 我自己喜欢用的一些devops的工具

> https://www.cnblogs.com/zgq123456/articles/12015027.html 这个博客里面基本上算是包含了大多数工具的名字了 真正使用的没这么多

#### 个人常用工具列表

|名称|功能|备注|
|:--|:--|:--| 
|jenkins|持续集成工具|来控制整个持续集成流程和调度的 建立工作流管道、`使用script pipe模式 可以实现复杂的需求|
|gitlab|git仓库+缺陷跟踪+持续集成|gitlab 主要功能是做git仓库 也可以做持续集成相关工作 相对来说比jenkins简单一点| 
|github|actions(持续集成)、packages(成品仓库)、issue(缺陷跟踪)等等功能|github没啥说的 功能齐全 基本上包含整个devops过程| 
|nexus|成品仓库|用来保存和管理各种打包之后的文件 如 jar、npm 包、 docker 镜像、yum/apt包等等| 
|harbor repository|镜像仓库|保存和管理docker镜像的仓库| 
|docker|容器|使用docker作为基础容器| 
|swarm|docker编排工具|小规模集群、简单场景下使用 例如10台以内服务器|
|k8s|容器编排工具|大规模集群、复杂场景使用，不局限于docker容器 可以使用其他符合oci标准的容器| 
|jira|缺陷管理工具|| 
|禅道|项目管理工具|跟踪管理整个项目的工具| 
|confluence|文档管理工具||
|rancher|k8s管理工具|用来管理k8s的工具| 
|postman|接口测试工具|可以配合server 做自动化测试| 
|jmeter|测试工具|编写jmeter脚本进行自动化测试|
|zabbix|监控工具|主要监控服务器相关参数| 
|prometheus|监控|主要监控k8s和应用| 
|grafana|仪表盘|配合prometheus使用查看监控数据| 
|efk|elastic search + filebeat + kibana|日志组件 配合mq使用更好|
|sonar qube |代码质量检测|代码质量检测 和依赖漏洞检测(owasp)|
> 上面的工具都是业内相关的有名的工具 直接去对应官网查看文档即可

#### 常用工具组合

* gitlab + nexus + k8s + jira/禅道 + postman/jmeter + efk +监控组件(zabbix、prometheus+grafana) + sonar qube
  gitlab 作为代码仓库 + 持续集成 工具   也可以做部分缺陷跟踪功能 但是没有jira 和禅道那么完善    
  nexus 做代码软件包 容器镜像的仓库    
  k8s 做运行环境和容器编排管理   
  jira或者禅道用来做缺陷跟踪和项目管理    
  postman或者jmeter 用来做自动化测试  
  efk 提供实时日志       
  监控组件(zabbix、prometheus+grafana) 用来监控服务器 应用 和报警等相关功能
  sonar qube  代码质量检测和依赖漏洞检测平台   
  

* svn/git + jenkins + jira/禅道 + postman/jmeter + nexus + harbor repository + k8s/swarm + efk +监控组件(zabbix、prometheus+grafana) + sonar qube
  svn或者git 作为代码仓库   
  jenkins  持续集成 工具  
  nexus 做代码软件包的仓库   
  harbor repository 做容器镜像仓库  因为harbor 管理容器镜像比较方便一点   
  k8s/swarm 做运行环境和容器编排管理      
  jira或者禅道用来做缺陷跟踪和项目管理       
  postman或者jmeter 用来做自动化测试     
  efk 提供实时日志          
  监控组件(zabbix、prometheus+grafana) 用来监控服务器 应用 和报警等相关功能   
  sonar qube  代码质量检测和依赖漏洞检测平台   


#### 总结  
在做devops相关建设的时候  总是要面临很多选择    
按照优先级  优先做到快速发布    
后面慢慢补充 缺陷跟踪  代码审查  服务监控     
要经常看各种工具的更新日志  方便进行升级调整适配 
总的来说 就是合理利用工具提高 代码编写到生产产出的速度  利用自动化工具减少人工介入 避免重复性劳动   
