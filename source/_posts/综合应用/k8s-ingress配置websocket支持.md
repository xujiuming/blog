---
title: k8s-ingress配置websocket支持
comments: true
categories: k8s
tags:
  - k8s
  - websocket
abbrlink: 9c16d977
date: 2019-07-26 13:54:32
---
#### 前言 
本来接我运维工作的同事跑路了  又是我接锅
其中一个遗留问题就是 k8s集群中websocket无法正常使用 

#### 问题分析
##### 表现形式
websocket链接 报错 200 
```text
WebSocket connection to 'ws://*********' faile Error during WebSocket handshake: Unexpected response code: 200 
```
##### 猜测引起原因以及应对方式 
* 后端服务某些filter或者interceptor不兼容ws协议     
排查后端服务的filter 或者interceptor 代码   
实在不行 将websocket地址给放开限制  
 
* 流量入口没有兼容ws协议访问 如nginx未配置ws协议支持   
nginx反向代理要配置一些参数 来达到转发 websocket请求    
```text
  location /websocket地址 {
    proxy_pass http://websocket服务;
    proxy_set_header Upgrade "websocket";
    proxy_set_header Connection "Upgrade";
  }
```
#### 解决方案 
由于k8s集群入口是通过边缘路由(ingress)来管理的 会存在如下的坑 
* 额外的配置 只能配置在 ingress的 metadata中 这样在一个ingress中会全部生效 

##### 那么这个时候有两种方案 来解决    
###### 1: ingress转发tcp 内部增加一个nginx 进行分发     
略。。。   因为这个方案 为认为是回避了k8s的原则  不使用此方案 理论上 这个方案是很好做的 就是有点违背k8s的玩法 

###### 2: 配置一个新的单独为所有websocket服务服务的ingress  
参考文档: https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#configuration-snippet   
新增一个配置如下的ingress    文件名为 websocket-ingress.yaml 
```yaml
apiVersion: extensions/v1beta1     
kind: Ingress    
metadata:           
  name: ingress名称
  namespace: ingress所属命名空间
  annotations:           
    #ingress使用那种软件 
    kubernetes.io/ingress.class: nginx
    #配置websocket 需要的配置   
    nginx.ingress.kubernetes.io/configuration-snippet: |
       proxy_set_header Upgrade "websocket";
       proxy_set_header Connection "Upgrade";
spec:      
  rules: 
  - host: 识别的域名
    http:
      paths: 
        #代理websocket服务
      - path: /websocket地址
        backend:
          serviceName: websocket服务名称
          servicePort: websocket服务端口
```
启动该ingress 
```bash
kubectl apply -f websocket-ingress.yaml 
```
测试链接服务  
#### 总结 
websocket 用的时候还是挺爽的  就是对环境有一定的要求   
配置比较麻烦点    
还有就是k8s ingress 有时候的确很草蛋     
