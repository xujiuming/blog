---
title: 服务端推送技术及其实现(轮询、长链接、sse、webSocket)
categories: 实战
tags:
  - java
  - js
  - 服务端推送
abbrlink: 3a488ac5
date: 2017-10-30 19:23:02
---
## 前言

## 服务端推送信息方案及其实现
* 轮询 ：就是一直循环访问服务端 服务端压力比较大  利用js的一些定时功能 隔一段时间发起一次请求
* 长轮询:一次请求 服务端吧请求保留 等由数据返回的时候 再返回 管理麻烦 
* 长链接 例如用iframe 维护长链接开销较大 而且页面会显示一直在加载 不利于使用
* flash socket:利用flash插件提供的socket 麻烦 需要会flash  flash的缺点无法避免如安全 
* WebSocket： html5技术 利用提供的html5本身特性来实现socket ws或者wss协议 现阶段几乎所有的浏览器最新版都支持 除开个别奇葩版本  
* sse: server-sent event http协议变通实现的 通过服务端向客户端声明 接下来是要发送的是流信息 本质上就是完成一次耗时长的下载   
### web socket服务端推送
#####maven依赖
```
<dependencies>
    <!-- https://mvnrepository.com/artifact/javax.websocket/javax.websocket-api -->
    <dependency>
        <groupId>javax.websocket</groupId>
        <artifactId>javax.websocket-api</artifactId>
        <version>1.1</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>3.1.0</version>
    </dependency>
</dependencies>
```
#####页面代码
```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>客户端</title>
</head>
<body>
<div>
    <input type="button" id="bconnection" value="链接">
    <input type="button" id="bclose" value="关闭">
    <input type="button" id="bsend" value="发送">
        <input type="text" id="sessionId" name="sessionId">
        <input type="text" id="message" name="message">
        <input type="button" onclick="sendServlet()" value="jdklsjlfs">
</div>
<script src="http://apps.bdimg.com/libs/jquery/1.6.4/jquery.min.js" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript">

    //模拟另外个客户向服务器发起推送消息服务
    function sendServlet() {
        $.get("http://localhost:8080/ws/sendServlet?sessionId="+$("#sessionId").val()+"&message="+$("#message").val());
    }


    //设立全局变量方便后续操作
    var socket;
    //判断是否支持WebSocket
    if (typeof (WebSocket)=="undefined"){
        alert("浏览器不支持websocket");
    }
    $("#bconnection").click(function () {
        //实例化weosocket 制定服务器地址和端口
        socket=new WebSocket("ws://localhost:8080/ws/websocket/xu");
        //打开链接
        socket.onopen=function () {
            console.log("打开weosocket端口");
            socket.send("客户端发送打开链接请求成功")
        };
        //获取消息事件
        socket.onmessage=function (msg) {
            console.log("获取的消息"+msg.data);
        };
        //关闭socket
        socket.onclose=function () {
            console.log("关闭socket");
        };
        //监听错误
        socket.onerror=function () {
            console.log("socket发生错误");
        }

    });

    //发送消息
    $("#bsend").click(function () {
        socket.send("客户端消息:"+location.href+new Date());
    });

    //手动关闭socket
    $("#bclose").click(function () {
        socket.close();
    });
</script>
</body>
</html>
```
#####服务端代码
######websocket链接类
```
package com.xxx.websocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by xianyu on 17-3-7.
 * websocket 服务端响应
 */
@ServerEndpoint("/websocket/{user}")
public class WebSocketService {
    private String socketUser;

    //记录session列表
    private static Map<String ,Session> sessionMap=new HashMap<>();

    public static void sendMessage(String sessionId,String message){
        sessionMap.get(sessionId).getAsyncRemote().sendText("推送消息："+message);
    }


    //打开socket链接的时候操作
    @OnOpen
    public void onOpen(@PathParam("user")String user, Session session) throws IOException {
        socketUser=user;
        System.out.println("链接打开账户为"+user+"sessionid为"+session.getId());
        sessionMap.put(session.getId(),session);
        session.getAsyncRemote().sendText("服务端成功接受链接;sessionId="+session.getId());
    }

    //接受消息的时候操作
    @OnMessage
    public String onMessage(String message) {
        System.out.println(socketUser+"客户的消息"+message);
        return socketUser+":"+message;
    }


    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println(String.format("session id 为%s 用户为%s  closeReason%s",session.getId(),socketUser,reason));
    }


}

```
#####模拟发送服务端推送消息servlet 
```
package com.xxx.websocket;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Created by xianyu on 17-3-7.
 */
public class SendServlet extends HttpServlet {

    private String messagePrefix="servlet:";
    @Override
    public void init() throws ServletException {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        WebSocketService.sendMessage(req.getParameter("sessionId"),messagePrefix+req.getParameter("message")+"||||||||||||"+new Date());

    }

    @Override
    public void destroy() {
        super.destroy();
    }
}

```
#### 总结
webscoket是现阶段实现服务端推送、在线聊天、等等需要使用tcp长链接的地比较合适的一个技术 现在市面上的浏览器最新版有不支持的websocket的  ws 和wss区别 相当于http和https区别一样
