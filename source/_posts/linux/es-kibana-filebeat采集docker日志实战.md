---
title: es+kibana+filebeat采集docker日志实战
comments: true
categories: 实战
tags:
  - 日志
  - es
  - kibana
  - filebeat
  - docker
abbrlink: 7383a41
date: 2018-07-17 11:35:03
---
#### 前言
最近看了看 docker 标准输出和标准错误的日志采集  
发现 filebeat 可以直接监听 docker的启动结束等动作  并且几乎不需要配置什么 就可以很轻松的采集到node上的docker运行的日志 
而且也可以直接接入 k8s的标准输出和错误日志  贯彻了 elastic 的简单粗暴的套路   

#### 实战 
##### 启动 elastic-search 和kibana
这里直接使用docker 启动即可  如果是生产或者预发环境 建议使用官方的镜像作基础镜像在上面进行个性化优化  
```
sudo docker run -d -p 9200:9200 -p 5601:5601 --name es-kibana-test  nshou/elasticsearch-kibana
```
##### 安装filebeat 
这里使用下载压缩包方式安装 因为改配置简单  如果需要使用docker方式启动 改配置不是很方便 需要进行调整 这里只是作演示  就直接选择压缩包方式使用 
```
#https://www.elastic.co/downloads/beats/filebeat
weget   https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-6.3.1-linux-x86_64.tar.gz
sudo zxvf ./filebeat-6.3.1-linux-x86_64.tar.gz
```
##### 修改filebeat配置
先备份 默认的filebeat.yml
```
sudo cp ./filebeat.yml filebeat.yml.bak
```
修改安装目录中filebeat.yml 主要是修改 inputs的type  自动安装kibana的索引  和es地址  
具体配置详情参考:https://www.elastic.co/docker-kubernetes-container-monitoring
```
#=========================== Filebeat inputs =============================

filebeat.inputs:
 - type: docker
   containers.ids:
           - '*'
   processors:
       - add_docker_metadata: ~


#============================== Kibana =====================================

setup.kibana:
  host: "localhost:5601"


#================================ Outputs =====================================

#-------------------------- Elasticsearch output ------------------------------
output.elasticsearch:
  # Array of hosts to connect to.
  hosts: ["localhost:9200"]

```
##### 启动并且根据配置自动安装filebeat 索引  
```
# 启动并且安装 filebeat 
sudo ./filebeat --setup -e 
```
这个时候 在这台node上 所有docker的日志都会被filebeat采集起来并且发送到es上   

产出日志后 直接在kibana上查看即可    

#### 遇到的问题
* kibana 建立读取索引的时候 报错 FORBIDDEN/12/index read-only / allow delete (api)]
删除 索引的 只读权限即可     
```
curl -XPUT -H "Content-Type: application/json" http://ip:9200/_all/_settings -d '{"index.blocks.read_only_allow_delete": null}'
```

#### 总结 
由于 docker集群的日志采集比较操蛋 可能是多种不同的系统运行(例如java类应用、各种服务软件的日志之类的) 那么在日志格式上无法做到 统一 
采用这种通过收集docker 的标准输出和错误输出 可以收集整个docker集群中的docker的日志  方便查看和管理 

当然filebeat也支持直接和k8s集群接入  






