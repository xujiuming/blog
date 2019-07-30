---
title: awk中print和printf区别
comments: true
categories: 坑
tags:
  - linux
abbrlink: '58835920'
date: 2019-07-26 17:35:40
---
#### 前言
在写jenkinsfile 的时候使用 shell中的awk来拆分一些字符  发现有些字符竟然变成好几行 
平常在shell上用awk一般就输出出来看  没关注这个问题 

参考文档: https://blog.csdn.net/qq_35696312/article/details/88169556  
#### 问题重现 
执行如下命令:
```bash
echo aaaaaa bbbbbb  cccc | awk '{print $1}' > tmp.xx && echo ming >> tmp.xx && cat tmp.xx
```
执行结果:
```text
aaaaaa
ming
```

执行如下命令:
```bash
echo aaaaaa bbbbbb  cccc | awk '{printf $1}' > tmp.xx && echo ming >> tmp.xx && cat tmp.xx

```
执行结果:
```text
aaaaaaming
```

#### 问题原因 
print 输入默认会在尾行加一个 \n 换行
printf 如果不配置 默认原样输出 

#### 总结 
平常使用的时候 就输出出来看 没仔细看说明 
到需要的时候 才发现 这些小毛病  

日常使用 print 即可    
如果需要原样输出或者格式化输出 使用printf 进行输出  