---
title: spring cloud ribbon 负载均衡笔记
categories: 笔记
tags:
  - spring
abbrlink: 878fb6fb
date: 2017-11-11 00:00:00
---
###ribbon负载均衡  
必须存在
```
    @Bean
    @LoadBalanced//通过再restTemplate中添加拦截器 实现负载均衡、重写uri等功能
    RestTemplate restTemplate(){
        return new RestTemplate();
    }
```
#### ServiceInstanceChooser  服务实例选择
choose 根据serviceId选择一个实例
####LoadBalancerClient extends ServiceInstanceChooser 负载均衡客户端
execute 获取负载均衡后的实例、服务id之后 执行请求
reconstructURI 根据实例重写uri 获取服务实例后 重写uri
####LoadBalancerAutoConfiguration 自动配置类
* 必须存在 restTemplate 通过http请求 添加拦截器来实现负载均衡
* 必须存在 LoadBalancerClient 的实现
核心功能:
static class LoadBalancerInterceptorConfig通过这个配置类 配置负载均衡拦截器 向restTemplate添加负载均衡功能    
ribbonInterceptor(LoadBalancerClient loadBalancerClient,LoadBalancerRequestFactory loadBalancerRequestFactory);  创建 负载均衡拦截器   
restTemplateCustomizer(LoadBalancerInterceptor loadBalancerInterceptor);为restTemplate设置负载均衡拦截器  
通过执行 负载均衡器的execute方法来选择实例和重写uri、执行请求

static class RetryAutoConfiguration 自动重试机制配置
也是通过创建重试拦截器 添加到restTemplate中 来设置自动重试
####ILoadBalancer 
获取服务实例不是采用serviceInstancerChooser获取的，而是 netflix ribbon中的ILoadBalancer  
查看 ribbon实现的LoadBalancerClient的getServer方法 就看得到
addServers 添加实例
chooseServer 根据负载均衡策略获取一个实例
markServerDown 标记实例不可用
getReachableServers 获取可用实例
getAllServers 获取所有实例   
里面主要就是维护实例列表和获取列表方法、server类定义了一个服务节点的基本信息
实现类：
* AbstractLoadBalancer implements ILoadBalancer 负载均衡器 抽象类  
抽象定义:   
serverGroup：ALL(所有)、STATUS_UP(可用)、STATUS_NOT_UP(不可用)    
实现的方法：  
chooseServer 调用接口的chooseServer(null)  选择实例的时候忽略key的条件判断    
抽象方法:
abstract getServerList(serverGroup) 根据实例组 枚举来获取 相应实例列表  
abstract getLoadBalancerStats() 获取负载均衡 统计数据  
* NoOpLoadBalancer extends AbstractLoadBalancer   

* BaseLoadBalancer extends AbstractLoadBalancer   实现了基本的负载均衡器
定义可用实例集合 allServerList 、upServerList   
定义负载均衡统计信息 LoadBalancerStats   
定义 IPing=null 需要注入  
定义 IPingStrategy iping执行策略  默认使用 BaseLoadBalancer中的SerialPingStrategy 线性执行策略 通过重写IPingStrategy 来使用更高效率的 ping策略  
定义 IRule 负载均衡处理规则  默认使用RoundRobinRule  线性负载均衡规则   
启动 ping任务  默认pingIntervalSeconds=10s  使用Timer 执行定时任务   maxTotalPingTimeSeconds=5s最大执行时间
实现 ILoadBalancer 基本操作 addServers 、chooseServer、markServerDown、getReachAbleServers、getAllServers  
定义 其他附加的的信息 例如 IClientConfig、changeListeners、serverStatusListeners等  
* DynamicServerListLoadBalancer extends AbstractLoadBalancer    动态实例负载均衡器  
定义 isSecure = false 默认不使用https
定义 useTunnel =false  是否使用隧道 估计是是否使用pptp协议隧道的意思
定义 ServerList<T> serverListsImpl 实例列表 serverList 有获取初始化服务列表和获取更新的服务列表两个方法  查看实现类 org.springframework.cloud.netflix.ribbon.eureka.EurekaRibbonClientConfiguration#ribbonServerList  DiscoveryEnabledNIWSServerList--》DomainExtractingServerList来构建默认的serverList  获取初始化服务方法、更新服务方法 通过#obtainServersViaDiscovery 方法实现 这个是客户端 获取服务端注册信息 转换成本地缓存 serverList方法
定义 ServerListFilter<T> filter 过滤器  根据过滤条件过滤实例
定义 ServerListUpdater.UpdateAction   服务更新器具体实现  
定义 ServerListUpdater 服务更新器 通过updateListOfServers 先获取要更新的列表--》根据过滤条件 过滤 --》 添加到serverList   
ServerListUpdater 接口:
start 启动服务更新器
stop 关闭服务更新器
getLastUpdate 获取最后更新时间戳
getDurationSinceLastUpdateMs 获取上一次到现在的时间间隔
getNumberMissedCycles 获取错过的更新期数
getCoreThreads 获取核心线程数
PollingServerListUpdater实现类:默认实现 使用定时任务 定时从eureka server 拉取
EurekaNotificationServerListUpdater实现类:通过eureka事件监听 来驱动 
* ZoneAwareLoadBalancer extends DynamicServerListLoadBalancer 区域感知负载均衡器   
#### RibbonClientConfiguration ribbon 客户端配置
ribbonClientConfig 设置ribbonClient 配置
ribbonRule 设置负载均衡 处理规则 实现
ribbonPing  设置负载均衡 ping 方式实现
ribbonServerList 设置实例列表
static RestClientRibbonConfiguration 配置ribbon的restClient相关参数
    ribbonRestClient 根据ribbon相关配置 获取 restClient
ribbonLoadBalancer 获取负载均衡器
ribbonServerListFilter 设置负载均衡服务的过滤器
ribbonLoadBalancerContext 负载均衡器上下文
retryHandler 重试处理
serverIntrospector 实例拦截器
#### ServiceInstance  服务实例
存储实例的 serviceId 、host、port、isSecure(是否使用https)、uri、metaData(元数据map)


    



