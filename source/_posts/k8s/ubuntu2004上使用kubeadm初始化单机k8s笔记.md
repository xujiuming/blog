---
title: ubuntu20.04上使用kubeadm初始化单机k8s笔记
comments: true
categories: 笔记
tags:
  - kubeadm
  - k8s
  - ubuntu
abbrlink: 47ddc293
date: 2020-05-25 08:10:07
---
#### 前言 
个人测试学习 测试使用的k8s集群  部署方案 大致有三种 minikube microk8s kubeadm
前两种 略过  
kubeadm 是k8s官方提供的安装部署工具 能够很简单的搭建管理 单体、或者HA的k8s集群  

#### 示例   
环境:  
* ubuntu20.04

> 参考文档:  
> https://kubernetes.io/zh/docs/reference/setup-tools/kubeadm/kubeadm/
> https://www.kubernetes.org.cn/7189.html   
> https://blog.csdn.net/l1028386804/article/details/105904557

##### 调整配置 
* 关闭swap 

```shell script
#关闭
swapoff -a
# 删除 swap行 
cat /etc/fstab 
```

* 配置docker加速
```shell script
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://7vm1yv9c.mirror.aliyuncs.com"]
}
EOF
```

* 配置内核参数 
```shell script
cat > /etc/sysctl.d/k8s.conf <<EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
# 生效
sysctl --system 
```

#####  安装kubectl kubelet kubeadm 
```shell script
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

##### 初始化集群
```shell script
kubeadm init --kubernetes-version=1.18.0 \
--pod-network-cidr 152.16.0.0/16  \
--image-repository registry.aliyuncs.com/google_containers  
```
日志:
```text
W0525 14:53:34.914975    8467 configset.go:202] WARNING: kubeadm cannot validate component configs for API groups [kubelet.config.k8s.io kubeproxy.config.k8s.io]
[init] Using Kubernetes version: v1.18.0
[preflight] Running pre-flight checks
	[WARNING Service-Docker]: docker service is not enabled, please run 'systemctl enable docker.service'
	[WARNING IsDockerSystemdCheck]: detected "cgroupfs" as the Docker cgroup driver. The recommended driver is "systemd". Please follow the guide at https://kubernetes.io/docs/setup/cri/
[preflight] Pulling images required for setting up a Kubernetes cluster
[preflight] This might take a minute or two, depending on the speed of your internet connection
[preflight] You can also perform this action in beforehand using 'kubeadm config images pull'
^C
root@ming:/home/ming# kubeadm init --kubernetes-version=1.18.0 \
> --pod-network-cidr 152.16.0.0/16  \
> --image-repository registry.aliyuncs.com/google_containers  
W0525 14:53:51.259525    8991 configset.go:202] WARNING: kubeadm cannot validate component configs for API groups [kubelet.config.k8s.io kubeproxy.config.k8s.io]
[init] Using Kubernetes version: v1.18.0
[preflight] Running pre-flight checks
	[WARNING Service-Docker]: docker service is not enabled, please run 'systemctl enable docker.service'
	[WARNING IsDockerSystemdCheck]: detected "cgroupfs" as the Docker cgroup driver. The recommended driver is "systemd". Please follow the guide at https://kubernetes.io/docs/setup/cri/
[preflight] Pulling images required for setting up a Kubernetes cluster
[preflight] This might take a minute or two, depending on the speed of your internet connection
[preflight] You can also perform this action in beforehand using 'kubeadm config images pull'
[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
[kubelet-start] Starting the kubelet
[certs] Using certificateDir folder "/etc/kubernetes/pki"
[certs] Generating "ca" certificate and key
[certs] Generating "apiserver" certificate and key
[certs] apiserver serving cert is signed for DNS names [ming kubernetes kubernetes.default kubernetes.default.svc kubernetes.default.svc.cluster.local] and IPs [10.96.0.1 172.23.50.141]
[certs] Generating "apiserver-kubelet-client" certificate and key
[certs] Generating "front-proxy-ca" certificate and key
[certs] Generating "front-proxy-client" certificate and key
[certs] Generating "etcd/ca" certificate and key
[certs] Generating "etcd/server" certificate and key
[certs] etcd/server serving cert is signed for DNS names [ming localhost] and IPs [172.23.50.141 127.0.0.1 ::1]
[certs] Generating "etcd/peer" certificate and key
[certs] etcd/peer serving cert is signed for DNS names [ming localhost] and IPs [172.23.50.141 127.0.0.1 ::1]
[certs] Generating "etcd/healthcheck-client" certificate and key
[certs] Generating "apiserver-etcd-client" certificate and key
[certs] Generating "sa" key and public key
[kubeconfig] Using kubeconfig folder "/etc/kubernetes"
[kubeconfig] Writing "admin.conf" kubeconfig file
[kubeconfig] Writing "kubelet.conf" kubeconfig file
[kubeconfig] Writing "controller-manager.conf" kubeconfig file
[kubeconfig] Writing "scheduler.conf" kubeconfig file
[control-plane] Using manifest folder "/etc/kubernetes/manifests"
[control-plane] Creating static Pod manifest for "kube-apiserver"
[control-plane] Creating static Pod manifest for "kube-controller-manager"
W0525 14:54:07.094575    8991 manifests.go:225] the default kube-apiserver authorization-mode is "Node,RBAC"; using "Node,RBAC"
[control-plane] Creating static Pod manifest for "kube-scheduler"
W0525 14:54:07.096371    8991 manifests.go:225] the default kube-apiserver authorization-mode is "Node,RBAC"; using "Node,RBAC"
[etcd] Creating static Pod manifest for local etcd in "/etc/kubernetes/manifests"
[wait-control-plane] Waiting for the kubelet to boot up the control plane as static Pods from directory "/etc/kubernetes/manifests". This can take up to 4m0s
[apiclient] All control plane components are healthy after 24.002131 seconds
[upload-config] Storing the configuration used in ConfigMap "kubeadm-config" in the "kube-system" Namespace
[kubelet] Creating a ConfigMap "kubelet-config-1.18" in namespace kube-system with the configuration for the kubelets in the cluster
[upload-certs] Skipping phase. Please see --upload-certs
[mark-control-plane] Marking the node ming as control-plane by adding the label "node-role.kubernetes.io/master=''"
[mark-control-plane] Marking the node ming as control-plane by adding the taints [node-role.kubernetes.io/master:NoSchedule]
[bootstrap-token] Using token: 58pvbb.xkvn7l91zil7kofv
[bootstrap-token] Configuring bootstrap tokens, cluster-info ConfigMap, RBAC Roles
[bootstrap-token] configured RBAC rules to allow Node Bootstrap tokens to get nodes
[bootstrap-token] configured RBAC rules to allow Node Bootstrap tokens to post CSRs in order for nodes to get long term certificate credentials
[bootstrap-token] configured RBAC rules to allow the csrapprover controller automatically approve CSRs from a Node Bootstrap Token
[bootstrap-token] configured RBAC rules to allow certificate rotation for all node client certificates in the cluster
[bootstrap-token] Creating the "cluster-info" ConfigMap in the "kube-public" namespace
[kubelet-finalize] Updating "/etc/kubernetes/kubelet.conf" to point to a rotatable kubelet client certificate and key
[addons] Applied essential addon: CoreDNS
[addons] Applied essential addon: kube-proxy

Your Kubernetes control-plane has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 172.23.50.141:6443 --token 58pvbb.xkvn7l91zil7kofv \
    --discovery-token-ca-cert-hash sha256:17780ec54bf6d118f95fa644b067504b3721fafdb95c8bd674c231192a763dfa 
```

##### 创建kubectl 

```shell script 
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
kubectl get all
```

##### 安装网络 calico 

```shell script
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml
```

执行日志：    

```text
configmap/calico-config created
customresourcedefinition.apiextensions.k8s.io/bgpconfigurations.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/bgppeers.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/blockaffinities.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/clusterinformations.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/felixconfigurations.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/globalnetworkpolicies.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/globalnetworksets.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/hostendpoints.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/ipamblocks.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/ipamconfigs.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/ipamhandles.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/ippools.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/kubecontrollersconfigurations.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/networkpolicies.crd.projectcalico.org created
customresourcedefinition.apiextensions.k8s.io/networksets.crd.projectcalico.org created
clusterrole.rbac.authorization.k8s.io/calico-kube-controllers created
clusterrolebinding.rbac.authorization.k8s.io/calico-kube-controllers created
clusterrole.rbac.authorization.k8s.io/calico-node created
clusterrolebinding.rbac.authorization.k8s.io/calico-node created
daemonset.apps/calico-node created
serviceaccount/calico-node created
deployment.apps/calico-kube-controllers created
serviceaccount/calico-kube-controllers created
```

```shell script
# 查看calico插件安装启动完毕 没有 
kubectl get pod --all-namespaces    
```

运行成功的样子:     

```text
NAMESPACE     NAME                                       READY   STATUS    RESTARTS   AGE
kube-system   calico-kube-controllers-789f6df884-jjsxt   1/1     Running   0          2m41s
kube-system   calico-node-pl5nm                          1/1     Running   0          2m41s
kube-system   coredns-7ff77c879f-26bcj                   1/1     Running   0          6m23s
kube-system   coredns-7ff77c879f-r9fr5                   1/1     Running   0          6m23s
kube-system   etcd-ming                                  1/1     Running   0          6m33s
kube-system   kube-apiserver-ming                        1/1     Running   0          6m33s
kube-system   kube-controller-manager-ming               1/1     Running   0          6m33s
kube-system   kube-proxy-c6f8v                           1/1     Running   0          6m23s
kube-system   kube-scheduler-ming                        1/1     Running   0          6m33s
```

##### 安装kubernetes-dashboard 

```shell script
wget  https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-rc7/aio/deploy/recommended.yaml
 # 修改service 的暴露方式 使用NodePort
vim recommended.yaml
# kind: Service
# apiVersion: v1
# metadata:
#   labels:
#     k8s-app: kubernetes-dashboard
#   name: kubernetes-dashboard
#   namespace: kubernetes-dashboard
# spec:
#   type: NodePort
#   ports:
#     - port: 443
#       targetPort: 8443
#       nodePort: 30000
#   selector:
#     k8s-app: kubernetes-dashboard
# 启动 dashboard
kubectl create -f recommended.yaml
# namespace/kubernetes-dashboard created
# serviceaccount/kubernetes-dashboard created
# service/kubernetes-dashboard created
# secret/kubernetes-dashboard-certs created
# secret/kubernetes-dashboard-csrf created
# secret/kubernetes-dashboard-key-holder created
# configmap/kubernetes-dashboard-settings created
# role.rbac.authorization.k8s.io/kubernetes-dashboard created
# clusterrole.rbac.authorization.k8s.io/kubernetes-dashboard created
# rolebinding.rbac.authorization.k8s.io/kubernetes-dashboard created
# clusterrolebinding.rbac.authorization.k8s.io/kubernetes-dashboard created
# deployment.apps/kubernetes-dashboard created
# service/dashboard-metrics-scraper created
# deployment.apps/dashboard-metrics-scraper created
```

> 访问: https://localhost:30000

获取token
```shell script
# 获取token 
 kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep dashboard-admin | awk '{print $1}')
```
![20200525155126](https://xujiuming.com/ming-static/vscode/caf3782174393d803256b08eaa136a66.png)


如果报错如下:
权限不足:
![20200525153933](https://xujiuming.com/ming-static/vscode/d6e789389ccbec17918226f24ae1fb26.png)

新增权限:   

```shell script 
echo '
apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    k8s-app: kubernetes-dashboard
  name: dashboard-admin
  namespace: kubernetes-dashboard' > dashboard-admin.yaml 
kubectl create -f ./dashboard-admin.yaml 

echo '
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: dashboard-admin-bind-cluster-role
  labels:
    k8s-app: kubernetes-dashboard
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: dashboard-admin
  namespace: kubernetes-dashboard' > dashboard-role.yaml
kubectl create -f ./dashboard-role.yaml   
#重新获取token  
 kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep dashboard-admin | awk '{print $1}')
```
![20200525155747](https://xujiuming.com/ming-static/vscode/f3f09730c1031d5032166b2fedce2da1.png)


##### 重启服务器恢复K8s 
```shell script
docker start $(docker ps -a | awk '{print $1}' |tail -n +2)
```

#### 总结 
网络上 大多是以centos 来举例  事实上ubuntu部署会比centos更加简单  因为版本迭代快 各种依赖、内核 都是最新的 
使用kubeadm 部署一套单机环境 每什么大问题 主要还是配置好服务器 如关闭swap  selinux等常规要求   


