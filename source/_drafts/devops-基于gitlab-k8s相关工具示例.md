---
title: devops-基于gitlab+k8s相关工具示例
comments: true
categories: 实例
tags:
  - devops
  - gitlab
  - k8s
abbrlink: 31b671f6
date: 2021-01-14 13:41:42
---
#### 前言 
基于gitlab + k8s持续集成示例 
gitlab做代码仓库 和 ci/cd  
k8s做基础环境 所有应用全部部署在k8s中 

> gitlab+k8s+docker仓库 最简示例 
>大概流程:代码提交到gitlab ->触发ci/cd任务 ->编译构建->推送镜像->部署镜像   
#### 部署组件  
基础环境:   
操作系统: ubuntu 20.04 server版本  
kubeadm版本: kubeadm version: &version.Info{Major:"1", Minor:"20", GitVersion:"v1.20.2", GitCommit:"faecb196815e248d3ecfb03c680a4507229c2a56", GitTreeState:"clean", BuildDate:"2021-01-13T13:25:59Z", GoVersion:"go1.15.5", Compiler:"gc", Platform:"linux/amd64"}

k8s版本: 
gitlab版本:

##### 部署k8s 
* 初始化系统  
从 {% post_link k8s/ubuntu2004上使用kubeadm初始化单机k8s笔记 %} 笔记复制过来的  方便自己查询    
```shell
#关闭
swapoff -a
# 删除 swap行 
cat /etc/fstab
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://7vm1yv9c.mirror.aliyuncs.com"]
}
EOF
cat > /etc/sysctl.d/k8s.conf <<EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
# 生效
sysctl --system
```
* 安装kubeadm kubelet kubectl   
```shell
sudo apt-get update && sudo apt-get install -y ca-certificates curl software-properties-common apt-transport-https curl
curl -s https://mirrors.aliyun.com/kubernetes/apt/doc/apt-key.gpg | sudo apt-key add -
 
sudo tee /etc/apt/sources.list.d/kubernetes.list <<EOF 
deb https://mirrors.aliyun.com/kubernetes/apt/ kubernetes-xenial main
EOF
 
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# 设置kubelet 开机自启动  
systemctl enable kubelet
```
* 初始化k8s集群 版本未1.20.2    
```shell
kubeadm init --kubernetes-version=1.20.2 \
--pod-network-cidr 152.16.0.0/16  \
--image-repository registry.aliyuncs.com/google_containers
```

* 初始化kubectl  
```shell
  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config
```
* 查看 集群信息  
```shell
kubectl get all --all-namespaces   
```
* 部署 calico 
```shell
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml
```

> 由于做示例 这里就 不部署 dashboard-ui了  

##### 部署gitlab 
> 直接部署在k8s集群中   

* gitlab deployment配置  
gitlab-deployment.yaml
```yaml
```

* 启动
```shell
kubectl apply -c gitlab-deployment.yaml  
```

* 登录  

##### 部署docker仓库  
> 偷懒 不部署 harbor 或者nexus  直接部署  docker registry 

#### 构建持续集成 
##### 初始化示例项目 
##### 编写gitlab runner 配置
##### 测试 持续集成结果  


#### 总结  



