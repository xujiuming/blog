---
title: guava-basic笔记
categories: 笔记
tags:
  - guava
  - tools
abbrlink: d9ddd520
date: 2017-11-11 00:00:00
---


##guava basic 基本工具
#### 避免null optional  
如果是jdk1.8以上直接使用optional  1.8以下需要使用guava basic工具包
#### 前置检查 Preconditions
|方法声明（不包括额外参数）|描述|检查失败时抛出的异常|
|----|-----|------|
|checkArgument(boolean)|检查boolean是否为true，用来检查传递给方法的参数。|IllegalArgumentException
|checkNotNull(T)|检查value是否为null，该方法直接返回value，因此可以内嵌使用checkNotNull。|NullPointerException
|checkState(boolean)|用来检查对象的某些状态。|IllegalStateException
|checkElementIndex(int index, int size)|检查index作为索引值对某个列表、字符串或数组是否有效。index>=0 && index<size *|IndexOutOfBoundsException
|checkPositionIndex(int index, int size)|检查index作为位置值对某个列表、字符串或数组是否有效。index>=0 && index<=size *|IndexOutOfBoundsException
|checkPositionIndexes(int start, int end, int size)|检查[start, end]表示的位置范围对某个列表、字符串或数组是否有效*|IndexOutOfBoundsException
#### object方法 Objects
jdk1.7 后提供了相应的方法 可以不用guava
提供比较链 ComparisonChain 
#### 排序 Ordering
排序器

|方法|描述| 
|----|-----|
|natural()|对可排序类型做自然排序，如数字按大小，日期按先后排序
|usingToString()|按对象的字符串形式做字典排序[lexicographical ordering]
|from(Comparator)|把给定的Comparator转化为排序器

链式调用

|方法|描述|
|----|----|
reverse()|获取语义相反的排序器
nullsFirst()|使用当前排序器，但额外把null值排到最前面。
nullsLast()|使用当前排序器，但额外把null值排到最后面。
compound(Comparator)|合成另一个比较器，以处理当前排序器中的相等情况。
lexicographical()|基于处理类型T的排序器，返回该类型的可迭代对象Iterable<T>的排序器。
onResultOf(Function)|对集合中元素调用Function，再按返回值用当前排序器排序。

排序器

|方法|描述|
|----|----|
greatestOf(Iterable iterable, int k)|获取可迭代对象中最大的k个元素。|leastOf
isOrdered(Iterable)|判断可迭代对象是否已按排序器排序：允许有排序值相等的元素。|isStrictlyOrdered
sortedCopy(Iterable)|判断可迭代对象是否已严格按排序器排序：不允许排序值相等的元素。|immutableSortedCopy
min(E, E)|返回两个参数中最小的那个。如果相等，则返回第一个参数。|max(E, E)
min(E, E, E, E...)|返回多个参数中最小的那个。如果有超过一个参数都最小，则返回第一个最小的参数。|max(E, E, E, E...)
min(Iterable)|返回迭代器中最小的元素。如果可迭代对象中没有元素，则抛出NoSuchElementException。|max(Iterable), min(Iterator), max(Iterator)


#### 总结: guava的基本工具类用的多的可能就是 Preconditions 来检查参数了     optional jdk8 已经包含了 比较的方法 也有   或者 jdk8中stream集合提供更加强大的  宝马都有了 还用个毛的guava的自行车
