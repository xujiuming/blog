---
title: certBot自动签发管理https证书
comments: true
categories: 笔记
tags:
  - certBot
  - https
  - nginx
abbrlink: 43748ac1
date: 2023-06-27 10:01:42
---
#### 前言  
一直听说过  Let\`s Encrypt 自动签发证书 一直用的阿里云申请的免费证书 一年换一次  鸡儿痛    
最近干脆切换到 certBot 来自动申请管理Let\`s Encrypt 证书         

> https://certbot.eff.org/   certbot官网   

#### 实例 
> 以在debian上 nginx+certBot 自动签发管理为例  

* 安装Nginx     
> https://www.osgeo.cn/post/1607c  

```shell
echo "deb http://nginx.org/packages/debian/ bullseye nginx">> /etc/apt/sources.list
echo "deb-src http://nginx.org/packages/debian/ bullseye nginx">> /etc/apt/sources.list
wget http://nginx.org/keys/nginx_signing.key && apt-key add nginx_signing.key
apt update && apt dist-upgrade -y
apt dist-upgrade -y nginx
sudo apt install nginx 
```

* 安装certBot   
>  https://certbot.eff.org/  在官网选择 nginx或者其他系统和操作系统及其安装方式   

```shell
# debian 默认是 snap方式  不想用  直接安装 python版本 pip安装 没使用官方的文档 直接搜索 ppa里面的    
sudo apt install certbot python3-certbot-nginx  
```

* 配置nginx    
> 配置文件在/etc/nginx/conf.d 新增show.conf    

```nginx configuration
server {
    listen       80;
    server_name  show.xujiuming.com;

    #access_log  /var/log/nginx/host.access.log  main;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }  
}
```


* certBot自动配置Nginx 
> 如果是国内必须确定是否可以访问443端口   自动配置会验证443端口可用性  
> 例如未备案被拦截 或者被阻止访问 都无法申请成功  

```shell
# 开始自动配置  会要求输入邮箱 选择各种方式  按照提示选择即可   实在不会 参考: https://www.jianshu.com/p/4d4f9376fe20
sudo certbot --nginx   
```

* 验证结果   
1. 查看nginx配置 是否存在 开放443端口和相关证书配置   
```nginx configuration
server {
    server_name  show.xujiuming.com;

    #access_log  /var/log/nginx/host.access.log  main;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }


    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/show.xujiuming.com/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/show.xujiuming.com/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

}

server {
    if ($host = show.xujiuming.com) {
        return 301 https://$host$request_uri;
    } # managed by Certbot


    listen       80;
    server_name  show.xujiuming.com;
    return 404; # managed by Certbot


}   
```

2. 访问443端口 
```shell
curl https://show.xujiuming.com 
```
3. 查看certBot自动续期任务是否存在 
```shell
# 模拟运行 自动续期
sudo certbot renew --dry-run
# 查看crontab 配置中是否有 certbot 
ls /etc/cron.d/ |grep certbot
# 查看是否存在 certbot.timer
sudo systemctl  list-timers |grep certbot 
```

#### 常用命令 
```shell
#给nginx自动配置   
certbot --nginx 
# 指定域名  
certbot --nginx -d \[domain]
# 续期 
certbot renew      
# 测试续期 
certbot renew --dry-run   
# help
certbot --help 
```  

#### 总结 
certBot 自动处理证书相关配置  简单粗暴 适合懒狗使用  

