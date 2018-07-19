---
title: 常用json解析框架对比
comments: true
categories: 实战
tags:
  - json
  - tools
abbrlink: fd97bf37
date: 2018-07-17 14:52:41
---
#### 前言
最近使用基于http作为rpc调用协议的时候 由于选择使用json来传递数据 
不得不做一波常用的json解析框架的性能、稳定性、可定制性、使用难度、对复杂对象的支持程度等等方面来对比一下
只针对常用的几种 json解析框架对比 有些小众在某些方面很优秀的不再对比之列 
#### 对比 
|名称|使用方式|可配置性|优点|缺点|备注|
|:---|:-----|:-----|:---|
|fast json|JSON.toJSONString、JSON.parseObject|调用toJSONString、parseObject方法的重载方法去配置|使用简单、简单的转换可以直接使用静态方法使用|对于复杂对象处理非常弱、对于泛型处理非常弱|处理简单的对象与json字符串转换的时候 比较适用|
|jackson|获取objectMapper对象 通过writeValueAsString()、 readValue()方式互相转换|提供DeserializationConfig、SerializationConfig各种配置|功能完善、对各种复杂的情况都能使用、对于泛型支持较为完善|功能太多导致使用复杂、配置复杂、需要new|spring等常用框架中使用的就是jackson 即保证速度也保证适应于各种复杂情况|处理复杂的对象 又不想引入gson 那么直接使用jackson 这个速度和fastjson差不多但是能处理很多复杂的功能|
|gson|获取Gson对象 通过toJson()、fromJson()方法互相转换|提供GsonBuilder 去配置不同处理方式的的gson实例 |功能完善、对泛型支持较为完善 | 速度和jackson和fastjson相比 略慢、使用的时候 要专门引用gson 依赖、需要一个实例对象|用于处理复杂的对象和json字符串相互转换 不过速度比不上jackson  不过也够用了| 
|genson|获取genson对象 通过serialize()、deserialize()来互相转换|提供GensonBuilder 去配置不同的处理方式的 gensonBuilder|对泛型支持较为完善|需要实例对象、速度略慢 |这个用的少 如果要对简单对象转换直接fastjson、复杂的又jackson、gson 这个看不到什么特殊的厉害的地方|
#### 测试用例
##### 基础类
* 简单对象  
一个只有简单属性的 类
```
package com.ming.json;

import java.math.BigDecimal;

/**
 * 普通对象
 *
 * @author ming
 * @date 2018-07-17 16:11:05
 */
public class MyData {
    private Integer id;
    private String name;
    private BigDecimal age;


    @Override
    public String toString() {
        return "MyData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAge() {
        return age;
    }

    public void setAge(BigDecimal age) {
        this.age = age;
    }
}

```
* 复杂对象
一个稍微复杂点的类 包含属性是对象的这种类 
```
package com.ming.json;


/**
 * 内嵌对象的对象
 *
 * @author ming
 * @date 2018-07-17 16:11:15
 */
public class CyclicData {
    private Integer id;
    private CyclicData cyclicData;


    @Override
    public String toString() {
        return "CyclicData{" +
                "id=" + id +
                ", cyclicData=" + cyclicData +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CyclicData getCyclicData() {
        return cyclicData;
    }

    public void setCyclicData(CyclicData cyclicData) {
        this.cyclicData = cyclicData;
    }
}

```
* DataUtils   
构建统一的测试数据的工具类 
```
package com.ming.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据工具类 用来获取数据 提供给各种json框架解析
 *
 * @author ming
 * @date 2018-07-17 15:30:30
 */
public class DataUtils {

    /**
     * 获取String list
     *
     * @param size
     * @author ming
     * @date 2018-07-17 16:08:50
     */
    public static List<String> getStringList(int size) {
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            resultList.add("test" + i);
        }
        return resultList;
    }


    /**
     * 获取String  String map
     *
     * @param size
     * @author ming
     * @date 2018-07-17 16:09:04
     */
    public static Map<String, String> getStringMap(int size) {
        Map<String, String> resultMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            resultMap.put("k" + i, "v" + i);
        }
        return resultMap;
    }

    /**
     * 获取 自定义对象
     *
     * @author ming
     * @date 2018-07-17 16:09:20
     */
    public static MyData getMyData() {
        MyData myData = new MyData();
        myData.setId(1);
        myData.setName("ming");
        myData.setAge(BigDecimal.TEN);
        return myData;
    }

    /**
     * 获取自定义对象 list
     *
     * @param size
     * @author ming
     * @date 2018-07-17 16:09:30
     */
    public static List<MyData> getMyDataList(int size) {
        List<MyData> resultList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            MyData tmp = new MyData();
            tmp.setId(i);
            tmp.setName("ming" + i);
            tmp.setAge(BigDecimal.valueOf(i));
            resultList.add(tmp);
        }
        return resultList;
    }

    /**
     * 获取String  自定义对象 map
     *
     * @param size
     * @author ming
     * @date 2018-07-17 16:09:47
     */
    public static Map<String, MyData> getMyDataMap(int size) {
        Map<String, MyData> resultMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            MyData tmp = new MyData();
            tmp.setId(i);
            tmp.setName("ming" + i);
            tmp.setAge(BigDecimal.valueOf(i));
            resultMap.put("k" + i, tmp);
        }
        return resultMap;
    }

    /**
     * 获取内嵌对象的对象
     *
     * @author ming
     * @date 2018-07-17 16:10:03
     */

    public static CyclicData getCyclicData() {
        CyclicData result = new CyclicData();
        result.setId(1);

        CyclicData tmp = new CyclicData();
        tmp.setId(2);
        result.setCyclicData(tmp);

        return result;
    }

    /**
     * 获取内嵌对象的对象 的list
     *
     * @param size
     * @author ming
     * @date 2018-07-17 16:10:17
     */
    public static List<CyclicData> getCyclicDataList(int size) {
        List<CyclicData> resultList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CyclicData tmp = new CyclicData();
            tmp.setId(i);

            CyclicData t = new CyclicData();
            tmp.setId(i * 1000);
            tmp.setCyclicData(t);
            resultList.add(tmp);
        }
        return resultList;
    }

    /**
     * 获取内嵌对象的对象的map
     *
     * @param size
     * @author ming
     * @date 2018-07-17 16:10:32
     */
    public static Map<String, CyclicData> getCyclicDataMap(int size) {
        Map<String, CyclicData> resultMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            CyclicData tmp = new CyclicData();
            tmp.setId(i);

            CyclicData t = new CyclicData();
            tmp.setId(i * 1000);
            tmp.setCyclicData(t);
            resultMap.put("k" + i, tmp);
        }
        return resultMap;
    }


    /**
     * 获取 kv 都是对象的map
     *
     * @param size
     * @author ming
     * @date 2018-07-17 16:10:49
     */
    public static Map<MyData, CyclicData> getMyDataAndCyclicDataMap(int size) {
        Map<MyData, CyclicData> resultMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            CyclicData tmp = new CyclicData();
            tmp.setId(i);

            CyclicData t = new CyclicData();
            tmp.setId(i * 1000);
            tmp.setCyclicData(t);


            MyData k = new MyData();
            k.setId(i);
            k.setName("ming" + i);
            k.setAge(BigDecimal.valueOf(i));

            resultMap.put(k, tmp);
        }
        return resultMap;
    }


}
```
* 测试用例的统一接口
定义 各种解析框架 的测试用例的格式 
```
package com.ming.json;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public interface TestJson {

    //测试 对象转换 json 字符串 -------------------------------------------------------------------------------------------
    void testStringListObjToString() throws JsonProcessingException;

    void testStringMapObjToString() throws JsonProcessingException;

    void testMyDataToString() throws JsonProcessingException;

    void testMyDataListToString() throws JsonProcessingException;

    void testMyDataMapToString() throws JsonProcessingException;

    void testCyclicDataToString() throws JsonProcessingException;

    void testCyclicDataListToString() throws JsonProcessingException;

    void testCyclicDataMapToString() throws JsonProcessingException;

    void testMyDataAndCyclicDataMapToString() throws JsonProcessingException;


    //json 字符串转换 对象测试方法------------------------------------------------------------------------------------------
    void zTestStringListObjStringToObj() throws IOException;

    void zTestStringMapObjStringToObj() throws IOException;

    void zTestMyDataStringToObj() throws IOException;

    void zTestMyDataListStringToObj() throws IOException;

    void zTestMyDataMapStringToObj() throws IOException;

    void zTestCyclicDataStringToObj() throws IOException;

    void zTestCyclicDataListStringToObj() throws IOException;

    void zTestCyclicDataMapStringToObj() throws IOException;

    void zTestMyDataAndCyclicDataMapStringToObj() throws IOException;
}
```
* 测试用例的统一的抽象类 实现 统一的接口 
对所有的测试用例实现做一个统一的配置抽象类 
提供所有实现测试用例类的公共配置 、前置后置方法 
```
package com.ming.json;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestJsonAbstract extends TestCase implements TestJson {
    //公共变量区
    protected static String stringList = null;
    protected static String stringMap = null;
    protected static String myData = null;
    protected static String myDataList = null;
    protected static String myDataMap = null;
    protected static String cyclicData = null;
    protected static String cyclicDataList = null;
    protected static String cyclicDataMap = null;
    protected static String myDataAndCyclicDataMap = null;
    //获取集合的大小
    protected int size = 10000;
    protected Long now = null;
    private Logger logger = LoggerFactory.getLogger(TestJsonAbstract.class);

    @Before
    public void init() {
        System.out.println("开始计算耗时。。。。。。");
        now = System.currentTimeMillis();
    }

    @After
    public void close() {
        System.out.println("结束耗时,共耗时:" + (System.currentTimeMillis() - now));
        logger.info("结束耗时,共耗时:" + (System.currentTimeMillis() - now));
        now = null;
    }
}

```
##### 测试用例

* fast json测试类
继承抽象类实现 统一的测试方法 
```
package com.ming.json;

import com.alibaba.fastjson.JSON;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Map;

/**
 * 测试fastJson 解析
 * 通过指定 test case 执行顺序 来保证 先调用obj转换String  然后在调用 string 转换obj
 *
 * @author ming
 * @date 2018-07-17 15:23:36
 */
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class TestFastJson extends TestJsonAbstract {


    @Test
    @Override
    public void testStringListObjToString() {
        stringList = JSON.toJSONString(DataUtils.getStringList(size));
        System.out.println(stringList);
    }


    @Test
    @Override
    public void testStringMapObjToString() {
        stringMap = JSON.toJSONString(DataUtils.getStringMap(size));
        System.out.println(stringMap);
    }

    @Test
    @Override
    public void testMyDataToString() {
        myData = JSON.toJSONString(DataUtils.getMyData());
        System.out.println(myData);
    }

    @Test
    @Override
    public void testMyDataListToString() {
        myDataList = JSON.toJSONString(DataUtils.getMyDataList(size));
        System.out.println(myDataList);
    }

    @Test
    @Override
    public void testMyDataMapToString() {
        myDataMap = JSON.toJSONString(DataUtils.getMyDataMap(size));
        System.out.println(myDataMap);
    }

    @Test
    @Override
    public void testCyclicDataToString() {
        cyclicData = JSON.toJSONString(DataUtils.getCyclicData());
        System.out.println(cyclicData);
    }

    @Test
    @Override
    public void testCyclicDataListToString() {
        cyclicDataList = JSON.toJSONString(DataUtils.getCyclicDataList(size));
        System.out.println(cyclicDataList);
    }

    @Test
    @Override
    public void testCyclicDataMapToString() {
        cyclicDataMap = JSON.toJSONString(DataUtils.getCyclicDataMap(size));
        System.out.println(cyclicDataMap);
    }

    @Test
    @Override
    public void testMyDataAndCyclicDataMapToString() {
        myDataAndCyclicDataMap = JSON.toJSONString(DataUtils.getMyDataAndCyclicDataMap(size));
        System.out.println(myDataAndCyclicDataMap);
    }


    @Test
    @Override
    public void zTestStringListObjStringToObj() {
        System.out.println(JSON.parseArray(stringList));
    }


    @Test
    @Override
    public void zTestStringMapObjStringToObj() {
        System.out.println(JSON.parseObject(stringMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataStringToObj() {
        System.out.println(JSON.parseObject(myData, MyData.class));
    }

    @Test
    @Override
    public void zTestMyDataListStringToObj() {
        System.out.println(JSON.parseArray(myDataList, MyData.class));
    }

    @Test
    @Override
    public void zTestMyDataMapStringToObj() {
        System.out.println(JSON.parseObject(myDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestCyclicDataStringToObj() {
        System.out.println(JSON.parseObject(cyclicData, CyclicData.class));
    }

    @Test
    @Override
    public void zTestCyclicDataListStringToObj() {
        System.out.println(JSON.parseArray(cyclicDataList, CyclicData.class));
    }

    @Test
    @Override
    public void zTestCyclicDataMapStringToObj() {
        System.out.println(JSON.parseObject(cyclicDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataAndCyclicDataMapStringToObj() {
        System.out.println(JSON.parseObject(myDataAndCyclicDataMap, Map.class));
    }


}

```
* jackson测试类
使用jackson 实现的统一的测试用例 继承抽象类
```
package com.ming.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 测试解析 jackson
 *
 * @author ming
 * @date 2018-07-17 15:24:12
 */
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class TestJackson extends TestJsonAbstract {
    //获取 jackson 处理json的 mapper   可以通过setConfig 配置这个objectMapper
    private ObjectMapper objectMapper = new ObjectMapper();

    public void init1() {
        //DeserializationConfig,SerializationConfig
        //配置 objectMapper 编解码 配置
        //objectMapper.setConfig()

        //设置序列化和反序列化时候的配置
        //objectMapper.configure();

    }

    @Test
    @Override
    public void testStringListObjToString() throws JsonProcessingException {
        stringList = objectMapper.writeValueAsString(DataUtils.getStringList(size));
        System.out.println(stringList);
    }


    @Test
    @Override
    public void testStringMapObjToString() throws JsonProcessingException {
        stringMap = objectMapper.writeValueAsString(DataUtils.getStringMap(size));
        System.out.println(stringMap);
    }

    @Test
    @Override
    public void testMyDataToString() throws JsonProcessingException {
        myData = objectMapper.writeValueAsString(DataUtils.getMyData());
        System.out.println(myData);
    }

    @Test
    @Override
    public void testMyDataListToString() throws JsonProcessingException {
        myDataList = objectMapper.writeValueAsString(DataUtils.getMyDataList(size));
        System.out.println(myDataList);
    }

    @Test
    @Override
    public void testMyDataMapToString() throws JsonProcessingException {
        myDataMap = objectMapper.writeValueAsString(DataUtils.getMyDataMap(size));
        System.out.println(myDataMap);
    }

    @Test
    @Override
    public void testCyclicDataToString() throws JsonProcessingException {
        cyclicData = objectMapper.writeValueAsString(DataUtils.getCyclicData());
        System.out.println(cyclicData);
    }

    @Test
    @Override
    public void testCyclicDataListToString() throws JsonProcessingException {
        cyclicDataList = objectMapper.writeValueAsString(DataUtils.getCyclicDataList(size));
        System.out.println(cyclicDataList);
    }

    @Test
    @Override
    public void testCyclicDataMapToString() throws JsonProcessingException {
        cyclicDataMap = objectMapper.writeValueAsString(DataUtils.getCyclicDataMap(size));
        System.out.println(cyclicDataMap);
    }

    @Test
    @Override
    public void testMyDataAndCyclicDataMapToString() throws JsonProcessingException {
        myDataAndCyclicDataMap = objectMapper.writeValueAsString(DataUtils.getMyDataAndCyclicDataMap(size));
        System.out.println(myDataAndCyclicDataMap);
    }


    @Test
    @Override
    public void zTestStringListObjStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(stringList, List.class));
    }


    @Test
    @Override
    public void zTestStringMapObjStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(stringMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(myData, MyData.class));
    }

    @Test
    @Override
    public void zTestMyDataListStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(myDataList, List.class));
    }

    @Test
    @Override
    public void zTestMyDataMapStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(myDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestCyclicDataStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(cyclicData, CyclicData.class));
    }

    @Test
    @Override
    public void zTestCyclicDataListStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(cyclicDataList, List.class));
    }

    @Test
    @Override
    public void zTestCyclicDataMapStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(cyclicDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataAndCyclicDataMapStringToObj() throws IOException {
        System.out.println(objectMapper.readValue(myDataAndCyclicDataMap, Map.class));
    }

}

```
* gson测试类
gson实现统一的测试类 继承抽象类 
```
package com.ming.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

/**
 * 测试gson 解析
 *
 * @author ming
 * @date 2018-07-17 15:23:58
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGson extends TestJsonAbstract {

    private Gson gson = new GsonBuilder().create();


    @Test
    @Override
    public void testStringListObjToString() {
        stringList = gson.toJson(DataUtils.getStringList(size));
        System.out.println(stringList);
    }


    @Test
    @Override
    public void testStringMapObjToString() {
        stringMap = gson.toJson(DataUtils.getStringMap(size));
        System.out.println(stringMap);
    }

    @Test
    @Override
    public void testMyDataToString() {
        myData = gson.toJson(DataUtils.getMyData());
        System.out.println(myData);
    }

    @Test
    @Override
    public void testMyDataListToString() {
        myDataList = gson.toJson(DataUtils.getMyDataList(size));
        System.out.println(myDataList);
    }

    @Test
    @Override
    public void testMyDataMapToString() {
        myDataMap = gson.toJson(DataUtils.getMyDataMap(size));
        System.out.println(myDataMap);
    }

    @Test
    @Override
    public void testCyclicDataToString() {
        cyclicData = gson.toJson(DataUtils.getCyclicData());
        System.out.println(cyclicData);
    }

    @Test
    @Override
    public void testCyclicDataListToString() {
        cyclicDataList = gson.toJson(DataUtils.getCyclicDataList(size));
        System.out.println(cyclicDataList);
    }

    @Test
    @Override
    public void testCyclicDataMapToString() {
        cyclicDataMap = gson.toJson(DataUtils.getCyclicDataMap(size));
        System.out.println(cyclicDataMap);
    }

    @Test
    @Override
    public void testMyDataAndCyclicDataMapToString() {
        myDataAndCyclicDataMap = gson.toJson(DataUtils.getMyDataAndCyclicDataMap(size));
        System.out.println(myDataAndCyclicDataMap);
    }


    @Test
    @Override
    public void zTestStringListObjStringToObj() {
        System.out.println(gson.fromJson(stringList, List.class));
    }


    @Test
    @Override
    public void zTestStringMapObjStringToObj() {
        System.out.println(gson.fromJson(stringMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataStringToObj() {
        System.out.println(gson.fromJson(myData, MyData.class));
    }

    @Test
    @Override
    public void zTestMyDataListStringToObj() {
        System.out.println(gson.fromJson(myDataList, List.class));
    }

    @Test
    @Override
    public void zTestMyDataMapStringToObj() {
        System.out.println(gson.fromJson(myDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestCyclicDataStringToObj() {
        System.out.println(gson.fromJson(cyclicData, CyclicData.class));
    }

    @Test
    @Override
    public void zTestCyclicDataListStringToObj() {
        System.out.println(gson.fromJson(cyclicDataList, List.class));
    }

    @Test
    @Override
    public void zTestCyclicDataMapStringToObj() {
        System.out.println(gson.fromJson(cyclicDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataAndCyclicDataMapStringToObj() {
        System.out.println(gson.fromJson(myDataAndCyclicDataMap, Map.class));
    }


}

```
* genson测试类
genson实现的测试类 继承 抽象类 
```
package com.ming.json;

import com.owlike.genson.Genson;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

/**
 * 测试使用genson
 *
 * @author ming
 * @date 2018-07-17 15:24:33
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGenson extends TestJsonAbstract {
    private Genson genson = new Genson();


    @Test
    @Override
    public void testStringListObjToString() {
        stringList = genson.serialize(DataUtils.getStringList(size));
        System.out.println(stringList);
    }


    @Test
    @Override
    public void testStringMapObjToString() {
        stringMap = genson.serialize(DataUtils.getStringMap(size));
        System.out.println(stringMap);
    }

    @Test
    @Override
    public void testMyDataToString() {
        myData = genson.serialize(DataUtils.getMyData());
        System.out.println(myData);
    }

    @Test
    @Override
    public void testMyDataListToString() {
        myDataList = genson.serialize(DataUtils.getMyDataList(size));
        System.out.println(myDataList);
    }

    @Test
    @Override
    public void testMyDataMapToString() {
        myDataMap = genson.serialize(DataUtils.getMyDataMap(size));
        System.out.println(myDataMap);
    }

    @Test
    @Override
    public void testCyclicDataToString() {
        cyclicData = genson.serialize(DataUtils.getCyclicData());
        System.out.println(cyclicData);
    }

    @Test
    @Override
    public void testCyclicDataListToString() {
        cyclicDataList = genson.serialize(DataUtils.getCyclicDataList(size));
        System.out.println(cyclicDataList);
    }

    @Test
    @Override
    public void testCyclicDataMapToString() {
        cyclicDataMap = genson.serialize(DataUtils.getCyclicDataMap(size));
        System.out.println(cyclicDataMap);
    }

    @Test
    @Override
    public void testMyDataAndCyclicDataMapToString() {
        myDataAndCyclicDataMap = genson.serialize(DataUtils.getMyDataAndCyclicDataMap(size));
        System.out.println(myDataAndCyclicDataMap);
    }


    @Test
    @Override
    public void zTestStringListObjStringToObj() {
        System.out.println(genson.deserialize(stringList, List.class));
    }


    @Test
    @Override
    public void zTestStringMapObjStringToObj() {
        System.out.println(genson.deserialize(stringMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataStringToObj() {
        System.out.println(genson.deserialize(myData, MyData.class));
    }

    @Test
    @Override
    public void zTestMyDataListStringToObj() {
        System.out.println(genson.deserialize(myDataList, List.class));
    }

    @Test
    @Override
    public void zTestMyDataMapStringToObj() {
        System.out.println(genson.deserialize(myDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestCyclicDataStringToObj() {
        System.out.println(genson.deserialize(cyclicData, CyclicData.class));
    }

    @Test
    @Override
    public void zTestCyclicDataListStringToObj() {
        System.out.println(genson.deserialize(cyclicDataList, List.class));
    }

    @Test
    @Override
    public void zTestCyclicDataMapStringToObj() {
        System.out.println(genson.deserialize(cyclicDataMap, Map.class));
    }

    @Test
    @Override
    public void zTestMyDataAndCyclicDataMapStringToObj() {
        System.out.println(genson.deserialize(myDataAndCyclicDataMap, Map.class));
    }

}

```
* 所有的测试类实现 统一进行批量测试 
```
package com.ming.json;


import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聚合多个测试用例 在一起测试
 *
 * @author ming
 * @date 2018-07-17 17:31:55
 */
//@RunWith(Suite.class)
//@Suite.SuiteClasses({TestJackson.class, TestGson.class,  TestGenson.class,TestFastJson.class})
public class AllTest {

    public static void main(String[] args) {
        //执行测试用例次数 通过增大执行次数 取平均数 减小误差
        int size = 10000;
        List<Map<String, Object>> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        result.add(get(TestJackson.class, size));
        result.add(get(TestGson.class, size));
        result.add(get(TestGenson.class, size));
        result.add(get(TestFastJson.class, size));
        System.out.println("总耗时:" + (System.currentTimeMillis() - now) + "ms");
        System.out.println("明细信息-------------------------------");
        result.forEach(f -> {
            System.out.println("执行的测试用例" + f.get("class"));
            System.out.println("执行平均耗时" + f.get("avgTime") + "ms");
            System.out.println("每次执行的耗时详情" + f.get("testList"));
            System.out.println("--------------------------------");
        });
    }

    private static Map<String, Object> get(Class<? extends TestCase> tClass, int size) {
        TestSuite testSuite = new TestSuite();
        testSuite.addTestSuite(tClass);
        long now;
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            now = System.currentTimeMillis();
            TestRunner.run(testSuite);
            list.add(System.currentTimeMillis() - now);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("class", tClass);
        map.put("testList", list);
        map.put("avgTime", list.stream().collect(Collectors.averagingLong(a -> a)));
        return map;
    }
}

```

开启 AllTest类中的main方法即进行所有的模块的测试 

其中一次测试结果记录
```
总耗时:3627599ms
明细信息-------------------------------
执行的测试用例class com.ming.json.TestJackson
执行平均耗时84.3811ms
每次执行的耗时详情[439, 215, 184, 183, 176, 198。。。。。]
--------------------------------
执行的测试用例class com.ming.json.TestGson
执行平均耗时94.915ms
每次执行的耗时详情[299, 126, 123, 113, 95, 109, 。。。。]
--------------------------------
执行的测试用例class com.ming.json.TestGenson
执行平均耗时99.3964ms
每次执行的耗时详情[247, 144, 115, 98, 102, 85, 。。。。。]
--------------------------------
执行的测试用例class com.ming.json.TestFastJson
执行平均耗时84.0026ms
每次执行的耗时详情[263, 143, 94, 104, 69, 72, 。。。。。]
--------------------------------

```

#### jackson gson针对泛型的特殊处理 
```
package com.ming.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 测试泛型类型 json转换
 * 引用地址:https://www.jianshu.com/p/ca03c2fe36e3
 *
 * @author ming
 * @date 2018-07-19 14:03:12
 */
public class TestT {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        Map<String, List<Long>> map = Maps.newHashMap();
        map.put("one", Arrays.asList(10001L, 10002L, 10003L, 10004L));
        map.put("two", Arrays.asList(20001L, 20002L, 20003L, 20004L));
        map.put("three", Arrays.asList(30001L, 30002L, 30003L, 30004L));
        map.put("four", Arrays.asList(40001L, 40002L, 40003L, 40004L));

        String json = new Gson().toJson(map);
        System.err.println("=======================错误示范=====================");
        //Gson
        Map<String, List<Long>> mapResult = gson.fromJson(json, Map.class);
        System.out.println("通过Gson转换...");
//      printType(mapResult);
        System.out.println(mapResult);
        //Json
        Map<String, List<Long>> jsonMapResult = mapper.readValue(json, Map.class);
        System.out.println("通过Jackson转换...");
//      printType(jsonMapResult);

        System.out.println(jsonMapResult);
        System.out.println("=======================正确做法=====================");
        //Gson
        Map<String, List<Long>> mapResult1 = gson.fromJson(json, new TypeToken<Map<String, List<Long>>>() {
        }.getType());
        System.out.println("通过Gson转换...");
        printType(mapResult1);
        System.out.println(mapResult1);
        //Json
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<Long>> jsonMapResult1 = mapper.readValue(json, new TypeReference<Map<String, List<Long>>>() {
        });
        System.out.println("通过Jackson转换...");
        printType(jsonMapResult1);

        System.out.println(jsonMapResult1);

    }

    public static void printType(Map<String, List<Long>> map) {
        for (Map.Entry<String, List<Long>> entry : map.entrySet()) {
            System.out.println("key 类型:" + entry.getKey().getClass() + ", value类型:"
                    + entry.getValue().getClass() + ", List中元素类型" + entry.getValue().get(0).getClass());
        }

    }


}

```

#### 总结  
java处理json相关的操作 工具包是很多的 但是用的多的也就上面说的几种    
对于简单并且要求速度的对象和json转换 直接使用fast json即可 不过有时候 fast json 会坑爹   
其他情况使用jackson 完全可以胜任   gson也行 不过 我更加喜欢jackson  毕竟 很多框架就已经引用了jackson  跟着大佬走肯定没错的  
带有泛型的 一定要用jackson或者gson 去使用  否则可能会无法反序列化成想要的类型  特别是fast json 只能处理第一层的泛型 复杂的泛型直接gg



