---
title: java注解笔记
categories: 笔记
tags:
  - java基础
abbrlink: 478f654a
date: 2017-11-11 00:00:00
---

##java定义的4个标准元数据注解:
1. @Target
2. @Retention
3. @Documented
4. @Inherited

|名称 | 作用 | 取值 | 备注|
|---------|:------|:-------|------|
|@target | 用来说明annotation修饰对象范围 描述注解使用范围| 1.CONSTRUCTOR:用于描述构造器 2.FIELD:用于描述域 3.LOCAL_VARIABLE:用于描述局部变量 4.METHOD:用于描述方法 5.PACKAGE:用于描述包 6.PARAMETER:用于描述参数 7.TYPE:用于描述类、接口(包括注解类型) 或enum声明|在自定义注解时候 定义注解使用范围|
|@Retention|定义annotation保留时间长短 也就是生命周期|1.SOURCE:在源文件中有效（即源文件保留）  2.CLASS:在class文件中有效（即class保留） 3.RUNTIME:在运行时有效（即运行时保留）|用来定义注解生命周期|
|@Documented|标记注解| 描述这个类型的注解是作为被标注的程序成员变量的公共api|就是一个标记 例如javadoc的注解 可用被javadoc这样的工具进行操作生成文档|
|@Inherited|标记注解 描述某个被标注的类型是被继承的 如果使用此注解标记一个类 啦么这个注解将用于该类的子类|当@Inherited annotation类型标注的annotation的Retention是RetentionPolicy.RUNTIME，则反射API增强了这种继承性。如果我们使用java.lang.reflect去查询一个@Inherited annotation类型的annotation时，反射代码检查将展开工作：检查class和其父类，直到发现指定的annotation类型被发现，或者到达类继承结构的顶层。|

## 注解定义格式
定义注解格式 public @interface 注解名{定义内容}
注解参数可支持类型：
1. 所有基本数据类型（int,float,boolean,byte,double,char,long,short)
2. String类型
3. Class类型
4. enum类型
5. Annotation类型
6. 以上所有类型的数组

annotation类型的参数设定要求：    
第一,只能用public或默认(default)这两个访问权修饰.例如,String value();这里把方法设为defaul默认类型；　 　
第二,参数成员只能用基本类型byte,short,char,int,long,float,double,boolean八种基本数据类型和 String,Enum,Class,annotations等数据类型,以及这一些类型的数组.例如,String value();这里的参数成员就为String;　　
第三,如果只有一个参数成员,最好把参数名称设为"value",后加小括号.例:下面的例子FruitName注解就只有一个参数成员。


> 注解元素必须有默认值 要么默认指定 要么注解时候指定。非基本类型的注解元素不能为null


> 注解如果不处理 几乎和注释一样 所以需要注解处理器
主要是通过反射来构建自定义注解处理器
