# PXC

pxc集群的存活数量需要大于总节点数量的一半才可以正常工作。[参考](https://blog.csdn.net/W1324926684/article/details/130512670)

优点：

+ 集群高可用，真正的多节点读写
+ 改善了传统binlog到replylog中存在的延迟问题。基本做到实时同步。
+ 新节点自动部署，无需太多操作。
+ 故障无缝转移。

缺点：

+ 新加入节点开销大，需要把数据完全复制一次。SST协议开销大。
+ 任何事务都要全局验证，MGR是大多数就行。性能取决于最差的节点。
+ 为了保证数据一致性，在并写的时候，锁冲突会比较严重。
+ 只支持innodb引擎。
+ 没有表锁，只能锁集群。



## pxc:8.0

3台服务器 172.16.1.105/106/107，部署方式为PXC集群模式，镜像使用percona-xtradb-cluster:8.0。

### 准备工作

#### 拉取镜像

以下内容在三个服务器都要执行

```bash
# 切换root用户
su -

# 拉取官方镜像，更名
docker pull docker.1ms.run/percona/percona-xtradb-cluster:8.0
docker tag docker.1ms.run/percona/percona-xtradb-cluster:8.0 pxc:8.0
docker rmi docker.1ms.run/percona/percona-xtradb-cluster:8.0
```



#### 目录初始化

```bash
# PXC容器用户mysql权限很低，对写入的目录赋予读写权限
mkdir -m 777 -p /data/mysql-pxc/{logs,data}
# 配置和证书，如果给777权限，那么mysql认为不安全会拒绝读取配置，所以给755
mkdir -m 755 -p /data/mysql-pxc/{conf,cert}
# 在需要做备份的节点多增加socket目录（选做）
mkdir -m 777 -p /data/mysql-pxc/socket

# 使用容器创建证书 --rm：容器退出时自动删除该容器（只做一次，后续节点都使用此套证书）
docker run --name pxc-cert --rm -v /data/mysql-pxc/cert:/cert pxc:8.0 mysql_ssl_rsa_setup -d /cert

# 在conf目录下新增cert.cnf，内容见下方
vim /data/mysql-pxc/conf/cert.cnf

# 复制配置文件和证书到其他服务器
scp /data/mysql-pxc/cert/* 192.168.100.129:/data/mysql-pxc/cert/
scp /data/mysql-pxc/conf/* 192.168.100.129:/data/mysql-pxc/conf/

# 修改权限和所属用户（每台服务器）
chmod 644 /data/mysql-pxc/conf/cert.cnf
chown root:root /data/mysql-pxc/conf/cert.cnf

# 进入容器，验证证书，要保证每台机器的证书一样（可选）
openssl x509 -noout -modulus -in /cert/server-cert.pem | md5sum
openssl rsa  -noout -modulus -in /cert/server-key.pem  | md5sum
```

##### cert.cnf

```cnf
[mysqld]
skip-name-resolve
ssl-ca = /cert/ca.pem
ssl-cert = /cert/server-cert.pem
ssl-key = /cert/server-key.pem

pxc_encrypt_cluster_traffic = ON

[client]
ssl-ca = /cert/ca.pem
ssl-cert = /cert/client-cert.pem
ssl-key = /cert/client-key.pem

[sst]
encrypt = 4
ssl-ca = /cert/ca.pem
ssl-cert = /cert/server-cert.pem
ssl-key = /cert/server-key.pem
```

#####  最终目录结构

```bash
root@node105:/data/mysql-pxc# ll
total 24
drwxr-xr-x  6 root root 4096 Dec  7 13:40 ./
drwxr-xr-x  5 root root 4096 Dec  5 17:36 ../
drwxr-xr-x  2 wzy  wzy  4096 Dec  5 17:58 cert/		# 必须具备高权限，推荐755/777，chmod 755 /data/mysql-pxc/cert
drwxr-xr-x  2 root root 4096 Dec  7 13:36 conf/		# 必须具备高权限，推荐755/777，chmod 755 /data/mysql-pxc/conf
drwxrwxrwx 10 root root 4096 Dec  7 15:54 data/		# 推荐777，chmod 777 /data/mysql-pxc/data
drwxrwxrwx  2 root root 4096 Dec  5 18:00 logs/		# 推荐777，chmod 777 /data/mysql-pxc/logs

root@node105:/data/mysql-pxc/conf# ll				# 配置文件权限不能高，高了会被忽略掉
total 12
drwxr-xr-x 2 root root 4096 Dec  7 13:36 ./
drwxr-xr-x 6 root root 4096 Dec  7 13:40 ../
-rw-r--r-- 1 root root  348 Dec  7 13:36 cert.cnf

root@node105:/data/mysql-pxc/cert# ll				# 证书文件权限不能高，高了会被忽略掉
total 40
drwxr-xr-x 2 wzy   wzy   4096 Dec  5 17:58 ./
drwxr-xr-x 6 root  root  4096 Dec  7 13:40 ../
-rw-r--r-- 1 yuwei yuwei 1704 Dec  5 17:58 ca-key.pem
-rw-r--r-- 1 yuwei yuwei 1155 Dec  5 17:58 ca.pem
-rw-r--r-- 1 yuwei yuwei 1200 Dec  5 17:58 client-cert.pem
-rw-r--r-- 1 yuwei yuwei 1704 Dec  5 17:58 client-key.pem
-rw-r--r-- 1 yuwei yuwei 1700 Dec  5 17:58 private_key.pem
-rw-r--r-- 1 yuwei yuwei  451 Dec  5 17:58 public_key.pem
-rw-r--r-- 1 yuwei yuwei 1200 Dec  5 17:58 server-cert.pem
-rw-r--r-- 1 yuwei yuwei 1704 Dec  5 17:58 server-key.pem
root@node105:/data/mysql-pxc/cert#
```



#### 网络

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
# 查看节点加入token
docker swarm join-token worker/manager
# 上述命令得到token命令
docker swarm join --token <SWARM-TOKEN> <MANAGER-IP>:2377

# 管理员管理此网络的节点列表
docker node ls
docker node rm node105
# 离开swarm网络，--force可选
docker swarm leave --force
```

除了上述准备工作外，推荐将服务器host名称更改，便于区分。如：node105，node106，node107



### 集群部署

```bash
# 107 第一个节点做集群初始化，不加-e CLUSTER_JOIN
docker run  -d \-e CLUSTER_JOIN=mysql-pxc8.0-106 \
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
sudo docker run -d \
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
-v /data/mysql-pxc/logs:/var/log/mysql \
pxc:8.0

```

#### 验证

启动成功后，进入任一节点，查看集群节点数量，应为总数

```mysql
docker exec -it mysql-pxc8.0-105 bash
mysql -uroot -pP@ssword


SHOW VARIABLES LIKE 'pxc_encrypt_cluster_traffic';		# on代表通过Galera复制和集群的流量都会通过TLS/SSL进行加密；OFF明文传输
SHOW STATUS LIKE 'wsrep_ready';							# 节点是否可用  ON：可用
SHOW STATUS LIKE 'wsrep_cluster_size';  				# 集群节点数量
SHOW VARIABLES LIKE 'wsrep_provider_options';			# 看目前使用证书的路径
# 期望结果
wsrep_provider_options 
        gmcast.listen_addr = ssl://0.0.0.0:4567
        socket.ssl        = YES
        socket.ssl_ca     = /cert/ca.pem
        socket.ssl_cert   = /cert/server-cert.pem
        socket.ssl_key    = /cert/server-key.pem
```



### 备份

#### 准备工作

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



### 问题整理

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



### 问题整理

#### 证书认证失败

挂载目录权限问题，`cert.cnf` 权限若是777，mysql会因为所有人可以修改此文件而自动忽略掉这个配置文件，导致不会使用到/cert目录下的证书，而去使用自带的证书。

#### tlsv1 alert decrypt error

 TLS 握手时解密失败，然后回了一个 alert。一个节点日志中打印这个，并不代表自己节点出问题了，也有可能是想加入此节点的节点证书有问题。验证方法为查看两边证书的md5值是否一致。

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

#### 加入集群错误

在 Galera 集群中，`safe_to_bootstrap` 用于指示该节点是否可以安全地作为集群的启动节点，特别是当集群中的其他节点出现故障或无法访问时。如果一个节点作为集群的引导节点启动，它将重新初始化集群并成为集群的唯一节点，其他节点将无法加入集群，直到它们重新连接并与该节点同步。

**`safe_to_bootstrap: 1`**：表示这个节点可以安全地启动集群。这通常意味着该节点是集群中最后一个离开的节点（或唯一的节点），并且它包含所有集群更新的完整数据，因此可以重新引导集群。

**`safe_to_bootstrap: 0`**：表示该节点不能作为引导节点。这个值通常设置为 `0`，当集群中有其他节点存活并正常运行时，意味着当前节点不应引导集群。通常在集群还存在其他节点时使用此设置。

所有应该去查找为1的节点，启动，其他节点再加入。只有当集群的其他节点无法启动或不存在时，`safe_to_bootstrap` 才可以被手动设置为 `1` 来引导集群。

#### 一直提示连接错误

```bash
# 查看日志
docker logs -f mysql-pxc8.0-106

# 发现一直报错
2025-12-07T06:59:51.378587Z 0 [Note] [MY-000000] [Galera] Failed to establish connection: No route to host
```

**清空iptables规则**

```bash
# 关闭 ufw
sudo ufw disable
sudo ufw status

# 备份当前 iptables 规则
sudo iptables-save > /root/iptables-backup-$(date +%s).txt

# 清空 filter / nat / mangle 三张表
sudo iptables -F
sudo iptables -t nat -F
sudo iptables -t mangle -F

# 删除所有自定义链（可选，但更干净）
sudo iptables -X
sudo iptables -t nat -X
sudo iptables -t mangle -X
```

**重启容器**

```bash
# 重新获取swarm_mysql中106的ip
root@node106:~# docker inspect -f '{{ .NetworkSettings.Networks.swarm_mysql.IPAddress }}' mysql-pxc8.0-106
10.0.1.28

# 在107测试，功能正常
root@node107:~# docker run --rm --network=swarm_mysql busybox sh -c "
  echo '=== ping 10.0.1.28 (node106) ===';
  ping -c 3 10.0.1.28 || echo 'ping 10.0.1.28 FAILED';
"
=== ping 10.0.1.28 (node106) ===
PING 10.0.1.28 (10.0.1.28): 56 data bytes
64 bytes from 10.0.1.28: seq=0 ttl=64 time=0.412 ms
64 bytes from 10.0.1.28: seq=1 ttl=64 time=0.403 ms
64 bytes from 10.0.1.28: seq=2 ttl=64 time=0.286 ms
```



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



## Haproxy + keepalived

使用Haproxy做**负载均衡**的，将请求均匀地发送给集群中的每一个节点。

HAProxy支持**协议级** MySQL 探活、连接与队列控制、细粒度限流/观察。为什么不用nginx，是因为nginx开源版本没这些功能。

Keepalived 是做**高可用**的，提供 VIP（虚拟 IP）浮动， 用 VRRP 协议选举 MASTER/BACKUP。

Keepalived 保证对外提供的 IP 在服务器故障时自动切换，不中断业务连接。

### 系统准备

每台服务器都需要设置

```bash
# 切换到root用户
su -
# 加载 ip_vs 模块
modprobe ip_vs
# 允许绑定非本地 IP
sysctl -w net.ipv4.ip_nonlocal_bind=1
# 持久化
cat >/etc/sysctl.d/99-haproxy-keepalived.conf <<EOF
net.ipv4.ip_nonlocal_bind=1
EOF
sysctl -p /etc/sysctl.d/99-haproxy-keepalived.conf
```

### 容器准备

```bash
# 创建配置目录
mkdir -p /data/haproxy_keepalived/{haproxy,keepalived}
vim /data/haproxy_keepalived/haproxy/haproxy.cfg
vim /data/haproxy_keepalived/keepalived/keepalived.conf
vim /data/haproxy_keepalived/docker-compose.yml
# 启动容器
cd /data/haproxy_keepalived
docker compose up -d
```

#### haproxy.cfg

```tex
# Web 监控界面 (可选，如果不用系统自带的，就加这一段)
listen admin_stats
    bind 0.0.0.0:8404
    mode http
    stats enable
    stats uri /dbs
    stats realm Global\ statistics
    stats auth admin:abc123456

# MySQL 负载均衡（PXC 三节点）
listen proxy-mysql
    bind 172.16.1.200:3307         # VIP，配合 keepalived 漂移
    mode tcp
    balance roundrobin
    option tcplog
    option tcpka
    option mysql-check user haproxy

    server pxc1 172.16.1.105:3306 check inter 2000 rise 3 fall 3 maxconn 2000
    server pxc2 172.16.1.106:3306 check inter 2000 rise 3 fall 3 maxconn 2000
    server pxc3 172.16.1.107:3306 check inter 2000 rise 3 fall 3 maxconn 2000
```

#### keepalived.conf

> 这里的网卡 `enp0s31f6` 需要替换为本机的网卡名称，查询命令 `ip a`，看172.16.1.105所在的网卡名

```tex
vrrp_instance VI_1 {
    state MASTER		# 注意：其他的节点更改为 BACKUP，只有一个MASTER
    interface eno1
    virtual_router_id 51
    priority 150		# 注意：这里要更改，BACKUP要比master小，如140，130，每一个都要不一样
    advert_int 1

    authentication {
        auth_type PASS
        auth_pass 123456
    }

    virtual_ipaddress {
        172.16.1.200/24 dev eno1
    }
}
```

#### docker-compose.yml

```yaml
version: "3.8"

services:
  haproxy-keepalived:
    image: instantlinux/haproxy-keepalived:latest
    container_name: haproxy-keepalived
    network_mode: "host"
    cap_add:
      - NET_ADMIN
    restart: always
    volumes:
      # HAProxy 配置目录
      - /data/haproxy_keepalived/haproxy:/usr/local/etc/haproxy.d:ro
      # 每个节点自己的 keepalived.conf
      - /data/haproxy_keepalived/keepalived/keepalived.conf:/etc/keepalived/keepalived.conf:ro
    environment:
      - TZ=Asia/Shanghai
      - STATS_ENABLE=yes
      - PORT_HAPROXY_STATS=8404
      # http://172.16.1.105:8404/stats haproxy:changeme
      - STATS_URI=/stats
```

### 测试

```sh
# 在master上执行
# 查看虚拟ip，MASTER占据VIP，BACKUP没有显示
ip addr | grep 172.16.1.200
    inet 172.16.1.200/24 scope global secondary enp0s31f6
    
# 查看3307端口，每一台都有结果才对
ss -tulnp | grep 3307
tcp     LISTEN   0        4096        172.16.1.200:3307           0.0.0.0:*      users:(("haproxy",pid=1243175,fd=5))

# 测试连接mysql，直接使用VIP:3307即可
mysql -h 172.16.1.200 -P 3307 -u youruser -p

# 把MASTER容器停止，然后VIP 会漂移到 priority 更高的那个（106）
# 106测试
ip addr | grep 172.16.1.200
    inet 172.16.1.200/24 scope global secondary eno1
    
# 再次测试连接mysql，依然可以连接，测试完成
mysql -h 172.16.1.200 -P 3307 -u youruser -p
```

### 问题整理

1. haproxy因为端口默认8080，和现有端口冲突了，改成8404端口就可以。

>  再遇到问题看日志分析即可，日志写的很详细。

```bash
# 进入容器
docker exec -it haproxy-keepalived sh

# 进入这两个目录下，查看配置
cd /usr/local/etc/haproxy
cd /usr/local/etc/haproxy.d
```



2. 遇到了脑裂问题

三个节点都写了MASTER，要有1个MASTER，剩下的都是BACKUP









