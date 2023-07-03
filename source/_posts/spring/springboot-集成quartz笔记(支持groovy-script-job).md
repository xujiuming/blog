---
title: spring boot  + quartz实现 动态定时任务笔记及其想法
categories: 实战
tags:
  - spring
  - quartz
abbrlink: b7d64853
date: 2017-11-11 00:00:00
---
#### quartz 集成 到spring boot 中
有个偷懒的方法 直接引用 这个  但是这个不是spring组织搞 的

1:gradle 依赖配置
```
buildscript {
	ext {
		springBootVersion = '1.5.8.RELEASE'
	}
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
	}
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'org.springframework.boot'

group = 'com.ming'
version = '1.0'
sourceCompatibility = 1.8
targetCompatibility = 1.8
jar {
	//指定 main class
	manifest{
		attributes 'Main-Class': 'com.ming.StartMing'
	}
}

repositories {

	maven { url "http://maven.aliyun.com/nexus/content/groups/public/" }
	maven { url "http://repo.maven.apache.org/maven2" }
	mavenCentral()
}


dependencies {
	compile('org.springframework.boot:spring-boot-starter-actuator')
	compile('org.springframework.boot:spring-boot-starter-aop')
	compile('org.springframework.boot:spring-boot-starter-data-jpa')
	compile('org.springframework.boot:spring-boot-starter-mail')
	compile('org.springframework.boot:spring-boot-starter-thymeleaf')
	compile('org.springframework.boot:spring-boot-starter-validation')
	compile('org.springframework.boot:spring-boot-starter-web')
	runtime('org.springframework.boot:spring-boot-devtools')
	runtime('mysql:mysql-connector-java')
	//lombok http://blog.csdn.net/victor_cindy1/article/details/72772841
	compile('org.projectlombok:lombok')
	testCompile('org.springframework.boot:spring-boot-starter-test')
	compile group: 'de.chandre.quartz', name: 'spring-boot-starter-quartz', version: '1.0.1'
	compile group: 'org.apache.shiro', name: 'shiro-spring', version: '1.4.0'
	compile group: 'org.apache.shiro', name: 'shiro-core', version: '1.4.0'
	compile group: 'com.google.guava', name: 'guava', version: '22.0'
	compile group: 'com.alibaba', name: 'fastjson', version: '1.2.33'
	compile group: 'org.apache.commons', name: 'commons-lang3', version: '3.6'
	compile group: 'org.apache.httpcomponents', name: 'httpcore', version: '4.4.6'
	compile(group: 'org.apache.httpcomponents', name: 'httpclient', version: '4.5.3') {
		exclude(module: 'commons-logging')
	}
	compile group: 'commons-io', name: 'commons-io', version: '2.5'
	compile group: 'commons-codec', name: 'commons-codec', version: '1.8'
}



```


2:指明 quartz 配置文件
这个是那个starter-quartz中的配置
```
quartz:
  properties-config-location: classpath:/config/application-quatrz.properties
```
3:配置 quartz  
就是从 quartz 中copy出来的 改成jdbc 存储
```
org.quartz.scheduler.instanceName:DefaultQuartzScheduler
org.quartz.scheduler.rmi.export:false
org.quartz.scheduler.rmi.proxy:false
org.quartz.scheduler.wrapJobExecutionInUserTransaction:false
org.quartz.threadPool.class:org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.threadCount:10
org.quartz.threadPool.threadPriority:5
org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread:true
org.quartz.jobStore.misfireThreshold:60000
#org.quartz.jobStore.class: org.quartz.simpl.RAMJobStore
# jdbc
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.tablePrefix=QRTZ_
org.quartz.jobStore.dataSource=qzDS
org.quartz.dataSource.qzDS.driver=com.mysql.jdbc.Driver
org.quartz.dataSource.qzDS.URL=jdbc:mysql://localhost:3306/ming?useUnicode=true&characterEncoding=UTF-8
org.quartz.dataSource.qzDS.user=root
org.quartz.dataSource.qzDS.password=ming1234
org.quartz.dataSource.qzDS.maxConnections=10

```
4:重头戏 代理job  使用spring bean 中定义的job 
先声明  这个是抄袭的 加上我自己的一点点想法   
原方案 只能支持spring bean 的 我扩展了一波 可使用groovy 来扩充配置、扩充bean  
前几个步骤都是集成进来 这一步 是将 quartz 的job 代理执行 使用注册在spring 中的job bean 执行 任务 
* 方便管理
* 可以使用 groovy 动态注入配置,job
* 方便嵌入其他业务 如日志 等等 
4.1:继承spring中QuartzJobBean 实现基础的job抽象类
通过建立抽象方法doExecute 将任务执行内容代理到实现这个抽象方法的job中 
```
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.Serializable;

/**
 * 基础job  抽象类
 *
 * @author ming
 * @date 2017-11-09 16:32
 */
//表示 Quartz 将会在成功执行 execute() 方法后（没有抛出异常）更新 JobDetail 的 JobDataMap，下一次执行相同的任务（JobDetail）将会得到更新后的值，而不是原始的值。
@PersistJobDataAfterExecution
//禁止 并发执行 job
@DisallowConcurrentExecution
public abstract class BaseJob extends QuartzJobBean implements Serializable {


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        doExecute(context);
    }

    /**
     * 使用代理执行
     *
     * @param context
     * @throws JobExecutionException
     * @author ming
     * @date 2017-11-09 16:08
     */
    protected abstract void doExecute(JobExecutionContext context) throws JobExecutionException;
}
```
4.2：建立代理执行类
实现doExecute 方法 
第一 指定 job实例来源于spring 容器
第二 可以插入其他业务 例如日志 之类的
```
/**
 * 代理执行 job  前后处理日志
 *
 * @author ming
 * @date 2017-11-09 16:11
 */
@Slf4j
public class ProxyJob extends BaseJob {

    @Override
    protected void doExecute(JobExecutionContext context) throws JobExecutionException {
       /* if (schedulerManageDao == null) {
            schedulerManageDao = applicationContext.getBean(SchedulerManageDao.class);
        }*/
        // 执行
        JobDetail jobDetail = context.getJobDetail();
        String jobName = jobDetail.getKey().getName();
        BaseProxyJob job;
        Date beginTime = new Date();
        // Long dispatchId = schedulerManageDao.addDispatchLog(ApplicationConfig.SCHEDULER_CLUSTER_NAME,  ApplicationConfig.SCHEDULER_INSTANCE_NAME, beginTime.getTime(), 0L, jobName, ScheduleExecuteLog.STATUS_BEGIN, 0L, "");
        try {
            job = SpringBeanManager.getbeanByNameAndType(jobName, BaseProxyJob.class);
            job.execute();
            log.info("[执行成功]" + jobName);
            Date endTime = new Date();
            // 记录任务完成
            //schedulerManageDao.updateDispatchLogById(dispatchId, ScheduleExecuteLog.STATUS_SUCCESS, endTime.getTime(), endTime.getTime() - beginTime.getTime(), null);
        } catch (Exception e) {
            log.error("[执行异常]" + jobName + ":::" + e.getMessage());
            Date endTime = new Date();
            // 打印异常并发送异常
            // String exceptionMessage = ExceptionUtils.getStackTrace(e);
            //logger.error("[doProcess][job({}) 异常：{}]", jobName, exceptionMessage);
            //if (dispatchId != null) {
            //    schedulerManageDao.updateDispatchLogById(dispatchId, ScheduleExecuteLog.STATUS_FAILURE, endTime.getTime(), endTime.getTime() - beginTime.getTime(), exceptionMessage);
            //}
        }
    }
}

```
4.3:建立基础代理job抽象类
```

import lombok.extern.slf4j.Slf4j;

/**
 * 定时器具体任务实现任务基类。所有子类需要继承它.
 * <pre>
 *     2. 使用{@link #setMemo(String)}可以设置任务结束后备注
 *     3. 当任务出现异常时，会被记录到日志里并标记任务失败。所以任务的异常需要抛出来，不要catch掉不抛出。
 *     4. 记得实现类加{@link org.springframework.stereotype.Service}注解，让它可以被spring扫描到
 * </pre>
 *
 * @author ming
 * @date 2017-11-09 16:32
 */
@Slf4j
public abstract class BaseProxyJob {

    /**
     * 任务执行完之后的备注
     */
    private String memo;

    /**
     * 实现
     */
    public abstract void execute();

    public String getMemo() {
        return memo;
    }

    /**
     * 设置执行后备注
     *
     * @param memo 备注
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }
}
```
4.4:细分job类型
这个是在上面的基础上继续细化job的来源类型
方便针对不同的job来进行处理 例如 groovy 脚本写的抽象类 需要从数据库中读取相关数据 动态注入到spring 容器中 
所有的来自groovy 脚本的job抽象类
```
/**
 * groovy job 实现这个接口
 *
 * @author ming
 * @date 2017-11-08 16:59
 */
public abstract class BaseScriptJob extends BaseProxyJob {
}
```
所有直接java写的编译好的job抽象类
```

/**
 * java 实现的 实现这个接口
 *
 * @author ming
 * @date 2017-11-08 16:59
 */
public abstract class BaseSimpleJob extends BaseProxyJob {
}
```
具体demo请参考:http://github.xujiuming.com  下mingqz项目 
####总结:通过编写抽象类  将 具体要执行的业务 代理执行掉  这样 就可以利用spring 的特性 去用groovy 做动态job 避免每次变更 都要从新部署  
