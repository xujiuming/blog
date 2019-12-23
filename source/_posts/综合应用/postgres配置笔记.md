---
title: postgres配置笔记
comments: true
categories: 笔记
tags:
  - postgres
abbrlink: ad95aacf
date: 2019-12-20 10:20:12
---
#### 前言 
postgres数据库用了很久了  也维护过很多次了  
今天抽时间将postgres 一些配置文件做一个翻译和记录 方便后续速查 

本身postgres官网的文档和配置里面的说明已经很清晰了 
这里只是作为一个翻译 对照 和速查

#### postgresql.conf
由于此处网络上有现成的文档  直接引用
 
> 原文链接：https://blog.csdn.net/weixin_39540651/article/details/100582554

##### 配置详解  
```editorconfig
# -----------------------------
# PostgreSQL configuration file
# -----------------------------
#
# This file consists of lines of the form:
#
#   name = value
#
# (The "=" is optional.)  Whitespace may be used.  Comments are introduced with
# "#" anywhere on a line.  The complete list of parameter names and allowed
# values can be found in the PostgreSQL documentation.
#
# The commented-out settings shown in this file represent the default values.
# Re-commenting a setting is NOT sufficient to revert it to the default value;
# you need to reload the server.
#
# This file is read on server startup and when the server receives a SIGHUP
# signal.  If you edit the file on a running system, you have to SIGHUP the
# server for the changes to take effect, run "pg_ctl reload", or execute
# "SELECT pg_reload_conf()".  Some parameters, which are marked below,
# require a server shutdown and restart to take effect.
#
# Any parameter can also be given as a command-line option to the server, e.g.,
# "postgres -c log_connections=on".  Some parameters can be changed at run time
# with the "SET" SQL command.
#
# Memory units:  kB = kilobytes        Time units:  ms  = milliseconds
#                MB = megabytes                     s   = seconds
#                GB = gigabytes                     min = minutes
#                TB = terabytes                     h   = hours
#                                                   d   = days


#------------------------------------------------------------------------------
# FILE LOCATIONS
#------------------------------------------------------------------------------

# The default values of these variables are driven from the -D command-line
# option or PGDATA environment variable, represented here as ConfigDir.

#指定用于数据存储的目录。这个选项只能在服务器启动时设置。
#data_directory = 'ConfigDir'        # use data in another directory
                    # (change requires restart)

#指定基于主机认证配置文件(通常叫pg_hba.conf)。 这个参数只能在服务器启动的时候
设置。
#hba_file = 'ConfigDir/pg_hba.conf'    # host-based authentication file
                    # (change requires restart)

#指定用于用户名称映射的配置文件(通常叫pg_ident.conf)。 这个参数只能在服务器启
动的时候设置。
#ident_file = 'ConfigDir/pg_ident.conf'    # ident configuration file
                    # (change requires restart)

#指定可被服务器创建的用于管理程序的额外进程 ID(PID)文件。这个参数只能在服务 器启动的时候设置。
# If external_pid_file is not explicitly set, no extra PID file is written.
#external_pid_file = ''            # write an extra PID file
                    # (change requires restart)


#------------------------------------------------------------------------------
# CONNECTIONS AND AUTHENTICATION
#------------------------------------------------------------------------------

# - Connection Settings -

#监听地址
#listen_addresses = 'localhost'        # what IP address(es) to listen on;
                    # comma-separated list of addresses;
                    # defaults to 'localhost'; use '*' for all
                    # (change requires restart)
#监听端口
#port = 5432                # (change requires restart)

# 最大连接数:建议不要大于 200 * 四分之一物理内存(GB)， 例如四分之一物理内存为16G，则建议不要超过3200.          
#(假设平均一个连接耗费5MB。  实际上syscache很大、SQL 使用到WORK_MEM，未使用hugepage并且访问到大量shared buffer page时，可能消耗更多内存)       
# 如果业务有更多并发连接，可以使用连接池，例如pgbouncer 
#max_connections = 100            # (change requires restart)

# 为超级用户保留多少个连接 
#superuser_reserved_connections = 3    # (change requires restart)

#指定服务器用于监听来自客户端应用的连接的 Unix 域套接字目录。
#unix_socket_directories = '/tmp'    # comma-separated list of directories
                    # (change requires restart)

#设置Unix域套接字的所属组
#unix_socket_group = ''            # (change requires restart)

#设置 Unix 域套接字的访问权限。默认是所有人都可以连接,建议值0700:除了OWNER和超级用户外都不可以访问
#unix_socket_permissions = 0777        # begin with 0 to use octal notation
                    # (change requires restart)

#通过Bonjour广告服务器的存在。默认值是关闭。                    
#bonjour = off                # advertise server via Bonjour
                    # (change requires restart)

#指定Bonjour服务名称。空字符串''(默认值)表示使用计算机名。                    
#bonjour_name = ''            # defaults to the computer name
                    # (change requires restart)

# - TCP settings -
# see "man 7 tcp" for details

#指定不活动多少秒之后通过 TCP 向客户端发送一个 keepalive消息。建议设置为60
#tcp_keepalives_idle = 0        # TCP_KEEPIDLE, in seconds;
                    # 0 selects the system default

#指定在多少秒之后重发一个还没有被客户端告知已收到的 TCP keepalive 消息。建议设置为10
#tcp_keepalives_interval = 0        # TCP_KEEPINTVL, in seconds;
                    # 0 selects the system default

#指定与客户端的服务器连接被认为死掉之前允许丢失的 TCP keepalive 数量。建议设置为10                    
#tcp_keepalives_count = 0        # TCP_KEEPCNT;
                    # 0 selects the system default

#指定在强制关闭连接之前传输数据可能保持未确认的毫秒数                    
#tcp_user_timeout = 0            # TCP_USER_TIMEOUT, in milliseconds;
                    # 0 selects the system default

# - Authentication -

#完成客户端认证的最长时间，以秒计。
#authentication_timeout = 1min        # 1s-600s

#如果用户密码的MD5会泄露，建议使用scram-sha-256，但是相互不兼容，请注意。
#password_encryption = md5        # md5 or scram-sha-256

#允许针对每个数据库的用户名。默认是关闭的。即按照username@dbname的方式创建用户
#db_user_namespace = off

# GSSAPI using Kerberos

#设置 Kerberos 服务器密钥文件的位置。
#krb_server_keyfile = ''

#设置 Kerberos 和 GSSAPI 用户名是否应区分大小写。默认是off(区分大小写)
#krb_caseins_users = off

# - SSL -

#启用SSL连接
#ssl = off

#指定包含 SSL 服务器证书颁发机构(CA)的文件名
#ssl_ca_file = ''

#指定包含 SSL 服务器证书的文件名。
#ssl_cert_file = 'server.crt'

#指定包含 SSL 服务器证书撤销列表(CRL)的文件名。
#ssl_crl_file = ''

#指定包含 SSL 服务器私钥的文件名。
#ssl_key_file = 'server.key'

#指定一个SSL密码列表，用于安全连接。
#ssl_ciphers = 'HIGH:MEDIUM:+3DES:!aNULL' # allowed SSL ciphers

#指定是否使用服务器的 SSL 密码首选项，而不是用客户端的。
#ssl_prefer_server_ciphers = on

#指定用在ECDH密钥交换中的曲线名称。
#ssl_ecdh_curve = 'prime256v1'

#设置要使用的最小SSL / TLS协议版本。
#ssl_min_protocol_version = 'TLSv1'

#设置要使用的最大SSL / TLS协议版本。
#ssl_max_protocol_version = ''

#指定包含用于所谓的临时DH系列SSL密码的Diffie-Hellman参数的文件的名称
#ssl_dh_params_file = ''

#设置在需要获取用于解密SSL文件（例如私钥）的密码短语时要调用的外部命令
#ssl_passphrase_command = ''
#ssl_passphrase_command_supports_reload = off


#------------------------------------------------------------------------------
# RESOURCE USAGE (except WAL)
#------------------------------------------------------------------------------

# - Memory -
#设置数据库服务器将使用的共享内存缓冲区量。建议1/4主机内存
#IF use hugepage: 主机内存*(1/4) ELSE: min(32GB, 主机内存*(1/4)) 
shared_buffers = 128MB            # min 128kB
                    # (change requires restart)
#是否使用hugepage,建议shared buffer设置超过32GB时使用                    
#huge_pages = try            # on, off, or try
                    # (change requires restart)

#设置每个数据库会话使用的临时缓冲区的最大数目。                    
#temp_buffers = 8MB            # min 800kB

#如果用户需要使用两阶段提交，需要设置为大于0，建议与max_connections一样大 
#max_prepared_transactions = 0        # zero disables the feature
                    # (change requires restart)
# Caution: it is not advisable to set max_prepared_transactions nonzero unless
# you actively intend to use prepared transactions.

# 可以在会话中设置，如果有大量JOIN，聚合操作，并且期望使用hash agg或hash join。(排序，HASH都会用到work_mem)    
# 可以设大一些，但是不建议大于1/4内存除以最大连接数        
# (一条QUERY中可以使用多倍WORK_MEM，与执行计划中的NODE有关)  
#work_mem = 4MB                # min 64kB

#创建索引时使用的内存空间
#maintenance_work_mem 公式： min( 8G, (主机内存*1/8)/max_parallel_maintenance_workers )        
#maintenance_work_mem = 64MB        # min 1MB

#默认值为 -1，表示转而使用 maintenance_work_mem的值。
#autovacuum_work_mem 公式： min( 8G, (主机内存*1/8)/autovacuum_max_workers )
#autovacuum_work_mem = -1        # min 1MB, or -1 to use maintenance_work_mem

#指定服务器的执行堆栈的最大安全深度。
#max_stack_depth = 2MB            # min 100kB

#指定服务器应该用于保存PostgreSQL的共享缓冲区和其他共享数据的主共享内存区域的共享内存实现。
#shared_memory_type = mmap        # the default is the first option
                    # supported by the operating system:
                    #   mmap
                    #   sysv
                    #   windows
                    # (change requires restart)

#指定服务器应该使用的动态共享内存实现。
#dynamic_shared_memory_type = posix    # the default is the first option
                    # supported by the operating system:
                    #   posix
                    #   sysv
                    #   windows
                    #   mmap
                    # (change requires restart)

# - Disk -

# 如果需要限制临时文件使用量，可以设置。      
# 例如, 防止有异常的递归调用，无限使用临时文件。
#temp_file_limit = -1            # limits per-process temp file space
                    # in kB, or -1 for no limit

# - Kernel Resources -

## 如果你的数据库有非常多小文件（比如有几十万以上的表，还有索引等，并且每张表都会被访问到时），      
# 建议FD可以设多一些，避免进程需要打开关闭文件。      
## 但是不要大于前面章节系统设置的ulimit -n(open files) 
#max_files_per_process = 1000        # min 25
                    # (change requires restart)

# - Cost-Based Vacuum Delay -

# 如果你的系统IO非常好，则可以关闭vacuum delay,避免因为垃圾回收任务周期长导致的膨胀。      
#vacuum_cost_delay = 0            # 0-100 milliseconds (0 disables)

#清理一个在共享缓存中找到的缓冲区的估计代价
#vacuum_cost_page_hit = 1        # 0-10000 credits

#清理一个必须从磁盘上读取的缓冲区的代价
#vacuum_cost_page_miss = 10        # 0-10000 credits

#当清理修改一个之前干净的块时需要花费的估计代价
#vacuum_cost_page_dirty = 20        # 0-10000 credits

#io很好，CPU核数很多的机器，设大一些。如果设置了vacuum_cost_delay = 0 ，则这个不需要配置
#vacuum_cost_limit = 200        # 1-10000 credits

# - Background Writer -

#指定后台写入器活动轮次之间的延迟。
#bgwriter_delay = 200ms            # 10-10000ms between rounds

#在每个轮次中，不超过这么多个缓冲区将被后台写入器写出。
#bgwriter_lru_maxpages = 100        # max buffers written/round, 0 disables

#每一轮次要写的脏缓冲区的数目基于最近几个轮次中服务器进程需要的新缓冲区的数 目
#bgwriter_lru_multiplier = 2.0        # 0-10.0 multiplier on buffers scanned/round

#不管何时后端写入器写入了超过bgwriter_flush_after字节，尝试强制 OS 把这些写发送到底层存储上。
#bgwriter_flush_after = 512kB        # measured in pages, 0 disables

# - Asynchronous Behavior -

#设置PostgreSQL可以同时被执行的并发磁盘 I/O 操作的数量
#effective_io_concurrency = 1        # 1-1000; 0 disables prefetching

# wal sender, user 动态fork的process, parallel worker等都算作 worker process, 所以你需要设置足够大. 
#max_worker_processes = 8        # (change requires restart)

#  如果需要使用并行创建索引，设置为大于1 ，不建议超过 主机cores-4      
# max_parallel_maintenance_workers 公式： min( max(2, CPU核数/2) , 16 )  
#max_parallel_maintenance_workers = 2    # taken from max_parallel_workers

#  如果需要使用并行查询，设置为大于1 ，不建议超过 主机cores-4
# max_parallel_workers_per_gather 公式： min( max(2, CPU核数-4) , 24 )           
#max_parallel_workers_per_gather = 2    # taken from max_parallel_workers

# leader 是否与work process一起参与并行计算，如果ON，则并行度会默认+1。      
#parallel_leader_participation = on

#  如果需要使用并行查询，设置为大于1 ，不建议超过 主机cores-2      
#  必须小于 max_worker_processes       
# max_parallel_workers 公式： max(2, CPU核数-4)  
#max_parallel_workers = 8        # maximum number of max_worker_processes that
                    # can be used in parallel operations

# 是否启用snapshot too old技术，避免长事务导致的膨胀    
# 会导致性能一定的下降，约8%                     
#old_snapshot_threshold = -1        # 1min-60d; -1 disables; 0 is immediate
                    # (change requires restart)

#只要一个后端写入了超过backend_flush_after字节， 就会尝试强制 OS 把这些写发送 到底层存储。
#backend_flush_after = 0        # measured in pages, 0 disables


#------------------------------------------------------------------------------
# WRITE-AHEAD LOG
#------------------------------------------------------------------------------

# - Settings -

# 需要流复制物理备库、归档、时间点恢复时，设置为replica，需要逻辑订阅或逻辑备库则设置为logical
#wal_level = replica            # minimal, replica, or logical
                    # (change requires restart)

#如果打开这个参数，PostgreSQL服务器将尝试确保更新被物理地写入到磁盘
#fsync = on                # flush data to disk for crash safety
                    # (turning this off can cause
                    # unrecoverable data corruption)

# 如果双节点，设置为ON，如果是多副本，同步模式，建议设置为remote_write。       
# 如果磁盘性能很差，并且是OLTP业务。可以考虑设置为off降低COMMIT的RT，提高吞吐(设置为OFF时，可能丢失部分XLOG RECORD)
#synchronous_commit = on        # synchronization level;
                    # off, local, remote_write, remote_apply, or on

# 建议使用pg_test_fsync测试后，决定用哪个最快。通常LINUX下open_datasync比较快。                          
#wal_sync_method = fsync        # the default is the first option
                    # supported by the operating system:
                    #   open_datasync
                    #   fdatasync (default on Linux)
                    #   fsync
                    #   fsync_writethrough
                    #   open_sync

# 如果文件系统支持COW例如ZFS，则建议设置为OFF。     
# 如果文件系统可以保证datafile block size的原子写，在文件系统与IO系统对齐后也可以设置为OFF。      
# 如果底层存储能保证IO的原子写，也可以设置为OFF。 
#full_page_writes = on            # recover from partial page writes

# 当写FULL PAGE WRITE的io是瓶颈时建议开启      
#wal_compression = off            # enable compression of full-page writes

# 如果要使用pg_rewind，flashback 时间线，需要打开这个功能    
#wal_log_hints = off            # also do full page writes of non-critical updates
                    # (change requires restart)

#wal_init_zero = on            # zero-fill new WAL files
#wal_recycle = on            # recycle WAL files

# 建议 min( WAL segment size(默认16MB) , shared_buffers/32 ) 
#wal_buffers = -1            # min 32kB, -1 sets based on shared_buffers
                    # (change requires restart)

# 如果设置了synchronous_commit = off，建议设置wal_writer_delay                     
#wal_writer_delay = 200ms        # 1-10000 milliseconds
#wal_writer_flush_after = 1MB        # measured in pages, 0 disables

# 如果synchronous_commit=on, 并且已知业务系统为高并发，对数据库有写操作的小事务，则可以设置commit_delay来实现分组提交，合并WAL FSYNCIO 。      
# 分组提交
#commit_delay = 0            # range 0-100000, in microseconds

# 同时处于提交状态的事务数超过commit_siblings时，使用分组提交      
#commit_siblings = 5            # range 1-1000

# - Checkpoints -

#  不建议频繁做检查点，否则XLOG会产生很多的FULL PAGE WRITE(when full_page_writes=on)。      
#checkpoint_timeout = 5min        # range 30s-1d

# 建议等于SHARED BUFFER，或2倍。      
# 同时需要考虑崩溃恢复时间, 越大，检查点可能拉越长导致崩溃恢复耗时越长。但是越小，开启FPW时，WAL日志写入量又越大。 建议采用COW文件系统，关闭FPW。
# max_wal_size 公式： # min(shared_buffers*2 ,   用户存储空间/10)   
max_wal_size = 1GB

# 建议是SHARED BUFFER的2分之一      
# min_wal_size 公式： # min(shared_buffers/2  , 用户存储空间/10) 
min_wal_size = 80MB

# 硬盘好(nvme ssd)的情况下，值越小可以让检查点快速结束，恢复时也可以快速达到一致状态。否则建议0.5~0.9       
# 如果有hot standby作为HA节点，这个值也可以设置为0.5~0.9   避免写高峰时CHECKPOINT对写带来的冲击。 
#checkpoint_completion_target = 0.5    # checkpoint target duration, 0.0 - 1.0

# IO很好的机器，不需要考虑平滑调度, 否则建议128~256kB 
#checkpoint_flush_after = 256kB        # measured in pages, 0 disables
#checkpoint_warning = 30s        # 0 disables

# - Archiving -

# 建议默认打开，因为修改它需要重启实例      
# 打开后，一个WAL文件写满后，会在pg_wal/archive_status目录中创建xxxxxx.ready的文件，归档命令archive_command正常结束后，会清除这个状态文件。
#archive_mode = off        # enables archiving; off, on, or always
                # (change requires restart)

#  后期再修改，如  'test ! -f /disk1/digoal/arch/%f && cp %p /disk1/digoal/arch/%f'                
#archive_command = ''        # command to use to archive a logfile segment
                # placeholders: %p = path of file to archive
                #               %f = file name only
                # e.g. 'test ! -f /mnt/server/archivedir/%f && cp %p /mnt/server/archivedir/%f'                
#archive_timeout = 0        # force a logfile segment switch after this
                # number of seconds; 0 disables

# - Archive Recovery -

# These are only used in recovery mode.

#执行本地shell命令以检索WAL文件系列的归档段
#restore_command = ''        # command to use to restore an archived logfile segment
                # placeholders: %p = path of file to restore
                #               %f = file name only
                # e.g. 'cp /mnt/server/archivedir/%f %p'
                # (change requires restart)
#archive_cleanup_command = ''    # command to execute at every restartpoint
#recovery_end_command = ''    # command to execute at completion of recovery

# - Recovery Target -

# Set these only when performing a targeted recovery.

#recovery_target = ''        # 'immediate' to end recovery as soon as a
                                # consistent state is reached
                # (change requires restart)
#recovery_target_name = ''    # the named restore point to which recovery will proceed
                # (change requires restart)
#recovery_target_time = ''    # the time stamp up to which recovery will proceed
                # (change requires restart)
#recovery_target_xid = ''    # the transaction ID up to which recovery will proceed
                # (change requires restart)
#recovery_target_lsn = ''    # the WAL LSN up to which recovery will proceed
                # (change requires restart)
#recovery_target_inclusive = on # Specifies whether to stop:
                # just after the specified recovery target (on)
                # just before the recovery target (off)
                # (change requires restart)
#recovery_target_timeline = 'latest'    # 'current', 'latest', or timeline ID
                # (change requires restart)
#recovery_target_action = 'pause'    # 'pause', 'promote', 'shutdown'
                # (change requires restart)


#------------------------------------------------------------------------------
# REPLICATION
#------------------------------------------------------------------------------

# - Sending Servers -

# Set these on the master and on any standby that will send replication data.


# 同时允许几个流复制协议的连接，根据实际需求设定 ，可以设置一个默认值例如64     
#max_wal_senders = 10        # max number of walsender processes
                # (change requires restart)

# 根据实际情况设置保留WAL的数量，主要是防止过早的清除WAL，导致备库因为主库的WAL清除而中断。根据实际情况设定。                      
#wal_keep_segments = 0        # in logfile segments; 0 disables
#wal_sender_timeout = 60s    # in milliseconds; 0 disables

# 根据实际情况设置需要创建多少replication slot      
# 使用slot，可以保证流复制下游没有接收的WAL会在当前节点永久保留。所以必须留意下游的接收情况，否则可能导致WAL爆仓      
# 建议大于等于max_wal_senders      
# max_replication_slots 公式： max_replication_slots=max_wal_senders 
#max_replication_slots = 10    # max number of replication slots
                # (change requires restart)
#track_commit_timestamp = off    # collect timestamp of transaction commit
                # (change requires restart)

# - Master Server -

# These settings are ignored on a standby server.

#synchronous_standby_names = ''    # standby servers that provide sync rep
                # method to choose sync standbys, number of sync standbys,
                # and comma-separated list of application_name
                # from standby(s); '*' = all
#vacuum_defer_cleanup_age = 0    # number of xacts by which cleanup is delayed

# - Standby Servers -

# These settings are ignored on a master server.

#primary_conninfo = ''            # connection string to sending server
                    # (change requires restart)
#primary_slot_name = ''            # replication slot on sending server
                    # (change requires restart)
#promote_trigger_file = ''        # file name whose presence ends recovery
#hot_standby = on            # "off" disallows queries during recovery
                    # (change requires restart)
#max_standby_archive_delay = 30s    # max delay before canceling queries
                    # when reading WAL from archive;
                    # -1 allows indefinite delay
#max_standby_streaming_delay = 30s    # max delay before canceling queries
                    # when reading streaming WAL;
                    # -1 allows indefinite delay
#wal_receiver_status_interval = 10s    # send replies at least this often
                    # 0 disables
#hot_standby_feedback = off        # send info from standby to prevent
                    # query conflicts
#wal_receiver_timeout = 60s        # time that receiver waits for
                    # communication from master
                    # in milliseconds; 0 disables
#wal_retrieve_retry_interval = 5s    # time to wait before retrying to
                    # retrieve WAL after a failed attempt
#recovery_min_apply_delay = 0        # minimum delay for applying changes during recovery

# - Subscribers -

# These settings are ignored on a publisher.

#max_logical_replication_workers = 4    # taken from max_worker_processes
                    # (change requires restart)
#max_sync_workers_per_subscription = 2    # taken from max_logical_replication_workers


#------------------------------------------------------------------------------
# QUERY TUNING
#------------------------------------------------------------------------------

# - Planner Method Configuration -

#enable_bitmapscan = on
#enable_hashagg = on
#enable_hashjoin = on
#enable_indexscan = on
#enable_indexonlyscan = on
#enable_material = on
#enable_mergejoin = on
#enable_nestloop = on
#enable_parallel_append = on
#enable_seqscan = on
#enable_sort = on
#enable_tidscan = on
#enable_partitionwise_join = off
#enable_partitionwise_aggregate = off
#enable_parallel_hash = on
#enable_partition_pruning = on

# - Planner Cost Constants -

#seq_page_cost = 1.0            # measured on an arbitrary scale
#random_page_cost = 4.0            # same scale as above
#cpu_tuple_cost = 0.01            # same scale as above
#cpu_index_tuple_cost = 0.005        # same scale as above
#cpu_operator_cost = 0.0025        # same scale as above
#parallel_tuple_cost = 0.1        # same scale as above
#parallel_setup_cost = 1000.0    # same scale as above

#jit_above_cost = 100000        # perform JIT compilation if available
                    # and query more expensive than this;
                    # -1 disables
#jit_inline_above_cost = 500000        # inline small functions if query is
                    # more expensive than this; -1 disables
#jit_optimize_above_cost = 500000    # use expensive JIT optimizations if
                    # query is more expensive than this;
                    # -1 disables

#min_parallel_table_scan_size = 8MB
#min_parallel_index_scan_size = 512kB

# 扣掉会话连接RSS，shared buffer, autovacuum worker, 剩下的都是OS可用的CACHE。      
# effective_cache_size 公式： 主机内存*0.75  
#effective_cache_size = 4GB

# - Genetic Query Optimizer -

#geqo = on
#geqo_threshold = 12
#geqo_effort = 5            # range 1-10
#geqo_pool_size = 0            # selects default based on effort
#geqo_generations = 0            # selects default based on effort
#geqo_selection_bias = 2.0        # range 1.5-2.0
#geqo_seed = 0.0            # range 0.0-1.0

# - Other Planner Options -

#default_statistics_target = 100    # range 1-10000
#constraint_exclusion = partition    # on, off, or partition
#cursor_tuple_fraction = 0.1        # range 0.0-1.0
#from_collapse_limit = 8
#join_collapse_limit = 8        # 1 disables collapsing of explicit
                    # JOIN clauses
#force_parallel_mode = off
#jit = on                # allow JIT compilation
#plan_cache_mode = auto            # auto, force_generic_plan or
                    # force_custom_plan


#------------------------------------------------------------------------------
# REPORTING AND LOGGING
#------------------------------------------------------------------------------

# - Where to Log -

#log_destination = 'stderr'        # Valid values are combinations of
                    # stderr, csvlog, syslog, and eventlog,
                    # depending on platform.  csvlog
                    # requires logging_collector to be on.

# This is used when logging to stderr:
#logging_collector = off        # Enable capturing of stderr and csvlog
                    # into log files. Required to be on for
                    # csvlogs.
                    # (change requires restart)

# These are only used if logging_collector is on:
#log_directory = 'log'            # directory where log files are written,
                    # can be absolute or relative to PGDATA

# 日志保留一天，每个小时一个文件取决于log_rotation_age    每小时切换一下                    
#log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'    # log file name pattern,
                    # can include strftime() escapes
#log_file_mode = 0600            # creation mode for log files,
                    # begin with 0 to use octal notation
#log_truncate_on_rotation = off        # If on, an existing log file with the
                    # same name as the new log file will be
                    # truncated rather than appended to.
                    # But such truncation only occurs on
                    # time-driven rotation, not on restarts
                    # or size-driven rotation.  Default is
                    # off, meaning append to existing files
                    # in all cases.
#log_rotation_age = 1d            # Automatic rotation of logfiles will
                    # happen after that time.  0 disables.
#log_rotation_size = 10MB        # Automatic rotation of logfiles will
                    # happen after that much log output.
                    # 0 disables.

# These are relevant when logging to syslog:
#syslog_facility = 'LOCAL0'
#syslog_ident = 'postgres'
#syslog_sequence_numbers = on
#syslog_split_messages = on

# This is only relevant when logging to eventlog (win32):
# (change requires restart)
#event_source = 'PostgreSQL'

# - When to Log -

#log_min_messages = warning        # values in order of decreasing detail:
                    #   debug5
                    #   debug4
                    #   debug3
                    #   debug2
                    #   debug1
                    #   info
                    #   notice
                    #   warning
                    #   error
                    #   log
                    #   fatal
                    #   panic

#log_min_error_statement = error    # values in order of decreasing detail:
                    #   debug5
                    #   debug4
                    #   debug3
                    #   debug2
                    #   debug1
                    #   info
                    #   notice
                    #   warning
                    #   error
                    #   log
                    #   fatal
                    #   panic (effectively off)

#log_min_duration_statement = -1    # logs statements and their durations
                    # according to log_statement_sample_rate. -1 is disabled,
                    # 0 logs all statements, > 0 logs only statements running
                    # at least this number of milliseconds.

#log_statement_sample_rate = 1.0    # Fraction of logged statements exceeding
                    # log_min_duration_statement to be logged.
                    # 1.0 logs all such statements, 0.0 never logs.

#log_transaction_sample_rate = 0.0    # Fraction of transactions whose statements
                    # are logged regardless of their duration. 1.0 logs all
                    # statements from all transactions, 0.0 never logs.

# - What to Log -

#debug_print_parse = off
#debug_print_rewritten = off
#debug_print_plan = off
#debug_pretty_print = on

# 记录检查点的详细统计信息    
#log_checkpoints = off

# 如果业务是短连接，建议设置为OFF，否则建议设置为ON      
#log_connections = off

# 如果业务是短连接，建议设置为OFF，否则建议设置为ON      
#log_disconnections = off
#log_duration = off

# 记录错误代码的代码位置，是什么代码输出的日志，更好的跟踪问题    
#log_error_verbosity = default        # terse, default, or verbose messages
#log_hostname = off
#log_line_prefix = '%m [%p] '        # special values:
                    #   %a = application name
                    #   %u = user name
                    #   %d = database name
                    #   %r = remote host and port
                    #   %h = remote host
                    #   %p = process ID
                    #   %t = timestamp without milliseconds
                    #   %m = timestamp with milliseconds
                    #   %n = timestamp with milliseconds (as a Unix epoch)
                    #   %i = command tag
                    #   %e = SQL state
                    #   %c = session ID
                    #   %l = session line number
                    #   %s = session start timestamp
                    #   %v = virtual transaction ID
                    #   %x = transaction ID (0 if none)
                    #   %q = stop here in non-session
                    #        processes
                    #   %% = '%'
                    # e.g. '<%u%%%d> '

# 是否打印锁等待事件                     
#log_lock_waits = off            # log lock waits >= deadlock_timeout

# 如果需要审计SQL，则可以设置为all      
#log_statement = 'none'            # none, ddl, mod, all

#log_replication_commands = off

# 当使用的临时文件超过多大时，打印到日志中，跟踪大SQL
#log_temp_files = -1            # log temporary files equal or larger
                    # than the specified size in kilobytes;
                    # -1 disables, 0 logs all temp files
log_timezone = 'PRC'

#------------------------------------------------------------------------------
# PROCESS TITLE
#------------------------------------------------------------------------------

#cluster_name = ''            # added to process titles if nonempty
                    # (change requires restart)
#update_process_title = on


#------------------------------------------------------------------------------
# STATISTICS
#------------------------------------------------------------------------------

# - Query and Index Statistics Collector -

#track_activities = on
#track_counts = on

# 跟踪IO耗时会带来一定的性能影响，默认是关闭的      
# 如果需要统计IO的时间开销，设置为ON      
# 建议用pg_test_timing测试一下获取时间的开销，如果开销很大，建议关闭这个时间跟踪。
#track_io_timing = off

# 是否需要跟踪函数被调用的次数，耗时 
#track_functions = none            # none, pl, all

# 单条被跟踪的QUERY最多能存储多少字节，如果有超长SQL，则日志中被截断。 根据需要设置
#track_activity_query_size = 1024    # (change requires restart)

# 相对路径（$PGDATA）或绝对路径。用于存储统计信息的临时目录。可以设置为ram based directory，提高性能 
#stats_temp_directory = 'pg_stat_tmp'


# - Monitoring -

#log_parser_stats = off
#log_planner_stats = off
#log_executor_stats = off
#log_statement_stats = off


#------------------------------------------------------------------------------
# AUTOVACUUM
#------------------------------------------------------------------------------

# 打开自动垃圾回收    
#autovacuum = on            # Enable autovacuum subprocess?  'on'
                    # requires track_counts to also be on.
#log_autovacuum_min_duration = -1    # -1 disables, 0 logs all actions and
                    # their durations, > 0 logs only
                    # actions running at least this number
                    # of milliseconds.

# CPU核多，并且IO好的情况下，可多点，但是注意最多可能消耗这么多内存：       
# autovacuum_max_workers * autovacuum mem(autovacuum_work_mem)，      
# 会消耗较多内存，所以内存也要有基础。           
# 当DELETE/UPDATE非常频繁时，建议设置多一点，防止膨胀严重           
# autovacuum_max_workers 公式： max(min( 8 , CPU核数/2 ) , 5)                    
#autovacuum_max_workers = 3        # max number of autovacuum subprocesses
                    # (change requires restart)

# 建议不要太高频率，否则会因为vacuum产生较多的XLOG。或者在某些垃圾回收不掉的情况下(例如长事务、feed back on，等)，导致一直触发vacuum，CPU和IO都会升高                    
#autovacuum_naptime = 1min        # time between autovacuum runs
#autovacuum_vacuum_threshold = 50    # min number of row updates before
                    # vacuum
#autovacuum_analyze_threshold = 50    # min number of row updates before
                    # analyze
#autovacuum_vacuum_scale_factor = 0.2    # fraction of table size before vacuum
#autovacuum_analyze_scale_factor = 0.1    # fraction of table size before analyze
#autovacuum_freeze_max_age = 200000000    # maximum XID age before forced vacuum
                    # (change requires restart)
#autovacuum_multixact_freeze_max_age = 400000000    # maximum multixact age
                    # before forced vacuum
                    # (change requires restart)

# 如果数据库UPDATE非常频繁，建议设置为0。并且建议使用SSD                          
#autovacuum_vacuum_cost_delay = 2ms    # default vacuum cost delay for
                    # autovacuum, in milliseconds;
                    # -1 means use vacuum_cost_delay
#autovacuum_vacuum_cost_limit = -1    # default vacuum cost limit for
                    # autovacuum, -1 means use
                    # vacuum_cost_limit


#------------------------------------------------------------------------------
# CLIENT CONNECTION DEFAULTS
#------------------------------------------------------------------------------

# - Statement Behavior -

#client_min_messages = notice        # values in order of decreasing detail:
                    #   debug5
                    #   debug4
                    #   debug3
                    #   debug2
                    #   debug1
                    #   log
                    #   notice
                    #   warning
                    #   error
#search_path = '"$user", public'    # schema names
#row_security = on
#default_tablespace = ''        # a tablespace name, '' uses the default

# 临时表的表空间，可以设置多个，轮询使用。    
# 临时表的表空间，建议为SSD目录。速度快。
#temp_tablespaces = ''            # a list of tablespace names, '' uses
                    # only default tablespace
#check_function_bodies = on
#default_transaction_isolation = 'read committed'
#default_transaction_read_only = off
#default_transaction_deferrable = off
#session_replication_role = 'origin'

# 可以用来防止雪崩，但是不建议全局设置 
#statement_timeout = 0            # in milliseconds, 0 is disabled

# 执行DDL时，建议加上超时      
# 可以用来防止雪崩 
#lock_timeout = 0            # in milliseconds, 0 is disabled

# 空闲中事务自动清理，根据业务实际情况设置
#idle_in_transaction_session_timeout = 0    # in milliseconds, 0 is disabled
#vacuum_freeze_min_age = 50000000
#vacuum_freeze_table_age = 150000000
#vacuum_multixact_freeze_min_age = 5000000
#vacuum_multixact_freeze_table_age = 150000000
#vacuum_cleanup_index_scale_factor = 0.1    # fraction of total number of tuples
                        # before index cleanup, 0 always performs
                        # index cleanup
#bytea_output = 'hex'            # hex, escape
#xmlbinary = 'base64'
#xmloption = 'content'

#限制GIN扫描的返回结果集大小，在想限制超多匹配的返回时可以设置
#gin_fuzzy_search_limit = 0

# GIN索引pending list的大小      
#gin_pending_list_limit = 4MB

# - Locale and Formatting -

datestyle = 'iso, mdy'
#intervalstyle = 'postgres'
timezone = 'PRC'
#timezone_abbreviations = 'Default'     # Select the set of available time zone
                    # abbreviations.  Currently, there are
                    #   Default
                    #   Australia (historical usage)
                    #   India
                    # You can create your own file in
                    # share/timezonesets/.

# 浮点精度扩展值                     
#extra_float_digits = 1            # min -15, max 3; any value >0 actually
                    # selects precise output mode
#client_encoding = sql_ascii        # actually, defaults to database
                    # encoding

# These settings are initialized by initdb, but they can be changed.
lc_messages = 'C'            # locale for system error message
                    # strings
lc_monetary = 'C'            # locale for monetary formatting
lc_numeric = 'C'            # locale for number formatting
lc_time = 'C'                # locale for time formatting

# default configuration for text search
default_text_search_config = 'pg_catalog.english'

# - Shared Library Preloading -

# 需要加载什么LIB，预先加载，对于经常访问的库也建议预加载，例如postgis 
#shared_preload_libraries = ''    # (change requires restart)
#local_preload_libraries = ''
#session_preload_libraries = ''
#jit_provider = 'llvmjit'        # JIT library to use

# - Other Defaults -

#dynamic_library_path = '$libdir'


#------------------------------------------------------------------------------
# LOCK MANAGEMENT
#------------------------------------------------------------------------------

#deadlock_timeout = 1s
#max_locks_per_transaction = 64        # min 10
                    # (change requires restart)
#max_pred_locks_per_transaction = 64    # min 10
                    # (change requires restart)
#max_pred_locks_per_relation = -2    # negative values mean
                    # (max_pred_locks_per_transaction
                    #  / -max_pred_locks_per_relation) - 1
#max_pred_locks_per_page = 2            # min 0


#------------------------------------------------------------------------------
# VERSION AND PLATFORM COMPATIBILITY
#------------------------------------------------------------------------------

# - Previous PostgreSQL Versions -

#array_nulls = on
#backslash_quote = safe_encoding    # on, off, or safe_encoding
#escape_string_warning = on
#lo_compat_privileges = off
#operator_precedence_warning = off
#quote_all_identifiers = off
#standard_conforming_strings = on
#synchronize_seqscans = on

# - Other Platforms and Clients -

#transform_null_equals = off


#------------------------------------------------------------------------------
# ERROR HANDLING
#------------------------------------------------------------------------------

#exit_on_error = off            # terminate session on any error?
#restart_after_crash = on        # reinitialize after backend crash?
#data_sync_retry = off            # retry or panic on failure to fsync
                    # data?
                    # (change requires restart)


#------------------------------------------------------------------------------
# CONFIG FILE INCLUDES
#------------------------------------------------------------------------------

# These options allow settings to be loaded from files other than the
# default postgresql.conf.

#include_dir = ''            # include files ending in '.conf' from
                    # a directory, e.g., 'conf.d'
#include_if_exists = ''            # include file only if it exists
#include = ''                # include file


#------------------------------------------------------------------------------
# CUSTOMIZED OPTIONS
#------------------------------------------------------------------------------

# Add settings for extensions here
```


##### 按需调整的参数 简易计算规则
```editorconfig
max_connections=            # 规格内存(GB)*1000*(1/4)/10   +   superuser_reserved_connections  
shared_buffers=             # IF use hugepage: 规格内存*(1/4)   ELSE: min(32GB, 规格内存*(1/4))    
max_prepared_transactions      # max_prepared_transactions=max_connections   
work_mem        # max(min(规格内存/4096, 64MB), 4MB)   
maintenance_work_mem          # min( 8G, (主机内存*1/8)/max_parallel_maintenance_workers )    
autovacuum_work_mem            # min( 8G, (规格内存*1/8)/autovacuum_max_workers )     
max_parallel_maintenance_workers     # min( max(2, CPU核数/2) , 16 )   
max_parallel_workers_per_gather      # min( max(2, CPU核数-4) , 24 )     
max_parallel_workers       # min(max(2, CPU核数-4) ,32)   
max_wal_size            # min(shared_buffers*2,   用户存储空间/10)    
min_wal_size             # min(shared_buffers/2  , 用户存储空间/10)  
max_sync_workers_per_subscription   # min ( 32 , max(2, CPU核数-4) )    
effective_cache_size          # 规格内存*0.75  
autovacuum_max_workers        # max(min( 8 , CPU核数/2 ) , 5)   
synchronous_commit = off      # 当高并发写事务遇到了WAL瓶颈时，优先考虑提高磁盘IOPS能力，如果需要立即提升性能可以使用异步
```


#### pg_hba.conf
> 参考文档: https://www.postgresql.org/docs/current/auth-pg-hba-conf.html

##### 配置格式 

```editorconfig
# TYPE        DATABASE        USER            ADDRESS                 METHOD
# 访问方式类型  数据库名         用户名          访问地址范围              验证方式 
```

##### 配置属性范围说明 
* TYPE

|名称|备注|
|:--|:---|
|local|使用unix套接字连接|
|host|通过tcp/ip连接 ssl或者非ssl都支持|
|hostssl|使用tcp/ip+ssl 服务器本身要支持 --with-openssl|
|hostnossl|只通过tcp/ip连接 不使用ssl加密的连接|
|hostgssenc|使用tcp/ip+gssapi加密 服务器本身需要支持 gssapi|
|hostgssenc|只通过tcp/ip 不使用gssapi加密的连接|

* DATABASE

|名称|备注|
|:--|:---|
|all| 所有实例   但是不包括replication|
|sameuser|-|
|samerole|-|
|replication| 流复制实例|
|以 ,分割的数据库名|

* USER
|名称|备注|
|:--|:---|
|all|所有用户|
|以 ,分割的用户名、用户组|
|或者使用@为前缀的文件名 文件内部使用,分割的用户、用户组|


* ADDRESS
符合ip地址范围规范标准格式

|名称|备注|
|:--|:---|
|0.0.0.0/0|任意ipv4地址|


* METHOD 

|名称|备注|
|:--|:---|
|trust|无条件允许连接。此方法允许任何可以连接到PostgreSQL数据库服务器的用户以他们想要的任何PostgreSQL用户身份登录，而无需密码或任何其他身份验证。|
|reject|无条件拒绝连接。这对于"筛选掉"组中的某些主机很有用，例如，线路可能会阻止特定主机连接，而后面的线路允许特定网络中的剩余主机进行连接|
|md5|执行 SCRAM-SHA-256 或 MD5 身份验证以验证用户的密码。|
|password|要求客户端提供未加密的身份验证密码。由于密码通过网络以明文形式发送，因此不应在不受信任的网络上使用。|
|scram-sha-256|执行 SCRAM-SHA-256 身份验证以验证用户的密码。|
|gss|使用 GSSAPI 对用户进行身份验证。这仅适用于 TCP/IP 连接。|
|sspi|使用 SSPI 对用户进行身份验证。这仅在 Windows 上可用。|
|ident|通过联系客户端上的标识服务器来获取客户端的操作系统用户名，并检查其是否与请求的数据库用户名匹配。标识身份验证只能在 TCP/IP 连接上使用。为本地连接指定时，将改为使用对等身份验证。|
|peer|从操作系统获取客户端的操作系统用户名，并检查其是否与请求的数据库用户名匹配。这仅适用于本地连接。|
|pam|使用操作系统提供的可插入身份验证模块 （PAM） 服务进行身份验证。|
|ldap|使用LDAP服务器进行身份验证。|
|radius|使用 RADIUS 服务器进行身份验证|
|cert|使用 SSL 客户端证书进行身份验证。|
|bsd|使用操作系统提供的 BSD 身份验证服务进行身份验证|

##### 常用配置模版
允许tcp/ip方式访问的 所有数据库 所有用户 任意地址 通过密码方式访问 
```editorconfig
host   all  all   0.0.0.0/0   md5 
host   all  all   0.0.0.0/0   scram-sha-256 
```

#### pg_ident.conf

```editorconfig
# MAP_NAME               SYSTEM_USER_NAME PG_USER_NAME
# 映射名称(pg_hba.conf使用) 系统用户名         数据库用户名
```
##### 示例  

配置映射关系
```editorconfig
# key为mingmap  系统用户为ming  映射为数据库用户postgres上 
mingmap     ming     postgres
```
配置pg_hba.conf   map为 pg_ident.conf中的 mingmap 
```editorconfig
host   all  all  0.0.0.0/0  ident map=mingmap
```

#### 总结
postgres 本身的配置是比较齐全的 大多数情况下 需要根据具体的运行情况进行调整优化   
数据库本身的系统配置 postgresql.conf  
权限和访问控制 主要是pg_hba.conf    
不过pg_ident.conf配合pg_hba.conf 可以将不同的用户映射进来 防止用户帐号泄露之类的


