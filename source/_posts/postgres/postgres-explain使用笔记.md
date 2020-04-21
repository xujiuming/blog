---
title: postgres-explain使用笔记
comments: true
categories: 笔记
tags:
  - db
  - postgres
  - sql
abbrlink: 2c157461
date: 2019-12-16 15:10:15
---
#### 前言 
最近发现有几个接口响应缓慢 看了下 主要是之前偷懒写法 导致网络io过大 当数据量上来之后速度变慢
现在把部分业务功能 拆分到不同的sql中  进行处理 

#### 参考文档 
explain浅析: http://mysql.taobao.org/monthly/2018/11/06/      
postgres explain文档(pg11):  https://www.postgresql.org/docs/11/using-explain.html    

#### 命令说明 
指令格式：
```text
EXPLAIN 指令... 要进行分析的sql 
```
常用指令说明:    

|名称|说明|备注|  
|:--|:--|:--|  
|ANALYZE|选项为TRUE 会实际执行SQL，并获得相应的查询计划，默认为FALSE|如果优化一些修改数据的SQL 需要真实的执行但是不能影响现有的数据，可以放在一个事务中，分析完成后可以直接回滚。|
|VERBOSE| 选项为TRUE 会显示查询计划的附加信息，默认为FALSE|附加信息包括查询计划中每个节点（后面具体解释节点的含义）输出的列（Output），表的SCHEMA 信息，函数的SCHEMA 信息，表达式中列所属表的别名，被触发的触发器名称等。|
|COSTS|选项为TRUE 会显示每个计划节点的预估启动代价（找到第一个符合条件的结果的代价）和总代价，以及预估行数和每行宽度，默认为TRUE|-|
|BUFFERS|选项为TRUE 会显示关于缓存的使用信息，默认为FALSE|该参数只能与ANALYZE 参数一起使用。缓冲区信息包括共享块（常规表或者索引块）、本地块（临时表或者索引块）和临时块（排序或者哈希等涉及到的短期存在的数据块）的命中块数，更新块数，挤出块数。|
|TIMING|选项为TRUE 会显示每个计划节点的实际启动时间和总的执行时间，默认为TRUE。|该参数只能与ANALYZE 参数一起使用。因为对于一些系统来说，获取系统时间需要比较大的代价，如果只需要准确的返回行数，而不需要准确的时间，可以把该参数关闭。|
|SUMMARY|选项为TRUE 会在查询计划后面输出总结信息，例如查询计划生成的时间和查询计划执行的时间。当ANALYZE 选项打开时，它默认为TRUE。|-|
|FORMAT| 指定输出格式，默认为TEXT。|各个格式输出的内容都是相同的，其中XML 、 JSON 、 YAML 更有利于我们通过程序解析SQL 语句的查询计划，为了更有利于阅读，我们下文的例子都是使用TEXT 格式的输出结果。|


#### 结果说明

##### 节点类型 

* 控制节点（Control Node)
* 扫描节点（ScanNode)
* 物化节点（Materialization Node)
* 连接节点（Join Node)

##### 扫描节点(ScanNode)
|名称|作用|备注|
|:---|:---|:--|
|Seq Scan|顺序扫描|-|
|Index Scan|基于索引扫描，但不只是返回索引列的值|-|
|IndexOnly Scan|基于索引扫描，并且只返回索引列的值，简称为覆盖索引|-|
|BitmapIndex Scan|利用Bitmap 结构扫描|-|
|BitmapHeap Scan|把BitmapIndex Scan 返回的Bitmap 结构转换为元组结构|-|
|Tid Scan|用于扫描一个元组TID 数组|-|
|Subquery Scan|扫描一个子查询|-|
|Function Scan|处理含有函数的扫描|-|
|TableFunc Scan|处理tablefunc 相关的扫描|-|
|Values Scan|用于扫描Values 链表的扫描|-|
|Cte Scan|用于扫描WITH 字句的结果集|-|
|NamedTuplestore Scan|用于某些命名的结果集的扫描|-|
|WorkTable Scan|用于扫描Recursive Union 的中间数据|-|
|Foreign Scan|用于外键扫描|-|
|Custom Scan|用于用户自定义的扫描|-|

#### 总结
一般来说 用explain 看看是那些需要很大的cost的节点 进行优化即可    
绝大多数情况下 只需要吧 Seq Scan 的节点 按需建立索引 优化成 Index Scan即可   
具体情况 具体的处理 不过基本上建立好索引 做好分表分库 常规项目没啥毛病    
当然 也可以根据分析结果 对一些条件、之类的做优化 具体的还是要根据具体的场景进行定制化    


