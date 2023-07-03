---
title: netty-传输手段笔记
comments: true
categories: 笔记
tags:
  - netty
  - 传输协议
abbrlink: 597b09c9
date: 2018-05-22 16:38:36
---
###传输案例 
#### java oio
```
package com.ming;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * 基于java 阻塞io的 服务端案例
 *
 * @author ming
 * @date 2018-04-11 14:53
 */
public class JavaOio {

    public static void main(String[] args) throws IOException {
        new JavaOio().Start(20000);
    }

    /**
     * 启动java oio 方法
     *
     * @param port
     * @author ming
     * @date 2018-04-11 14:57
     */
    public void Start(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);
        for (; ; ) {
            //接受链接
            final Socket clientSocket = socket.accept();
            System.out.println("accept client::" + clientSocket);
            //建立新线程执行 客户端的请求
            new Thread(() -> {
                OutputStream outputStream = null;
                try {
                    //获取输出流
                    outputStream = clientSocket.getOutputStream();
                    //输出数据
                    outputStream.write("hi ming ".getBytes(Charset.forName("UTF-8")));
                    //刷新数据
                    outputStream.flush();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}

```
#### java nio
```
package com.ming;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * java 实现nio 模式的服务端
 *
 * @author ming
 * @date 2018-04-11 15:08
 */
public class JavaNio {


    public static void main(String[] args) throws IOException {
        new JavaNio().Start(20000);
    }

    /**
     * 启动java nio 服务端
     *
     * @param port
     * @author ming
     * @date 2018-04-11 15:09
     */
    public void Start(int port) throws IOException {
        //打开 server socket channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //设置 为非阻塞队列
        serverSocketChannel.configureBlocking(false);
        //通过socket channel 获取socket
        ServerSocket socket = serverSocketChannel.socket();
        //获取socket 的地址
        InetSocketAddress socketAddress = new InetSocketAddress(port);
        //绑定 socket地址到socket
        socket.bind(socketAddress);
        //打开selector
        Selector selector = Selector.open();
        //将socket channel 注册到selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //返回客户端的消息
        final ByteBuffer msg = ByteBuffer.wrap("hi ming ".getBytes("UTF-8"));

        for (; ; ) {
            try {
                //阻塞等待需要处理的新事件
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            //获取所有连接事件的 select key
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            readyKeys.forEach(key -> {
                        try {
                            //检查时间 是否是一个新的 可以被接受的链接
                            if (key.isAcceptable()) {
                                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                                SocketChannel client = server.accept();
                                if (null == client) {
                                    return;
                                }
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());
                                System.out.println("accept from " + client);
                            }
                            //检查socket是否准备好写数据
                            if (key.isWritable()) {
                                SocketChannel client = (SocketChannel) key.channel();
                                ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                                while (byteBuffer.hasRemaining()) {
                                    if (client.write(byteBuffer) == 0) {
                                        break;
                                    }
                                }
                                client.close();
                            }

                        } catch (IOException e) {
                            key.cancel();
                            try {
                                key.channel().close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
            );
        }
    }
}

```
#### netty oio 
```
package com.ming;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * netty oio 实现
 *
 * @author ming
 * @date 2018-04-16 15:56
 */
public class NettyOio {
    public static void main(String[] args) throws InterruptedException {
        new NettyOio().Start(20000);
    }

    /**
     * netty oio bootstrap
     *
     * @author ming
     * @date 2018-04-16 15:56
     */
    public void Start(int port) throws InterruptedException {
        final ByteBuf byteBuf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hi ming ", CharsetUtil.UTF_8));
        // 使用 oio 事件循环处理
        EventLoopGroup eventLoopGroup = new OioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    //使用oio 渠道
                    .channel(OioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("accept " + ctx);
                                    //输出一条信息 关闭 渠道
                                    ctx.writeAndFlush(byteBuf.duplicate())
                                            .addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            //绑定服务器
            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}

```
#### netty nio 
```
package com.ming;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * netty nio 实现
 *
 * @author ming
 * @date 2018-04-16 16:11
 */
public class NettyNio {

    public static void main(String[] args) throws InterruptedException {
        new NettyNio().Start(20000);
    }

    /**
     * 启动 netty nio server
     *
     * @author ming
     * @date 2018-04-16 16:12
     */
    public void Start(int port) throws InterruptedException {
        final ByteBuf byteBuf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hi ming ", CharsetUtil.UTF_8));
        //使用 nio 事件循环处理器
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    //使用nio渠道
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("accept " + ctx);
                                    ctx.writeAndFlush(byteBuf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }


    }
}

```
#### epoll
```
package com.ming;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * 基于linux 的Epoll 非阻塞 方式实现的 io传输
 *
 * @author ming
 * @date 2018-04-16 17:28
 */
public class NettyEpoll {

    public static void main(String[] args) throws InterruptedException {
        new NettyEpoll().Start(20000);
    }

    /**
     * 启动 基于 epoll 的server
     *
     * @author ming
     * @date 2018-04-16 17:29
     */
    public void Start(int port) throws InterruptedException {
        final ByteBuf byteBuf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hi ming ", CharsetUtil.UTF_8));
        //使用epoll
        EventLoopGroup eventLoopGroup = new EpollEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(EpollServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("accept" + ctx);
                                    ctx.writeAndFlush(byteBuf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });

                        }
                    });
            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }

}

```
#### jvm local
1:jvm local server 
```
package com.ming.JvmLocal;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.util.CharsetUtil;

/**
 * netty 基于jvm内部本地通信 server实现
 *
 * @author ming
 * @date 2018-04-17 13:45
 */
public class NettyJvmLocalServer {


    /**
     * 启动方法
     *
     * @author ming
     * @date 2018-04-17 13:50
     */
    public void Start() throws InterruptedException {
        final ByteBuf byteBuf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hi ming ", CharsetUtil.UTF_8));
        //注册 local 事件处理器
        EventLoopGroup eventLoopGroup = new LocalEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    //使用Local server channel
                    .channel(LocalServerChannel.class)
                    //使用 local 地址
                    .localAddress(new LocalAddress(NettyJvmLocal.LOCAL_ADDRESS))
                    .childHandler(new ChannelInitializer<LocalChannel>() {
                        @Override
                        protected void initChannel(LocalChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("accept " + ctx);
                                    ctx.writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }


    }

}

```
2: jvm local client 
```
package com.ming.JvmLocal;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.util.CharsetUtil;

/**
 * netty 基于jvm内部本地通信 client 实现
 *
 * @author ming
 * @date 2018-04-17 13:45
 */
public class NettyJvmLocalClient {


    /**
     * 启动方法
     *
     * @author ming
     * @date 2018-04-17 13:50
     */
    public void Start() throws InterruptedException {
        final ByteBuf byteBuf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hi ming client", CharsetUtil.UTF_8));
        //注册 local 事件处理器
        EventLoopGroup eventLoopGroup = new LocalEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    //使用 localChannel渠道
                    .channel(LocalChannel.class)
                    //注册 local模式的地址
                    .remoteAddress(new LocalAddress(NettyJvmLocal.LOCAL_ADDRESS))
                    .handler(new ChannelInitializer<LocalChannel>() {
                        @Override
                        protected void initChannel(LocalChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("connect " + ctx);
                                    ctx.writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }

}

```
3:jvm local demo 
```
package com.ming.JvmLocal;

/**
 * 调用 netty jvm  local server 和client 的客户端
 *
 * @author ming
 * @date 2018-04-17 14:09
 */
public class NettyJvmLocal {
    /**
     * 本地地址
     *
     * @author ming
     * @date 2018-04-17 14:16
     */
    public static final String LOCAL_ADDRESS = "ming";

    public static void main(String[] args) throws InterruptedException {
        // 启动 server
        new Thread(() -> {
            try {
                new NettyJvmLocalServer().Start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        //暂停 线程 1s 等待server 启动完毕
        Thread.sleep(1000L);

        //启动client
        new Thread(() -> {
            try {
                new NettyJvmLocalClient().Start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}

```
### 传输
传输是基于channel 的  

channel是有顺序的 因为继承 java.lang.Comparable   
channelHandler:常规用途  
* 数据格式转换   
* 异常处理  
* 提供channel 状态变更通知  
* 提供channel 注册或者注销 EventLoop中的通知  
* 提供用户自定义事件通知  

channel 线程安全 并且保证顺序发送   

### 内置传输手段
|名称|包|备注|    
|:--|:-|:--|    
|NIO|io.netty.channel.socket.nio|使用java nio|     
|Epoll|io.netty.channel.epoll|基于jni驱动的epoll()和非阻塞io 只能在linux上支持 比NIO快 并且完全非阻塞|  
|OIO|io.netty.channel.socket.oio|使用java net包 阻塞流|  
|Local|io.netty.channel.local|直接使用jvm内部管道通信|  
|Embedded|io.netty.channel.embedded|测试channelHandler使用的不依赖网络|   

### nio  非阻塞 io
基于selector 
状态变化:  
* 新的channel 已经被接受并且准备就绪   
* channel 链接完成  
* channel 有就绪的可供读取的数据  
* channel 可用于写数据  
通过java.nio.channels.SelectionKey定义的位 组合成一组应用程序正在请求通知的状态变化集合  
|名称|描述|  
|:--|:--|  
|OP_ACCEPT|请求接受新链接创建channel获得通知|  
|OP_CONNECT|请求建立第一个链接时候获得通知|  
|OP_READ|请求数据就绪 可从channel 读取时获得通知|  
|OP_WRITE|请求当可以向 channel中写入数据时候 获得通知、|  

### Epoll 基于linux的本地非阻塞传输  
在linux 2.5.44内核引入的功能   比POSIX 的select poll 更加牛逼  
不考虑 夸平台  只考虑linux 的话  epoll 直接走起     
### oio 阻塞io  
基于jdk  java.net包的阻塞io   
是netty 为了兼容 可能存在的老旧的系统 保留的 oio支持   
### 基于jvm内部的local传输  
在同一个jvm中运行的客户端和服务端程序直接的异步通信使用这种 可以大幅度提高速度   


### 总结：   
java实现oio和nio的代码 提供的api 区别比较大     
oio 不需要selector 直接server socket接受 处理    
nio 通过selector 协调 分配给相应的 socket 处理   
netty 实现oio和nio 提供的api 基本相同 就是在使用发送方式和发送渠道时候 需要保持一致   
例如 使用NioEventLoopGroup 那么传输渠道必须是NioServerChannelSocket   
使用OioEventLoopGroup那么必须使用OioServerChannelSocket   


### 传输支持的协议
|传输类型|支持类型|  
|:-----|:-----|   
|NIO| tcp、udp、sctp、udt|  
|Epoll(linux)|tcp、udp|   
|OIO|tcp、udp、sctp、udt|   

sctp 增强版本的tcp  
udt  增强版本的udp  

### 传输类型选择:  
|需求类型|推荐传输类型|备注|  
|:-----|:---------|:--|  
|非阻塞代码、常规套路|nio、或者epoll| 如果不考虑跨平台等功能 并且只考虑linux 平台运行 那么 epoll 是肯定的 毕竟是个特殊优化的非阻塞模式|  
|阻塞代码|oio|处理遗留代码的时候选择|  
|同jvm通信|jvm local|在同一个jvm中 进行通信的话 选择jvm local 可以直接省掉网络io的开销|  
|测试ChannelHandler|Embedded|只有写单元测试用用 |   

零拷贝:  
```
   零拷贝(zero-copy)是一种目前只有在使用 NIO 和 Epoll 传输时才可使用的特性。它使你可以快速
   高效地将数据从文件系统移动到网络接口,而不需要将其从内核空间复制到用户空间,其在像 FTP 或者
   HTTP 这样的协议中可以显著地提升性能。但是,并不是所有的操作系统都支持这一特性。特别地,它对
   于实现了数据加密或者压缩的文件系统是不可用的——只能传输文件的原始内容。反过来说,传输已被
   加密的文件则不是问题。
```