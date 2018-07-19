---
title: 深入理解jvm阅读笔记-垃圾收集
comments: true
categories: 笔记
tags:
  - java
  - jvm
abbrlink: c4beb856
date: 2018-07-12 12:59:48
---
#### 前言 
看完jvm对于内存的划分调用区分  其实大部分时候 我能需要调整的 也就是堆内存 和方法区  其他几个区域调整不多 
因为堆内存、方法区 涉及到垃圾回收   方法区虽然没有要求虚拟机一定要有垃圾收集  其实也是有虚拟机可以对方法区回收的 例如HotSpot 的-Xnoclassgc参数   
其它区域都是线程私有变量 当线程死亡的时候  随之消亡   
垃圾收集(Garbage Collection ,GC) 
* 那些内存需要回收(怎么判度内存还需不需要使用)
* 什么时候回收(什么时候把那些确定需要回收的内存回收)
* 怎么回收(怎么回收影响最小)
#### 判断对象是否可回收算法
##### 引用计数法(Reference Counting) 
暂时没有java虚拟机采用这个方法   
给对象添加一个引用计数器,当有引用的时候计数器+1, 当引用失效的时候 计数器-1 ,只要计数器=0 那么就是不可能再被引用  
* 好处  
实现简单、高效  
* 坏处  
无法解决对象互相循环引用 
```
package com.ming;


public class Test {
    @org.junit.Test
    public void test() {
        T1 t1 = new T1();
        T2 t2 = new T2();
        //此时 t1 计数器=1  t2 计数器=1 

        t1.setT2(t2);
        t2.setT1(t1);
        //测试 t1 计数器=2  t2 计数器=2


        t1 = null;
        t2 = null;
        //此时 t1 计数器=1  t2 计数器=1 
        //如果是采用引用计数方式计算对象是否可回收 t1 t2 无法回收          
        System.gc();
    }

}

class T1 {
    private T2 t2;

    public T2 getT2() {
        return t2;
    }

    public void setT2(T2 t2) {
        this.t2 = t2;
    }
}

class T2 {
    private T1 t1;

    public T1 getT1() {
        return t1;
    }

    public void setT1(T1 t1) {
        this.t1 = t1;
    }
}
```
##### 可达性分析算法(Reachability Analysis)
主流虚拟机采用的方案     
通过GC Roots 对象为起点   从起点开始向下搜索 搜索的链路称之为 引用链(Reference Chain) 当一个对象和GC Roots没有任何引用链路相连 那么这个对象不可用 可以回收 
![对象是否存活可达性分析算法](http://asset.xujiuming.com/private-asset/jvm_%E5%AF%B9%E8%B1%A1%E5%8F%AF%E8%BE%BE%E6%80%A7%E7%AE%97%E6%B3%95.jpg)

java中可以作为GC Roots 的对象:  
* 虚拟机栈  
* 方法区中类静态属性引用对象  
* 方法区常量的引用对象  
* 本地方法中引用对象   

##### 引用类型
* 强引用
* 软引用
* 弱引用
* 虚引用

|引用类型|gc回收时间|用途|生存时间|备注|
|:-----|:--------|:--|:------|:---|
|




#### 垃圾收集算法


#### HotSpot虚拟机算法实现 
##### 垃圾收集器 种类 


#### 内存分配和回收策略 






#### 方法区回收 



#### 总结 
