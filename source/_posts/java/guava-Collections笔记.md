---
title: guava-collections笔记
categories: 笔记
tags:
  - guava
  - tools
abbrlink: 54bd928f
date: 2017-11-11 00:00:00
---


## guava 集合工具
#### 不可变集合
* 当对象被不可信的库调用时，不可变形式是安全的；
* 不可变对象被多个线程调用时，不存在竞态条件问题
* 不可变集合不需要考虑变化，因此可以节省时间和空间。所有不可变的集合都比它们的可变形式有更好的内存利用率（分析和测试细节）；
* 不可变对象因为有固定不变，可以作为常量来安全使用
```
guava 不可变集合不接受null   如果需要可使用null使用jdk中的Collections.unmonifiableXXX（）
```
###### 创建不可变集合
* copyOf ImmutableSet.copyOf(set);
* of ImmutableSet.of("1","2");
* Builder 
```
ImmutableSet.<Color>builder()
            .addAll(WEBSAFE_COLORS)
            .add(new Color(0, 191, 255))
            .build();
```
有序的不可变集合 在构建的时候 就排序
###### asList() 视图
所有不可变集合提供 asList() 视图  方便的获取指定值   sortedSet.asList().get(k)从ImmutableSortedSet中读取第k个最小元素。

###### 可变集合和不可变集合对照表

|可变集合接口|属于JDK还是Guava|不可变版本|
|------|---------|-------|
Collection|JDK|ImmutableCollection
List|JDK|ImmutableList
Set|JDK|ImmutableSet
SortedSet/NavigableSet|JDK|ImmutableSortedSet
Map|JDK|ImmutableMap
SortedMap|JDK|ImmutableSortedMap
Multiset|Guava|ImmutableMultiset
SortedMultiset|Guava|ImmutableSortedMultiset
Multimap|Guava|ImmutableMultimap
ListMultimap|Guava|ImmutableListMultimap
SetMultimap|Guava|ImmutableSetMultimap
BiMap|Guava|ImmutableBiMap
ClassToInstanceMap|Guava|ImmutableClassToInstanceMap
Table|Guava|ImmutableTable

#### guava 新集合
1: Multiset

方法|描述
|----|----|
count(E)|给定元素在Multiset中的计数
elementSet()|Multiset中不重复元素的集合，类型为Set<E>
entrySet()|和Map的entrySet类似，返回Set<Multiset.Entry<E>>，其中包含的Entry支持getElement()和getCount()方法
add(E, int)|增加给定元素在Multiset中的计数
remove(E, int)|减少给定元素在Multiset中的计数
setCount(E, int)|设置给定元素在Multiset中的计数，不可以为负数
size()|返回集合元素的总个数（包括重复的元素）

2:Multimap  单键多值map

方法签名|描述|等价于
|----|----|---|
put(K, V)|添加键到单个值的映射|multimap.get(key).add(value)
putAll(K, Iterable<V>)|依次添加键到多个值的映射|Iterables.addAll(multimap.get(key), values)
remove(K, V)|移除键到值的映射；如果有这样的键值并成功移除，返回true。|multimap.get(key).remove(value)
removeAll(K)|清除键对应的所有值，返回的集合包含所有之前映射到K的值，但修改这个集合就不会影响Multimap了。|multimap.get(key).clear()
replaceValues(K, Iterable<V>)|清除键对应的所有值，并重新把key关联到Iterable中的每个元素。返回的集合包含所有之前映射到K的值。|multimap.get(key).clear(); Iterables.addAll(multimap.get(key), values)

3: BiMap 双向映射map 
* 通过inverse() 反转键值对
* 保证值是唯一的 values()返回是set
* 键映射到已经存在的值 需要使用 forcePut

4: Table => Map<R,Map<C,V>> ==》表格 Table<R,C,V>
* rowMap()：用Map<R, Map<C, V>>表现Table<R, C, V>。同样的， rowKeySet()返回”行”的集合Set<R>。
* row(r) ：用Map<C, V>返回给定”行”的所有列，对这个map进行的写操作也将写入Table中。
* 类似的列访问方法：columnMap()、columnKeySet()、column(c)。（基于列的访问会比基于的行访问稍微低效点）
* cellSet()：用元素类型为Table.Cell<R, C, V>的Set表现Table<R, C, V>。Cell类似于Map.Entry，但它是用行和列两个键区分的。

5: CLassToInstanceMap 它的键是类型，而值是符合键所指类型的对象。
Map<Class<? extends B>, B> ==》 ClassToInstanceMap<K,V>

#### 集合扩展工具类
1: Forwarding(装饰器)
自定义集合的时候可以前后加一些操作　例如日志
2: PeekingIterator(将jdk中的Iterator增强提供一个peek()方法)
注意：Iterators.peekingIterator返回的PeekingIterator不支持在peek()操作之后调用remove()方法。
```
	List<E> result = Lists.newArrayList();
	PeekingIterator<E> iter = Iterators.peekingIterator(source.iterator());
	while (iter.hasNext()) {
	    E current = iter.next();
	    while (iter.hasNext() && iter.peek().equals(current)) {
	        //跳过重复的元素
	        iter.next();
	    }
	    result.add(current);
	}
```
3:AbstractIterator  AbstractSequentialIterator jdk8有更加强大的迭代器
http://ifeve.com/google-guava-newcollectiontypes/
#### 总结: guava工具包最出名应该就是集合的相关操作了 
#### 1：提供不可变集合 来提升性能 但是这个用起来要注意 要确定后续肯定不会在向集合添加更新删除操作了，之前在项目中 调用dubbo服务的时候 用不可变集合 直接gg、
####2：第二大的功能 应该就是他的一些变种集合类了  不过用的比较多的话 也就是table、Multimap  其他有用到 很少
