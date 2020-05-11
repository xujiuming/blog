---
title: 基于shardingJDBC读写分离笔记
comments: true
categories: 笔记
tags:
  - shardingJDBC
  - 读写分离
abbrlink: ef88076c
date: 2020-05-11 17:34:26
---
#### 前言
读写分离、、、  降低masterDB的负载 提高查询速度  常规手段 
有很多方案可以实现  
最近在看sharding share 的jdbc组件  也能很快速的实现 一主多从 定制从节点负载均衡策略  

#### 示例
##### 增加依赖
```xml
  <!-- https://mvnrepository.com/artifact/org.apache.shardingsphere/sharding-jdbc-core -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-jdbc-core</artifactId>
            <version>4.1.0</version>
        </dependency>
```
##### 配置dataSource 
示例使用的spring data jpa 只需要替换spring ioc容器中的dataSource即可  由于使用的是hikari 连接池  所以dataSource的实现使用hikari的dataSource

此处使用 一主两从  从节点负载均衡使用轮询策略   

```java
package com.ming.base.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.strategy.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * sharding jdbc 配置
 *
 * @author ming
 * @date 2020-05-11 14:56
 */
@Configuration
public class ShardingJDBCConfig {

    /**
     * 基于 hikari 连接池 配置
     * 一主多从  轮询从节点
     *
     * @author ming
     * @date 2020-05-11 15:08
     */
    @Bean
    @Primary
    public DataSource shardingJDBC() throws SQLException {
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>();

        // 配置主库
        HikariDataSource masterDataSource = new HikariDataSource();
        masterDataSource.setDriverClassName("org.postgresql.Driver");
        masterDataSource.setJdbcUrl("jdbc:postgresql://tx.xujiuming.com:15432/ming_master");
        masterDataSource.setUsername("用户名");
        masterDataSource.setPassword("密码");
        dataSourceMap.put("ming_master", masterDataSource);

        // 配置第一个从库
        HikariDataSource slaveDataSource1 = new HikariDataSource();
        slaveDataSource1.setDriverClassName("org.postgresql.Driver");
        slaveDataSource1.setJdbcUrl("jdbc:postgresql://tx.xujiuming.com:15432/ming_slave0");
        slaveDataSource1.setUsername("用户名");
        slaveDataSource1.setPassword("密码");
        dataSourceMap.put("ming_slave0", slaveDataSource1);

        // 配置第二个从库
        HikariDataSource slaveDataSource2 = new HikariDataSource();
        slaveDataSource2.setDriverClassName("org.postgresql.Driver");
        slaveDataSource2.setJdbcUrl("jdbc:postgresql://tx.xujiuming.com:15432/ming_slave1");
        slaveDataSource2.setUsername("用户名");
        slaveDataSource2.setPassword("密码");
        dataSourceMap.put("ming_slave1", slaveDataSource2);

        // 配置读写分离规则 使用轮询负载均衡    参考 RoundRobinMasterSlaveLoadBalanceAlgorithm
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("ming_master_slave"
                , "ming_master"
                , Arrays.asList("ming_slave0", "ming_slave1")
                , new LoadBalanceStrategyConfiguration(new RoundRobinMasterSlaveLoadBalanceAlgorithm().getType()));

        // 获取数据源对象
        return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig, new Properties());
    }

}
```

##### 数据库构建   
由于是本地测试 读写分离是否成功  
这里采用  master 写入  
另外两个slave 复制master库    这样读取是复制master的那个时候的数据  
两个slave 库 数据不一致 用来测试 多slave 负载均衡  

master -> 主库   
slave0 -> 从库1 n条数据 
slave1 -> 从库2  n-m条数据    
```sql
create database ming_master;
create database ming_slave0;
create database ming_slave1;
```

##### 负载均衡策略 
MasterSlaveLoadBalanceAlgorithm的实现类      
默认有两种       
* RoundRobinMasterSlaveLoadBalanceAlgorithm 轮询    
* RandomMasterSlaveLoadBalanceAlgorithm  随机算法   
###### 自实现负载均衡策略 

> sharding jdbc 提供负载均衡算法spi  继承MasterSlaveLoadBalanceAlgorithm 注册spi服务即可 

* 负载均衡算法实现  
```java
package com.ming.base.orm;

import org.apache.shardingsphere.spi.masterslave.MasterSlaveLoadBalanceAlgorithm;

import java.util.List;
import java.util.Properties;

public class MingSlaveLoadBalanceAlgorithm implements MasterSlaveLoadBalanceAlgorithm {
    /**
     * Get data source.
     *
     * @param name                 master-slave logic data source name
     * @param masterDataSourceName name of master data sources
     * @param slaveDataSourceNames names of slave data sources
     * @return name of selected data source
     */
    @Override
    public String getDataSource(String name, String masterDataSourceName, List<String> slaveDataSourceNames) {
        //此处主要是演示 自定义负载均衡实现 不是演示负载均衡具体的策略 这里就简单点
        return slaveDataSourceNames.get(0);
    }

    /**
     * Get algorithm type.
     *
     * @return type
     */
    @Override
    public String getType() {
        return "MING";
    }

    /**
     * Get properties.
     *
     * @return properties of algorithm
     */
    @Override
    public Properties getProperties() {
        return new Properties();
    }

    /**
     * Set properties.
     *
     * @param properties properties of algorithm
     */
    @Override
    public void setProperties(Properties properties) {

    }
}

```
* 注册spi服务  
在 resources/META-INF/services 目录下创建名称为 org.apache.shardingsphere.spi.masterslave.MasterSlaveLoadBalanceAlgorithm文本文件 内容为
```text
com.ming.base.orm.MingSlaveLoadBalanceAlgorithm
```

* 使用自定义实现的负载均衡算法 
```java
package com.ming.base.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.strategy.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * sharding jdbc 配置
 *
 * @author ming
 * @date 2020-05-11 14:56
 */
@Configuration
public class ShardingJDBCConfig {

    /**
     * 基于 hikari 连接池 配置
     * 一主多从  轮询从节点
     *
     * @author ming
     * @date 2020-05-11 15:08
     */
    @Bean
    @Primary
    public DataSource shardingJDBC() throws SQLException {
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>();

        // 配置主库
        HikariDataSource masterDataSource = new HikariDataSource();
        masterDataSource.setDriverClassName("org.postgresql.Driver");
        masterDataSource.setJdbcUrl("jdbc:postgresql://tx.xujiuming.com:15432/ming_master");
        masterDataSource.setUsername("用户名");
        masterDataSource.setPassword("密码");
        dataSourceMap.put("ming_master", masterDataSource);

        // 配置第一个从库
        HikariDataSource slaveDataSource1 = new HikariDataSource();
        slaveDataSource1.setDriverClassName("org.postgresql.Driver");
        slaveDataSource1.setJdbcUrl("jdbc:postgresql://tx.xujiuming.com:15432/ming_slave0");
        slaveDataSource1.setUsername("用户名");
        slaveDataSource1.setPassword("密码");
        dataSourceMap.put("ming_slave0", slaveDataSource1);

        // 配置第二个从库
        HikariDataSource slaveDataSource2 = new HikariDataSource();
        slaveDataSource2.setDriverClassName("org.postgresql.Driver");
        slaveDataSource2.setJdbcUrl("jdbc:postgresql://tx.xujiuming.com:15432/ming_slave1");
        slaveDataSource2.setUsername("用户名");
        slaveDataSource2.setPassword("密码");
        dataSourceMap.put("ming_slave1", slaveDataSource2);

        // 配置读写分离规则 使用自定义负载均衡策略  
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("ming_master_slave"
                , "ming_master"
                , Arrays.asList("ming_slave0", "ming_slave1")
                , new LoadBalanceStrategyConfiguration(new MingSlaveLoadBalanceAlgorithm().getType()));
        // 获取数据源对象
        return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig, new Properties());
    }

}
```
#### 总结 
sharding jdbc  实现读写分离 不影响原本业务功能  直接在jdbc层进行处理 
单独使用 除了不太满足多写场景  
配合sharding jdbc 本身的数据分片等功能 可以轻松做到 读写分离+数据分片    