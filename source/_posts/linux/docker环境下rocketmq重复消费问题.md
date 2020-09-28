---
title: rocketmq使用docker运行出现重复消费
categories: 实战
tags:
  - docker
abbrlink: 814d8f0a
date: 2017-11-11 00:00:00
---

##### 最近公司网docker切换 现在开发 测试环境切换  我们发现docker环境下 多节点的rocketmq存在一个重复消费的问题 一个消息 被多次消费了 

-----------------------------------------------------------------------------------------

@author 欢总
rocketmq 版本 3.2.6
问题描述：测试环境在docker内部署了两个mq consumer，集群消费模式下，消息理应被平均消费，实际情况是每条消息都被消费了两次。
线上环境是同样的配置，没有出现这个问题。
关掉dokcer内一个consumer节点，本地再启动一个节点，发现也是正常的。
猜想是mq consumer instanceName引起的，如果instanceName一样会重复消费，因为集群消费模式是按instanceName做为唯一消费实例的。
翻了下源码发现，如果没有指定instanceName默认会把pid做为instanceName
```
if (this.instanceName.equals("DEFAULT")) {
    this.instanceName = String.valueOf(UtilAll.getPid());
}
public static int getPid() {
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    String name = runtime.getName(); // format: "pid@hostname"
    try {
        return Integer.parseInt(name.substring(0, name.indexOf('@')));
    }
    catch (Exception e) {
        return -1;
    }
}
```
查看dokcer内consumer的pid发现都是1（由于每台容器都是干净的，只跑一个程序，所以pid都是1），所以导致了重复消费，一般情况下跑在物理机上pid是不一样的（这里略坑，如果真碰巧一样就惨了），所以不会有问题。
有两种解决方案：
1.手动设置instanceName,使每个consumer节点instanceName不一样
2.修改docker容器配置，使它运行时使用宿主机的pid

####总结:这个问题 略坑  mmp 大佬果然厉害
#### 一个是rocketmq 如果没有instanceName会使用pid 做instanceName
####一个是 docker容器都是纯净的会导致pid相同
