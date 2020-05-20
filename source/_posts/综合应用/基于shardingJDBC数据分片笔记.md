---
title: 基于shardingJDBC数据分片笔记
comments: true
categories: 笔记
tags:
  - shardingJDBC
  - 数据分片
abbrlink: cc7a4fb9
date: 2020-05-20 03:37:01
---
#### 前言
数据分片 。。。 降低单个数据库节点压力、和处理大量数据的方案  
其中 拆分方案 分为  单独的垂直分片、水平分片  和垂直、水平混合分片  
数据分片之后 + 读写分离 基本上解决大多数数据的容量、性能问题  对于现在的项目基本上都是可以支撑起来 
最近在看sharding share 的jdbc组件  也能很快速的实现 数据分片  

#### 垂直分片(单独分库) 
* 优点: 缓解一定情况下的数据量和单个数据库节点的压力  
* 缺点: 没有解决单表数据量大的问题 、无法跨库连表查询

垂直分片 可以说是解决同库多张表的数据量过大的优化 最简单快速的方案    
因为大多数情况下 只需要多个数据源定好访问规则就行  不涉及分表    
但是有上限 例如单表数据超过极限 例如mysql 百万级别数据  postgres 千万级别数据 这个时候 sql执行就会相对来说比较慢  
##### shardingJDBC 垂直分片示例
分片方案:
staff表 -> ds0 
api_consuming_log -> ds1 
其他表 分配到ds0 上 

> 为了加深印象 采用java配置模式  

```java
package com.ming.base.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * sharding jdbc 垂直分片配置
 * ds0 -> staff
 * ds1 -> api_consuming_log
 * <p>
 * 定制 表的rule config 来路由规则 达到垂直分片
 * staff -> ds0.staff
 * api_consuming_log -> ds1.api_consuming_log
 * <p>
 * 参考文档: https://shardingsphere.apache.org/document/current/cn/manual/shardingsphere-jdbc/configuration/config-java/
 * </p>
 *
 * @author ming
 * @date 2020-05-20 10:34
 */
@Configuration
public class ShardingJDBCVerticalFragmentationConfig {
    /**
     * 构建 sharding 数据源
     *
     * @author ming
     * @date 2020-05-20 11:02
     */
    @Bean
    @Primary
    DataSource getShardingDataSource() throws SQLException {
        //配置路由规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        //配置staff 规则
        shardingRuleConfig.getTableRuleConfigs().add(getStaffTableRuleConfiguration());
        //配置 log 规则
        shardingRuleConfig.getTableRuleConfigs().add(getApiConsumingLogTableRuleConfiguration());
        //绑定规则的表列表
        shardingRuleConfig.getBindingTableGroups().add("staff,api_consuming_log");
        //广播表
        //shardingRuleConfig.getBroadcastTables().add("t_config");
        //设置默认的数据源 、分库策略 和分表策略  只在默认库中操作  不拆分库和表
        shardingRuleConfig.setDefaultDataSourceName("ds0");
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new Properties());
    }


    /**
     * 获取通过雪花算法生成id的生成器配置
     *
     * @author ming
     * @date 2020-05-20 11:02
     */
    private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
        KeyGeneratorConfiguration result = new KeyGeneratorConfiguration("SNOWFLAKE", "id");
        return result;
    }

    /**
     * 获取staff 表的路由规则配置
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    TableRuleConfiguration getStaffTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("staff", "ds0.staff");
        result.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
        return result;
    }

    /**
     * 获取log表的路由规则
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    TableRuleConfiguration getApiConsumingLogTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("api_consuming_log", "ds1.api_consuming_log");
        return result;
    }

    /**
     * 创建数据源map
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        HikariDataSource ds0 = new HikariDataSource();
        ds0.setDriverClassName("org.postgresql.Driver");
        ds0.setJdbcUrl("jdbc:postgresql://数据库地址/ds0");
        ds0.setUsername("postgres");
        ds0.setPassword("密码");
        result.put("ds0", ds0);

        HikariDataSource ds1 = new HikariDataSource();
        ds1.setDriverClassName("org.postgresql.Driver");
        ds1.setJdbcUrl("jdbc:postgresql://数据库地址/ds1");
        ds1.setUsername("postgres");
        ds1.setPassword("密码");
        result.put("ds1", ds1);
        return result;
    }
}
```

#### 水平分片(分库+分表) 
优点: 解决单表数据量问题 提升部分功能性能
缺点: 增加多个数据库 需要冗余副本 耗费资源、实际存储表较为分散   、事务难以控制 

制定好分片规则 理论上可以大规模扩展  

##### shardingJDBC 水平分片示例

分片方案:
staff 均匀分布在ds0 ds1 中的  staff0 staff1 库
api_consuming_log 分布在ds0 的api_consuming_log0   ds1的api_consuming_log1,api_consuming_log2,api_consuming_log3
其他表 分配到ds0 上 




```java
package com.ming.base.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * sharding jdbc 水平分片配置
 * staff 均匀分布在ds0 ds1 中的  staff0 staff1 库
 * api_consuming_log 分布在ds0 的api_consuming_log0   ds1的api_consuming_log1,api_consuming_log2,api_consuming_log3
 *
 * @author ming
 * @date 2020-05-20 10:34
 */
@Configuration
public class ShardingJDBCLevelFragmentationConfig {
    /**
     * 构建 sharding 数据源
     *
     * @author ming
     * @date 2020-05-20 11:02
     */
    @Bean
    @Primary
    DataSource getShardingDataSource() throws SQLException {
        //配置路由规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        //配置staff 规则
        shardingRuleConfig.getTableRuleConfigs().add(getStaffTableRuleConfiguration());
        //配置 log 规则
        shardingRuleConfig.getTableRuleConfigs().add(getApiConsumingLogTableRuleConfiguration());
        //绑定规则的表列表
        shardingRuleConfig.getBindingTableGroups().add("staff,api_consuming_log");
        //广播表
        //shardingRuleConfig.getBroadcastTables().add("t_config");
        //设置默认的数据源 、分库策略 和分表策略  只在默认库中操作  不拆分库和表
        shardingRuleConfig.setDefaultDataSourceName("ds0");
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new Properties());
    }


    /**
     * 获取通过雪花算法生成id的生成器配置
     *
     * @author ming
     * @date 2020-05-20 11:02
     */
    private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
        KeyGeneratorConfiguration result = new KeyGeneratorConfiguration("SNOWFLAKE", "id");
        return result;
    }

    /**
     * 获取staff 表的路由规则配置
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    TableRuleConfiguration getStaffTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("staff", "ds${0..1}.staff${0..1}");
        result.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
        return result;
    }

    /**
     * 获取log表的路由规则
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    TableRuleConfiguration getApiConsumingLogTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("api_consuming_log", "ds0.api_consuming_log0,ds1.api_consuming_log${1..3}");
        return result;
    }

    /**
     * 创建数据源map
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        HikariDataSource ds0 = new HikariDataSource();
        ds0.setDriverClassName("org.postgresql.Driver");
        ds0.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds0");
        ds0.setUsername("postgres");
        ds0.setPassword("mima");
        result.put("ds0", ds0);

        HikariDataSource ds1 = new HikariDataSource();
        ds1.setDriverClassName("org.postgresql.Driver");
        ds1.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds1");
        ds1.setUsername("postgres");
        ds1.setPassword("mima");
        result.put("ds1", ds1);
        return result;
    }

}

```
> 行表达式规则文档: https://shardingsphere.apache.org/document/current/cn/features/sharding/other-features/inline-expression/

#### 数据分片+读写分离
增加一个 masterSlave 配置即可  
```java
package com.ming.base.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * sharding jdbc 水平分片配置
 * staff 均匀分布在ds0 ds1 中的  staff0 staff1 库
 * api_consuming_log 分布在ds0 的api_consuming_log0   ds1的api_consuming_log1,api_consuming_log2,api_consuming_log3
 * <p>
 * ds0:master0为主节点  master0_slave0 master0_slave1 为从节点
 * ds1:master1为主节点  master1_slave0 master1_slave1 为从节点
 *
 * @author ming
 * @date 2020-05-20 10:34
 */
@Configuration
public class ShardingJDBCLevelFragmentationConfig {
    /**
     * 构建 sharding 数据源
     *
     * @author ming
     * @date 2020-05-20 11:02
     */
    @Bean
    @Primary
    DataSource getShardingDataSource() throws SQLException {
        //配置路由规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        //配置staff 规则
        shardingRuleConfig.getTableRuleConfigs().add(getStaffTableRuleConfiguration());
        //配置 log 规则
        shardingRuleConfig.getTableRuleConfigs().add(getApiConsumingLogTableRuleConfiguration());
        //绑定规则的表列表
        shardingRuleConfig.getBindingTableGroups().add("staff,api_consuming_log");
        //广播表
        //shardingRuleConfig.getBroadcastTables().add("t_config");
        //设置默认的数据源 、分库策略 和分表策略  只在默认库中操作  不拆分库和表
        shardingRuleConfig.setDefaultDataSourceName("ds0");
        shardingRuleConfig.setMasterSlaveRuleConfigs(getMasterSlaveRuleConfiguration());
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new Properties());
    }

    /**
     * 获取通过雪花算法生成id的生成器配置
     *
     * @author ming
     * @date 2020-05-20 11:02
     */
    private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
        KeyGeneratorConfiguration result = new KeyGeneratorConfiguration("SNOWFLAKE", "id");
        return result;
    }

    /**
     * 获取staff 表的路由规则配置
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    TableRuleConfiguration getStaffTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("staff", "ds${0..1}.staff${0..1}");
        result.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
        return result;
    }

    /**
     * 获取log表的路由规则
     *
     * @author ming
     * @date 2020-05-20 11:03
     */
    TableRuleConfiguration getApiConsumingLogTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("api_consuming_log", "ds0.api_consuming_log0,ds1.api_consuming_log${1..3}");
        return result;
    }


    Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = Maps.newHashMap();
        // 第0套 主从数据源 ：
        HikariDataSource masterDs0 = new HikariDataSource();
        masterDs0.setDriverClassName("org.postgresql.Driver");
        masterDs0.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds0");
        masterDs0.setUsername("postgres");
        masterDs0.setPassword("mima");
        result.put("master0", masterDs0);

        HikariDataSource masterDs0Slave0 = new HikariDataSource();
        masterDs0Slave0.setDriverClassName("org.postgresql.Driver");
        masterDs0Slave0.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds0");
        masterDs0Slave0.setUsername("postgres");
        masterDs0Slave0.setPassword("mima");
        result.put("master0_slave0", masterDs0Slave0);


        HikariDataSource masterDs0Slave1 = new HikariDataSource();
        masterDs0Slave1.setDriverClassName("org.postgresql.Driver");
        masterDs0Slave1.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds0");
        masterDs0Slave1.setUsername("postgres");
        masterDs0Slave1.setPassword("mima");
        result.put("master0_slave1", masterDs0Slave1);

        // 第1套 主从数据源 
        HikariDataSource masterDs1 = new HikariDataSource();
        masterDs1.setDriverClassName("org.postgresql.Driver");
        masterDs1.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds1");
        masterDs1.setUsername("postgres");
        masterDs1.setPassword("mima");
        result.put("master1", masterDs1);

        HikariDataSource masterDs1Slave0 = new HikariDataSource();
        masterDs1Slave0.setDriverClassName("org.postgresql.Driver");
        masterDs1Slave0.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds1");
        masterDs1Slave0.setUsername("postgres");
        masterDs1Slave0.setPassword("mima");
        result.put("master1_slave0", masterDs1Slave0);

        HikariDataSource masterDs1Slave1 = new HikariDataSource();
        masterDs1Slave1.setDriverClassName("org.postgresql.Driver");
        masterDs1Slave1.setJdbcUrl("jdbc:postgresql://10.10.10.42:5432/ds1");
        masterDs1Slave1.setUsername("postgres");
        masterDs1Slave1.setPassword("mima");
        result.put("master1_slave1", masterDs1Slave1);

        return result;
    }


    /**
     * 配置分片主从配置
     *
     * @author ming
     * @date 2020-05-20 15:59
     */
    List<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfiguration() {
        //配置 ds0 数据源   master0为主节点  master0_slave0 master0_slave1 为从节点  
        MasterSlaveRuleConfiguration ds0 = new MasterSlaveRuleConfiguration("ds0", "master0", Lists.newArrayList("master0_slave0", "master0_slave1"));
        
        //配置 ds1 数据源   master1为主节点  master1_slave0 master1_slave1 为从节点
        MasterSlaveRuleConfiguration ds1 = new MasterSlaveRuleConfiguration("ds1", "master1", Lists.newArrayList("master1_slave0", "master1_slave1"));
        return Lists.newArrayList(ds0, ds1);
    }

}

```
 

#### 总结
一般项目 遇到db的瓶颈    
优化的方向肯定是    数据分区  --->   读写分离 --->  分库 ---> 分库分表  
一般来说 初期做个数据分区够用   不够了 就做读写分离  当数据量达到一定的程度 再去采用分库分表     
因为使用分库分表涉及到很多复杂的问题 没有必要的话 还是不要轻易的使用  
shardingJDBC 除了不解决db本身的数据分区 其他的 读写分离、分库分表 都有比较良好的支持     
最主要的是 通过jdbc来支持的  不嵌入业务代码  也不需要部署中间件  只需要部署db即可   
非常的舒服      