---
title: kworkerds挖矿病毒处理
comments: true
categories: 笔记
tags:
  - linux安全
abbrlink: 65dd58e
date: 2019-02-25 15:00:17
---
#### 前言
mmp redis 由于使用了弱密码 导致被人植入 kworkerds挖矿病毒 
稍微看了下 这个病毒 只要清理相关文件 就行了    
#### kworkerds病毒
kworkerds挖矿病毒通过redis 6379端口入侵服务器 并且执行挖矿程序 
##### 处理方案
* redis 设置复杂密码 
```bash
docker run --name redis-test -p 6379:6379 -d  --restart=always redis:4.0.11-alpine redis-server --appendonly yes --requirepass "************"
```
* 通过脚本清理kworkerds病毒进程 源程序、定时脚本  
下面是从网上抄的脚本  就是删除kworkerds相关进程 脚本 并且将 pastebin.com 指向到本地   
```bash
echo '127.0.0.1 pastebin.com' >> /etc/hosts

chattr -i /etc/ld.so.preload

echo "" > /etc/ld.so.preload

chattr -i /usr/local/bin/dns

echo "" > /usr/local/bin/dns

chattr -i /etc/cron.d/root

echo "" > /etc/cron.d/root

chattr -i /etc/cron.d/apache

echo "" > /etc/cron.d/apache

chattr -i /var/spool/cron/root

echo "" > /var/spool/cron/root

chattr -i /var/spool/cron/crontabs/root

echo "" > /var/spool/cron/crontabs/root

rm -rf /etc/cron.hourly/oanacroner

rm -rf /etc/cron.daily/oanacroner

rm -rf /etc/cron.monthly/oanacroner

#rm -rf /bin/httpdns

#sed -i '$d' /etc/crontab

sed -i '$d' /etc/ld.so.preload

chattr -i /usr/local/lib/libntpd.so

rm -rf /usr/local/lib/libntpd.so

ps aux|grep kworkerds|grep -v color|awk '{print $2}'|xargs kill -9

chattr -i /tmp/thisxxs

rm -rf /tmp/thisxxs

chattr -i /tmp/kworkerds

rm -rf /tmp/kworkerds

```
 
 
 ##### 预防方式
 时常巡检 服务器 
 任何服务都要设置密码  就算在内网中也应该设置密码 
#### 总结
mmp  
其实还应该看看有没有定时脚本 如cron任务、timer之类的玩意   坑
