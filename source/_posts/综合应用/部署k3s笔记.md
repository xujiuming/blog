---
title: 部署k3s笔记
comments: true
date: 2022-05-12 15:18:19
categories: 笔记
tags:
 - k3s
 - docker
 - k8s
---
#### 前言  
想部署一个k8s实验环境 
看了很多  minikube  kind k3s 发现还是rancher的k3s对资源要求少

> https://rancher.com/docs/k3s/latest/en/

#### 部署 

##### 部署k3s 
> k3s 本质上就是一个独立的可执行文件 包含整个k8s的manager的组件  
> 大于1g的内存和能访问github站点的权限和安装好docker 
> https://rancher.com/docs/k3s/latest/en/installation/install-options/

```shell
# 官方脚本部署  
curl -sfL https://get.k3s.io | INSTALL_K3S_MIRROR=cn sh -
# 查看部署的k8s 集群信息 
k3s kubectl get all --all-namespaces 
# 增加一些常用的别名 
alias kubectl='k3s kubectl'
```

##### 安装k8s-dashboard  
> https://rancher.com/docs/k3s/latest/en/installation/kube-dashboard/

* 安装dashboard 
```shell
GITHUB_URL=https://github.com/kubernetes/dashboard/releases
VERSION_KUBE_DASHBOARD=$(curl -w '%{url_effective}' -I -L -s -S ${GITHUB_URL}/latest -o /dev/null | sed -e 's|.*/||')
sudo k3s kubectl create -f https://raw.githubusercontent.com/kubernetes/dashboard/${VERSION_KUBE_DASHBOARD}/aio/deploy/recommended.yaml
```

* 创建rbac角色权限配置   
dashboard.admin-user.yml
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard
```
dashboard.admin-user-role.yml
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kubernetes-dashboard
```

部署配置和生成token 
```shell
#部署角色权限配置 
sudo k3s kubectl create -f dashboard.admin-user.yml -f dashboard.admin-user-role.yml
# 获取token 
sudo k3s kubectl -n kubernetes-dashboard describe secret admin-user-token | grep '^token'
```

> 此时可以本地访问 http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/

通过转发代理配置远程访问
> https://kubernetes.io/docs/tasks/access-application-cluster/port-forward-access-application-cluster/
> https://segmentfault.com/a/1190000023130407

```shell
# 查看部署的service的端口 
sudo k3s kubectl get  service -A |grep dashboard 
# 使用port-forward 访问dashboard  访问https://ip:9000  输入刚刚获取的token    
sudo k3s kubectl -n kubernetes-dashboard port-forward service/kubernetes-dashboard 9000:443  --address 0.0.0.0
```

* 升级仪表板链接
```shell
sudo k3s kubectl delete ns kubernetes-dashboard
GITHUB_URL=https://github.com/kubernetes/dashboard/releases
VERSION_KUBE_DASHBOARD=$(curl -w '%{url_effective}' -I -L -s -S ${GITHUB_URL}/latest -o /dev/null | sed -e 's|.*/||')
sudo k3s kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/${VERSION_KUBE_DASHBOARD}/aio/deploy/recommended.yaml -f dashboard.admin-user.yml -f dashboard.admin-user-role.yml
```

* 删除仪表板和管理员用户配置链接
```shell
sudo k3s kubectl delete ns kubernetes-dashboard
sudo k3s kubectl delete clusterrolebinding kubernetes-dashboard
sudo k3s kubectl delete clusterrole kubernetes-dashboard
```

##### 安装接入helm 
> k3s当前版本使用最新的helm即可  
> https://helm.sh/docs/

* 安装helm 
> https://helm.sh/docs/intro/install/
> 服务器版本是ubuntu 

```shell
curl https://baltocdn.com/helm/signing.asc | sudo apt-key add -
sudo apt-get install apt-transport-https --yes
echo "deb https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
sudo apt-get update
sudo apt-get install helm
```

* 设置KUBECONFIG变量
> 直接设置在当前shell或者写入 .bashrc .zshrc之类的地方都可以 只要当前shell 有KUBECONFIG变量就行 
```shell
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
echo $KUBECONFIG
helm list -A 
```

##### 安装示例应用 
> k8s 安装各种应用 可以直接去编写 yaml来实现  也可以直接使用helm这种工具 
> 这里演示 就直接使用helm来   

```shell
# 添加指定仓库
sudo helm repo add bitnami https://charts.bitnami.com/bitnami
# 安装bitnami/nginx 名称为testnginx 
sudo helm install testnginx bitnami/nginx 
# 查看helm安装的testnginx状态
sudo helm status testnginx
# 查看当前helm 安装的charts列表   
sudo helm list -A
# 查看当前k3s集群的各种资源状态  这个charts 默认是在service开启nodePort 例如这里显示的就是30371  
sudo kubectl get all -A  
# 访问node 的30371 安装的testnginx的服务   
curl http://127.0.0.1:30371 
```


#### 总结
k3s 总体安装使用 感觉还行 符合我的预期  
简单粗暴 然后和k8s 区别不大    



















