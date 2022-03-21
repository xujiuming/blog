---
title: java.util.concurrent包笔记
comments: true
categories: 笔记
tags:
  - java并发
  - JUC
abbrlink: e0e9dfb4
date: 2022-03-21 13:51:11
---
#### 前言 
没啥好说的  日常复习 想起来juc 发现juc还没记录笔记 每次都是看别人的总结 
借着这次机会 对整个juc 做个汇总总结 方便自己速查      

> 学习文章地址: https://segmentfault.com/a/1190000015558984

java.util.concurrent包，按照功能可以大致划分如下：

* juc-locks 锁框架
* juc-atomic 原子类框架
* juc-sync 同步器框架
* juc-collections 集合框架
* juc-executors 执行器框架

#### 模块笔记
全部都是阅读大神的一系列文章 自己记录一下而已   

##### 锁框架
> 不管是什么类型的锁 总的来说就是保证在部分函数执行的时候保证并发安全的操作 必须要合理的释放锁   


* ReentrantLock     
提供设置公平/非公平策略的锁,默认是非公平锁    
>公平策略：在多个线程争用锁的情况下，公平策略倾向于将访问权授予等待时间最长的线程。也就是说，相当于有一个线程等待队列，先进入等待队列的线程后续会先获得锁，这样按照“先来后到”的原则，对于每一个等待线程都是公平的。
>非公平策略：在多个线程争用锁的情况下，能够最终获得锁的线程是随机的（由底层OS调度）。

```java
    @Test
    public void testLock() {
        //创建锁对象  默认为非公平策略    new ReentrantLock(true);方式创建公平策略模式的锁
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            //...
            System.out.println("锁定执行业务");
        } finally {
            lock.unlock();
            System.out.println("释放锁 ");
        }
    }
```

* ReadWriteLock    
也区分公平策略和非公平策略、提供锁重入  和锁降级\(只支持写锁降级为读锁)   
写锁可以获取 Condition对象 Condition是对wait\()和notify\()的增强  
```java
    @Test
    public void readWriteLock() {
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Lock r = readWriteLock.readLock();
        Lock w = readWriteLock.writeLock();
        try {
            //获取读锁
            r.lock();
            //获取写锁
            w.lock();
            Condition condition = w.newCondition();
            condition.await();
            condition.signal();
            //释放写锁 降级为读锁
            w.unlock();
            //...
            System.out.println("锁定执行业务");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            r.unlock();
            System.out.println("释放锁 ");
        }
    }
```

* StampedLock    
邮戳锁 在使用的时候返回一个stamp 解锁的时候必须使用对应的stamp解锁      
所有获取锁的方法，都返回一个邮戳（Stamp），Stamp为0表示获取失败，其余都表示成功；   
所有释放锁的方法，都需要一个邮戳（Stamp），这个Stamp必须是和成功获取锁时得到的Stamp一致   
StampedLock是不可重入的；（如果一个线程已经持有了写锁，再去获取写锁的话就会造成死锁）   

> StampedLock有三种访问模式：
> ①Reading（读模式）：功能和ReentrantReadWriteLock的读锁类似
> ②Writing（写模式）：功能和ReentrantReadWriteLock的写锁类似
> ③Optimistic reading（乐观读模式）：这是一种优化的读模式。

StampedLock可以通过tryConvertToReadLock tryConvertToWriteLock  tryConvertToOptimisticRead 三种锁互相转换      
无论写锁还是读锁，都不支持Conditon等待    
在ReentrantReadWriteLock中，当读锁被使用时，如果有线程尝试获取写锁，该写线程会阻塞。   
但是，在Optimistic reading中，即使读线程获取到了读锁，写线程尝试获取写锁也不会阻塞，这相当于对读模式的优化，但是可能会导致数据不一致的问题。所以，当使用Optimistic reading获取到读锁时，必须对获取结果进行校验。   
StampedLock为了兼容ReentrantLock、ReadWriteLock 提供了 asReadLock\()  asWriteLock\() asReadWriteLock\() 方式转换成对应的xxxView    



stampedLock 乐观锁使用固定模式: 
```text
        //
        long stamp = lock.tryOptimisticRead();  // 非阻塞获取版本信息
        copyVaraibale2ThreadMemory(); e          // 拷贝变量到线程本地堆栈
        if(!lock.validate(stamp)){              // 校验
            long stamp = lock.readLock();       // 获取读锁
            try {
                copyVaraibale2ThreadMemory();   // 拷贝变量到线程本地堆栈
            } finally {
                lock.unlock(stamp);              // 释放悲观锁
            }

        }
        useThreadMemoryVarables();              // 使用线程本地堆栈里面的数据进行操作
```

```java
    @Test
    public void stampedLock() {
        StampedLock lock = new StampedLock();
        //转换为ReadLockView  提供类似 ReentrantLock的操作
        Lock readLock = lock.asReadLock();
        //转换为WriteLockView  提供类似 ReentrantLock的操作
        Lock writeLock = lock.asWriteLock();
        //转换为ReadWriteView 提供类似 ReadWriteLock 的操作
        ReadWriteLock readWriteLock = lock.asReadWriteLock();

        //读写锁互转 获取邮戳
        long readStamp = lock.readLock();
        try {
            System.out.println("读锁");
            while (true) {
                //转换指定邮戳的读锁为写锁 并且返回写的邮戳
                long ws = lock.tryConvertToReadLock(readStamp);
                if (ws != 0) {
                    //成功转换
                    readStamp = ws;
                    //执行写入操作
                    //跳出循环
                    break;
                } else {
                    //转换失败 解除读锁
                    lock.unlockRead(readStamp);
                    // 获取写锁位置
                    readStamp = lock.writeLock();
                }
            }
        } finally {
            lock.unlock(readStamp);
        }
    }
```

##### 原子类框架 
* AtomicInteger   
基础类型的原子封装类 AtomicInteger AtomicBoolean AtomicLong 之类的  
jdk8之前 自旋+cas   
jdk8之后 都是使用unSafe操作直接cas操作     
lazySet+lock 配合使用来减少突破内存屏障的次数 增加性能   
```java
    @Test
    public void atomicInteger() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    atomicInteger.incrementAndGet();
                }
            });
            threads.add(t);
            t.start();
        }

        //等待所有任务执行完毕
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(atomicInteger.get());

        //------------------使用lazySet 配合lock 来减少突破内存屏障的次数 增加性能---------------
        //lazySet内部调用了Unsafe类的putOrderedInt方法，通过该方法对共享变量值的改变，不一定能被其他线程立即看到。也就是说以普通变量的操作方式来写变量。
        //lock()方法获取锁时，和volatile变量的读操作一样，会强制使CPU缓存失效，强制从内存读取变量。
        //unlock()方法释放锁时，和volatile变量的写操作一样，会强制刷新CPU写缓冲区，把缓存数据写到主内存
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            atomicInteger.lazySet(10);
        } finally {
            lock.unlock();
        }
    }
```

* AtomicReference    
对某个对象的操作实现原子化   
以无锁方式访问共享资源的能力   自旋+cas 并且提供记录值的变化次数 或者是否变化的atomic   

```java
    @Test
    public void atomicReference() throws InterruptedException {
        {
            //自旋+cas 实现对象累加
            AtomicReference<Integer> atomicReference = new AtomicReference<>(new Integer(1000));
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Thread t = new Thread(() -> {
                    //自旋  while 和for 循环都可以
                    while (true) {
                        Integer old = atomicReference.get();
                        //cas操作 操作成功结束循环
                        if (atomicReference.compareAndSet(old, old + 1)) {
                            break;
                        }
                    }
                });
                threads.add(t);
                t.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
            System.out.println(atomicReference.get());
        }

        {
            //记录值的变化次数 的atomicReference
            AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(new Integer(1000), 0);

            //获取邮戳
            int[] stampArr = new int[1];
            //获取当前邮戳版本和value
            Integer value = atomicStampedReference.get(stampArr);
            int oldStamp = stampArr[0];

            //cas操作 更新值和邮戳
            atomicStampedReference.compareAndSet(value, value, oldStamp, oldStamp + 1);
        }

        {
            //记录值是否变化过的atomicReference
            AtomicMarkableReference<Integer> atomicMarkableReference = new AtomicMarkableReference<>(new Integer(1000), false);
            boolean[] markArr = new boolean[1];
            Integer value = atomicMarkableReference.get(markArr);
            boolean oldMark = markArr[0];
            atomicMarkableReference.compareAndSet(value, value, oldMark, !oldMark);
        }
    }
```


* AtomicIntegerArray
atomicXXXarray  对基本类型的数组提供原子化操作  
Unsafe直接操作数组的内存  arraBaseOffset
```java
    @Test
    public void atomicIntegerArray() {
        AtomicIntegerArray array = new AtomicIntegerArray(10);
        array.getAndIncrement(0);
        System.out.println(array);
    }
```

* AtomicReferenceFieldUpdater
也是类似的 有AtomicIntegerFieldUpdater AtomicLongFieldUpdater AtomicReferenceFieldUpdater 各种变体  
用法就是对原本不支持原子操作的类中的属性字段 进行包装 在不改变外部调用的情况下  变更为并发安全的操作     
```java
    @Test
    public void atomicXXXFieldUpdate() throws InterruptedException {
        {
            //构建account对象 在不改变调用方式的时候  amount值变化的时候 增加atomic处理    保证并发有效
            class Account {
                /**
                 *AtomicReferenceFieldUpdater只能修改对于它可见的字段，也就是说对于目标类的某个字段field，如果修饰符是private，但是AtomicReferenceFieldUpdater所在的使用类不能看到field，那就会报错；
                 * 目标类的操作字段，必须用volatile修饰；
                 * 目标类的操作字段，不能是static的；
                 * AtomicReferenceFieldUpdater只适用于引用类型的字段；
                 * */
                private volatile int amount;
                //初始化 updater
                private static final AtomicIntegerFieldUpdater<Account> accountUpdater = AtomicIntegerFieldUpdater.newUpdater(Account.class, "amount");

                public Account(int amount) {
                    this.amount = amount;
                }

                public int getAmount() {
                    return amount;
                }

                public void setAmount(int amount) {
                    accountUpdater.set(this, amount);
                }

                public void increment() {
                    accountUpdater.incrementAndGet(this);
                }

                @Override
                public String toString() {
                    return "Account{" +
                            "amount=" + amount +
                            '}';
                }
            }

            Account account = new Account(0);

            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                Thread t = new Thread(() -> {
                    account.increment();
                });
                threads.add(t);
                t.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
            System.out.println(account);
        }

        // -------------------------------------------------------
        {
            class A {
                private int age;

                public int getAge() {
                    return age;
                }

                public void setAge(int age) {
                    this.age = age;
                }

                public A(int age) {
                    this.age = age;
                }
            }
            class B {
                private volatile A a;
                private static final AtomicReferenceFieldUpdater<B, A> updater = AtomicReferenceFieldUpdater.newUpdater(B.class, A.class, "a");

                public B(A a) {
                    this.a = a;
                }

                public A getA() {
                    return a;
                }

                //使用自旋+cas操作实现并发安全
                public void setA(A a) {
                    while (true) {
                        if (updater.compareAndSet(this, this.a, a)) {
                            break;
                        }
                    }

                }
            }
        }
    }

```

* LongAdder   
利用将数字分组存储 来减少并发热点 提高效率 典型用空间换时间做法    
sum求和，这个方法只能得到某个时刻的近似值，这也就是LongAdder并不能完全替代LongAtomic的原因之一  
继承Striped64 并且有优化过的并发控制的封装类 基本上原理差不多 主要分两类   
xxxAdder   类似currentMap的分段锁的方式实现  把一个类型分开存储到数组中  来减少并发热点 提高效率  
xxxAccumulator  增强版xxxAdder 提供自定义计算函数   
LongAdder只能针对数值的进行加减运算   
```java
    @Test
    public void XXXAdder() {
        {
            LongAdder longAdder = new LongAdder();
            longAdder.add(1);
            longAdder.increment();
        }
        {
            //自定义一个计算函数  (o1, o2) -> o3
            LongAccumulator longAccumulator = new LongAccumulator((o1, o2) -> o1 * o2, 1);
            longAccumulator.accumulate(2);
            System.out.println(longAccumulator.get());
        }
    }
```

##### 同步器框架
##### 集合框架 
##### 执行器框架 

#### 常用功能代码 
##### 多线程执行
##### F/K拆分任务执行
##### 定时执行多线程任务

#### 总结  




