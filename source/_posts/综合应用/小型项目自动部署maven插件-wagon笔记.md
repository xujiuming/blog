---
title: 小型项目自动部署maven插件-wagon笔记
comments: true
categories: 笔记
tags:
  - maven
  - wagon
abbrlink: 9b404ebb
date: 2022-03-03 16:54:14
---
#### 前言
有时候小项目 直接上ci cd真心划不来     
写shell脚本 有不是所有的大哥都是linux mac下开发     
然后就找了下插件 找到这个 wagon-maven 插件   
这个插件主要功能就是上传下载、远程服务器执行某些命令    
插件很简单      
  
> 官网:http://www.mojohaus.org/wagon-maven-plugin/          
> maven中央仓库地址: https://mvnrepository.com/artifact/org.codehaus.mojo/wagon-maven-plugin      


#### 示例   
   
> 指定一个固定部署相关文件的目录  使用插件的上传目录的方式 去上传并且执行相关命令     

##### 配置maven settings.xml    

> 使用settings.xml 配置需要连接的服务器的信息    

配置一个id 为 ming-ubuntu的服务器配置    
  
```xml
 <servers>
    <!-- server
     | Specifies the authentication information to use when connecting to a particular server, identified by
     | a unique name within the system (referred to by the 'id' attribute below).
     |
     | NOTE: You should either specify username/password OR privateKey/passphrase, since these pairings are
     |       used together.
     |
    <server>
      <id>deploymentRepo</id>
      <username>repouser</username>
      <password>repopwd</password>
    </server>
    -->

    <!-- Another sample, using keys to authenticate.
    <server>
      <id>siteServer</id>
      <privateKey>/path/to/private/key</privateKey>
      <passphrase>optional; leave empty if not used.</passphrase>
    </server>
    -->
    <server>
      <id>ming-ubuntu</id>
      <username>ubuntu</username>
      <password>xxxxxxxxxx</password>
    </server>
    
 </servers>
```

##### 部署shell   

在root目录编写执行脚本 deploy.sh  内容如下:    

```shell
#!/bin/bash
#author lcz
# date 2019-09-26

# upload package name
NAMEDEPLOY='workbench.jar'

# stop the process before
ID=`ps -ef | grep "$NAMEDEPLOY" | grep -v "grep" | awk '{print $2}'`
echo The process pid is $ID
for id in $ID
do
    kill -9 $id
    echo killed $id
done
cd /home/ubuntu/workbench/ && nohup /home/ubuntu/.sdkman/candidates/java/17.0.1-open/bin/java -jar $NAMEDEPLOY --spring.profiles.active=prod  > workbench.out 2>&1 &
exit 0
```

##### 配置pom.xml 

```xml
 <build>
        <finalName>workbench</finalName>

        <!-- wagon upload and deploy jar -->
        <extensions>
            <extension>
                <groupId>org.apache.maven.wagon</groupId>
                <artifactId>wagon-ssh</artifactId>
                <version>3.5.1</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <!--指定编译jdk版本 默认为1.5-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <encoding>${encoding}</encoding>
                    <compilerArgs>--enable-preview</compilerArgs>
                </configuration>
            </plugin>
            <!-- 解决资源文件的编码问题 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <version>${maven.resources.plugin.version}</version>
                <configuration>
                    <encoding>${encoding}</encoding>
                </configuration>
                <executions>
                    <!--复制部署脚本-->
                    <execution>
                        <id>copy-deploy-script</id>
                        <!-- 绑定到install 生命周期上 -->
                        <phase>install</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${basedir}/target/deploy/</outputDirectory>
                            <encoding>${encoding}</encoding>
                            <resources>
                                <resource>
                                    <directory>${basedir}</directory>
                                    <includes>
                                        <include>deploy.sh</include>
                                    </includes>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                    <!--复制部署脚本-->
                    <execution>
                        <id>copy-deploy-jar</id>
                        <!-- 绑定到install 生命周期上 -->
                        <phase>install</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${basedir}/target/deploy/</outputDirectory>
                            <encoding>${encoding}</encoding>
                            <resources>
                                <resource>
                                    <directory>${basedir}/target</directory>
                                    <includes>
                                        <include>workbench.jar</include>
                                    </includes>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>


            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>wagon-maven-plugin</artifactId>
                <version>2.0.1</version>
                <executions>
                    <execution>
                        <id>upload-deploy</id>
                        <!-- 绑定到install 生命周期上 -->
                        <phase>install</phase>
                        <goals>
                            <goal>upload</goal>
                            <goal>sshexec</goal>
                        </goals>
                        <configuration>
                            <!--执行的服务器 必须和settings.xml中的server id 一样-->
                            <serverId>ming-ubuntu</serverId>
                            <!--upload 配置 -->
                            <fromDir>${project.basedir}/target/deploy/</fromDir>
                            <url>scp://show.xujiuming.com:22/home/ubuntu/workbench</url>
                            <!--执行远程命令配置-->
                            <commands>
                                <!--查看工作目录-->
                                <command>ls -alh</command>
                                <!--授权脚本执行权限-->
                                <command>chmod +x /home/ubuntu/workbench/deploy.sh </command>
                                <!--执行脚本-->
                                <command><![CDATA[/home/ubuntu/workbench/deploy.sh >/dev/null 2>&1 & ]]></command>
                            </commands>
                            <displayCommandOutputs>true</displayCommandOutputs>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

##### 执行    

```shell
mvn -DskipTests clean install package  
```


#### 总结      
就是一个小工具    
直接shell脚本实现 也很简单    但是这样兼容性会更好点 
不过要注意 shell脚本的编码格式 是LF\(linux/mac) 还是 CRLF\(windows) 一定要是 LF否则在linux上执行会无法识别     


