---
title: postgres-Money类型笔记
comments: true
categories: 笔记
tags:
  - postgres
abbrlink: '62686138'
date: 2020-04-13 12:58:16
---
#### 前言 
由于开发的时候 有个同事当初选择使用的money类型  这里对money类型做一些笔记 方便速查

#### 货币类型存储方案 
在只处理RMB的情况下  ×10 ×100 ×1000 来转换为int类型 避免精度丢失是比较简单的方案
如果存在不同货币 如RMB 和USD 
一般来说 也是 ×10 ×100 ×1000 然后使用一个type类型来区分也是很容易 

postgres提供一个专用类型 'money' 存储的时候也是使用bigint存储 但是会根据区域自动格式化为所在区域货币字符串
如 RMB '￥100' USD'$100'

#### 调整money类型识别的区域配置
>  zh_CN.UTF-8 , en_US.UTF-8
```sql
--显示当前money 区域
show lc_monetary ; 
-- 设置money 区域
set lc_monetary="zh_CN.UTF-8";
```
或者设置 postgresql.conf 
```text
lc_monetary=zh_CN.UTF-8
```

#### 操作money类型字段
```sql
--insert 按照常规int insert即可  
-- 查询的时候 money 会根据lc_monetary的区域识别  中国区域￥xxx 美国区域$xxx
select 999::money;
```

#### 总结
postgres 提供一些特殊类型 尽量科学的使用  避免bug  
例如money类型 会跟当前的本地区域配置 或者lc_monetary配置有关 需要去独立处理 
