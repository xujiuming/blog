---
title: maven 远程部署tomcat 上的java web 项目
categories: 笔记
tags:
  - maven
  - java
  - tomcat
abbrlink: d7957e83
date: 2017-11-11 00:00:00
---
####由于有朋友不会这个远程部署又懒的百度、google 于是我写这个maven+tomcat远程部署javaweb项目的教程。以后心情好说不定会继续更新项目管理的一些环境搭建:如jenkins+gitlab+项目管理工具(如禅道)这样自动化部署测试环境
##工具和环境准备
1:工具 maven tomcat 
2:环境maven能下jar即可、tomcat管理员账户和一些安全设置设定(8.5以上需要设定远程管理员访问的配置)
3:能访问远程tomcat管理页面
##maven远程部署配置
####1: maven工具配置
需要在maven的setting.xml中添加如下配置
settins.xml 一般在maven安装目录的conf文件夹下 不排除有人更改默认地址了
```
<servers>
    <!--maven tomcat远程部署密码帐号密码设置-->
    <server>
        <id>名称 例如“laji” (任意取   pom.xml中需要使用)</id>
        <username>tomcat管理员用户名</username>
        <password>tomcat管理员密码</password>
    </server>
</servers>
```
####2:maven项目中的pom设置 在build的插件中添加下面这个插件并且配置
亲测可以支持8.5tomcat 和7.x的  ,  tomcat 9.x的没测试
```
<!--tomcat远程部署插件-->
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <artifactId>tomcat7-maven-plugin</artifactId>
                <version>2.1</version>
                <configuration>
                 <!-- 此处的名字必须和setting.xml中配置的ID一致-->
                    <server>laji</server>
                    <!-- tomcat远程部署访问页面-->
                    <url>http://服务器ip:8080/manager/text</url>
 <!-- 此处的名字是项目发布的工程名 可以任意取 访问部署好的项目旧是这个名称-->
                    <path>/lajiproject</path>
                </configuration>
            </plugin>
```

##tomcat远程部署配置
####1:需要配置tomcat管理员账户 并且能访问远程tomcat管理员地址例如：localhost:8080/manager/text
在tomcat安装目录的conf下面的tomcat-users.xml中修改
```
<tomcat-users xmlns="http://tomcat.apache.org/xml"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd"
              version="1.0">
<role rolename="admin-gui"/>
<role rolename="admin-script"/>
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<role rolename="manager-jmx"/>
<role rolename="manager-status"/>
<user username="admin" password="ideal123" roles="manager-gui,manager-script,manager-jmx,manager-status,admin-script,admin-gui"/>

</tomcat-users>
                 
```
####2:tomcat8.5远程需要添加远程管理员用户访问配置步骤
####tomcat 8.5之后呢对安全访问增强了  不仅仅需要配置管理帐号还需要配置允许远程访问管理员账户
```
在conf/Catalina/localhost/manager.xml  
没有就创建这个xml ，然后在manager.xml中添加

<Context privileged="true" antiResourceLocking="false"
         docBase="${catalina.home}/webapps/manager">
    <Valve className="org.apache.catalina.valves.RemoteAddrValve" allow="^.*$" />
</Context>

直接生效无须重启
在官方文档中提到：每个web应用应该有自己的安全管理文件（manager.xml）如果没有使用默认值 也就是不能远程访问
所以如果需要远程访问就需要创建manager.xml来指定允许远程访问规则
```
##执行远程部署
####例如idea 在你项目的maven配置的地方找到Plugins>tomcat7>tomcat7:redeploy
####尽量使用redeploy  因为你不从新部署有时候会产生乱七八糟的错误
####这个插件具体玩法 向具体了解就需要自行找官方文档 我也没看官方文档
###总结:部署过程： maven编译jar或者war》maven通过远程服务器的支持上传jar或者war
###这个时候需要保证本地编译的jar和war能符合服务器上的tomcat的jre版本
