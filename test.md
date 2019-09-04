- 1.boss应用发版
  每次应用发版必须执行如下操作
  - 1.0 停机   
    登录10.11.30.61:9000 上将 boss_frontend服务 实例缩减为0   
    > 注意如果发版之后有其他维护 需要再次调整boss_frontend服务实例数量进行停机  
  - 1.1更新服务容器    
    登录10.11.30.63:20000(root/root) jenkins上 执行new-boss-prod任务即可   
    是否启动 登录 10.11.30.60:9000上看service 是否全部启动完毕          
    如果不确定 登录10.11.30.61 通过docker service 相关命令查看具体服务启动情况    
  - 1.2日志归档     
    每周发版的时候需要对boss的日志手动打包归档到nas服务上 61、26上都要处理  
    - 1.2.1 打包
      ```bash
      #打包日志 并且删除打包过的文件 
       tar zcvf boss-时间(yyyyMMdd).tar.gz /logs/*.log --remove-files
      ```
    - 1.2.2 上传到nas盘  
      ```bash
        #复制文件到 nas上  由于61 不挂载nas服务 只有62挂载了 跟集团运维沟通过采用这种方式保存log   
        scp boss-时间(yyyyMMdd).tar.gz boss@10.11.30.62:/home/bak/data.bak/bosslog/boss-时间(yyyyMMdd).tar.gz
        rm -rf boss-时间(yyyyMMdd).tar.gz
      ```       
  - 1.3服务器巡检       
     手工登录 61 62 26 70机器检查服务器的 磁盘、内存、cpu、时间使用情况 
  - 1.4通知产品、运营生产查看生产是否有报错       
     发版完成之后 确定都启动之后通知 产品和运营进行生产发版检查 确保不会因为环境导致报错     
- 2.boss日志处理  
  - 2.1天备份         
    61和26 的/ossfs目录都是挂载在oss(阿里云oss服务 、现在是放在ming私人oss上的 后期需要迁移到公司oss)上的       
    备份压缩但是不清理log文件     
    61的归档脚本 /ossfs/syncbossmasterlog.sh   
    26的归档脚本 /ossfs/syncbossworkerlog.sh 
    每天会凌晨一点会执行备份前一天的日志到oss上
    ```bash
    # 查看定时任务     
    crontab -l 
    ```
  - 2.2周备份    
    每周手工备份 到62的nas盘上 会清理log文件    参考#1.2日志归档 
    
- 3.bossDB管理   
  - 3.1数据目录    
    62上的db数据目录为: /home/postgres/data11  
    > /home/postgres/data为原10.3版本的数据 已经停止使用了  /var/lib/pgsql/11/data/ 为升级11.5时候的临时数据目录 
  - 3.2停止、启动 重启操作   
    ```bash
    #启动 postgres11.5
    /usr/pgsql-11/bin/pg_ctl  start -D /home/postgres/data11
    #重启postgres11.5
    /usr/pgsql-11/bin/pg_ctl  restart -D /home/postgres/data11
    #关闭postgres11.5  
    /usr/pgsql-11/bin/pg_ctl  stop -D /home/postgres/data11
    ```
    
- 4.redis   
  redis使用容器启动 运行在61上 重启、开启、停止 直接操作容器即可  
- 5.rabbit mq      
  10.11.30.61上安装的  mq由集团运维安装、管理
           