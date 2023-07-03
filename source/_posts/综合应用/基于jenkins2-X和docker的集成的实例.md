---
title: 基于jenkins2.X和docker的持续集成的实例
comments: true
categories: 实战
tags:
  - jenkins
  - docker
  - groovy
  - linux
abbrlink: 4732d3fc
date: 2018-02-09 14:13:29
---
#### 实例功能
* 自动编译、打包
* 自动分发、部署
* 钉钉消息通知
* 交互式部署

通过jenkins部署一个index.html首页 

#### 实现所需技能
* jenkins 
* docker 
* groovy 写脚本
* 项目打包
* linux常用技术
* java 

#### 步骤
以这个目录为root目录: https://github.com/xuxianyu/info/tree/master/mingJenkins/simple  
0:创建index.html
```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>ming</title>
</head>
<body>
ming
</body>
</html>
```
1：创建docker file 
在项目目录下建立 Dockerfile
```
FROM docker.io/nginx:1.13-alpine
MAINTAINER 'ming'
# 复制index.html 到nginx 工作目录  
COPY index.html /usr/share/nginx/html
```
2：创建Jenkinsfile
Jenkinsfile 是jenkins出的用一套用来定义流水线模式的任务的脚本  
其中有两种模式
* pipeline jenkins标准的 配置脚本模式
* script  使用groovy dsl 实现的jenkins 流水线模式脚本  

我采用的是script模式  因为灵活 好用 两者区别请参考jenkins官网 
```
import java.text.SimpleDateFormat

/**jenkinsfile 使用groovy dsl方式实现
 *  采用 基于groovy 控制流程   groovy dsl +shell 共同协作 部署发布
 *  先在 dev rc 环境使用  单节点 直接停止之后部署   等待寻找对环境稳定影响较少的 多节点部署+dev rc  uat 环境单节点部署方案
 *  多节点部署:
 *  当前想法是使用 用空间换取时间的方式
 *  提供两套nginx 配置
 *  每次先启动 然后切换配置
 *  需要项目提供监听接口 监听当前是否有在执行的任务
 *  需要事务由一定的全局事务 或者一定的补偿机制 保证最终一致性
 *
 *
 *
 *
 *  修改此脚本需要的技能点:
 *  0: 熟悉 项目版本管理工具 如 svn  git
 *  1:熟悉 java groovy  此脚步使用是groovy 和部分jdk的语法书写
 *  2:熟悉jenkins 熟悉 2.x以上的pipe 方式的 jenkins task  由于jenkins官方文档 保持一贯的简略的风格 查阅资料请直接上github 或者stack overflow 之类的干货网站
 *  3:熟悉项目打包 如java的 maven打包 、 h5的 npm 打包、php的打包  、c# 打包
 *  4:熟悉 linux相关功能 及其原理 例如 ssh密钥验证原理
 *  5:熟悉docker 基础原理 及其相关命令
 *  6:了解 自动化测试 大致使用方法 如接口测试、ui测试 如何实现
 *  7:了解代码审查相关工具 使用
 *  8:了解钉钉、mail、jira 通知方式、如钉钉的webhook  mail发送原理、jira webhook
 *
 *
 *
 *  编码规范:
 *  0:注释一定要写全 特别是方法的注释
 *  1:遵循java 编码规范  只能说尽量遵循  毕竟不是java
 *  2:尽可能的使用 jenkins插件中的方法 而不是groovy dsl 调用 shell
 *  3:使用默认参数时候 请尽量在最顶层方法使用  基础性的方法 尽量避免使用默认参数
 *  4:由于groovy的sdk 可拆分的特性 jenkins对于部分groovy写法不支持
 *  5:groovy dsl  可以使用jdk的写法去写  支持完整的jdk  前提是环境里面有jdk  没有就不行
 *  6:为了保持 灵活性  只能使用 script pipe   禁止改称pipeline 模式书写
 *
 * @author ming
 * @mail 18120580001@163.com
 * @date 2017-12-21 16:44
 * */

node {
    //启动任务的参数
    properties([
            parameters([

                    //项目名称
                    string(defaultValue: "{项目名称 只能是小写英文 和-}", description: "项目名称", name: "projectName"),
                    //项目版本
                    string(defaultValue: "2.0.0", description: "项目版本", name: "projectVersion"),
                    //项目默认部署端口
                    string(defaultValue: "10000", description: "项目默认部署端口", name: "defaultPort"),
                    // 容器内部端口
                    string(defaultValue: "80", description: "项目默认部署端口", name: "imagePort"),
                    // dev 环境服务器
                    string(defaultValue: "{dev环境服务器地址}", description: "dev环境服务器地址", name: "devHost"),
                    //dev 环境 ssh 端口
                    string(defaultValue: "{dev环境服务器ssh登陆端口}", description: "dev环境服务器ssh端口", name: "devHostPort"),
                    //docker 私服地址
                    string(defaultValue: "{docker 私服仓库地址 }", description: "docker仓库地址", name: "dockerRegistry"),
                    // 项目通知用户组   钉钉艾特方式
                    string(defaultValue: '"{注册钉钉的电话号码}"', description: "项目组成员钉钉通知电话号码", name: "mobilesGroup"),
                    //容器名称
                    string(defaultValue: "{容器启动之后的名称}", description: "容器名称", name: "containerName"),
                    // 获取配置方式
                    // ${params.${name}}
            ])
    ])

    svnScmParams = checkout(scm)

    //全局变量
    // docker镜像私服
    String dockerRegistryHost
    if ("${params.dockerRegistry}".startsWith("http://")) {
        dockerRegistryHost = "${params.dockerRegistry}".substring(7)
    } else if ("${params.dockerRegistry}".startsWith("https://")) {
        dockerRegistryHost = "${params.dockerRegistry}".substring(8)
    } else {
        throw new Exception("docker镜像仓库前缀必须是http:// 或者https：//")
    }

    //dev 仓库镜像名称
    String devRegistryImageName


    // 刷新db的 镜像名称
    //流程定义
    try {
        //建立编译环境  node+ docker in docker 环境 
        docker.image('car2godeveloper/dind-node-build-runner').inside('--privileged') {

            stage('编译项目') {
                //闭包 递归 调用自己
                Closure compile = { ->
                    try {
                        print '编译项目'
                        //存放编译相关指令
                        sendDD("编译通过", "${params.mobilesGroup}")
                    } catch (e) {
                        if (isStop(e)) {
                            sendDD("编译失败::${e.getMessage()}", "${params.mobilesGroup}")
                            throw e
                        } else {
                            sendDD("编译失败重新执行::${e.getMessage()}", "${params.mobilesGroup}")
                            call()
                        }
                    }
                }
                compile.call()
            }


            stage('构建发布项目docker image') {
                Closure buildProjectDockerImage = { ->
                    try {
                        sh 'docker --version'
                        //发布到私服仓库 {镜像仓库地址}/{镜像命名空间}/{镜像名称}:{tag标签}
                        //imageName=${dockerRepository}/${namespace}/${projectName}-${projectVersion}-${环境}:${localDate}
                        String localDate = geFormatterLocalDate("yyyyMMdd.HHmmss")
                        imageName = "${params.projectName}-${params.projectVersion}-dev:${localDate}"
                        docker.withRegistry("${params.dockerRegistry}") {
                            docker.build(imageName).push()
                            devRegistryImageName = "${dockerRegistryHost}/${imageName}"
                        }
                        sendDD("构建发布镜像成功", "${params.mobilesGroup}")
                    } catch (e) {
                        if (isStop(e)) {
                            sendDD("构建发布镜像失败::${e.getMessage()}", "${params.mobilesGroup}")
                            throw e
                        } else {
                            sendDD("构建发布镜像失败重新执行::${e.getMessage()}", "${params.mobilesGroup}")
                            call()
                        }
                    }
                }
                buildProjectDockerImage.call()
            }



            stage('部署dev环境') {
                Closure deployDev = { ->
                    try {
                        print '部署dev环境'
                        deploy("${params.devHost}", "${params.defaultPort}", "${params.containerName}", "${devRegistryImageName}", "","${params.imagePort}", "${params.devHostPort}")
                        //ssh远程部署
                        sendDD("部署dev成功", "${params.mobilesGroup}")
                    } catch (e) {
                        if (isStop(e)) {
                            sendDD("部署dev失败::${e.getMessage()}", "${params.mobilesGroup}")
                            throw e
                        } else {
                            sendDD("部署dev失败重新执行::${e.getMessage()}", "${params.mobilesGroup}")
                            call()
                        }
                    }
                }
                deployDev.call()
            }


            stage('dev环境自动化测试') {
                Closure autoTestDev = { ->
                    try {
                        print 'dev环境自动化测试'
                        //自动化测试指令
                        sendDD("自动化测试dev环境成功", "${params.mobilesGroup}")
                    } catch (e) {
                        if (isStop(e)) {
                            sendDD("自动化测试dev环境失败::${e.getMessage()}", "${params.mobilesGroup}")
                            throw e
                        } else {
                            sendDD("自动化测试dev环境失败重新执行::${e.getMessage()}", "${params.mobilesGroup}")
                            call()
                        }
                    }
                }
                autoTestDev.call()
            }


            stage('dev环境功能确认？(发布rc环境镜像)') {
                Closure releaseDev = { ->
                    try {
                        def userInput = input(
                                id: 'userInput', message: '发布备注:', parameters: [
                                [$class: 'TextParameterDefinition', defaultValue: '无', description: '发布备注:', name: 'memo'],
                        ])
                        print userInput
                        //替换 名称中的 dev标记   在dev 机器上完成
                        rcRegistryImageName = devRegistryImageName.replaceAll("-dev", "-rc")
                        release(rcRegistryImageName, devRegistryImageName, "${params.devHost}", "${params.devHostPort}")
                        sh 'echo  dev环境功能测试确定'
                        sendDD("dev环境发布到rc仓库成功", "${params.mobilesGroup}")
                    } catch (e) {
                        if (isStop(e)) {
                            sendDD("dev环境发布到rc仓库失败::${e.getMessage()}", "${params.mobilesGroup}")
                            throw e
                        } else {
                            sendDD("dev环境发布到rc仓库失败重新执行::${e.getMessage()}", "${params.mobilesGroup}")
                            call()
                        }
                    }
                }
                releaseDev.call()
            }

            //........  后面都是重复性的节点  直接复制改改就是的

        }
    } catch (e) {
        print currentBuild.result
        sendDD("持续集成构建失败" + e.getMessage(), "${params.mobilesGroup}")
        throw e
    }

}

/** 发送到钉钉  使用 shell脚本发送 因为 jenkins 的groovy不是完整的gdk功能 调jdk 又比较麻烦   干脆直接拼写 shell命令执行算了
 *  默认钉钉 url  发版通知群jenkins 机器人
 * @param info 消息内容
 * @param mobiles 艾特电话号码组{@link *MobilesGroup }
 * @author ming
 * @date 2017-12-22 18:58
 * */
def sendDD(String info, String mobiles
           , String url = 'https://oapi.dingtalk.com/robot/send?access_token={钉钉机器人的token}') {
    String headers = 'Content-Type: application/json'
    String atStr = ""
    String[] mobilesArr = mobiles.split(",")
    for (int i = 0; i < mobilesArr.length; i++) {
        //截取 2<=n <13中间11 位电话号码
        String tmp = "${mobilesArr[i]}".substring(1, 12)
        atStr = atStr + " @${tmp}"
    }
    String jsonStr = "{ \"msgtype\": \"markdown\"" +
            ", \"markdown\": { \"title\":\"执行结果通知\", \"text\": \"#### ${atStr} 执行结果通知:${params.projectName}项目:${info}\" }," +
            " \"at\": { \"atMobiles\": [${mobiles}], \"isAtAll\": false } }"
    String script = "curl \'${url}\' -H \'${headers}\' -d \'${jsonStr}\' && exit 0 "
    sh script
}

/** 预留方法 发送邮件
 *
 * @author ming
 * @date 2017-12-22 19:01
 * */
def sendMail() {

}

/**根据时间格式化格式 获取当前时间  默认 yyyy-MM-dd HH:mm:ss
 * @param patten
 * @return dateStr
 * @author ming
 * @date 2017-12-22 23:39
 * */
def geFormatterLocalDate(String patten = "yyyy-MM-dd HH:mm:ss") {
    Date date = new Date()
    SimpleDateFormat formatter = new SimpleDateFormat(patten)
    formatter.format(date)
}

/**流程错误 处理
 *
 * @author ming
 * @date 2017-12-27 22:29
 * */
def boolean isStop(Exception e) {
    print e.getMessage()
    def userInput = input(
            id: 'userInput', message: '是否中止 此次流程:', parameters: [
            [$class: 'TextParameterDefinition', defaultValue: 'true', description: '是否中止此次流程', name: 'flag'],
    ])
    print userInput
    if ('true'.equalsIgnoreCase(userInput)) {
        return true
    } else if ('false'.equalsIgnoreCase(userInput)) {
        return false
    }
}

/** 运行容器
 *
 * @param port
 * @param containerName
 * @param registryImageName
 * @param host
 * @param sshKey
 * @author ming
 * @date 2017-12-26 23:36
 * */
def runSSHDocker(String port, String containerName, String registryImageName, String env, String imagePort,String host, String sshPort, String sshKey) {
    // jenkins 配置的sshkey id
    sshagent([sshKey]) {
        //登陆服务器之后启动容器的命令
        String runCmd = "docker run -d -p ${port}:${imagePort} ${env} --name ${containerName} ${registryImageName}"
        // 登陆 服务器 运行容器
        sh "ssh -o StrictHostKeyChecking=no -t -t -p ${sshPort} root@${host} '${runCmd}'"
    }
}

/** 删除容器
 *
 * @param containerName
 * @param host
 * @param sshKey
 * @author ming
 * @date 2017-12-26 23:36
 * */
def removeSSHContainer(String containerName, String host, String sshPort, String sshKey) {
    sshagent([sshKey]) {
        //登陆服务器之后启动容器的命令
        String runCmd = "docker rm -f ${containerName}"
        // 登陆 服务器 运行容器
        sh "ssh -o StrictHostKeyChecking=no -t -t -p ${sshPort} root@${host} '${runCmd}'"
    }

}

/** 删除镜像
 *
 * @param registryImageName
 * @param host
 * @param sshKey
 * @author ming
 * @date 2017-12-26 23:36
 * */
def removeSSHImage(String registryImageName, String host, String sshPort, String sshKey) {
    sshagent([sshKey]) {
        //登陆服务器之后启动容器的命令
        String runCmd = "docker rmi ${registryImageName}"
        // 登陆 服务器 运行容器
        sh "ssh -o StrictHostKeyChecking=no -t -t -p ${sshPort} root@${host} '${runCmd}'"
    }
}

/**修改远程服务中的 imagename
 * 这里的imagename 必须是 仓库地址/镜像名:tag
 * @param newImageName
 * @param oldImageName
 * @author ming
 * @date 2017-12-27 10:04
 * */
def updateSSHImamgeName(String newImageName, String oldImageName, String host, String sshPort, String sshKey) {
    sshagent([sshKey]) {
        String runCmd = "docker tag ${oldImageName} ${newImageName}"
        sh "ssh -o StrictHostKeyChecking=no -t -t -p ${sshPort}  root@${host} '${runCmd}'"
    }
}

/** 推送远程服务器上的镜像
 * 这里镜像名称 必须是 仓库地址/镜像名:tag 格式
 * @param imageName
 * @author ming
 * @date 2017-12-27 10:08
 * */
def pushSSHImage(String imageName, String host, String sshPort, String sshKey) {
    sshagent([sshKey]) {
        String runCmd = "docker push ${imageName}"
        sh "ssh -o StrictHostKeyChecking=no -t -t -p ${sshPort} root@${host} '${runCmd}'"
    }
}

/** 部署
 * @param host
 * @param port
 * @param containerName
 * @param imageName
 * @author ming
 * @date 2017-12-27 09:43
 * */
def deploy(String host, String port, String containerName, String imageName, String env ,String imagePort, String sshPort = "22", String sshKey = 'efa90668-61e3-47f0-9730-b0e53ea7f97e') {
    boolean flag = true
    while (flag) {
        try {
            //运行docker
            runSSHDocker(port, containerName, imageName, env,imagePort, host, sshPort, sshKey)
            flag = false
        } catch (e) {
            print e.getMessage()
            //删除运行中 的容器
            removeSSHContainer(containerName, host, sshPort, sshKey)
            //删除服务器上的image
            removeSSHImage(imageName, host, sshPort, sshKey)
        }
    }
}

/**发布镜像到下一个阶段
 * @param newImageName
 * @param oldImageName
 * @param host
 * @param sshKey
 * @author ming
 * @date 2017-12-27 10:15
 * */
def release(String newImageName, String oldImageName, String host, String sshPort = "22", String sshKey = 'efa90668-61e3-47f0-9730-b0e53ea7f97e') {
    updateSSHImamgeName(newImageName, oldImageName, host, sshPort, sshKey)
    pushSSHImage(newImageName, host, sshPort, sshKey)
    removeSSHImage(oldImageName, host, sshPort, sshKey)
}



/** ui自动化测试
 *
 * @author ming
 * @date 2017-12-27 22:12
 * */
def uiAutoTest() {

}

/**代码审查 并且发送报告
 *
 * @author ming
 * @date 2017-12-27 22:12
 * */
def codeReview() {

}

```

将其中的用{} 包含的中文 替换成相应的参数即可 
sshKey 是jenkins配置sshAgent插件的id 

3:安装启动 jenkins 
docker 方式启动 参考: {% post_link docker启动jenkins docker启动jenkins %}

4: 安装插件
主要是要安装 sshAgent 、 docker相关插件、
5:jenkins 引入项目定义的Jenkinsfile 形成流水线任务

新增jenkins 流水线任务 
配置流水线相关配置 定义选择 Pipeline script from SCM 》 scm选择git 配置git地址等相关属性 》script path 选择 Jenkinsfile 即可
保存项目 》 启动项目 















