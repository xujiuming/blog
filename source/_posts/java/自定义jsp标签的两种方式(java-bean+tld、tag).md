---
title: jsp 自定义标签
categories: 笔记
tags:
  - java基础
  - jsp
abbrlink: dd01bc67
date: 2017-11-11 00:00:00
---
###在做jsp项目中除了一些框架的标签如spring、jstl、shiro等标签 可能还需要自定义一些常用标签 如page等
###自定义jsp标签有两种方式 
1:java bean+ tld  
 ```
 通过tld来调用java中的代码 这种写法 参考http://www.runoob.com/jsp/jsp-custom-tags.html
 ```
2:tag文件
```
直接定义一个后缀为tag的文件 这种方式比 java bean+tld 简单方便 容易使用和定义
tag文件头 如下设置   其他按照jsp使用即可 可以导入其他标签库、java代码、等
//定义tag的编码集合
<%@tag pageEncoding="UTF-8" %>
//定义一个名字为str 类型为string的必须元素
<%@attribute name="str" required="true" type="java.lang.String" %>
//定义一个名字为flag 类型为boolean 的非必须元素
<%@attribute name="flag" required="false" type="java.lang.Boolean" %>
。。。。。省略业务代码 这里就跟写jsp一样


调用:
在jsp头部加上这个标签所在的文件夹  是文件夹不是单个标签  
<%--自定义的标签位置--%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>

然后使用即可 例如实例代码文件名为 test.tag
 <tags:test str="test"></tags:test>
```
