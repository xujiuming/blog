---
title: web service笔记
categories: 笔记
tags:
  - java
  - web service
abbrlink: a451f549
date: 2017-11-11 00:00:00
---
##web service 三要素 soap、wsdl、uddi
###soap(Simple Object Access Protocol) 简单对象访问协议
soap是一种对象访问通信协议 基于xml的协议 用来访问结构化和固化的信息 

主要特点 制定程序之间的通信、消息格式; 独立平台、语言;基于xml;允许绕过防火墙;w3c标准


######soap消息是一个xml文档: 
必须包含Envelope元素 (标识此xml文档为soap消息)
必须包含Body元素(包含所有调用和响应的信息)
可选Header元素(头部信息)、Fault(处理此消息发生的错误信息)

语法规则:
soap消息必须是xml
soap消息必须使用soap Envelope、soap Encoding 命名空间来保证规范
soap消息不能包含DTD
soap不能包含xml处理指令
```
<?xml version="1.0">
<soap:Envelope
xmlns:soap="http://www.w3.org/2001/12/soap-envelope"
soap:encodingStyle="http://www.w3.org/2001/12/soap-encoding">

<soap:Body>

</soap:Body>
</soap:Envelope>
```
soap消息的xml文档 一般包含Header、Body、Fault等三个元素 具体直接查询w3c文档

###wsdl(Web Services Description Language )网络服务描述语言
wsdl也是xml文档 用来描述网络服务  不是w3c标准
主要元素:
portType：web service 执行的操作
message：web service 使用消息
types：web service使用数据类型
binding：web service 使用的通信协议

######portType端口
提供四种类型  
客户端主动
One-way:接受消息 不返回  
Request-response：此服务接受请求返回响应  
服务端主动  
Solicit-response:此服务发送请求等待响应  
Notiication：此服务发送一个请求 不等待响应  
######Request-response类型
```
<message name="myRequest“>
    <part name="param" type=”xs:string"/>
</message>

<message name="myResponse">
    <part name="value" type="xs:string"/>
</message>

<portType name="myService">
    <operation name="myRequestResponse>
        <input message="myRequest"/>
        <output message="myResponse"/>
    </operation>
</portType>
```
上述例子 一个服务myService 定义了一个名字是myRequestResponse的操作
myRequestResponse接受一个myRequest的输入消息，返回一个myResponse消息

######binding（并不是特别明白 只知道是seb service 具体细节的 以后明白了在补上)
###uddi(Universal Description, Discovery and Integration) 描述、发现、集成服务
看的不是特别明白 感觉就是一个列表 这个列表 里面存在大量的wsdl等相关信息 用来提供给其他调用方看的

##总结:web service 三要素 soap消息、wsdl、uddi soap定义了消息的格式xml文档、wsdl定义了服务调用的xml文档、uddi整理归纳wsdl等相关文档
