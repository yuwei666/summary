# PXC

pxc集群的存活数量需要大于总节点数量的一半才可以正常工作。[参考](https://blog.csdn.net/W1324926684/article/details/130512670)

优点：

+ 集群高可用，真正的多节点读写
+ 改善了传统binlog到replylog中存在的延迟问题。基本做到实时同步。
+ 新节点自动部署，无需太多操作。
+ 故障无缝转移。

缺点：

+ 新加入节点开销大，需要把数据完全复制一次。SST协议开销大。
+ 任何事务都要全局验证，MGR是大多数就行。性能取结予最差的节点。
+ 为了保证数据一致性，在并写的时候，锁冲突会比较严重。
+ 只支持innodb引擎。
+ 没有表锁，只能锁集群。



## pxc:8.0

3台服务器 172.16.1.105/106/107，部署Msql的PXC集群模式。镜像使用percona-xtradb-cluster:8.0[参考](https://blog.csdn.net/dirful/article/details/132233312)

### 准备工作

以下内容在三个服务器都要执行

```bash
su -

# PXC容器用户权限不够，挂载目录必须赋予权限777（所有人可读写）
mkdir -m 777 -p /data/mysql-pxc/{conf,cert,logs,data,socket}
# 如果已存在目录，需要更改权限
chmod 777 /data/mysql-pxc/{conf,cert,logs,data,socket}
# 改所属组（选做，好像不需要）
chown -R 1001:1001 /data/mysql-pxc/{conf,cert,logs,data,socket}

# 拉取官方镜像，更名
docker pull docker.1ms.run/percona/percona-xtradb-cluster:8.0
docker tag docker.1ms.run/percona/percona-xtradb-cluster:8.0 pxc:8.0
docker rmi docker.1ms.run/percona/percona-xtradb-cluster:8.0
```



如果部署不同服务器上，需要创建overlay 网络，这里我以105为主。

```bash
# 开放所需端口(swarm使用)
sudo ufw allow from 172.16.1.0/24 to any port 2377 proto tcp
sudo ufw allow from 172.16.1.0/24 to any port 7946 proto tcp
sudo ufw allow from 172.16.1.0/24 to any port 7946 proto udp
sudo ufw allow from 172.16.1.0/24 to any port 4789 proto udp
sudo ufw status verbose

# 把当前主机初始化为 Swarm 集群的 manager，成功后会有一个带token的例子，供swarm join用
docker swarm init
# 创建一个名为 swarm_mysql 的 overlay 网络 --attachable：允许独立容器（docker run 启动的）直接加入该网络
docker network create -d overlay --attachable swarm_mysql
# 加入虚拟网络（其他两台服务器）
docker swarm join --token XXXX 172.16.1.105:2377
# 在主服务器查看此网络的节点列表
docker node ls
```

除了上述准备工作外，推荐将服务器host名称更改，便于区分。如：mysql_node_105，mysql_node_106，mysql_node_107



### 证书

在任一台服务器执行，挂载目录下就会生成证书文件。

```bash
# --rm：容器退出时自动删除该容器
docker run --name pxc-cert --rm -v /data/mysql-pxc/cert:/cert pxc:8.0 mysql_ssl_rsa_setup -d /cert
```

然后将`/data/mysql-pxc/cert` 同步到另外两台服务器的相同目录下。



### 集群部署

```bash
# 107
docker run  -it \
--privileged \
--restart always \
--name=mysql-pxc8.0-107 \
--net=swarm_mysql \
-p 3306:3306 \
-e TZ=Asia/Shanghai \
-e CLUSTER_NAME=PXC \
-e MYSQL_ROOT_PASSWORD=P@ssword \
-e XTRABACKUP_PASSWORD=P@ssword \
-v /data/mysql-pxc/data:/var/lib/mysql \
-v /data/mysql-pxc/conf:/etc/percona-xtradb-cluster.conf.d \
-v /data/mysql-pxc/cert:/cert \
-v /data/mysql-pxc/logs:/var/log/mysql \
pxc:8.0

# 106 这里多挂载了一个socket目录，因为要使用这个容器做备份。
# -v /data/pxc/pxc_socket:/tmp 映射集群mysql.sock到宿主机，方便xtrabackup socket连接
docker run -d \
--privileged \
--restart always \
--name=mysql-pxc8.0-106 \
--net=swarm_mysql \
-p 3306:3306 \
-e TZ=Asia/Shanghai \
-e CLUSTER_NAME=PXC \
-e CLUSTER_JOIN=mysql-pxc8.0-107 \
-e MYSQL_ROOT_PASSWORD=P@ssword \
-e XTRABACKUP_PASSWORD=P@ssword \
-v /data/mysql-pxc/data:/var/lib/mysql \
-v /data/mysql-pxc/conf:/etc/percona-xtradb-cluster.conf.d \
-v /data/mysql-pxc/cert:/cert \
-v /data/mysql-pxc/logs:/var/log/mysql \
-v /data/mysql-pxc/socket:/tmp \
pxc:8.0

# 105
docker run -d \
--privileged \
--restart always \
--name=mysql-pxc8.0-105 \
--net=swarm_mysql \
-p 3306:3306 \
-e TZ=Asia/Shanghai \
-e CLUSTER_NAME=PXC \
-e CLUSTER_JOIN=mysql-pxc8.0-107 \
-e MYSQL_ROOT_PASSWORD=P@ssword \
-e XTRABACKUP_PASSWORD=P@ssword \
-v /data/mysql-pxc/data:/var/lib/mysql \
-v /data/mysql-pxc/conf:/etc/percona-xtradb-cluster.conf.d \
-v /data/mysql-pxc/cert:/cert \
pxc:8.0
```

启动成功后，进入任一节点，查看集群节点数量

```mysql
SHOW STATUS LIKE 'wsrep_cluster_size';
```



### 备份

使用106做备份，在106下载镜像`percona-xtrabackup:8.0`

```bash
docker pull docker.1ms.run/percona/percona-xtrabackup:8.0
docker tag docker.1ms.run/percona/percona-xtrabackup:8.0 percona-xtrabackup:8.0
docker rmi docker.1ms.run/percona/percona-xtrabackup:8.0
```



#### 全量备份

```bash
mkdir -m 777 -p /data/mysql-backup/{full_backup,inc_backup}

cd /data/mysql-backup/inc_backup   	# 因为后面还要验证增量备份，所以在这个目录下测试
mkdir -m 777 $(date -d "next-sunday - 7 days" +%Y%m%d) 	# 创建目录 上周日，此时上周日为20251102
mkdir -m 777 $(date -d "next-sunday - 7 days" +%Y%m%d)/full

# 这里需要加上上面的-v /data/pxc/pxc_socket:/tmp
docker run --rm \
-v /data/mysql-backup/inc_backup/20251102:/backup \
--volumes-from mysql-pxc8.0-106 \
percona/percona-xtrabackup:8.0 \
xtrabackup --backup --datadir=/var/lib/mysql --target-dir=/backup/full \
--user=root --password=P@ssword -S /tmp/mysql.sock
```

`--volumes-from`：指新的容器要挂载一个已存在的容器的卷。这里挂载了`mysql-pxc8.0-106`容器中的卷，为MySQL数据目录。

`xtrabackup --backup --datadir=/var/lib/mysql --target-dir=/backup/full`：这是在容器中执行的命令，使用 XtraBackup 工具进行备份。备份数据会被存放在容器内的 `/backup/full` 目录，也就是主机上的 `/data/mysql-backup/inc_backup/20251102/full` 目录。



##### 查看

查看目录下 xtrabackup_checkpoints 或 xtrabackup_inf。backup_type为**full-backuped**，from_lsn是备份的起始LSN，to_lsn是结束LSN。下一次增量备份的**from_lsn正确应该和上一次的to_lsn相同**。

```bash
root@node106:/data/mysql-backup/inc_backup/20251102/full# ll
总用量 76860
drwxr-x--- 5 hyx  hyx      4096 Nov  3 16:02 ./
drwxrwxrwx 3 root root     4096 Nov  3 16:01 ../
-rw-r----- 1 hyx  hyx       447 Nov  3 16:02 backup-my.cnf
-rw-r----- 1 hyx  hyx       157 Nov  3 16:02 binlog.000009
-rw-r----- 1 hyx  hyx        16 Nov  3 16:02 binlog.index
-rw-r----- 1 hyx  hyx      4306 Nov  3 16:02 ib_buffer_pool
-rw-r----- 1 hyx  hyx  12582912 Nov  3 16:01 ibdata1
drwxr-x--- 2 hyx  hyx      4096 Nov  3 16:02 mysql/
-rw-r----- 1 hyx  hyx  32505856 Nov  3 16:01 mysql.ibd
drwxr-x--- 2 hyx  hyx      4096 Nov  3 16:02 performance_schema/
drwxr-x--- 2 hyx  hyx      4096 Nov  3 16:01 sys/
-rw-r----- 1 hyx  hyx  16777216 Nov  3 16:01 undo_001
-rw-r----- 1 hyx  hyx  16777216 Nov  3 16:01 undo_002
-rw-r----- 1 hyx  hyx        18 Nov  3 16:02 xtrabackup_binlog_info
-rw-r----- 1 hyx  hyx       134 Nov  3 16:02 xtrabackup_checkpoints
-rw-r----- 1 hyx  hyx       527 Nov  3 16:02 xtrabackup_info
-rw-r----- 1 hyx  hyx      2560 Nov  3 16:02 xtrabackup_logfile
-rw-r----- 1 hyx  hyx        39 Nov  3 16:02 xtrabackup_tablespaces
root@node106:/data/mysql-backup/inc_backup/20251102/full# cat xtrabackup_checkpoints
backup_type = full-backuped
from_lsn = 0
to_lsn = 30304090
last_lsn = 30304090
flushed_lsn = 30304080
redo_memory = 0
redo_frames = 0
```



##### 还原

已经全量备份，现在尝试更改数据库（过程省略，进数据库自己随便加），再还原（验证通过✓）。

```bash
# 预处理
# --prepare：预处理备份，将增量日志应用到全量备份中。确保备份数据的一致性，并且使得备份可以被恢复。
# --apply-log-only：这个选项指定 xtrabackup 仅应用日志，而不执行其他的恢复操作。
docker run --rm -v /data/mysql-backup/inc_backup/20251102:/backup \
percona/percona-xtrabackup:8.0 \
xtrabackup --prepare --apply-log-only --target-dir=/backup/full

# 删除 mysql-pxc8.0-106 容器，清空数据，重新创建容器
docker rm -f mysql-pxc8.0-106
rm -rf /data/mysql-pxc/data/*

# 创建重新创建pxc容器
docker run -d \
--privileged \
--restart always \
--name=mysql-pxc8.0-106 \
--net=swarm_mysql \
-p 3306:3306 \
-e TZ=Asia/Shanghai \
-e CLUSTER_NAME=PXC \
-e CLUSTER_JOIN=mysql-pxc8.0-107 \
-e MYSQL_ROOT_PASSWORD=P@ssword \
-e XTRABACKUP_PASSWORD=P@ssword \
-v /data/mysql-pxc/data:/var/lib/mysql \
-v /data/mysql-pxc/conf:/etc/percona-xtradb-cluster.conf.d \
-v /data/mysql-pxc/cert:/cert \
-v /data/mysql-pxc/socket:/tmp \
pxc:8.0

3复制待还原数据到PXC节点数据目录
docker cp /data/mysql-backup/inc_backup/20251102/full/. mysql-pxc8.0-106:/var/lib/mysql

4给容器mysql用户授权（不授权重启容器会因为没有权限容器启动失败）
docker exec -it -u 0 mysql-pxc8.0-106 chown -R mysql:mysql /var/lib/mysql

5重启pxc
docker restart mysql-pxc8.0-106
```



#### 增量备份

##### 第一次增量

```bash
cd /data/mysql-backup/inc_backup/20251102
# 创建当天目录
mkdir -m 777 $(date +%Y%m%d)

# 增量备份，它依赖于前一个全量备份的 xtrabackup_checkpoints 文件来进行增量操作，所以挂载的是20251102全量目录
docker run --rm -v /data/mysql-backup/inc_backup/20251102:/backup \
--volumes-from mysql-pxc8.0-106 \
percona/percona-xtrabackup:8.0 \
xtrabackup --backup --datadir=/var/lib/mysql --target-dir=/backup/$(date +%Y%m%d) --incremental-basedir=/backup/full \
--user=root --password=P@ssword -S /tmp/mysql.sock

# 此时，这个目录下就是我们增量的备份 /data/mysql-backup/inc_backup/20251102/20251105
vim /data/mysql-backup/inc_backup/20251102/20251105/xtrabackup_info
```

##### 查看

可以看到，增量的innodb_from_lsn和全量innodb_to_lsn都是30304090。

> `to_lsn` 应该大于 `from_lsn`，因为增量备份是基于旧日志序列号（`from_lsn`）到新日志序列号（`to_lsn`）之间的事务进行的。我这里小是因为之前进行了一次回滚，是正常的。

```tex
root@node106:/data/mysql-backup/inc_backup/20251102/20251105# cat xtrabackup_checkpoints
backup_type = incremental
from_lsn = 30304090
to_lsn = 29944818
last_lsn = 29944844
flushed_lsn = 29944818
redo_memory = 0
redo_frames = 0
```



##### 第二次增量

```bash
在宿主机创建对应的目录
cd /data/mysql-backup/inc_backup/20251102
mkdir -m 777 20251106  # 这里实际应该也是到第二天时，创建第二天的目录

# --incremental-basedir=/backup/20251105 选项指定增量备份的基础目录。目录包含了上一个增量备份（或全量备份）
docker run --rm -v /data/mysql-backup/inc_backup/20251102:/backup \
--volumes-from mysql-pxc8.0-106 \
percona/percona-xtrabackup:8.0 \
xtrabackup --backup --datadir=/var/lib/mysql --target-dir=/backup/20251106 --incremental-basedir=/backup/20251105 \
--user=root --password=P@ssword -S /tmp/mysql.sock
```



##### 还原

```
# 对全备份执行预处理操作 –apply-log-only 阻止回滚未完成的事务
docker run --rm -v /data/mysql-backup/inc_backup/20251102:/backup \
percona/percona-xtrabackup:8.0 \
xtrabackup --prepare --apply-log-only --target-dir=/backup/full

# 合并合并第一个增量备份 
docker run --rm -v /data/mysql-backup/inc_backup/20251102:/backup \
percona/percona-xtrabackup:8.0 \
xtrabackup --prepare --apply-log-only --target-dir=/backup/full \
--incremental-dir=/backup/20251105

# 合并第2此增量备份，最后一次还原不需要加选项–apply-log-only
docker run --rm -v /data/mysql-backup/inc_backup/20251102:/backup \
percona/percona-xtrabackup:8.0 \
xtrabackup --prepare --target-dir=/backup/full \
--incremental-dir=/backup/20251106

# 再进行 全量备份-还原过程
```



#### 定时执行全量/增量备份

```
# 106创建脚本（脚本在下面）
vim /data/mysql-backup/backup_script.sh
# 赋予执行权限
chmod +x /data/mysql-backup/backup_script.sh

# 编辑当前用户的定时任务
crontab -e
	> 选择nano最简单 1
	
	# 添加内容到单独的一行
    # Linux系统常见的cron表达式通常是5字段格式：分 时 日 月 星期
    # 这里表示每天凌晨2点执行
    0 2 * * * mkdir -p /var/data/mysql-backup/sh/log && /bin/bash /data/mysql-backup/sh/backup_script.sh >> /var/data/mysql-backup/sh/log/backup.log 2>&1

# 查看已有的定时任务
crontab -l
```



**backup_script.sh** 

每周一执行全量，周内其他天执行增量。这里全量目录名使用的也是日期，未使用full，便于查找。

```bash
#!/bin/bash

# 配置变量
BACKUP_DIR="/data/mysql-backup/inc_backup"
MYSQL_CONTAINER_NAME="mysql-pxc8.0-106"
MYSQL_ROOT_PASSWORD="P@ssword"
DATE=$(date +%Y%m%d)
MONDAY_DATE=$(date -d "last monday" +%Y%m%d)  # 获取本周一的日期
YESTODAY_DATE=$(date -d "yesterday" +%Y%m%d)  # 获取昨天的日期
WEEKDAY=$(date +%u) # 1是周一，7是周日

# 创建必要的备份目录（如果不存在）
create_backup_dir() {
    mkdir -m 777 -p "$BACKUP_DIR/$MONDAY_DATE/$DATE"
}

# 定义备份功能
backup_full() {
    # 创建目录
    create_backup_dir

    # 执行全量备份
    docker run --rm -v "$BACKUP_DIR/$MONDAY_DATE:/backup" \
    --volumes-from "$MYSQL_CONTAINER_NAME" \
    percona/percona-xtrabackup:8.0 \
    xtrabackup --backup --datadir=/var/lib/mysql --target-dir=/backup/$DATE \
    --user=root --password="$MYSQL_ROOT_PASSWORD" -S /tmp/mysql.sock

    # 全量备份成功后删除上周的备份
    if [ $? -eq 0 ]; then
        # 删除上周备份的目录
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 全量备份成功"
        
        DEL_DIR=$(date -d "last monday -14 days" +%Y%m%d)  # 获取两周前的日期
        if [ -d "$BACKUP_DIR/$DEL_DIR" ]; then
            rm -rf "$BACKUP_DIR/$DEL_DIR"
            echo "$(date '+%Y-%m-%d %H:%M:%S') - 删除备份数据 $BACKUP_DIR/$DEL_DIR"
        fi
    else
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 全量备份失败"
    fi
    
    # 判断路径是否    # 判断路径是否存在
    if [ -d "$BACKUP_DIR/$DEL_DIR" ]; then
        rm -rf "$BACKUP_DIR/$DEL_DIR"
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 删除备份数据 $BACKUP_DIR/$DEL_DIR"
    else
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 路径 $BACKUP_DIR/$DEL_DIR 不存在，无法删除"
    fi
}

backup_incremental() {
    # 创建目录
    create_backup_dir

    # 执行增量备份
    docker run --rm -v "$BACKUP_DIR/$MONDAY_DATE:/backup" \
    --volumes-from "$MYSQL_CONTAINER_NAME" \
    percona/percona-xtrabackup:8.0 \
    xtrabackup --backup --datadir=/var/lib/mysql --target-dir="/backup/$DATE" \
    --incremental-basedir="/backup/$YESTODAY_DATE" \
    --user=root --password="$MYSQL_ROOT_PASSWORD" -S /tmp/mysql.sock
    
    if [ $? -eq 0 ]; then
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 增量备份成功"
    else
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 增量备份失败"
    fi
}

# 每周一执行全量备份，周内其他天执行增量备份
if [ "$WEEKDAY" -eq 1 ]; then
    backup_full
else
    # 获取昨天备份目录
    YESTODAY_BACKUP=$(ls -d "$BACKUP_DIR/$MONDAY_DATE/$YESTODAY_DATE/"* | tail -n 1 | xargs basename)
    if [ -z "$YESTODAY_BACKUP" ]; then
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 未找到昨日备份，增量备份失败"
        exit 1
    fi
    backup_incremental
fi
```



##### 全量or增量

全量备份是数据库的完整副本，如果数据量较大，每次全量备份都会占用大量存储空间，还会需要锁定大量的数据库资源。

增量备份仅仅备份自上次备份以来的数据变化部分。占用的存储空间更小，对系统的性能影响较小，更快的备份和恢复时间。

最常见的备份策略是结合全量备份和增量备份。每周执行一次全量备份，保留完整的数据库状态。在全量备份之间，定期执行增量备份（例如每天凌晨1点），只备份自上次备份以来的数据变化部分。





## pxc:5.7.21

**集群部署** [参考](https://www.cnblogs.com/nhdlb/p/14032657.html)

环境准备，分别在三台服务器进行以下操作  ，镜像使用percona-xtradb-cluster:5.7.21，此过程无需加密。

```bash
# 拉取官方镜像，更名
docker pull docker.1ms.run/percona/percona-xtradb-cluster:5.7.21
docker tag docker.1ms.run/percona/percona-xtradb-cluster:5.7.21 pxc:5.7.21
docker rmi docker.1ms.run/percona/percona-xtradb-cluster:5.7.21

# 开放所需端口(swarm使用)
sudo ufw allow from 172.16.1.0/24 to any port 2377 proto tcp
sudo ufw allow from 172.16.1.0/24 to any port 7946 proto tcp
sudo ufw allow from 172.16.1.0/24 to any port 7946 proto udp
sudo ufw allow from 172.16.1.0/24 to any port 4789 proto udp
sudo ufw status verbose

# 把当前主机初始化为 Swarm 集群的 manager，成功后会有一个带token的例子，供swarm join用
docker swarm init
# 创建一个名为 swarm_mysql 的 overlay 网络 --attachable：允许独立容器（docker run 启动的）直接加入该网络
docker network create -d overlay --attachable swarm_mysql
# 加入虚拟网络（其他两台服务器）
docker swarm join --token XXXX 172.16.1.105:2377

# 启动容器集群
# CLUSTER_NAME，MYSQL_ROOT_PASSWORD，XTRABACKUP_PASSWORD 需要都使用同一个
# -e CLUSTER_JOIN=node1 选填，如果不存在集群或全挂了则加上
# 不能使用挂载目录，（只能使用卷，或权限777巨坑）
docker run -d -p 3306:3306   -e MYSQL_ROOT_PASSWORD=P@ssword   -e CLUSTER_NAME=PXC   -e XTRABACKUP_PASSWORD=P@ssword   -e TZ=Asia/Shanghai   -v mysql-pxc-105:/var/lib/mysql   --name=mysql-pxc-105 --net=swarm_mysql pxc:5.7.21
docker run -d -p 3306:3306   -e MYSQL_ROOT_PASSWORD=P@ssword   -e CLUSTER_NAME=PXC   -e XTRABACKUP_PASSWORD=P@ssword -e CLUSTER_JOIN=mysql-pxc-105  -e TZ=Asia/Shanghai   -v mysql-pxc-106:/var/lib/mysql   --name=mysql-pxc-106 --net=swarm_mysql pxc:5.7.21
docker run -d -p 3306:3306   -e MYSQL_ROOT_PASSWORD=P@ssword   -e CLUSTER_NAME=PXC   -e XTRABACKUP_PASSWORD=P@ssword -e CLUSTER_JOIN=mysql-pxc-105   -e TZ=Asia/Shanghai   -v mysql-pxc-107:/var/lib/mysql   --name=mysql-pxc-107 --net=swarm_mysql pxc:5.7.21

# 查看卷
docker volume inspect mysql-pcx-105
```



## 负载

使用Haproxy做负载，将请求均匀地发送给集群中的每一个节点。

HAProxy支持**协议级** MySQL 探活、连接与队列控制、细粒度限流/观察。为什么不用nginx，是因为nginx开源版本没这些功能。

```bash
# 拉取镜像haproxy
docker pull docker.1ms.run/library/haproxy
docker tag docker.1ms.run/library/haproxy:latest haproxy
docker rmi docker.1ms.run/library/haproxy:latest

# 创建Haproxy配置文件,文件在最后
vim /opt/haproxy/haproxy.cfg

# 在数据库集群中创建空密码、无权限用户haproxy，来供Haproxy对MySQL数据库进行心跳检测
docker exec -it mysql-pxc-105 bash
	mysql -uroot -p
        P@ssword
        use mysql;
        create user 'haproxy'@'%' identified by '';
        exit
	exit

# 105上创建haproxy容器
docker run -it -d -p 4001:8888 -p 4002:3306 -v /opt/haproxy:/usr/local/etc/haproxy --name haproxy --net=swarm_mysql --privileged haproxy
	# 访问方式：
    http://172.16.1.105:4001/dbs	用户名admin，密码abc123456
```

附1：/opt/haproxy/haproxy.cfg

```properties
global
    #日志文件，使用rsyslog服务中local5日志设备（/var/log/local5），等级info
    log 127.0.0.1 local5 info
    #守护进程运行
    daemon

defaults
    log    global
    mode    http
    #日志格式
    option    httplog
    #日志中不记录负载均衡的心跳检测记录
    option    dontlognull
    #连接超时（毫秒）
    timeout connect 5000
    #客户端超时（毫秒）
    timeout client  50000
    #服务器超时（毫秒）
    timeout server  50000

#监控界面    
listen  admin_stats
    #监控界面的访问的IP和端口
    bind  0.0.0.0:8888
    #访问协议
    mode        http
    #URI相对地址
    stats uri   /dbs
    #统计报告格式
    stats realm     Global\ statistics
    #登陆帐户信息
    stats auth  admin:abc123456
#数据库负载均衡
listen  proxy-mysql
    #访问的IP和端口
    bind  0.0.0.0:3306  
    #网络协议
    mode  tcp
    #负载均衡算法（轮询算法）
    #轮询算法：roundrobin
    #权重算法：static-rr
    #最少连接算法：leastconn
    #请求源IP算法：source 
    balance  roundrobin
    #日志格式
    option  tcplog
    #在MySQL中创建一个没有权限的haproxy用户，密码为空。Haproxy使用这个账户对MySQL数据库心跳检测
    option  mysql-check user haproxy
    server  MySQL_1 172.18.0.2:3306 check weight 1 maxconn 2000  
    server  MySQL_2 172.18.0.3:3306 check weight 1 maxconn 2000  
    server  MySQL_3 172.18.0.4:3306 check weight 1 maxconn 2000 
    #使用keepalive检测死链
    option  tcpka
```

附2：/etc/keepalived/keepalived.conf

```
vrrp_instance  VI_1 {
    state  MASTER
    interface  eth0
    virtual_router_id  51
    priority  100	# 备用服务器可以修改小一点
    advert_int  1
    authentication {
        auth_type  PASS
        auth_pass  123456
    }
    virtual_ipaddress {
        172.18.0.201
    }
}
```

高可用(未完成)

```bash
# ------ 以下部分为haproxy高可用，须有两个节点以上，我没有做----------
# 以root身份进入容器
docker exec -u 0 -it haproxy bash
	apt-get update
	# 安装keepalived
	apt-get install -y keepalived
	apt-get install -y vim
	vim /etc/keepalived/keepalived.conf # 内容在附录2
	
# （以下切换到106，不试了，）106上创建haproxy容器，
docker run -it -d -p 4001:8888 -p 4002:3306 -v /opt/haproxy:/usr/local/etc/haproxy --name haproxy-backup --net=swarm_mysql --privileged haproxy
docker exec -u 0 -it haproxy-backup bash
	apt-get update
	# 安装keepalived
	apt-get install -y keepalived
	apt-get install -y vim
	vim /etc/keepalived/keepalived.conf
```



## 问题整理

#### 节点启动失败

**问题描述：**上次容器启动的节点，直接被`docker rm -f mysql-8.0-107` 删除，再次创建容器时，因为之前挂载的文件还在，读取配置文件导致报错如下。该错误表示当前节点并不是最后离开集群的节点，因此它可能不包含所有最新的更新。

```txt
# -it 启动容器进入交互模式，直接在控制台上查看日志输出。
docker run -it ...

[ERROR] [MY-000000] [Galera] It may not be safe to bootstrap the cluster from this node. It was not the last one to leave the cluster and may not contain all the updates. To force cluster bootstrap with this node, edit the grastate.dat file manually and set safe_to_bootstrap to 1 .
```

**解决方案：**  查看/data/mysql-pxc/data/grastate.dat，找到 `safe_to_bootstrap` 并将其值改为 `1`。然后保存文件并重新启动容器。

```
vi /data/mysql-pxc/data/grastate.dat
    # GALERA saved state
    version: 2.1
    uuid:    2b77159a-b637-11f0-99e8-262469fd2726
    seqno:   28
    safe_to_bootstrap: 1
```

**验证**：wsrep_ready为on时，该集群可用。

```tex
mysql> SHOW STATUS LIKE 'wsrep_ready%';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| wsrep_ready   | ON    |
+---------------+-------+
1 row in set (0.00 sec)
```



#### 还原错误

**问题描述：** 使用xtrabackup还原一次全量和两次增量，还原失败。报错如下

```bash
docker run --rm -v /data/mysql-backup/inc_backup/20251102:/backup \
percona/percona-xtrabackup:8.0 \
xtrabackup --prepare --target-dir=/backup/full \
--incremental-dir=/backup/20251105 \
--incremental-dir=/backup/20251106
```

```tex
[ERROR] [Xtrabackup] This incremental backup seems not to be proper for the target.
[ERROR] [Xtrabackup] Check 'to_lsn' of the target and 'from_lsn' of the incremental.
解释：正在尝试 **应用（prepare）一个不匹配的增量备份** 到你的全量备份目录上。 也就是说，**LSN（日志序列号）不连续**。
```

**解决方案：**增量备份的 LSN 必须是连续的，确保每个增量备份的 `from_lsn` 和 `to_lsn` 是正确的，且按时间顺序递增。

**验证**：发现序号不连续。原因是备份过一次，所以**需要重新生成备份**。

```bash
root@node106:~# cat /data/mysql-backup/inc_backup/20251102/full/xtrabackup_checkpoints
backup_type = log-applied
from_lsn = 0
to_lsn = 30304090
last_lsn = 30304090
flushed_lsn = 30304080
redo_memory = 0
redo_frames = 0
root@node106:~# cat /data/mysql-backup/inc_backup/20251102/20251105/xtrabackup_checkpoints
backup_type = incremental
from_lsn = 30304090
to_lsn = 29944818		# 这里的问题，原因是之前还原过一次
last_lsn = 29944844
flushed_lsn = 29944818
redo_memory = 0
redo_frames = 0
root@node106:~# cat /data/mysql-backup/inc_backup/20251102/20251106/xtrabackup_checkpoints
backup_type = incremental
from_lsn = 29944818
to_lsn = 30490532
last_lsn = 30490532
flushed_lsn = 30490522
redo_memory = 0
redo_frames = 0
```



#### 加入集群错误

在 Galera 集群中，`safe_to_bootstrap` 用于指示该节点是否可以安全地作为集群的启动节点，特别是当集群中的其他节点出现故障或无法访问时。如果一个节点作为集群的引导节点启动，它将重新初始化集群并成为集群的唯一节点，其他节点将无法加入集群，直到它们重新连接并与该节点同步。

**`safe_to_bootstrap: 1`**：表示这个节点可以安全地启动集群。这通常意味着该节点是集群中最后一个离开的节点（或唯一的节点），并且它包含所有集群更新的完整数据，因此可以重新引导集群。

**`safe_to_bootstrap: 0`**：表示该节点不能作为引导节点。这个值通常设置为 `0`，当集群中有其他节点存活并正常运行时，意味着当前节点不应引导集群。通常在集群还存在其他节点时使用此设置。

所有应该去查找为1的节点，启动，其他节点再加入。只有当集群的其他节点无法启动或不存在时，`safe_to_bootstrap` 才可以被手动设置为 `1` 来引导集群。



















