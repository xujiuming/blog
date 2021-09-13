---
title: apache-IOUtils笔记
comments: true
categories: 笔记
tags:
  - apache
  - commons-io
abbrlink: a0eb41ae
date: 2021-06-17 16:04:36
---
#### 前言
最近使用了很多io相关操作 一直没记录 
今天记录一下  方便速查 

#### 示例

|函数名|功能|备注|
|:----|:---|:---|
|buffer()|将各种in out流 转换为带buffer的 流||
|close()|关闭流||
|closeQuietly()|安全的关闭流||
|contentEquals|比较内容||
|contentEqualsIgnoreEOL|比较内容 忽略EOL|EOL:结尾|
|copy()|复制流||
|copyLarge()|复制超过2g的数据流||
|length()|获取长度||
|lineIterator()|获取行数据迭代器||
|read()|读取数据||
|readFully()|安全的读取数据 ||
|readLines()|按行读取||
|resourceTo*()|把数据转换成string  byte url等格式||
|skip()|跳过数据||
|skipFully()|安全的跳过||
|to*()|转换为某种对象||
|write*()|写数据||


#### 总结 
ioutils  包含了 大多数操作 stream的操作  复制 读写 转换 等等操作  

