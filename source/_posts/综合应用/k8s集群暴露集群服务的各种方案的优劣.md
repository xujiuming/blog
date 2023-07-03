---
title: k8s集群暴露集群服务的各种方案的优劣
comments: true
categories: 实战
tags:
  - k8s
  - docker
  - 实用
abbrlink: c013ae57
date: 2018-02-12 08:57:52
---
参考文档:   
http://blog.csdn.net/liyingke112/article/details/76022267   
https://www.kubernetes.org.cn/1885.html
k8s集群暴露服务 常见的就四种   


#### 1: clusterIp  
clusterIp就是service的ip  k8s在创建service的时候 会给service一个ip 集群内部可以通过这个clusterIp访问相应的service  
这种方式呢稍微麻烦 最终要暴露外部服务 还是要建立nginx之类的反向代理 然后再暴露出集群   
实践:
这个需要在集群中的一个节点去访问 
登陆集群中同一个命名空间的容器  直接ping 对应的clusterIp 或者直接telnet 访问clusterIp:clusterPort  clusterPort就是service配置中的targetPort
```
ping <clusterIp>
telnet <clusterIp> <targetPort>
```
#### 2:nodePort  
node在k8s中表示是一台物理机或者虚拟机 nodePort是k8s集群在创建service的时候 会在每个node上暴露出nodePort 外部可以直接通过nodeIp:nodePort访问 服务   
这种方式 很容易实现 但是不太符合实际生产环境 因为在新开服务或者新增node的时候需要增加配置 会导致依赖一个外部的负载均衡器去分发各个node 
配置太多了 不利于维护   nodePort模式依旧存在clusterIp 依旧可以通过第一种方式访问 
实例:
配置 service
```
kubectl edit service <serviceName>
```
type 为NodePort  拥有port、targetPort、nodePort
这个如果不是这个配置 可以修改  
```
。。。
spec:
  clusterIP: 10.103.100.161
  externalTrafficPolicy: Cluster
  ports:
  - nodePort: 30115
    port: 8080
    protocol: TCP
    targetPort: 8080
  selector:
    run: hello-node
  sessionAffinity: None
  type: NodePort
  。。。
```
直接ping node的ip 或者直接通过telnet 访问nodeIp:nodePort
```
ping <nodeIp>
telnet <nodeIp> <nodePort>
```
#### 3:loadbalance
必须要在支持 这个模式的云平台上才能用 基于nodePort 只不过 这个模式会请求底层云平台的服务创建一个负载均衡器 来访问   
这种方式 要看各个云平台的支持 例如阿里云就有点坑爹 每个服务都为你申请一个负载均衡器 贵的很 而且还不如直接用nodePort模式然后自己做负载均衡来的直接 简单  

这个 哎 懒的尝试没必要 直接请求云平台的负载均衡服务 略坑 太贵 而且不好管理  建议就算了把  有用这个 不如直接使用nodePort 或者ic 或者自己维护nginx方式来做 
#### 4:ingress 
这个是k8s 算是比较官方的一种解决方案了   
大神也总结的有文档 
http://blog.csdn.net/liyingke112/article/details/77066814  
https://www.kubernetes.org.cn/1885.html   
https://mritd.me/2017/03/04/how-to-use-nginx-ingress/#%E4%B8%80ingress-%E4%BB%8B%E7%BB%8D
https://mritd.me/2016/12/06/try-traefik-on-kubernetes/
比较麻烦 只有当集群大了之后 可以  小集群 需求并不大
主要就是 ingress 和ingress controller 、代理负载均衡器(例如nginx 之类的)
ingress : 配置规则的地方  
ingress controller :将ingress中配置的规则 生成相应的配置 例如生成nginx的配置 
负载均衡器: 例如nginx 具体分发流量的软件   ingress controller 通过ingress获取配置 自动刷新nginx中的配置 
#### 5：自定义方案   
nginx+config server 
用nginx 直接分发service  配置通过config server 进行刷新   
其实跟ingress差不多 只不过 由开发去维护 这个代理作用的nginx的pod 而不是直接由k8s直接管理   因为懂nginx配置的人多  懂ingress的人并不多
这个方案缺点就是要配置很多不同的service的配置 比较麻烦 只能手动配置  但是 这个配合loadbalancer 就可以很不错的做到代理分发 并且屏蔽底层serviceIp变化
最后通过暴露nginx的nodePort去给外部服务访问  
1:按照http://docs.kubernetes.org.cn/126.html 这个教程搭建 minikube 的hello-node 服务 这个时候可以通过nodePort 访问hello-node服务的   
2:通过如下配置启动一个nginx-all deployment
```
{
  "kind": "Deployment",
  "apiVersion": "extensions/v1beta1",
  "metadata": {
    "name": "nginx-all",
    "namespace": "default",
    "selfLink": "/apis/extensions/v1beta1/namespaces/default/deployments/nginx-all",
    "uid": "050be590-1b6d-11e8-a211-080027fc8712",
    "resourceVersion": "45623",
    "generation": 1,
    "creationTimestamp": "2018-02-27T03:19:28Z",
    "labels": {
      "k8s-app": "nginx-all"
    },
    "annotations": {
      "deployment.kubernetes.io/revision": "1"
    }
  },
  "spec": {
    "replicas": 1,
    "selector": {
      "matchLabels": {
        "k8s-app": "nginx-all"
      }
    },
    "template": {
      "metadata": {
        "name": "nginx-all",
        "creationTimestamp": null,
        "labels": {
          "k8s-app": "nginx-all"
        }
      },
      "spec": {
        "containers": [
          {
            "name": "nginx-all",
            "image": "nginx:1.13.8-alpine",
            "resources": {},
            "terminationMessagePath": "/dev/termination-log",
            "terminationMessagePolicy": "File",
            "imagePullPolicy": "IfNotPresent",
            "securityContext": {
              "privileged": false
            }
          }
        ],
        "restartPolicy": "Always",
        "terminationGracePeriodSeconds": 30,
        "dnsPolicy": "ClusterFirst",
        "securityContext": {},
        "schedulerName": "default-scheduler"
      }
    },
    "strategy": {
      "type": "RollingUpdate",
      "rollingUpdate": {
        "maxUnavailable": "25%",
        "maxSurge": "25%"
      }
    },
    "revisionHistoryLimit": 10,
    "progressDeadlineSeconds": 600
  },
  "status": {
    "observedGeneration": 1,
    "replicas": 1,
    "updatedReplicas": 1,
    "readyReplicas": 1,
    "availableReplicas": 1,
    "conditions": [
      {
        "type": "Available",
        "status": "True",
        "lastUpdateTime": "2018-02-27T03:19:29Z",
        "lastTransitionTime": "2018-02-27T03:19:29Z",
        "reason": "MinimumReplicasAvailable",
        "message": "Deployment has minimum availability."
      },
      {
        "type": "Progressing",
        "status": "True",
        "lastUpdateTime": "2018-02-27T03:19:29Z",
        "lastTransitionTime": "2018-02-27T03:19:28Z",
        "reason": "NewReplicaSetAvailable",
        "message": "ReplicaSet \"nginx-all-9bf75995b\" has successfully progressed."
      }
    ]
  }
}
```
3:登陆 nginx-all容器 修改/etc/nginx/conf.d/default.conf  这一步 可以通过配合config server 来实现重启容器更新配置来实现配置的刷新

{% post_link 综合应用/非java应用接入springConfigServer11 %} 

```
server {
    listen       80;
    server_name  localhost;

    #charset koi8-r;
    #access_log  /var/log/nginx/host.access.log  main;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }


    location /test{
        # 配置 服务名称+port 不需要使用nodePort直接使用启动容器时候映射的那个port  hello-node 启动参数为-p 8080:80 所以这里使用8080 即可 
        proxy_pass http://hello-node:8080;
    }


    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }

}

```
4：配置nginx service
配置 nginx service的nodePort
```
kubectl edit service  nginx-all
```
修改配置如下 就是配置 nodePort
```
{
  "kind": "Service",
  "apiVersion": "v1",
  "metadata": {
    "name": "nginx-all",
    "namespace": "default",
    "selfLink": "/api/v1/namespaces/default/services/nginx-all",
    "uid": "050ea702-1b6d-11e8-a211-080027fc8712",
    "resourceVersion": "45659",
    "creationTimestamp": "2018-02-27T03:19:28Z",
    "labels": {
      "k8s-app": "nginx-all"
    }
  },
  "spec": {
    "ports": [
      {
        "name": "tcp-8080-80-cvw8l",
        "protocol": "TCP",
        "port": 8080,
        "targetPort": 80,
        "nodePort": 31000
      }
    ],
    "selector": {
      "k8s-app": "nginx-all"
    },
    "clusterIP": "10.102.241.166",
    "type": "NodePort",
    "sessionAffinity": "None",
    "externalTrafficPolicy": "Cluster"
  },
  "status": {
    "loadBalancer": {}
  }
}
```
6:通过nginx访问hello-node服务
minikube的 node ip 默认为192.168.99.100
访问 http://192.168.99.100:31000/test 就是通过nginx代理访问了hello-node 服务  


#### 6: service loadbalancer  
可以用一个clusterIP 共享一个ip   
配合自定义代理可以做到一个ip 多个不同端口访问不同服务 配合nginx做代理 做到不受服务的变更 只需要一个clusterIp即可配置nginx 屏蔽底层的serviceIp变化  
这种方案 有单点故障的隐患 和性能问题 暂时不成熟


## 总结: 
说到底 暴露服务还是要通过k8s内部的网络去做   
如果像我一样懒 那就选择ingress  
当然我最终选择还是 自定义方案  
没啥别的 就是nginx配置 之前就有  ingress的配置 特么还要我自己写 肯定选择自定义方案 
如果是新项目 或者不偷懒 建议选择ingress方案 因为毕竟是官方的套路 很多人实践过 后续简单  自定义方案最终还是需要一个基佬去维护配置的  
这里有个坑  就是 如果为了方便 配置 建议所有服务节点一定要有一个统一前缀  如果没有 请尽量推动各位基佬增加一个统一前缀 不然写配置 要写疯    









