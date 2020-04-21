---
title: ansilbe安装和command模式使用笔记
comments: true
categories: 笔记
tags:
  - ansilbe
  - devops
abbrlink: 8e7a2ca0
date: 2019-12-10 16:00:23
---
#### 前言 
ansible 听说过很多次了 一直偷懒  没好好看过一些教程文章 
今天抽点时间 把安装和使用command模式记录一下 方便后续使用 
#### 安装 
此处直接复制 https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html

##### ubuntu 安装
```shell script
sudo apt update
sudo apt install software-properties-common
sudo apt-add-repository --yes --update ppa:ansible/ansible
sudo apt install ansible
```
##### cent os 安装  
由于 ansible 是redhat出品  在redhat系列系统 直接安装即可  
* yum 
```shell script
 sudo yum install ansible
```
* dnf 
```shell script
sudo dnf install ansible
```
##### pip安装 
```shell script
pip install ansible
```

#### command模式使用
##### 配置hosts 
> 默认位置 /etc/ansible/hosts

* Inventory 分组
    Ansible可同时操作属于一个组的多台主机，组和主机之间的关系通过inventory文件配置，默认文件路径为/etc/ansible/hosts
 
* 常用参数配置：

|配置名|功能|例子|
|:---|:---|:---|
|ansible_ssh_host|将要连接的远程主机名.与你想要设定的主机的别名不同的话,可通过此变量设置.|ansible_ssh_host=192.169.1.1|
|ansible_ssh_port|ssh端口号.如果不是默认的端口号,通过此变量设置.|ansible_ssh_port=5000|
|ansible_ssh_user|默认的 ssh 用户名	ansible_ssh_user=ming|
|ansible_ssh_pass|ssh 密码(这种方式并不安全,我们强烈建议使用 --ask-pass 或 SSH 密钥)|ansible_ssh_pass=’123456’|
|ansible_sudo_pass|sudo 密码(这种方式并不安全,我们强烈建议使用 --ask-sudo-pass)|ansible_sudo_pass=’123456’|
|ansible_sudo_exe|sudo 命令路径(适用于1.8及以上版本)|ansible_sudo_exe=/usr/bin/sudo|
|ansible_connection|与主机的连接类型.比如:local, ssh 或者 paramiko. Ansible 1.2 以前默认使用 paramiko.1.2 以后默认使用 'smart','smart' 方式会根据是否支持 ControlPersist, 来判断'ssh' 方式是否可行.|ansible_connection=local|
|ansible_ssh_private_key_file|ssh 使用的私钥文件.适用于有多个密钥,而你不想使用 SSH 代理的情况.|ansible_ssh_private_key_file=/root/key|
|ansible_shell_type|目标系统的shell类型.默认情况下,命令的执行使用 'sh' 语法,可设置为 'csh' 或 'fish'.|ansible_shell_type=zsh|
|ansible_python_interpreter|目标主机的 python 路径.适用于的情况: 系统中有多个 Python, 或者命令路径不是"/usr/bin/python",比如 \*BSD, 或者 /usr/bin/python不是 2.X 版本的 Python.我们不使用 "/usr/bin/env" 机制,因为这要求远程用户的路径设置正确,且要求 "python" 可执行程序名不可为 python以外的名字(实际有可能名为python26).|ansible_python_interpreter=/usr/bin/python2.6|
|ansible_\*\_interpreter|定义其他语言解释器|ansible_*_interpreter=/usr/bin/ruby|
|ansible_sudo|定义sudo用户|ansible_sudo=ming|
 
* 格式：\[组名] 
    例如 : 
    　　\[test]   　　# 组名  
    　　10.0.0.1　 # 主机ip  或者10.0.0.1:65522 自定义端口
* 别名
    s1 ansible_ssh_port=65522 ansible_ssh_host=10.0.0.1 ansible_ssh_user=simon  　　# 别名s1
    
##### 执行命令  
ping    
ansible 组名 -m 模块名   
```shell script
ansible all -m ping
```

echo    
ansible 组名  -m  模块名  -a "在远程机器上执行的命令"   
```shell script  
ansible all -m command -a "echo Hello World"
```
#### 总结 
ansible 这个工具 相见恨晚  早点学会 之前就不用做那么多苦力活了 
一直一来 都想找个顺手的运维管理工具  用过一些 不是很顺手 当是也看到ansible 觉得学起来好难 懒得学 


