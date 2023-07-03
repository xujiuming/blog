---
title: guava-cache笔记
categories: 笔记
tags:
  - guava
  - tools
abbrlink: 1be97d35
date: 2017-11-11 00:00:00
---

##　guava cache工具
#### 示例
```
   /**
      * 缓存示例
      *
      * @author ming
      * @date 2017/8/7
      */
     @Test
     public void helloWorldTest() throws ExecutionException {
         LoadingCache<Long, String> strCache = CacheBuilder.newBuilder().maximumSize(1000)
                 .expireAfterWrite(10, TimeUnit.MINUTES)
                 .build(new CacheLoader<Long, String>() {
                     //有这个键就从缓存中去 没有就根据load方法从新获取
                     //如果load没有显示抛出异常 可以用getUnchecked查找缓存 如果显示抛出 就不能使用getUnchecked
                     @Override
                     public String load(Long o) throws Exception {
                         return "缓存:" + o;
                     }
 
                     //批量加载
                     @Override
                     public Map<Long, String> loadAll(Iterable<? extends Long> keys) throws Exception {
                         Map<Long,String> tempMap = Maps.newConcurrentMap();
                         keys.forEach(key->{
                             tempMap.put(key,"缓存:"+key);
                         });
                         return tempMap;
                     }
                     //重新加载
                     @Override
                     public ListenableFuture<String> reload(Long key, String oldValue) throws Exception {
                         return super.reload(key, oldValue);
                     }
                 });
         System.out.println(strCache.get(1L));
         System.out.println(strCache.get(1L));
         System.out.println(strCache.get(2L));
     }
```
####适合的场景
* 通过消耗内存提高速度
* 预料到某些数据会被频繁查询
* 缓存数量不会超过内存 
####需要注意的点
* 如果显示抛出异常 不可使用getUnchecked();
* 可以通过重写load、loadAll、reload方法来进行单个加载获取、组合加载获取、重新加载
* getAll 默认是通过load来加载没有缓存的信息 除非重写loadAll  
####Callable加载
可以通过不同的回调函数 来缓存从不同数据源来的数据 不局限于load方法来缓存数据
```
  /** 回调方式 执行获取缓存 方便实现"如果有缓存则返回；否则运算、缓存、然后返回"
       * 可以在同一个cache对象中 通过不同方法获取 源数据
      * @author ming
      * @date 2017/8/7
      */
     @Test
     public void callableTest() throws ExecutionException {
         //创建缓存对象 不重写cacheLoader 利用callable来从源数据获取缓存 不管有没有重写 callable优先
         Cache<Long,String> cache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<Long, String>() {
             //使用带回调方式获取缓存 优先执行回调方法获取的缓存
             @Override
             public String load(Long key) throws Exception {
                 return "缓存:"+key;
             }
         });
 
         //创建 通过回调获取缓存
         System.out.println(cache.get(1L, new Callable<String>() {
             public String call() throws Exception{
                 return "回调缓存:"+1L;
             }
         }));
 
         System.out.println(cache.get(2L, new Callable<String>() {
             @Override
             public String call() throws Exception {
                 return "回调缓存2："+2;
             }
         }));
     }
```
####缓存回收
* 基于容量回收(size-based eviction)
如果只是不超过固定值 直接使用maximumSize()构建  如果要通过不同的权重来计算实现Weigher 

* 定时回收(timed eviction)
    expireAfterAccess(long, TimeUnit)：缓存项在给定时间内没有被读/写访问，则回收。请注意这种缓存的回收顺序和基于大小回收一样。
    expireAfterWrite(long, TimeUnit)：缓存项在给定时间内没有被写访问（创建或覆盖），则回收。如果认为缓存数据总是在固定时候后变得陈旧不可用，这种回收方式是可取的。
* 基于引用(Reference-based eviction)
    CacheBuilder.weakKeys()：使用弱引用存储键。当键没有其它（强或软）引用时，缓存项可以被垃圾回收。因为垃圾回收仅依赖恒等式（==），使用弱引用键的缓存用==而不是equals比较键。
    CacheBuilder.weakValues()：使用弱引用存储值。当值没有其它（强或软）引用时，缓存项可以被垃圾回收。因为垃圾回收仅依赖恒等式（==），使用弱引用值的缓存用==而不是equals比较值。
    CacheBuilder.softValues()：使用软引用存储值。软引用只有在响应内存需要时，才按照全局最近最少使用的顺序回收。考虑到使用软引用的性能影响，我们通常建议使用更有性能预测性的缓存大小限定（见上文，基于容量回收）。使用软引用值的缓存同样用==而不是equals比较值。
* 显示清除
    个别清除：Cache.invalidate(key)
    批量清除：Cache.invalidateAll(keys)
    清除所有缓存项：Cache.invalidateAll()
####监听器
removalListener默认是同步进行的 可以通过RemovalListeners.asynchronous(RemovalListener,Executor)装饰成 异步
```
    /**清除缓存监听器
        * @author ming
        * @date 2017/8/8
        */
        @Test
        public void listenerTest() throws ExecutionException {
            RemovalListener<Long,String> removalListener = new RemovalListener<Long, String>() {
                //移除动作监听器  同步进行
                @Override
                public void onRemoval(RemovalNotification<Long, String> notification) {
                    System.out.println("\n删除缓存:"+notification);
                    System.out.println(notification.getKey());
                    System.out.println(notification.getValue());
                    //清除原因 返回是什么情况下清除的 例如超过大小、手动清除等
                    System.out.println(notification.getCause());
                    //是否是自动清除
                    System.out.println(notification.wasEvicted());
                }
            };
            //装饰成异步的
            //RemovalListeners.asynchronous(removalListener, new Executor{...});
            Cache<Long,String> cache= CacheBuilder.newBuilder().maximumSize(1000)
                    .removalListener(removalListener).build();
            //添加缓存
            cache.get(1L, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "回调缓存:"+1L;
                }
            });
            //显示删除缓存  被removalListener监听到
            cache.invalidate(1L);
        }
```
####刷新
* 指定刷新 cache.refresh(key)
* 定时刷新CacheBuilder.refreshAfterWrite(到时间、访问过期的数据才会触发)、CacheBuilder.expireAfterWrite(到时间直接刷新数据)
```
 /**
     * 刷新缓存
     *
     * @author ming
     * @date 2017/8/8
     */
    @Test
    public void refreshTest() throws ExecutionException, InterruptedException {
        //定时执行服务
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(10);
        LoadingCache<Integer, String> cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                //定时刷新 到时间后 访问过期数据后进行刷新 优先级比expireAfterWrite高
                //.refreshAfterWrite(100,TimeUnit.MILLISECONDS)
                //定时刷新 到时间直接刷新
                //.expireAfterWrite(100,TimeUnit.MILLISECONDS)
                .build(new CacheLoader<Integer, String>() {
                    @Override
                    public String load(Integer key) throws Exception {
                        return "load缓存+" + key;
                    }

                    @Override
                    public ListenableFuture<String> reload(Integer key, String oldValue) throws Exception {
                        //当key <2的时候 直接刷新 当key>=2 异步刷新
                        if (key < 2) {
                            return Futures.immediateFuture(oldValue);
                        } else {
                            //异步
                            ListenableFutureTask<String> task = ListenableFutureTask.create(new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    return "异步刷新缓存" + System.currentTimeMillis();
                                }
                            });
                            executor.execute(task);
                            return task;
                        }
                    }
                });

        System.out.println(cache.get(1));
        System.out.println(cache.get(3));
        //key<2
        cache.refresh(1);
        System.out.println(cache.get(1));
        //key >= 2 异步刷新
        cache.refresh(3);
        //由于是异步刷新 获取最新数据 主线程休眠1s
        Thread.sleep(1000);
        System.out.println(cache.get(3));
    }
```
####统计缓存信息 
guava缓存提供统计缓存信息方法 CacheBuilder.recordStats()开启缓存  cache.stats()获取缓存
```
  /**
     * 缓存统计信息
     *
     * @author ming
     * @date 2017/8/8
     */
    @Test
    public void statTest() throws ExecutionException {
        LoadingCache<Integer, String> cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                //开启缓存统计功能
                .recordStats()
                .build(new CacheLoader<Integer, String>() {
                    @Override
                    public String load(Integer key) throws Exception {
                        return "缓存:" + key;
                    }
                });
        //查询缓存
        for (int i = 0; i < 99; i++) {
            cache.get(i);
        }
        //查询已经缓存的数据 此时命中率 1%
        cache.get(1);
        CacheStats stats = cache.stats();
        //请求次数
        System.out.println("请求中次数:" + stats.requestCount());
        //命中次数
        System.out.println("命中次数:" + stats.hitCount());
        //命中率
        System.out.println("命中率:" + stats.hitRate());
        //miss数量
        System.out.println("miss数量:" + stats.missCount());
        //miss 比例
        System.out.println("miss率:" + stats.missRate());
        //加载数量
        System.out.println("加载总数量:" + stats.loadCount());
        //加载成功数量
        System.out.println("加载成功数量:" + stats.loadSuccessCount());
        //加载异常数量
        System.out.println("加载异常数量:" + stats.loadExceptionCount());
        //加载异常比例
        System.out.println("加载异常比例" + stats.loadExceptionRate());
        //加载总耗时 ns
        System.out.println("加载总耗时:" + stats.totalLoadTime());
        //加载新值的平均 时间  ns   (ns/1000 = ms)
        System.out.println("加载源数据平均时间:" + stats.averageLoadPenalty());
        //缓存被回收的总数量 显示清除不算
        System.out.println("被自动回收的数量:" + stats.evictionCount());
        // 减 本身-other 小于0  返回0
        //System.out.println(stats.minus(new CacheStats(...)));
        // 加 本身+other
        //System.out.println(stats.plus(new CacheStats(...)));
        System.out.println(stats);
    }
```
#### 总结:guava的cache 适合那些微型项目、或者是一些小地方用用 ;大项目还是得靠 redis或者其他的方式来做;
#### 不过这个guava的cache 真心好用  异步加载、缓存刷新、过期策略 、缓存监控、都相当好用
