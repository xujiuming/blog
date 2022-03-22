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

|名称|功能|备注| 
|:--|:--|:---| 
|CountDownLatch|	倒数计数器，构造时设定计数值，当计数值归零后，所有阻塞线程恢复执行；其内部实现了AQS框架||
|CyclicBarrier|	循环栅栏，构造时设定等待线程数，当所有线程都到达栅栏后，栅栏放行；其内部通过ReentrantLock和Condition实现同步||
|Semaphore|	信号量，类似于“令牌”，用于控制共享资源的访问数量；其内部实现了AQS框架||
|Exchanger|	交换器，类似于双向栅栏，用于线程之间的配对和数据交换；其内部根据并发情况有“单槽交换”和“多槽交换”之分||
|Phaser|	多阶段栅栏，相当于CyclicBarrier的升级版，可用于分阶段任务的并发控制执行；其内部比较复杂，支持树形结构，以减少并发带来的竞争||

* CountDownLatch    
倒数计数器   
基本上两种用法:    
1:作为开关or入口      
2:作为一个完成某些操作的信号   
核心函数:
await\():阻塞当前线程 一直到计数器归零
countDown\();计数器计数-1
```java
    @SneakyThrows
    @Test
    public void countDownLatch() {
        //作为开关or入口
        CountDownLatch switcher = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                long now = System.currentTimeMillis();
                System.out.println("进入子线程" + now);
                try {
                    switcher.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("继续执行子线程!" + now);
            }).start();
        }
        //执行其他任务
        System.out.println("执行其他任务........");
        Thread.sleep(2000L);
        //释放await的线程
        switcher.countDown();
        System.out.println("释放子线程任务。。。。。。");

        // 作为完成信号
        CountDownLatch overSignal = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                long now = System.currentTimeMillis();
                System.out.println("执行任务:" + now);
                overSignal.countDown();
            }).start();
        }
        //等待十个任务执行完成
        overSignal.await();
        System.out.println("所有任务已执行完成!");
    }
```

* CyclicBarrier
循环栅格 当满足指定数量的参与者 执行一次放行的函数      
await\()会抛出BrokenBarrierException表示当前的CyclicBarrier已经损坏了，可能等不到所有线程都到达栅栏了，所以已经在等待的线程也没必要再等了，可以散伙了。   
出现以下几种情况之一时，当前等待线程会抛出BrokenBarrierException异常：  
其它某个正在await等待的线程被中断了  
其它某个正在await等待的线程超时了   
某个线程重置了CyclicBarrier\(调用了reset方法，后面会讲到)  
另外，只要正在Barrier上等待的任一线程抛出了异常，那么Barrier就会认为肯定是凑不齐所有线程了，就会将栅栏置为损坏（Broken）状态，并传播BrokenBarrierException给其它所有正在等待（await）的线程。   
当一个线程中断了 会造成整个栅栏损坏 给其他未释放的线程发送 BrokenBarrierException  
```java
    @SneakyThrows
    @Test
    public void cyclicBarrier() {
        int n = 100;
        AtomicInteger atomicInteger = new AtomicInteger();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10, () -> {
            System.out.println("========释放任务:" + atomicInteger.get());
        });

        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                System.out.println("执行任务:" + atomicInteger.incrementAndGet());
                ;
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        System.out.println("循环栅格是否损坏:" + cyclicBarrier.isBroken());
        //阻塞主线程 等待循环栅格释放
        Thread.sleep(10000L);
        //await超时   线程中断   reset cyclicBarrier
        Thread t = new Thread(() -> {
            System.out.println("测试中断子线程");
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("线程中断!");
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
                System.out.println("BrokenBarrierException");
            }
        });
        t.start();
        t.interrupt();
        System.out.println("循环栅格是否损坏:" + cyclicBarrier.isBroken());
    }
```

* Semaphore
信号量 用来控制稀缺资源使用量的 例如某函数最大并发 等等               
acquire\() 申请 可以申请多个  申请不到就阻塞  可以使用tryAcquire\()   
release\() 释放  可以归还多个                                        

```java
    @SneakyThrows
    @Test
    public void semaphore() {
        //创建公平策略模式的信号量
        //Semaphore semaphore  = new Semaphore(5,true);
        //默认为非公平模式的信号量
        Semaphore semaphore = new Semaphore(5);
        //阻塞方式获取凭证
        semaphore.acquire();
        //阻塞方式一次性获取多个凭证
        semaphore.acquire(2);
        //尝试获取凭证
        semaphore.tryAcquire();
        //归还一个凭证
        semaphore.release();
        //一次性归还多个凭证
        semaphore.release(2);
    }
```

* Exchanger
exchanger 交换就是 A B线程 一个个的互换数据   
Exchanger有两种数据交换的方式，当并发量低的时候，内部采用“单槽位交换”；并发量高的时候会采用“多槽位交换”。  
如果在单槽交换中，同时出现了多个配对线程竞争修改slot槽位，导致某个线程CAS修改slot失败时，就会初始化arena多槽数组，后续所有的交换都会走arenaExchange：  
多槽交换方法arenaExchange的整体流程和slotExchange类似，主要区别在于它会根据当前线程的数据携带结点Node中的index字段计算出命中的槽位。                 
如果槽位被占用，说明已经有线程先到了，之后的处理和slotExchange一样；                                                                           
如果槽位有效且为null，说明当前线程是先到的，就占用槽位，然后按照：spin->yield->block这种锁升级的顺序进行优化的等待，等不到配对线程就会进入阻塞。             
另外，由于arenaExchange利用了槽数组，所以涉及到槽数组的扩容和缩减问题，读者可以自己去研读源码。                                                         
其次，在定位arena数组的有效槽位时，需要考虑缓存行的影响。由于高速缓存与内存之间是以缓存行为单位交换数据的，根据局部性原理，相邻地址空间的数据会被加载到高速缓存的同一个数据块上（缓存行），而数组是连续的（逻辑，涉及到虚拟内存）内存地址空间，因此，多个slot会被加载到同一个缓存行上，当一个slot改变时，会导致这个slot所在的缓存行上所有的数据（包括其他的slot）无效，需要从内存重新加载，影响性能。
exchanger 和longAdder conCurrentMap 一样 在高并发情况下 使用无锁 + 分段处理                                                                       

```java
    @SneakyThrows
    @Test
    public void exchange() {
        Exchanger<Object> exchanger = new Exchanger<>();
        Thread t1 = new Thread(() -> {
            String[] v = new String[]{"a", "b", "c", "d", "e", "f"};
            for (String s : v) {
                try {
                    System.out.println(Thread.currentThread().getName() + "发送:" + s);
                    Object obj = exchanger.exchange(s);
                    System.out.println(Thread.currentThread().getName() + "交换获取的数据:" + obj);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                Integer[] v = new Integer[]{1, 2, 3, 4, 5, 6, 7};
                for (Integer integer : v) {
                    System.out.println(Thread.currentThread().getName() + "发送:" + integer);
                    Object obj = exchanger.exchange(integer);
                    System.out.println(Thread.currentThread().getName() + "交换获取的数据:" + obj);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();

        Thread.sleep(10000L);
    }
```

* Phaser    
设定阶段和每个阶段的参与者然后出发后续函数的一个同步器  

CountDownLatch:	倒数计数器，初始时设定计数器值，线程可以在计数器上等待，当计数器值归0后，所有等待的线程继续执行  
CyclicBarrier:	循环栅栏，初始时设定参与线程数，当线程到达栅栏后，会等待其它线程的到达，当到达栅栏的总数满足指定数后，所有等待的线程继续执行   
Phaser:	多阶段栅栏，可以在初始时设定参与线程数，也可以中途注册/注销参与者，当到达的参与者数量满足栅栏设定的数量后，会进行阶段升级（advance）  
 1. phase\(阶段)   
我们知道，在CyclicBarrier中，只有一个栅栏，线程在到达栅栏后会等待其它线程的到达。  
Phaser也有栅栏，在Phaser中，栅栏的名称叫做phase\(阶段)，在任意时间点，Phaser只处于某一个phase\(阶段)，初始阶段为0，最大达到Integerr.MAX_VALUE，然后再次归零。当所有parties参与者都到达后，phase值会递增。
如果看过之前关于CyclicBarrier的文章，就会知道，Phaser中的phase\(阶段)这个概念其实和CyclicBarrier中的Generation很相似，只不过Generation没有计数。
 2. parties\(参与者)
parties(参与者)其实就是CyclicBarrier中的参与线程的概念。
CyclicBarrier中的参与者在初始构造指定后就不能变更，而Phaser既可以在初始构造时指定参与者的数量，也可以中途通过register、bulkRegister、arriveAndDeregister等方法注册/注销参与者。
arrive\(到达) / advance\(进阶)
Phaser注册完parties（参与者）之后，参与者的初始状态是unarrived的，当参与者到达（arrive）当前阶段（phase）后，状态就会变成arrived。当阶段的到达参与者数满足条件后（注册的数量等于到达的数量），阶段就会发生进阶（advance）——也就是phase值+1。 
Termination（终止）
代表当前Phaser对象达到终止状态，有点类似于CyclicBarrier中的栅栏被破坏的概念。 
 3. Tiering（分层）
Phaser支持分层（Tiering） —— 一种树形结构，通过构造函数可以指定当前待构造的Phaser对象的父结点。之所以引入Tiering，是因为当一个Phaser有大量参与者（parties）的时候，内部的同步操作会使性能急剧下降，而分层可以降低竞争，从而减小因同步导致的额外开销。
在一个分层Phasers的树结构中，注册和撤销子Phaser或父Phaser是自动被管理的。当一个Phaser的参与者（parties）数量变成0时，如果有该Phaser有父结点，就会将它从父结点中溢移除。

```java
    @Test
    @SneakyThrows
    public void phaser() throws IOException {
        {
            //设定10个参与者  100个线程 应该是十个阶段
            //注册参与者 可以初始化指定 也可以register、bulkRegister、arriveAndDeregister等方法注册/注销参与者
            Phaser phaser = new Phaser(10);
            List<Thread> threadList = Lists.newArrayList();

            for (int i = 0; i < 100; i++) {
                Thread t = new Thread(() -> {
                    System.out.println("执行任务,当前阶段:" + phaser.arriveAndAwaitAdvance());
                });
                threadList.add(t);
                t.start();
            }
            for (Thread thread : threadList) {
                thread.join();
            }
        }
        System.out.println("=====================================================================");
        {
            //满足指定条件 开始执行
            Phaser phaser = new Phaser(1);
            for (int i = 0; i < 10; i++) {
                phaser.register();
                new Thread(() -> {
                    int n = phaser.arriveAndAwaitAdvance();
                    System.out.println("执行任务,当前阶段:" + n);
                }).start();
            }
            System.out.println("等待释放所有任务!");
            // 打开开关
            Thread.sleep(2000L);
            //arriveAndDeregister方法不会阻塞，该方法会将到达数加1，同时减少一个参与者数量，最终返回线程到达时的phase值。
            //相当于增加一个到达数量 并且减少一个参与者 达到阶段晋升的条件  从而释放所有的到达并且等待的线程
            phaser.arriveAndDeregister();
            System.out.println("主线程打开了开关");
        }
        System.out.println("=====================================================================");
        {
            //通过Phaser控制任务的执行轮数  重写onAdvance 返回true 中断执行器
            int n = 3;
            Phaser phaser = new Phaser() {
                @Override
                protected boolean onAdvance(int phase, int registeredParties) {
                    System.out.println("当前执行阶段:" + phase + ",参与者数量:" + registeredParties);
                    return phase + 1 >= n || registeredParties == 0;
                }
            };
            for (int i = 0; i < 10; i++) {
                phaser.register();
                new Thread(() -> {
                    while (!phaser.isTerminated()) {
                        int a = phaser.arriveAndAwaitAdvance();
                        System.out.println("thread-" + Thread.currentThread().getName() + ",当前参与者:" + a);
                    }
                }).start();
            }

            Thread.sleep(2000L);
        }
        System.out.println("=====================================================================");
        {
            //phaser分层结构  phaser 可以多层继承处理  最大可以Integer.MAX_VALUE 个phaser
            //phaser分层 每个根节点是根据子节点的执行结果汇总  例如子节点三个节点都执行完毕 那么根节点认为都执行完毕
            //演示使用两层树结构的phaser tree 来演示  理论上可以通过预估参与者来构建一个更加合理枝节点的tree
            int n = 3;
            //定义根节点 并且定义当满足执行条件的执行任务
            Phaser rootPhaser = new Phaser() {
                @Override
                protected boolean onAdvance(int phase, int registeredParties) {
                    System.out.println("当前执行阶段:" + phase + ",参与者数量:" + registeredParties);
                    return phase + 1 >= n || registeredParties == 0;
                }
            };
            //这里使用硬编码方式来演示
            // 子节点1 分布4个任务
            Phaser subPhaser1 = new Phaser(rootPhaser);
            for (int i = 0; i < 4; i++) {
                subPhaser1.register();
                new Thread(()->{
                    while (!subPhaser1.isTerminated()){
                        int a = subPhaser1.arriveAndAwaitAdvance();
                        System.out.println("subPhaser1-" + Thread.currentThread().getName() + ",当前参与者:" + a);
                    }
                }).start();
            }

            Phaser subPhaser2 = new Phaser(rootPhaser);
            for (int i = 0; i < 5; i++) {
                subPhaser2.register();
                new Thread(()->{
                    while (!subPhaser2.isTerminated()){
                        int a = subPhaser2.arriveAndAwaitAdvance();
                        System.out.println("subPhaser2-" + Thread.currentThread().getName() + ",当前参与者:" + a);
                    }
                }).start();
            }
            Phaser subPhaser3 = new Phaser(rootPhaser);
            for (int i = 0; i < 1; i++) {
                subPhaser3.register();
                new Thread(()->{
                    while (!subPhaser3.isTerminated()){
                        int a = subPhaser3.arriveAndAwaitAdvance();
                        System.out.println("subPhaser3-" + Thread.currentThread().getName() + ",当前参与者:" + a);
                    }
                }).start();
            }

            Thread.sleep(2000L);

        }
    }
```


##### 集合框架 
常用队列比较:

|队列特性|有界队列|近似无界队列|无界队列|	特殊队列|
|:------|:-----|:-------------|:------|:------|
|有锁算法|ArrayBlockingQueue|	LinkedBlockingQueue、LinkedBlockingDeque|	/|	PriorityBlockingQueue、DelayQueue|
|无锁算法|/|	/|	LinkedTransferQueue	|SynchronousQueue|

* ConcurrentMap
1. 五种不同的Node
```text
  1. Node结点
  Node结点的定义非常简单，也是其它四种类型结点的父类。
  默认链接到table\[i]——桶上的结点就是Node结点。当出现hash冲突时，Node结点会首先以链表的形式链接到table上，当结点数量超过一定数目时，链表会转化为红黑树。因为链表查找的平均时间复杂度为O(n)，而红黑树是一种平衡二叉树，其平均时间复杂度为O(logn)。
  2. TreeNode结点
  TreeNode就是红黑树的结点，TreeNode不会直接链接到table\[i]——桶上面，而是由TreeBin链接，TreeBin会指向红黑树的根结点。
  3. TreeBin结点
  TreeBin相当于TreeNode的代理结点。TreeBin会直接链接到table\[i]——桶上面，该结点提供了一系列红黑树相关的操作，以及加锁、解锁操作。
  4. ForwardingNode结点
  ForwardingNode结点仅仅在扩容时才会使用
  5. ReservationNode结点
  保留结点，ConcurrentHashMap中的一些特殊方法会专门用到该类结点。
```
2. 常量解释
```text
基本上和 HashMap差不多  例如链表长度超过8 并且kv数量>64 才会转化treeNode节点  只是treeNode变更为链表的时候 一个是节点<6 还要判断kv数量<16才会转换为链表
节点长度必须是2的n次方原因:
因为计算索引方式为: i = \(n - 1) & hash
n - 1 == table.length - 1，table.length 的大小必须为2的幂次的原因就在这里。
读者可以自己计算下，当table.length为2的幂次时，\(table.length-1)的二进制形式的特点是除最高位外全部是1，
配合这种索引计算方式可以实现key在table中的均匀分布，减少hash冲突——出现hash冲突时，
结点就需要以链表或红黑树的形式链接到table\[i]，这样无论是插入还是查找都需要额外的时间。
```

3. putVal的四种情况
```text
1. 首次初始化 -懒加载
sizeCtl控制table的初始化和扩容.
0  : 初始默认值
-1 : 有线程正在进行table的初始化
\>0 : table初始化时使用的容量，或初始化/扩容完成后的threshold
-\(1 + nThreads) : 记录正在执行扩容任务的线程数
initTable\()   sizeCtl 的值最终需要变更为0.75 * n
2. table\[i] 为空
最简单的情况，直接CAS操作占用桶table\[i]即可。
3. 发现ForwardingNode结点 证明当前容器正在迁移数据 尝试协助迁移数据
ForwardingNode结点是ConcurrentHashMap中的五类结点之一，相当于一个占位结点，表示当前table正在进行扩容，当前线程可以尝试协助数据迁移。
4. 出现hash冲突
当table[i]的结点类型为Node——链表结点时，就会将新结点以“尾插法”的形式插入链表的尾部。
当table[i]的结点类型为TreeBin——红黑树代理结点时，就会将新结点通过红黑树的插入方式插入。
putVal方法的最后，涉及将链表转换为红黑树 —— treeifyBin ，但实际情况并非立即就会转换，当table的容量小于64时，出于性能考虑，只是对table数组扩容1倍——tryPresize：

```

4. 查询数据的逻辑:
```text
get方法的逻辑很简单，首先根据key的hash值计算映射到table的哪个桶——table[i]。
如果table[i]的key和待查找key相同，那直接返回；
如果table[i]对应的结点是特殊结点（hash值小于0），则通过find方法查找；
如果table[i]对应的结点是普通链表结点，则按链表方式查找。
find方法:
Node节点: 直接链表操作查询
TreeBin节点: TreeBin的查找比较特殊，我们知道当槽table[i]被TreeBin结点占用时，说明链接的是一棵红黑树。由于红黑树的插入、删除会涉及整个结构的调整，所以通常存在读写并发操作的时候，是需要加锁的。
ConcurrentHashMap采用了一种类似读写锁的方式：当线程持有写锁（修改红黑树）时，如果读线程需要查找，不会像传统的读写锁那样阻塞等待，而是转而以链表的形式进行查找（TreeBin本身时Node类型的子类，所有拥有Node的所有字段）
ForwardingNode节点: ForwardingNode是一种临时结点，在扩容进行中才会出现，所以查找也在扩容的table上进行
ReservationNode节点: ReservationNode是保留结点，不保存实际数据，所以直接返回null
```
> sumCount() 和longAdder 一样 多段汇总

5. 扩容机制:
```text
扩容思路
Hash表的扩容，一般都包含两个步骤：
①table数组的扩容
table数组的扩容，一般就是新建一个2倍大小的槽数组，这个过程通过由一个单线程完成，且不允许出现并发。
②数据迁移
所谓数据迁移，就是把旧table中的各个槽中的结点重新分配到新table中。比如，单线程情况下，可以遍历原来的table，然后put到新table中。
这一过程通常涉及到槽中key的rehash，因为key映射到桶的位置与table的大小有关，新table的大小变了，key映射的位置一般也会变化。
ConcurrentHashMap在处理rehash的时候，并不会重新计算每个key的hash值，而是利用了一种很巧妙的方法。我们在上一篇说过，ConcurrentHashMap内部的table数组的大小必须为2的幂次，原因是让key均匀分布，减少冲突，这只是其中一个原因。另一个原因就是：
当table数组的大小为2的幂次时，通过key.hash & table.length-1这种方式计算出的索引i，当table扩容后（2倍），新的索引要么在原来的位置i，要么是i+n。
而且还有一个特点，扩容后key对应的索引如果发生了变化，那么其变化后的索引最高位一定是1（见扩容后key2的最高位）。
这种处理方式非常利于扩容时多个线程同时进行的数据迁移操作，因为旧table的各个桶中的结点迁移不会互相影响，所以就可以用“分治”的方式，将整个table数组划分为很多部分，每一部分包含一定区间的桶，每个数据迁移线程处理各自区间中的结点，对多线程同时进行数据迁移非常有利，后面我们会详细介绍。
已经有其它线程正在执行扩容了，则当前线程会尝试协助“数据迁移”；（多线程并发）
没有其它线程正在执行扩容，则当前线程自身发起扩容。（单线程）
注意：这两种情况都是调用了transfer方法，通过第二个入参nextTab进行区分（nextTab表示扩容后的新table数组，如果为null，表示首次发起扩容）。
第二种情况下，是通过CAS和移位运算来保证仅有一个线程能发起扩容。

```
6. 扩容的原理
```text
CASE1：当前是最后一个迁移任务或出现扩容冲突
我们刚才说了，调用transfer的线程会自动领用某个区段的桶，进行数据迁移操作，当区段的初始索引i变成负数的时候，说明当前线程处理的其实就是最后剩下的桶，并且处理完了。
所以首先会更新sizeCtl变量，将扩容线程数减1，然后会做一些收尾工作：
设置table指向扩容后的新数组，遍历一遍旧数组，确保每个桶的数据都迁移完成——被ForwardingNode占用。
另外，可能在扩容过程中，出现扩容冲突的情况，比如多个线程领用了同一区段的桶，这时任何一个线程都不能进行数据迁移。
CASE2：桶table[i]为空
当旧table的桶table[i] == null，说明原来这个桶就没有数据，那就直接尝试放置一个ForwardingNode，表示这个桶已经处理完成。
ForwardingNode我们在上一篇提到过，主要做占用位，多线程进行数据迁移时，其它线程看到这个桶中是ForwardingNode结点，就知道有线程已经在数据迁移了。
另外，当最后一个线程完成迁移任务后，会遍历所有桶，看看是否都是ForwardingNode，如果是，那么说明整个扩容/数据迁移的过程就完成了。
CASE3：桶table[i]已迁移完成
没什么好说的，就是桶已经用ForwardingNode结点占用了，表示该桶的数据都迁移完了。
CASE4：桶table[i]未迁移完成
如果旧桶的数据未迁移完成，就要进行迁移，这里根据桶中结点的类型分为：链表迁移、红黑树迁移。
①链表迁移
链表迁移的过程如下，首先会遍历一遍原链表，找到最后一个相邻runBit不同的结点。
runbit是根据key.hash和旧table长度n进行与运算得到的值，由于table的长度为2的幂次，所以runbit只可能为0或最高位为1
然后，会进行第二次链表遍历，按照第一次遍历找到的结点为界，将原链表分成2个子链表，再链接到新table的槽中。可以看到，新table的索引要么是i，要么是i+n，
②红黑树迁移
红黑树的迁移按照链表遍历的方式进行，当链表结点超过/小于阈值时，涉及红黑树<->链表的相互转换
```

```java
    @Test
    public void conCurrentMap() {
        ConcurrentMap<String, Object> concurrentMap = new ConcurrentHashMap<>();
        //返回指定key对应的值；如果Map不存在该key，则返回defaultValue
        //concurrentMap.getOrDefault(Object key, V defaultValue);
        //遍历Map的所有Entry，并对其进行指定的aciton操作
        //concurrentMap.forEach(BiConsumer action);
        //如果Map不存在指定的key，则插入<K,V>；否则，直接返回该key对应的值
        //concurrentMap.putIfAbsent(K key, V value);
        //删除与<key,value>完全匹配的Entry，并返回true；否则，返回false
        //concurrentMap.remove(Object key, Object value);
        //如果存在key，且值和oldValue一致，则更新为newValue，并返回true；否则，返回false
        //concurrentMap.replace(K key, V oldValue, V newValue);
        //如果存在key，则更新为value，返回旧value；否则，返回null
        //concurrentMap.replace(K key, V value);
        //遍历Map的所有Entry，并对其进行指定的funtion操作
        //concurrentMap.replaceAll(BiFunction function);
        //如果Map不存在指定的key，则通过mappingFunction计算value并插入
        //concurrentMap.computeIfAbsent(K key, Function mappingFunction);
        //如果Map存在指定的key，则通过mappingFunction计算value并替换旧值
        //concurrentMap.computeIfPresent(K key, BiFunction remappingFunction);
        //根据指定的key，查找value；然后根据得到的value和remappingFunction重新计算新值，并替换旧值
        //concurrentMap.compute(K key, BiFunction remappingFunction);
        //如果key不存在，则插入value；否则，根据key对应的值和remappingFunction计算新值，并替换旧值
        //concurrentMap.merge(K key, V value, BiFunction remappingFunction);
    }
```

* ConcurrentSkipListMap
跳表map 实现ConcurrentNavigableMap 是一个key有序的map
跳表由很多层组成；
每一层都是一个有序链表；
对于每一层的任意结点，不仅有指向下一个结点的指针，也有指向其下一层的指针。
ConcurrentSkipListMap内部一共定义了3种不同类型的结点，元素的增删改查都从最上层的head指针指向的结点开始：
结点定义:
```text
普通结点：Node
普通结点——Node，也就是ConcurrentSkipListMap最底层链表中的结点，保存着实际的键值对，如果单独看底层链，其实就是一个按照Key有序排列的单链表：
索引结点：Index
Index结点是除底层链外，其余各层链表中的非头结点（见示意图中的蓝色结点）。每个Index结点包含3个指针：down、right、node。
down和right指针分别指向下层结点和后继结点，node指针指向其最底部的node结点。
头索引结点：HeadIndex
HeadIndex结点是各层链表的头结点，它是Index类的子类，唯一的区别是增加了一个level字段，用于表示当前链表的级别，越往上层，level值越大。
```
```java
    @Test
    public void concurrentSkipListMap() {
         <String, Object> concurrentSkipListMap = new ConcurrentSkipListMap<>();
        //put
        //concurrentSkipListMap.put(key ,value );
        //get
        //concurrentSkipListMap.get(key);
        //remove
        //concurrentSkipListMap.remove(key);
        //第一个节点
        //concurrentSkipListMap.firstEntry();
        //最后一个节点
        //concurrentSkipListMap.lastEntry();
        //第一个key
        //concurrentSkipListMap.firstKey();
        //最后一个key
        //concurrentSkipListMap.lastKey();
    }
```

* ConcurrentSkipListSet
并发控制的跳表顺序set
类似于 treeSet 和NavigableMap treeMap的关系
内部直接使用  ConcurrentSkipListMap使用
ConcurrentSkipListMap对键值对的要求是均不能为null，所以ConcurrentSkipListSet在插入元素的时候，用一个Boolean.TRUE对象（相当于一个值为true的Boolean型对象）作为value，同时putIfAbsent可以保证不会存在相同的Key。
所以，最终跳表中的所有Node结点的Key均不会相同，且值都是Boolean.True。
```java
    @Test
    public void concurrentSkipListSet() {
        ConcurrentSkipListSet<String> concurrentSkipListSet = new ConcurrentSkipListSet<>();
    }
```

* CopyOnWriteArrayList
  写入加锁的arrayList 
每次写入或者删除的时候 先获取旧的数组 然后使用 ReentrantLock  直接锁定 保证写入原子性  操作完成之后将新数组替换到原本的读数组
CopyOnWriteArrayList的思想和实现整体上还是比较简单，它适用于处理“读多写少”的并发场景。通过上述对CopyOnWriteArrayList的分析，读者也应该可以发现该类存在的一些问题：
1. 内存的使用
由于CopyOnWriteArrayList使用了“写时复制”，所以在进行写操作的时候，内存里会同时存在两个array数组，如果数组内存占用的太大，那么可能会造成频繁GC,所以CopyOnWriteArrayList并不适合大数据量的场景。
2. 数据一致性
CopyOnWriteArrayList只能保证数据的最终一致性，不能保证数据的实时一致性——读操作读到的数据只是一份快照。所以如果希望写入的数据可以立刻被读到，那CopyOnWriteArrayList并不适合。
```java
    @Test
    public void copyOnWriteArrayList() {
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList();
    }
```

* CopyOnWriteArraySet
CopyOnWriteArraySet，从名字上可以看出，也是基于“写时复制”的思想。事实上
CopyOnWriteArraySet内部引用了一个CopyOnWriteArrayList对象，以“组合”方式，
委托CopyOnWriteArrayList对象实现了所有API功能。
```java
    @Test
    public void copyOnWriteArraySet() {
        CopyOnWriteArraySet<String> copyOnWriteArraySet = new CopyOnWriteArraySet<>();
    }
```

* ConcurrentLinkedQueue
无锁队列
ConcurrentLinkedQueue底层是基于链表实现的。
Doug Lea在实现ConcurrentLinkedQueue时，并没有利用锁或底层同步原语，而是完全基于自旋+CAS的方式实现了该队列。回想一下AQS，AQS内部的CLH等待队列也是利用了这种方式。
由于是完全基于无锁算法实现的，所以当出现多个线程同时进行修改队列的操作（比如同时入队），很可能出现CAS修改失败的情况，那么失败的线程会进入下一次自旋，再尝试入队操作，直到成功。所以，在并发量适中的情况下，ConcurrentLinkedQueue一般具有较好的性能。
ConcurrentLinkedQueue使用了自旋+CAS的非阻塞算法来保证线程并发访问时的数据一致性。由于队列本身是一种链表结构，所以虽然算法看起来很简单，但其实需要考虑各种并发的情况，实现复杂度较高，并且ConcurrentLinkedQueue不具备实时的数据一致性，实际运用中，队列一般在生产者-消费者的场景下使用得较多，所以ConcurrentLinkedQueue的使用场景并不如阻塞队列那么多。
另外，关于ConcurrentLinkedQueue还有以下需要注意的几点：
ConcurrentLinkedQueue的迭代器是弱一致性的，这在并发容器中是比较普遍的现象，主要是指在一个线程在遍历队列结点而另一个线程尝试对某个队列结点进行修改的话不会抛出ConcurrentModificationException，这也就造成在遍历某个尚未被修改的结点时，在next方法返回时可以看到该结点的修改，但在遍历后再对该结点修改时就看不到这种变化。
size方法需要遍历链表，所以在并发情况下，其结果不一定是准确的，只能供参考。
```java
    @Test
    public void concurrentLinkedQueue() {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        //加入队列  add底层直接调用offer
        //queue.add();
        //queue.addAll();
        //queue.offer();
        //poll：将首个元素从队列中弹出，如果队列是空的，就返回null
        queue.poll();
        //peek：查看首个元素，不会移除首个元素，如果队列是空的就返回null
        queue.peek();
        //element：查看首个元素，不会移除首个元素，如果队列是空的就抛出异常NoSuchElementException
        queue.element();
        //是否包含元素
        //queue.contains();
        //删除元素 下一个poll出来的元素
        queue.remove();
        //清理整个队列
        queue.clear();
    }
```

* ConcurrentLinkedDeque
无锁双端队列
提供双端进出的操作的 linkedQueue版本
实现了Deque接口的功能
```java
    @Test
    public void concurrentLinkedDeque() {
        ConcurrentLinkedDeque<String> deque = new ConcurrentLinkedDeque<>();
        //加入队列  add=addLast 都是直接调用linkLast函数  
        //deque.add();
        //deque.push();
        //deque.addFirst();
        //deque.addLast();
        //deque.addAll();
        //poll：将首个元素从队列中弹出，如果队列是空的，就返回null poll = pollFirst
        deque.poll();
        deque.pollFirst();
        deque.pollLast();
        //peek：查看首个元素，不会移除首个元素，如果队列是空的就返回null  peek = peekFirst
        deque.peek();
        deque.peekFirst();
        deque.peekLast();
        //element：查看首个元素，不会移除首个元素，如果队列是空的就抛出异常NoSuchElementException
        //element getXXXX 都是调用的peek函数只是判断了如果是空抛出NoSuchElementException 
        deque.element();
        deque.getFirst();
        deque.getLast();
        //是否包含元素
        //deque.contains()
        //删除元素 下一个poll出来的元素
        deque.remove();
        deque.removeFirst();
        deque.removeLast();
        deque.pop();
        //deque.removeAll();
        //从头部开始删除第一个eq元素的
        //deque.removeFirstOccurrence();
        //从尾部部开始删除第一个eq元素的
        //deque.removeLastOccurrence();
        //清理整个队列
        deque.clear();
    }
```
 
* BlockingQueue
阻塞队列 只是个接口  主要定义实现阻塞类队列的queue 和deque   
```java
    @SneakyThrows
    @Test
    public void blockingQueue() {
        @AllArgsConstructor
        class Channel {
            private final BlockingQueue<String> blockingQueue;

            @SneakyThrows
            public void put(String str) {
                blockingQueue.put(str);
            }

            @SneakyThrows
            public String take() {
                return blockingQueue.take();
            }
        }
        Channel channel = new Channel(new ArrayBlockingQueue<String>(1024));
        Thread produceThread = new Thread(() -> {
            while (true) {
                var str = "生产数据:" + System.currentTimeMillis();
                System.out.println(Thread.currentThread().getName() + "线程生产消息:" + str);
                channel.put(str);
                //表示愿意放弃当前cpu对当前线程的使用
                Thread.yield();
            }
        });
        Thread consumerThread = new Thread(() -> {
            while (true) {
                System.out.println(Thread.currentThread().getName() + "线程生产消息:" + "消费数据:" + channel.take());
                Thread.yield();
            }
        });
        //启动生产者和消费者
        produceThread.start();
        consumerThread.start();

        //利用consumer线程无限循环的特性 卡住主线程
        consumerThread.join();
    }
```
* ArrayBlockingQueue
基于环形数组实现的阻塞队列 
内部是一个环形数组
利用takeIndex 和putIndex来表示写入和取出的坐标
内部直接使用ReentrantLock 来处理并发
ArrayBlockingQueue利用了ReentrantLock来保证线程的安全性，针对队列的修改都需要加全局锁。在一般的应用场景下已经足够。对于超高并发的环境，由于生产者-消息者共用一把锁，可能出现性能瓶颈。
另外，由于ArrayBlockingQueue是有界的，且在初始时指定队列大小，所以如果初始时需要限定消息队列的大小，则ArrayBlockingQueue 比较合适。
```java
    @Test
    public void arrayBlockingQueue() {
        //初始化queue 指定队列长度 和 公平/非公平策略 以及初始化的集合
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1024, true, Lists.newArrayList());
        //添加数据
        //queue.add();
        //queue.offer();
        //queue.put();
        //获取数据
        //queue.poll();
        //queue.take();
        queue.remove();
        queue.clear();
    }
```

* LinkedBlockingQueue
基于链表实现的阻塞队列 
近似无界阻塞队列
如果初始化不指定大小 则默认大小为 Integer.MAX_VALUE
<p>
LinkedBlockingQueue除了底层数据结构（单链表）与ArrayBlockingQueue不同外，另外一个特点就是：
它维护了两把锁——takeLock和putLock。
takeLock用于控制出队的并发，putLock用于入队的并发。这也就意味着，同一时刻，只能只有一个线程能执行入队/出队操作，其余入队/出队线程会被阻塞；但是，入队和出队之间可以并发执行，即同一时刻，可以同时有一个线程进行入队，另一个线程进行出队，这样就可以提升吞吐量。

```java
    @Test
    public void linkedBlockingQueue() {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        //queue.put();
        //queue.take();
    }
```

* PriorityBlockingQueue 
基于堆实现的无界队列 堆能有多大队列能有多长
而且可以按照优先级去消费  
PriorityBlockingQueue是一种无界阻塞队列，在构造的时候可以指定队列的初始容量。具有如下特点：
PriorityBlockingQueue与之前介绍的阻塞队列最大的不同之处就是：它是一种优先级队列，也就是说元素并不是以FIFO的方式出/入队，而是以按照权重大小的顺序出队；
PriorityBlockingQueue是真正的无界队列（仅受内存大小限制），它不像ArrayBlockingQueue那样构造时必须指定最大容量，也不像LinkedBlockingQueue默认最大容量为Integer.MAX_VALUE；
由于PriorityBlockingQueue是按照元素的权重进入排序，所以队列中的元素必须是可以比较的，也就是说元素必须实现Comparable接口；
由于PriorityBlockingQueue无界队列，所以插入元素永远不会阻塞线程；
PriorityBlockingQueue底层是一种基于数组实现的堆结构。
注意：堆分为“大顶堆”和“小顶堆”，PriorityBlockingQueue会依据元素的比较方式选择构建大顶堆或小顶堆。比如：如果元素是Integer这种引用类型，那么默认就是“小顶堆”，也就是每次出队都会是当前队列最小的元素。
插入元素——put(E e)
PriorityBlockingQueue插入元素不会阻塞线程，put(E e)方法内部其实是调用了offer(E e)方法：
首先获取全局锁（对于队列的修改都要获取这把锁），然后判断下队列是否已经满了，如果满了就先进行一次内部数组的扩容
PriorityBlockingQueue属于比较特殊的阻塞队列，适用于有元素优先级要求的场景。它的内部和ArrayBlockingQueue一样，使用一个了全局独占锁来控制同时只有一个线程可以进行入队和出队，另外由于该队列是无界队列，所以入队线程并不会阻塞。
PriorityBlockingQueue始终保证出队的元素是优先级最高的元素，并且可以定制优先级的规则，内部通过使用堆（数组形式）来维护元素顺序，它的内部数组是可扩容的，扩容和出/入队可以并发进行。

```java
    @Test
    public void priorityBlockingQueue() {
        //提供初始化的大小  默认为11   和比较器
        PriorityBlockingQueue<String> queue = new PriorityBlockingQueue<>(1024, String::compareTo);
        //queue.put();
        //queue.take();
    }
```

* SynchronousQueue
同步数据队列
SynchronousQueue的底层实现包含两种数据结构——栈和队列。这是一种非常特殊的阻塞队列，它的特点简要概括如下：
入队线程和出队线程必须一一匹配，否则任意先到达的线程会阻塞。比如ThreadA进行入队操作，在有其它线程执行出队操作之前，ThreadA会一直等待，反之亦然；
SynchronousQueue内部不保存任何元素，也就是说它的容量为0，数据直接在配对的生产者和消费者线程之间传递，不会将数据缓冲到队列中。
SynchronousQueue支持公平/非公平策略。其中非公平模式，基于内部数据结构——“栈”来实现，公平模式，基于内部数据结构——“队列”来实现；
SynchronousQueue基于一种名为“Dual stack and Dual queue”的无锁算法实现。
公平策略，内部构造了一个TransferQueue对象，
而非公平策略则是构造了TransferStack对象。
这两个类都继承了内部类Transferer，SynchronousQueue中的所有方法，其实都是委托调用了TransferQueue/TransferStack的方法
TransferStack一共定义了三种结点类型，任何线程对TransferStack的操作都会创建下述三种类型的某种结点：
```text
REQUEST：表示未配对的消费者（当线程进行出队操作时，会创建一个mode值为REQUEST的SNode结点 ）
DATA：表示未配对的生产者（当线程进行入队操作时，会创建一个mode值为DATA的SNode结点 ）
FULFILLING：表示配对成功的消费者/生产者
```

整个transfer方法考虑了限时等待的情况，且入队/出队其实都是调用了同一个方法，其主干逻辑就是在一个自旋中完成以下三种情况之一的操作，直到成功，或者被中断或超时取消：
栈为空，或栈顶结点类型与当前入队结点相同。这种情况，调用线程会阻塞；
栈顶结点还未配对成功，且与当前入队结点可以配对。这种情况，直接进行配对操作；
栈顶结点正在配对中。这种情况，直接进行下一个结点的配对。

```java
    @Test
    public void synchronousQueue() throws InterruptedException {
        //对于公平策略，内部构造了一个TransferQueue对象，而非公平策略则是构造了TransferStack对象
        SynchronousQueue<String> queue = new SynchronousQueue<>(true);
        Thread putThread = new Thread(() -> {
            var str = "putThread:" + System.currentTimeMillis();
            try {
                queue.put(str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + ":" + str);
        });
        Thread takeThread = new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + ":takeThread:" + queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        putThread.start();
        System.out.println("putThreadStart");
        Thread.sleep(2000L);
        takeThread.start();
        System.out.println("takeThreadStart");


        putThread.join();
        takeThread.join();
    }
```

* DelayQueue
无界延时阻塞队列
DelayQueue每次出队只会删除有效期最小且已经过期的元素
内部的PriorityQueue并非在构造时创建，而是对象创建时生成
leader字段，DelayQueue每次只会出队一个过期的元素，如果队首元素没有过期，就会阻塞出队线程，让线程在available这个条件队列上无限等待。
为了提升性能，DelayQueue并不会让所有出队线程都无限等待，而是用leader保存了第一个尝试出队的线程，该线程的等待时间是队首元素的剩余有效期。
这样，一旦leader线程被唤醒（此时队首元素也失效了），就可以出队成功，然后唤醒一个其它在available条件队列上等待的线程。之后，
会重复上一步，新唤醒的线程可能取代成为新的leader线程。这样，就避免了无效的等待，提升了性能。这其实是一种名为“Leader-Follower pattern”的多线程设计模式。

> 必须要实现Delayed接口的class才能丢进延时队列  

```java
    @SneakyThrows
    @Test
    public void delayQueue() {

        @lombok.Data
        @AllArgsConstructor
        class Data implements Delayed {
            private long time;
            private String content;


            @Override
            public long getDelay(TimeUnit unit) {
                return unit.convert(this.time - System.nanoTime(), TimeUnit.NANOSECONDS);
            }


            @Override
            public int compareTo(Delayed o) {
                if (o == this) {
                    return 0;
                }
                if (o instanceof Data) {
                    Data d = (Data) o;
                    long diff = this.time - d.getTime();
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else {
                    throw new RuntimeException("不是Data类型");
                }
            }
        }

        DelayQueue<Data> queue = new DelayQueue<>();
        Thread produceThread = new Thread(() -> {
            while (true) {
                var str = System.currentTimeMillis() + "";
                System.out.println("produce:" + str);
                Data d = new Data(System.nanoTime() + TimeUnit.SECONDS.toNanos(1), str);
                queue.put(d);
                Thread.yield();
            }
        });

        Thread consumerThread = new Thread(() -> {
            while (true) {
                try {
                    System.out.println(System.currentTimeMillis() + "consumer:" + queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread.yield();
            }
        });

        produceThread.start();
        consumerThread.start();

        produceThread.join();
        consumerThread.join();
    }
```

* LinkedBlockingDeque
基于双链表实现的有界双端阻塞队列
双链表 + ReentrantLock  所有对队列的修改操作都需要先获取这把全局锁
LinkedBlockingDeque作为一种阻塞双端队列，提供了队尾删除元素和队首插入元素的阻塞方法。该类在构造时一般需要指定容量，如果不指定，则最大容量为Integer.MAX_VALUE。另外，由于内部通过ReentrantLock来保证线程安全，所以LinkedBlockingDeque的整体实现时比较简单的。
另外，双端队列相比普通队列，主要是多了【队尾出队元素】/【队首入队元素】的功能。
阻塞队列我们知道一般用于“生产者-消费者”模式，而双端阻塞队列在“生产者-消费者”就可以利用“双端”的特性，从队尾出队元素。
考虑下面这样一种场景：有多个消费者，每个消费者有自己的一个消息队列，生产者不断的生产数据扔到队列中，消费者消费数据有快又慢。为了提升效率，速度快的消费者可以从其它消费者队列的队尾出队元素放到自己的消息队列中，由于是从其它队列的队尾出队，这样可以减少并发冲突（其它消费者从队首出队元素），又能提升整个系统的吞吐量。这其实是一种“工作窃取算法”的思路。

```java
    @SneakyThrows
    @Test
    public void linkedBlockingDeque() {
        LinkedBlockingDeque<String> deque = new LinkedBlockingDeque<>();
        //生产者 1s生产1个消息
        Thread produceThread = new Thread(() -> {
            while (true) {
                var str = "produce:" + System.currentTimeMillis();
                deque.addFirst(Thread.currentThread().getName() + ":" + str);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //消费者1 3s消费一个消息
        Thread consumer1 = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("consumer:" + deque.takeFirst());
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //消费者2 窃取消息 每2s消费一次
        Thread consumer2 = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("窃取consumer1的消息:" + deque.takeLast());
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        produceThread.start();
        consumer1.start();
        consumer2.start();

        produceThread.join();
    }
```

* LinkedTransferQueue
基于链表实现的  包含synchronousQueue的功能的无界无锁阻塞队列
而LinkedTransferQueue的transfer方法则比较特殊：
当有消费者线程阻塞等待时，调用transfer方法的生产者线程不会将元素存入队列，而是直接将元素传递给消费者；
如果调用transfer方法的生产者线程发现没有正在等待的消费者线程，则会将元素入队，然后会阻塞等待，直到有一个消费者线程来获取该元素。
LinkedTransferQueue的特点简要概括如下：
LinkedTransferQueue是一种无界阻塞队列，底层基于单链表实现；
LinkedTransferQueue中的结点有两种类型：数据结点、请求结点；
LinkedTransferQueue基于无锁算法实现。
Node结点，有以下几点需要特别注意：
Node结点有两种类型：数据结点、请求结点，通过字段isData区分，只有不同类型的结点才能相互匹配；
Node结点的值保存在item字段，匹配前后值会发生变化；
private static final int NOW   = 0; // for untimed poll, tryTransfer
private static final int ASYNC = 1; // for offer, put, add
private static final int SYNC  = 2; // for transfer, take
private static final int TIMED = 3; // for timed poll, tryTransfer
NOW表示即时操作（可能失败），即不会阻塞调用线程：
poll（获取并移除队首元素，如果队列为空，直接返回null）；tryTransfer（尝试将元素传递给消费者，如果没有等待的消费者，则立即返回false，也不会将元素入队）
ASYNC表示异步操作（必然成功）：
offer（插入指定元素至队尾，由于是无界队列，所以会立即返回true）；put（插入指定元素至队尾，由于是无界队列，所以会立即返回）；add（插入指定元素至队尾，由于是无界队列，所以会立即返回true）
SYNC表示同步操作（阻塞调用线程）：
transfer（阻塞直到出现一个消费者线程）；take（从队首移除一个元素，如果队列为空，则阻塞线程）
TIMED表示限时同步操作（限时阻塞调用线程）：
poll(long timeout, TimeUnit unit)；tryTransfer(E e, long timeout, TimeUnit unit)
LinkedTransferQueue其实兼具了SynchronousQueue的特性以及无锁算法的性能，并且是一种无界队列：
和SynchronousQueue相比，LinkedTransferQueue可以存储实际的数据；
和其它阻塞队列相比，LinkedTransferQueue直接用无锁算法实现，性能有所提升。
另外，由于LinkedTransferQueue可以存放两种不同类型的结点，所以称之为“Dual Queue”：
内部Node结点定义了一个 boolean 型字段——isData，表示该结点是“数据结点”还是“请求结点”。
为了节省 CAS 操作的开销，LinkedTransferQueue使用了松弛（slack）操作：
在结点被匹配（被删除）之后，不会立即更新队列的head、tail，而是当 head、tail结点与最近一个未匹配的结点之间的距离超过“松弛阀值”后才会更新（默认为 2）。这个“松弛阀值”一般为1到3，如果太大会增加沿链表查找未匹配结点的时间，太小会增加 CAS 的开销。

```java
    @SneakyThrows
    @Test
    public void linkedTransferQueue() {
        LinkedTransferQueue<String> queue = new LinkedTransferQueue<>();
        {
            //当常规队列使用
//            queue.put();
//            queue.take();
        }
        {
            //当作synchronousQueue使用
            Thread putThread = new Thread(() -> {
                var str = "putThread:" + System.currentTimeMillis();
                try {
                    //使用transfer 及其重载函数
                    queue.transfer(str);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + ":" + str);
            });
            Thread takeThread = new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + ":takeThread:" + queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            putThread.start();
            System.out.println("putThreadStart");
            Thread.sleep(2000L);
            takeThread.start();
            System.out.println("takeThreadStart");

            putThread.join();
            takeThread.join();
        }
    }
```


##### 执行器框架及其相关的功能  
* Executors
executors 创建常用的线程池类型
ThreadPoolExecutor在以下两种情况下会执行拒绝策略：
1.当核心线程池满了以后，如果任务队列也满了，首先判断非核心线程池有没满，没有满就创建一个工作线程（归属非核心线程池）， 否则就会执行拒绝策略；
2.提交任务时，ThreadPoolExecutor已经关闭了。
拒绝策略:
```text
AbortPolicy（默认）:AbortPolicy策略其实就是抛出一个RejectedExecutionException异常：
DiscardPolicy: DiscardPolicy策略其实就是无为而治，什么都不做，等任务自己被回收：
DiscardOldestPolicy:DiscardOldestPolicy策略是丢弃任务队列中的最近一个任务，并执行当前任务：
CallerRunsPolicy: CallerRunsPolicy策略相当于以自身线程来执行任务，这样可以减缓新任务提交的速度。
```
通过executors创建不同执行器方式:
```text
//多个线程的执行器
Executors.newFixedThreadPool(5);
//单个线程的执行器
Executors.newSingleThreadExecutor();
//单个线程的定时执行器
Executors.newSingleThreadScheduledExecutor();
//缓存 线程超过指定时间没有被使用回收
Executors.newCachedThreadPool();
//定时线程池
Executors.newScheduledThreadPool(5);
//创建fork/join线程池
Executors.newWorkStealingPool();
```
```java
    @Test
    public void executorsTest() {
        //executors 工具类创建
        //多个线程的执行器
        Executors.newFixedThreadPool(5);
        //单个线程的执行器
        Executors.newSingleThreadExecutor();
        //单个线程的定时执行器
        Executors.newSingleThreadScheduledExecutor();
        //缓存 线程超过指定时间没有被使用回收
        Executors.newCachedThreadPool();
        //定时线程池
        Executors.newScheduledThreadPool(5);
        //创建fork/join线程池
        Executors.newWorkStealingPool();


        //手动创建
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();
        //Common Thread Pool
        ExecutorService pool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        pool.execute(() -> System.out.println(Thread.currentThread().getName()));
        pool.shutdown();//gracefully shutdown

    }
```

* ThreadPoolExecutor
普通线程池
线程池的引入，主要解决以下问题：
减少系统因为频繁创建和销毁线程所带来的开销；
自动管理线程，对使用方透明，使其可以专注于任务的构建。
ThreadPoolExecutor内部定义了一个AtomicInteger变量——ctl，通过按位划分的方式，在一个变量中记录线程池状态和工作线程数——低29位保存线程数，高3位保存线程池状态：
ThreadPoolExecutor一共定义了5种线程池状态：
```text
RUNNING : 接受新任务, 且处理已经进入阻塞队列的任务
SHUTDOWN : 不接受新任务, 但处理已经进入阻塞队列的任务
STOP : 不接受新任务, 且不处理已经进入阻塞队列的任务, 同时中断正在运行的任务
TIDYING : 所有任务都已终止, 工作线程数为0, 线程转化为TIDYING状态并准备调用terminated方法
TERMINATED : terminated方法已经执行完成
```

拒绝策略:
```text
AbortPolicy（默认）:AbortPolicy策略其实就是抛出一个RejectedExecutionException异常：
DiscardPolicy: DiscardPolicy策略其实就是无为而治，什么都不做，等任务自己被回收：
DiscardOldestPolicy:DiscardOldestPolicy策略是丢弃任务队列中的最近一个任务，并执行当前任务：
CallerRunsPolicy: CallerRunsPolicy策略相当于以自身线程来执行任务，这样可以减缓新任务提交的速度。
```

```java
    @SneakyThrows
    @Test
    public void threadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
                10,
                0L,
                TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("demo-")
                        .build(),
                new ThreadPoolExecutor.AbortPolicy());
        executor.submit(() -> {
            System.out.println("執行任務");
        });
        Future<Long> longFuture = executor.submit(System::currentTimeMillis);
        System.out.println(longFuture.get(1L, TimeUnit.SECONDS));
        //关闭线程池
        executor.shutdown();
    }
```

* ScheduledThreadPoolExecutor
ThreadPoolExecutor中提交的任务都是实现了Runnable接口，但是ScheduledThreadPoolExecutor比较特殊，由于要满足任务的延迟/周期调度功能，它会对所有的Runnable任务都进行包装，包装成一个RunnableScheduledFuture任务。
ThreadPoolExecutor中，需要指定一个阻塞队列作为任务队列。ScheduledThreadPoolExecutor中也一样，不过特殊的是，ScheduledThreadPoolExecutor中的任务队列是一种特殊的延时队列（DelayQueue）。
ScheduledThreadPoolExecutor在内部定义了DelayQueue的变种——DelayedWorkQueue，它和DelayQueue类似，只不过要求所有入队元素必须实现RunnableScheduledFuture接口。
ScheduledThreadPoolExecutor的主要特点：
```text
对Runnable任务进行包装，封装成ScheduledFutureTask，该类任务支持任务的周期执行、延迟执行；
采用DelayedWorkQueue作为任务队列。该队列是无界队列，所以任务一定能添加成功，但是当工作线程尝试从队列取任务执行时，只有最先到期的任务会出队，如果没有任务或者队首任务未到期，则工作线程会阻塞；
ScheduledThreadPoolExecutor的任务调度流程与ThreadPoolExecutor略有区别，最大的区别就是，先往队列添加任务，然后创建工作线程执行任务。
另外，maximumPoolSize这个参数对ScheduledThreadPoolExecutor其实并没有作用，因为除非把corePoolSize设置为0，这种情况下ScheduledThreadPoolExecutor只会创建一个属于非核心线程池的工作线程；否则，ScheduledThreadPoolExecutor只会新建归属于核心线程池的工作线程，一旦核心线程池满了，就不再新建工作线程。
```

```java
    @SneakyThrows
    @Test
    public void scheduledThreadPoolExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10,
                new ThreadFactoryBuilder()
                        .setNameFormat("demo-")
                        .build(), new ThreadPoolExecutor.DiscardPolicy());
        //执行定时任务 初始执行时间1s 间隔时间1s
        executor.scheduleAtFixedRate(() -> {
            System.out.println("执行任务" + System.currentTimeMillis());
        }, 1, 1, TimeUnit.SECONDS);

        Thread.sleep(5000L);
        executor.shutdown();
    }
```

* Future
FutureTask一共给任务定义了7种状态：
```text
NEW：表示任务的初始化状态；
COMPLETING：表示任务已执行完成（正常完成或异常完成），但任务结果或异常原因还未设置完成，属于中间状态；
NORMAL：表示任务已经执行完成（正常完成），且任务结果已设置完成，属于最终状态；
EXCEPTIONAL：表示任务已经执行完成（异常完成），且任务异常已设置完成，属于最终状态；
CANCELLED：表示任务还没开始执行就被取消（非中断方式），属于最终状态；
INTERRUPTING：表示任务还没开始执行就被取消（中断方式），正式被中断前的过渡状态，属于中间状态；
INTERRUPTED：表示任务还没开始执行就被取消（中断方式），且已被中断，属于最终状态。
```

```java
    @SneakyThrows
    @Test
    public void future() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> futures = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> "future任务:" + System.currentTimeMillis()));
        }

        for (Future<String> future : futures) {
            System.out.println(future.get());
            //取消
            //future.cancel();
            //如果此任务在正常完成之前被取消，则返回true
            //future.isCancelled();
            //如果此任务完成，则返回true 。完成可能是由于正常终止、异常或取消——在所有这些情况下，此方法都将返回true 。
            //future.isDone();
        }
    }
```

* ForkJoinPool
forkJoin 执行器 
一般的线程池只有一个任务队列，但是对于Fork/Join框架来说，由于Fork出的各个子任务其实是平行关系，为了提高效率，减少线程竞争，应该将这些平行的任务放到不同的队列中去，如上图中，大任务分解成三个子任务：子任务1、子任务2、子任务3，那么就创建三个任务队列，然后再创建3个工作线程与队列一一对应。
由于线程处理不同任务的速度不同，这样就可能存在某个线程先执行完了自己队列中的任务的情况，这时为了提升效率，我们可以让该线程去“窃取”其它任务队列中的任务，这就是所谓的工作窃取算法。
Fork/Join框架 自己实现了 类似 linkedBlockingDeque 来提升性能和实现工作窃取
核心组件:
```text
ForkJoinPool：ExecutorService的实现类，负责工作线程的管理、任务队列的维护，以及控制整个任务调度流程；
ForkJoinTask：Future接口的实现类，fork是其核心方法，用于分解任务并异步执行；而join方法在任务结果计算完毕之后才会运行，用来合并或返回计算结果；
ForkJoinWorkerThread：Thread的子类，作为线程池中的工作线程（Worker）执行任务；
WorkQueue：任务队列，用于保存任务；
```

3类外部提交任务的方法：invoke、execute、submit，它们的主要区别在于任务的执行方式上。
```text
通过invoke方法提交的任务，调用线程直到任务执行完成才会返回，也就是说这是一个同步方法，且有返回结果；
通过execute方法提交的任务，调用线程会立即返回，也就是说这是一个异步方法，且没有返回结果；
通过submit方法提交的任务，调用线程会立即返回，也就是说这是一个异步方法，且有返回结果（返回Future实现类，可以通过get获取结果）。
```
ForkJoinPool对象的构建有两种方式：
 1. 通过3种构造器的任意一种进行构造；
 2. 通过ForkJoinPool.commonPool()静态方法构造。
构造参数说明:
parallelism：默认值为CPU核心数，ForkJoinPool里工作线程数量与该参数有关，但它不表示最大线程数；
factory：工作线程工厂，默认是DefaultForkJoinWorkerThreadFactory，其实就是用来创建工作线程对象——ForkJoinWorkerThread；
handler：异常处理器；
config：保存parallelism和mode信息，供后续读取；
ctl：线程池的核心控制字段
asyncMode这个字段是指worker的同步异步模式，ForkJoinPool支持两种模式：
同步模式（默认方式）
异步模式
mode = asyncMode ? FIFO_QUEUE : LIFO_QUEUE
注意：这里的同步/异步并不是指F/J框架本身是采用同步模式还是采用异步模式工作，而是指其中的工作线程的工作方式。在F/J框架中，每个工作线程（Worker）都有一个属于自己的任务队列（WorkQueue），这是一个底层采用数组实现的双向队列。
同步是指：对于工作线程（Worker）自身队列中的任务，采用后进先出（LIFO）的方式执行；异步是指：对于工作线程（Worker）自身队列中的任务，采用先进先出（FIFO）的方式执行。
异步模式比较适合于那些不需要返回结果的任务。其实如果将队列中的任务看成一棵树（无环连通图）的话，异步模式类似于图的广度优先遍历，同步模式类似于图的深度优先遍历

ForkJoinTask实现了Future接口，是一个异步任务，我们在使用Fork/Join框架时，一般需要使用线程池来调度任务，线程池内部调度的其实都是ForkJoinTask任务（即使提交的是一个Runnable或Callable任务，也会被适配成ForkJoinTask）。
除了ForkJoinTask，Fork/Join框架还提供了两个它的抽象实现，我们在自定义ForkJoin任务时，一般继承这两个类：
RecursiveAction：表示没有返回结果的ForkJoin任务
RecursiveTask：表示具有返回结果的ForkJoin任务
调用task的fork()方法会ForkJoinPool.commonPool()方法创建线程池，然后将自己作为任务提交给线程池。

ForkJoinWorkerThread
Fork/Join框架中，每个工作线程（Worker）都有一个自己的任务队列（WorkerQueue）， 所以需要对一般的Thread做些特性化处理，J.U.C提供了ForkJoinWorkerThread类作为ForkJoinPool中的工作线程
ForkJoinWorkerThread 在构造过程中，会保存所属线程池信息和与自己绑定的任务队列信息。同时，它会通过ForkJoinPool的registerWorker方法将自己注册到线程池中。
线程池中的每个工作线程（ForkJoinWorkerThread）都有一个自己的任务队列（WorkQueue），工作线程优先处理自身队列中的任务（LIFO或FIFO顺序，由线程池构造时的参数 mode 决定），自身队列为空时，以FIFO的顺序随机窃取其它队列中的任务。

WorkQueue
任务队列（WorkQueue）是ForkJoinPool与其它线程池区别最大的地方，在ForkJoinPool内部，维护着一个WorkQueue[]数组，它会在外部首次提交任务）时进行初始化：
volatile WorkQueue[] workQueues; // main registry
当通过线程池的外部方法（submit、invoke、execute）提交任务时，如果WorkQueue[]没有初始化，则会进行初始化；然后根据数组大小和线程随机数（ThreadLocalRandom.probe）等信息，计算出任务队列所在的数组索引（这个索引一定是偶数），如果索引处没有任务队列，则初始化一个，再将任务入队。也就是说，通过外部方法提交的任务一定是在偶数队列，没有绑定工作线程。
WorkQueue作为ForkJoinPool的内部类，表示一个双端队列。双端队列既可以作为栈使用(LIFO)，也可以作为队列使用(FIFO)。ForkJoinPool的“工作窃取”正是利用了这个特点，当工作线程从自己的队列中获取任务时，默认总是以栈操作（LIFO）的方式从栈顶取任务；当工作线程尝试窃取其它任务队列中的任务时，则是FIFO的方式。
我们在ForkJoinPool一节中曾讲过，可以指定线程池的同步/异步模式（mode参数），其作用就在于此。同步模式就是“栈操作”，异步模式就是“队列操作”，影响的就是工作线程从自己队列中取任务的方式。
ForkJoinPool中的工作队列可以分为两类：
有工作线程（Worker）绑定的任务队列：数组下标始终是奇数，称为task queue，该队列中的任务均由工作线程调用产生（工作线程调用FutureTask.fork方法）；
没有工作线程（Worker）绑定的任务队列：数组下标始终是偶数，称为submissions queue，该队列中的任务全部由其它线程提交（也就是非工作线程调用execute/submit/invoke或者FutureTask.fork方法）。

F/J框架的核心来自于它的工作窃取及调度策略，可以总结为以下几点：
```text
每个Worker线程利用它自己的任务队列维护可执行任务；
任务队列是一种双端队列，支持LIFO的push和pop操作，也支持FIFO的take操作；
任务fork的子任务，只会push到它所在线程（调用fork方法的线程）的队列；
工作线程既可以使用LIFO通过pop处理自己队列中的任务，也可以FIFO通过poll处理自己队列中的任务，具体取决于构造线程池时的asyncMode参数；
当工作线程自己队列中没有待处理任务时，它尝试去随机读取（窃取）其它任务队列的base端的任务；
当线程进入join操作，它也会去处理其它工作线程的队列中的任务（自己的已经处理完了），直到目标任务完成（通过isDone方法）；
当一个工作线程没有任务了，并且尝试从其它队列窃取也失败了，它让出资源（通过使用yields, sleeps或者其它优先级调整）并且随后会再次激活，直到所有工作线程都空闲了——此时，它们都阻塞在等待另一个顶层线程的调用。
```

任务提交
任务提交是整个调度流程的第一步，F/J框架所调度的任务来源有两种：*
①外部提交任务
所谓外部提交任务，是指通过ForkJoinPool的execute/submit/invoke方法提交的任务，或者非工作线程（ForkJoinWorkerThread）直接调用ForkJoinTask的fork/invoke方法提交的任务：
clipboard.png
外部提交的任务的特点就是调用线程是非工作线程。这个过程涉及以下方法：
ForkJoinPool.submit
ForkJoinPool.invoke
ForkJoinPool.execute
ForkJoinTask.fork
ForkJoinTask.invoke
ForkJoinPool.externalPush
ForkJoinPool.externalSubmit
②工作线程fork任务
所谓工作线程fork任务，是指由ForkJoinPool所维护的工作线程（ForkJoinWorkerThread）从自身任务队列中获取任务（或从其它任务队列窃取），然后执行任务。
工作线程fork任务的特点就是调用线程是工作线程。这个过程涉及以下方法：
ForkJoinTask.doExec
WorkQueue.push

创建工作线程
任务提交完成后，ForkJoinPool会根据情况创建或唤醒工作线程，以便执行任务。
ForkJoinPool并不会为每个任务都创建工作线程，而是根据实际情况（构造线程池时的参数）确定是唤醒已有空闲工作线程，还是新建工作线程。这个过程还是涉及任务队列的绑定、工作线程的注销等过程：
ForkJoinPool.signalWork
ForkJoinPool.tryAddWorker
ForkJoinPool.createWorker
ForkJoinWorkerThread.registerWorker
ForkJoinPool.deregisterWorker

任务执行
任务入队后，由工作线程开始执行，这个过程涉及任务窃取、工作线程等待等过程：
ForkJoinWorkerThread.run
ForkJoinPool.runWorker
ForkJoinPool.scan
ForkJoinPool.runTask
ForkJoinTask.doExec
ForkJoinPool.execLocalTasks
ForkJoinPool.awaitWork

任务结果获取
任务结果一般通过ForkJoinTask的join方法获得，其主要流程如下图：
任务结果获取的核心涉及两点：
互助窃取：ForkJoinPool.helpStealer
算力补偿：ForkJoinPool.tryCompensate

```java
    @SneakyThrows
    @Test
    public void forkJoin() {
        @Data
        @AllArgsConstructor
        class MyTask extends RecursiveTask<Long> {
            private final int[] arr;
            private final int begin;
            private final int end;
            private final static int THRESHOLD = 100;


            @Override
            protected Long compute() {
                long sum = 0L;
                //如果任务足够小 则执行任务
                if ((end - begin) + 1 < THRESHOLD) {
                    for (int i = begin; i <= end; i++) {
                        sum += arr[i];
                    }
                } else {
                    //任务大于阈值 拆分两个任务处理
                    int middle = (end + begin) / 2;
                    MyTask task1 = new MyTask(arr, begin, middle);
                    MyTask task2 = new MyTask(arr, middle + 1, end);

                    //执行任务
                    task1.fork();
                    task2.fork();

                    sum = task1.join() + task2.join();
                }
                return sum;
            }
        }

        //此示例 要注意超出number限制的问题
        int len = 10000;
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = i;
        }

        //初始化fork join pool
        ForkJoinPool pool = new ForkJoinPool(100);
        StopWatch forkJoinStopWatch = new StopWatch();
        forkJoinStopWatch.start();
        ForkJoinTask<Long> forkJoinTask = pool.submit(new MyTask(arr, 0, arr.length - 1));

        //判断是否有异常或者取消执行
        if (forkJoinTask.isCompletedAbnormally()) {
            System.out.println("任务执行异常或者取消:" + forkJoinTask.getException());
        }
        //获取结果
        forkJoinStopWatch.stop();
        System.out.println("获取任务执行结果:" + forkJoinTask.get() + ",耗时:" + forkJoinStopWatch.getTotalTimeNanos() + "ns");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        long result = Arrays.stream(arr).sum();
        stopWatch.stop();
        System.out.println("使用并行集合处理:" + result + ",耗时:" + stopWatch.getTotalTimeNanos() + "ns");
        System.out.println("使用并行stream汇总计算:" + Arrays.stream(arr).parallel().sum());
        //在超过1w的数据汇总的时候 才能明显看到fk框架比直接sum快很多
        //所以在使用fk框架的时候 要根据预估的任务量、类型、和计算难度去评估并行程度 也就是并行任务拆分的细粒度和执行线程数
    }
```



#### 常用功能代码 
##### 多线程执行
##### F/K拆分任务执行
##### 定时执行多线程任务

#### 总结  




