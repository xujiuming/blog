---
title: stream集合操作符笔记
comments: true
categories: 笔记
tags:
  - java
  - stream计算
abbrlink: 8486f105
date: 2018-06-26 13:03:34
---
#### 前言
用了这么久的jdk8 的stream集合 
感觉还是很吊的 特别是作统计计算、集合转化之类的操作 

虽然有parallelStream 来进行并行计算 但是 不推荐这么去使用 原因如下
虽然有并行 但是 parallelStream 的并行数量是按照jvm的核心数量去启动的  这个时候就有点蛋疼了 如果真的需要并行 还是建议使用 threadPool去执行 而不是简单的使用 parallelStream
parallelStream 只是把任务并行了 但是该有的竞争状态 还是有 就是说和你使用线程池是差不多的也是要考虑竞争状态的问题

#### 常用操作符列表

|名称|表达式|作用|备注|  
|:---|:---|:--|:---|
|filter|t->boolean|过滤数  据||
|map|t->r|处理数据|为每一个数据作map中的操作|
|flatMap|t->r.stream()|处理数据并且扁平化|为每个数据进行处理 并且会返回一个Stream 处理List<List>> 这种数据的时候可以通过此操作扁平化内部的那个list|
|distinct|-|去重并且返回一个新的stream|进行数据去重的时候使用 必须是有限的stream|
|sorted|-|排序并且返回一个新的stream|进行stream排序使用 但是这个必须要是有限的stream|
|peek|t->void|预览、执行某个不返回的操作  每次返回新的stream 避免消耗stream|做一些void的操作使用|
|limit|-|截断数据 |截断前x个数据 返回新的stream|
|skip|-|跳过数据|跳过前x个数据 返回新的stream|
|forEach|t->void|迭代数据|内部迭代 每啥好说的|
|reduce|(identity,(x,y)->result)、((x,y)->result)|计数器|做一些复杂的综合统计适合|
|collect|(void->t,r->void,r->void)、(collector)|收集结果|将结果收集返回给其他对象|
|min|(o1,o2)->o1 or o2|获取最小的元素|必须是有限的元素|
|max|(o1,o2)->o1 or o2|获取最大的元素|必须是有限的元素|
|count|-|统计数量|统计数量|  
|anyMatch|t->boolean|匹配|只要有一个匹配就返回true|  
|allMatch|t->boolean|匹配|必须所有元素匹配才返回true|  
|noneMatch|t->boolean|匹配|必须所有元素不匹配返回true|  
|findFirst|-|获取第一个元素|获取到第一个元素马上返回|  
|findAny|-|获取返回的元素||  

#### 案例
```
package com.ming;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试 stream 用法
 *
 * @author ming
 * @date 2018-06-26 15:45:08
 */
public class TestStream {


    /**
     * 将List<T1>  转换成 Map<id,T1>
     *
     * @author ming
     * @date 2018-06-26 15:54:54
     */
    @Test
    public void listToMap() {
        List<T1> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new T1("id" + i, "name" + i));
        }
        //转换成 id 为key  T1为value的map
        Map<String, T1> map = list.stream().collect(Collectors.toMap(T1::getId, t -> t));
        System.out.println(map);
        //当出现重复值 按照 (oV, nV) -> nV 来选择新的value
        Map<String, T1> map1 = list.stream().collect(Collectors.toMap(T1::getId, t -> t, (oV, nV) -> nV));
        System.out.println(map1);
    }


    /**
     * 获取List<T2> 中的t1的list的合集
     *
     * @author ming
     * @date 2018-06-26 16:02:35
     */
    @Test
    public void ListToFlatList() {
        List<T2> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<T1> t1List = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                t1List.add(new T1("id" + i, "name" + i));
            }
            list.add(new T2("id" + i, "name" + i, t1List));
        }
        List<T1> resultList = list.stream().flatMap(f -> f.getT1List().stream()).collect(Collectors.toList());
        System.out.println(resultList);
    }

    /**
     * 将 List<T3>中的num进行累加计数
     *
     * @author ming
     * @date 2018-06-26 16:06:24
     */
    @Test
    public void numReduce() {
        List<T3> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new T3("id" + i, i));
        }
        Integer countNum = list.stream().map(T3::getNum).reduce(0, (sum, item) -> sum + item);
        System.out.println(countNum);
        Integer countNum1 = list.stream().map(T3::getNum).reduce(0, Integer::sum);
        System.out.println(countNum1);
    }

}

class T1 {
    private String id;
    private String name;


    public T1() {
    }

    public T1(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class T2 {
    private String id;
    private String name;
    private List<T1> t1List;

    public T2() {
    }

    public T2(String id, String name, List<T1> t1List) {
        this.id = id;
        this.name = name;
        this.t1List = t1List;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T1> getT1List() {
        return t1List;
    }

    public void setT1List(List<T1> t1List) {
        this.t1List = t1List;
    }
}

class T3 {
    private String id;
    private Integer num;


    public T3() {
    }

    public T3(String id, Integer num) {
        this.id = id;
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}
```
#### 总结
jdk8 出了很多实用的功能 这个stream只是其中之一  算是常用的 在大多数 集合转换、数据计算类型的操作中 用stream 操作会节省很多代码 而且看起来容易理解 并且性能还稍高一点





