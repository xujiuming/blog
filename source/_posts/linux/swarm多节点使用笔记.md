---
title: swarm多节点使用笔记
comments: true
categories: 笔记
tags:
  - docker
  - swarm
abbrlink: 1d657211
date: 2019-07-22 15:01:40
---
#### 前言  
由于公司生产上运行的是为搭建的单机的swarm  业务扩展 需要的资源变多了  需要再加一台机器  


参考   {% post_link 综合应用/docker-swarm集群搭建 %}
#### 初始化环境
 准备两台机器master1,master2 
 参考 {% post_link 综合应用/centos-docker环境搭建%}
##### 初始化swarm集群manager节点 和join master2节点  
*  执行如下脚本 
```bash
# 初始化 swarm 集群  
sudo docker swarm init 

```
*  join worker节点 
在master1上执行
```bash
sudo docker swarm join-token worker 
```
* 执行完结果 
```text
To add a worker to this swarm, run the following command:

    docker swarm join --token SWMTKN-1-2yv56dcyrnao5z2qr74yfs9fmjk0qus5du4cbw04weq8ai174p-anq2tkr3pv9orth97sdluasce 192.168.1.123:2377

```
* 在master2上执行 
docker swarm join --token xxxxxxxxxx 这一行  即可 

* join manager节点 
跟添加worker节点差不多 只不过第一条命令变成 
```bash
sudo docker swarm join-token manager 
```


#### 调整节点 label 
* 获取node的信息 
```bash
sudo docker node ls 
```
* 调整node的信息
假设 查询出来的node Id为  master1NodeId
```bash
sudo docker node update --label-add func=标签名 master1NodeId
```

* 删除node的标签 
```bash
sudo docker node update --label-rm master1NodeId
```

#### 配置容器启动的节点
swarm 还是用docker-compose来进行管理配置即可     
参考文档:  https://docs.docker.com/compose/compose-file/ 

```yaml
version: '3.3'
services:
 master-nginx:
  image: :1.17.1-alpine
  networks:
    - ming
  deploy:
   replicas: 1
   update_config:
    parallelism: 1
    delay: 10s
   restart_policy:
    condition: on-failure
    
 node-nginx:
  image: nginx:1.17.1-alpine
  networks:
    - ming
  deploy:
   placement:
   # 配置容器运行的节点  此处的node 和上面修改 node的label的内容要相等  
    constraints:
     - node.labels.func==node
   replicas: 1
   update_config:
    parallelism: 1
    delay: 10s
   restart_policy:
    condition: on-failure
networks:
  ming:
   driver: overlay
   ipam:
    driver: default
    config:
     - subnet: 10.18.0.0/16
```

#### 总结 
swarm 还是在小规模集群中 特别是像我现在的公司  高配置的虚拟机  非常适合处理 和简单 
还是那句话 如果集群服务器规模小于20台  建议使用 swarm 
真的公司愿意去做大规模集群  再去建设k8s集群就行

唯一有点不爽的就是 swarm 毕竟是个小众 或者几乎没人玩的东西  没什么好的web管理界面 
就算是portainer 也有点毛病     