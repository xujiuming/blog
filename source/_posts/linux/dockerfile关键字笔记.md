---
title: dockerfile关键字笔记
comments: true
categories: 笔记
tags:
  - dockerfile
abbrlink: a44739e3
date: 2018-09-03 10:47:11
---
#### 前言
虽然经常写dockerfile 但是老是去官网查询 翻墙慢的一批  干脆 从官网上把常用的dockerfile关键字记录下来方便查询 
参考地址:   
https://docs.docker.com/engine/reference/builder/#parser-directives   
https://yeasy.gitbooks.io/docker_practice/image/dockerfile/references.html    

#### dockerfile 关键字  
|关键字|作用|备注|  
|:----|:--|:-------------------|  
|FROM|指定基础镜像|指定dockerfile基于那个image构建，建议格式写全 registry/namespace/project:tag  或者 registry/namespace/project:tag  as  aliasName|
|MAINTAINER|作者信息|用来标明这个dockerfile谁写的|
|LABEL|标签|用来标明dockerfile的标签 可以使用Label代替Maintainer 最终都是在docker image基本信息中可以查看|
|RUN|执行命令|执行一段命令 默认是/bin/sh 格式: RUN command 或者 RUN \["command" , "param1","param2"]|
|CMD|容器启动命令|提供启动容器时候的默认命令 和ENTRYPOINT配合使用 格式 CMD command param1 param2 或者 CMD \["command" , "param1","param2"] |
|ENTRYPOINT|入口|一般在制作一些执行就关闭的容器中会使用 配合CMD 使用更好 下面会专门说这个东西|
|COPY|复制文件|build的时候复制文件到image中  可以使用通配符 只要复合go的filepath.Match规则就行 COPY source target 如果是从标准输出流构建 无法使用这个关键字|
|ADD|添加文件|build的时候添加文件到image中 不仅仅局限于当前build上下文 可以来源于远程服务  ADD source target  注意: target如果带/ 那么会把target当成文件夹 内部文件名称自动推断 如果没有带/那么可能蒋target当成一个文件|
|ENV|环境变量|指定build时候的环境变量 可以在启动的容器的时候 通过-e覆盖 格式ENV name=value ... |
|ARG|构建参数|构建参数 只在构建的时候使用的参数 如果有ENV 那么ENV的相同名字的值始终覆盖arg的参数 |
|VOLUME|定义外部可以挂载的数据卷|指定build的image那些目录可以启动的时候挂载到文件系统中 启动容器的时候使用 -v 绑定  格式 VOLUME \["目录"] |
|EXPOSE|暴露端口|定义容器运行的时候监听的端口 启动容器的使用-p来绑定暴露端口 格式: EXPOSE 8080 或者 EXPOSE 8080/udp  如果不指定监听tcp还是udp 那么默认为tcp|
|WORKDIR|工作目录|指定容器内部的工作目录 如果没有创建则自动创建 如果指定/ 使用的是绝对地址 如果不是/开头那么是在上一条workdir的路径的相对路径|
|USER|指定执行用户|指定build或者启动的时候 用户   在RUN CMD ENTRYPONT执行的时候的用户|
|HEALTHCHECK|健康检查|指定监测当前容器的健康监测的命令  基本上没用 因为很多时候 应用本身有健康监测机制|
|ONBUILD|触发器|当存在ONBUILD关键字的镜像作为基础镜像的时候 当执行FROM完成之后 会执行 ONBUILD的命令 但是不影响当前镜像  用处也不怎么大 |
|STOPSIGNAL|发送信号量到宿主机|该STOPSIGNAL指令设置将发送到容器的系统调用信号以退出。此信号可以是与内核的系统调用表中的位置匹配的有效无符号数，例如9，或SIGNAME格式的信号名，例如SIGKILL。 这个不太明白干啥的 看起来是和宿主机通过信号量交互的|
|SHELL| 指定执行脚本的shell |指定RUN CMD ENTRYPOINT 执行命令的时候 使用的shell 例如 sh bash zsh  powershell 等 格式 SHELL zsh |

#### 需要注意的套路
#####  build环境选择  
必须linux 内核版本高于3.10   
不允许在windows上构建 容易出现坑爹的问题    
#####   CMD 和ENTRYPOINT 区别联系   
联系:   
当存在ENTRYPOINT的时候 CMD成为了参数  ENTRYPOINT = ENTRYPOINT \<CMD>
区别:  
CMD 就是单纯的执行一段shell  当要为 CMD添加参数的时候 只能选择全部覆盖CMD命令
如果是ENTRYPOINT 那么可以直接在启动容器最后添加 参数  因为当入口是ENTRYPOINT的时候CMD就成为了ENTRYPOINT的参数   
#####   ADD 和COPY的区别联系   
联系:都是把文件添加到容器的image中   
区别:     
COPY 只能读取当前构建的上下文 如果是从标准输出流构建 COPY无法使用  
ADD 可以使用远程服务的文件目录      

#####   VOLUME挂载目录需要注意的     
1: 如果宿主机是windows  不能挂载空目录、和c盘的目录   
2: 在VOLUME之后的构建操作 都无法生效 要求 暴露挂载卷 必须在最后一步 声明    

#####  构建镜像的注意点   
1: 尽量选择 微缩基础镜像 例如alpine  减少镜像的体积
2: 尽量不要打包无用的文件到镜像中 减少镜像的体积
3: 尽量删除 各种编译、下载、安装过程中产生的缓存文件 减少镜像体积 
4: 尽量使用分阶段构建镜像 一步步的扩展镜像功能  避免多层数镜像的产生
5: 准确使用dockerfile的关键字 例如CMD 和ENTRYPOINT  、 COPY 和ADD
#### 总结
dockerfile的关键字 不多 基本上该考虑的场景 也都考虑的    