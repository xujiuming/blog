---
title: javaSPI机制笔记
comments: true
categories: 笔记
tags:
  - SPI
abbrlink: 9c5dfff9
date: 2020-05-12 13:06:24
---
#### 前言 
最近研究 一些框架的时候 发现SPI机制 很多地方会用上  之前么没怎么遇到过  每次都是现场百度 
这次 写片笔记记录一下 方便后续查询

> 参考文档
>https://my.oschina.net/kipeng/blog/1789849
>https://zhuanlan.zhihu.com/p/28909673
>shardingJDBC的MasterSlaveLoadBalanceAlgorithm 
>dubbo的ExtensionLoader

#### spi用法
使用框架或者别人提供的spi接口  
需要如下几个条件 
* 实现spi接口  把spi接口按照自己的业务需求实现  
* spi配置实现类  配置 META-INF/services/spi接口的全限定名 内容为 自己实现spi接口的实现类 

![20200512131413](https://xujiuming.com/ming-static/vscode/e0cebb9aa2987b5b719118412c2a72dc.png)

![20200512131458](https://xujiuming.com/ming-static/vscode/3fe9f3d95a1adb1ebf1f549f7c0c5fb1.png)


#### 自定义spi接口示例
#####  定义spi接口 
```java
package com.ming.base;

/**
 * spi接口
 *
 * @author ming
 * @date 2020-05-12 13:15
 */
public interface MingSPI {
    /**
     * hello
     *
     * @author ming
     * @date 2020-05-12 13:15
     */
    void hello();
}
```

##### 实现spi接口 
* 实现服务接口 第一种
```java
package com.ming.base;

/**
 * 第一种spi接口实现
 *
 * @author ming
 * @date 2020-05-12 13:17
 */
public class OneSPiImpl implements MingSPI {
    @Override
    public void hello() {
        System.out.println("hello one");
    }
}

```
* 实现服务接口 第二种 
```java
package com.ming.base;

/**
 * 第二种实现
 *
 * @author ming
 * @date 2020-05-12 13:17
 */
public class TwoSPIImpl implements MingSPI {
    @Override
    public void hello() {
        System.out.println("hello two");
    }
}
```

##### 配置加载两种实现的spi服务
在META-INF/services目录下创建 MingSPI的配置文件  名字为MingSPI全限定名 : com.ming.base.MingSPI
内容如下:
```text
com.ming.base.OneSPiImpl
com.ming.base.TwoSPIImpl
```

##### 运行演示 
```java
package com.ming;

import com.ming.base.MingSPI;

import java.util.ServiceLoader;

/**
 * 演示
 *
 * @author ming
 * @date 2020-05-12 13:19
 */
public class Start {
    public static void main(String[] args) {
        ServiceLoader<MingSPI> spiServiceLoader = ServiceLoader.load(MingSPI.class);
        for (MingSPI mingSpi : spiServiceLoader) {
            System.out.println("执行spi函数-----------");
            mingSpi.hello();
        }
    }
}
```
结果如下:     
![20200512132035](https://xujiuming.com/ming-static/vscode/af99dc82d3701c2082967cd555b0cad7.png)

#### 总结 
很多框架提供spi接口提供扩展   
例如 spring  dubbo   shardingJDBC 
也有很多地方用到了spi去实现 例如 jdbc     
spi好处就是解耦 并且方便定制化扩展    
麻烦的地方就是要维护实现类 和加载、如何使用等  比较麻烦 