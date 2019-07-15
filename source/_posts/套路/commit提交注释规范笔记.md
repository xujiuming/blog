---
title: commit提交注释规范笔记
comments: true
categories: 笔记
tags:
  - 开发规范
  - tools
abbrlink: c031715d
date: 2019-07-15 11:21:25
---
#### 前言 
commit的时候 老是各有各的玩法 千奇百怪 
只好找个相对比较合适的规范 来约束大家的提交日志格式  也为了后续方便将commit log 接入到ci/cd流程中  
参考资料: 
http://www.ruanyifeng.com/blog/2016/01/commit_message_change_log.html  
#### 内容
大致格式如下:
```text
<type(类型必填)> (scope(影响范围非必填)):<subject(主题必填)>

<body(详细描述(必填))>

<footer(不兼容变更或者关闭issue)> ||<revert(撤销之前的提交) 上一次提交的header>
```
type类型:      

|类型名称|类型内容|
|:------|:-----|
|feat|新内容|
|fix|修复bug|
|docs|文档|
|style|格式化代码|
|refactor|重构|
|test|增加测试|
|chore|调整部署相关配置|

##### 实例
* 常规提交 
```text
feat : 增加xxx功能

xxx模块增加xxx功能

```
* 带有不兼容或者关闭issue的提交
```text
fix : 修改xxx功能

xxx模块xxx功能修改 

不兼容之前的版本 #xxxId 
```

* 回滚之前提交
```text
fix : 回滚xxx功能

xxx模块xxx功能回滚 

回滚xxx功能 #xxxId 
```


#### 总结 
个人搬砖的时候 提交规范 可有可无  反正自己玩  
团队搬砖的时候 还是要强制大家遵守一些基本规范 或许这些规范很繁琐 增加不少工作量    
这些工作量 带来后面维护和开发的时候提供巨大的便利性  




