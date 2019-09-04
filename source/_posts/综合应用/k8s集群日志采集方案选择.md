---
title: k8s集群日志采集方案选择
comments: true
categories: 实用
tags:
  - docker
  - k8s
  - 日志采集
abbrlink: d8147fe2
date: 2018-03-09 10:32:23
---
### 日志采集方案
常规的日志采集总的来说 分为 收集--》处理--》存储--》查看   
比较出名 并且用的比较多的 应该是elk模式   
常用组合应该是如下 三种   至于还有其他采集工具 其实都差不多   
* elk: es + kibana + logstash   
最经典的做法  es+kibana 做 存储 查询  logstash 做日志采集和管道处理   
缺陷是logstash 功能复杂 除非是搭建所有项目的统一日志 这种同集群的 logstash并不是特别好用   
efk: es +kibana + filebeat   
最直接的做法   es+kibana 做存储查询  filebeat采集数据 直接发送到es上  不经过logstash管道处理     
elkf: es+ kibana+logstash +filebeat   
功能最齐全的做法  es+kibana做存储查询  logstash做管道处理 filebeat 做采集   



为何选择filebeat做采集  因为短小精悍  go语言开发  占用资源少     
具体的可以查询官网:https://www.elastic.co/cn/ 随便搭配 总归就是一个收集处理存储查看的过程    

#### k8s集群配置日志采集方案  
1：在node上配置一个filebeat  所有的镜像的日志 按照一定规则映射到filebeat采集的目录下   
这种 配置简单  不过需要制定一套 项目的日志记录规范   
2:使用k8s的daemon set 为每个pod 绑定一个filebeat   利用的是k8s的pod 共享数据卷的套路    
这种较为消耗资源 一个filebeat20m内存  那么 100个容器 就是 2000m内存了  有点得不偿失   
好处就是如果资源足够可以很容易的进行动态伸缩容器  新开node不需要配置 只要有kubelet就行  



个人更加倾向 efk  因为 filebeat 非常适合 通过daemon set 去绑定到k8s pod中   
并且不是很需要 logstash   

#### k8s集群+efk日志采集 实践 
###### 0:启动 es+kibana 
通过镜像去启动 方便点 真实环境 请尽量采用 直接安装在服务器上  因为要映射数据文件乱七八糟的 不是很方便 
nshou/elasticsearch-kibana latest版本 es=6.2.1 kibana=6.2.1 
```
sudo docker run -d -p 9200:9200 -p 5601:5601 --name es-kibana-test  nshou/elasticsearch-kibana
```
直接在服务器上安装 请参考官网即可 
###### 1:搭建k8s实验环境
使用minikube 快速搭建一个k8s实验环境 
{% post_link 综合应用/minikube快速搭建k8s测试环境 %}
###### 2：建立并且修改deployment 
```

``` 
###### 3：建立并且调整daemon set 
```$xslt

```
#### 4：建立并且调整 service 
#### 


