**集群部署**

分别进入ubuntu系统，三个root用户（建议）

```bash
# 设置hosts
vim /etc/hosts
	172.16.1.105    db-105
    172.16.1.106    db-106
    172.16.1.107    db-107

# 安装mariadb
apt update -y # 更新安装包
apt install mariadb-server -y

# 查看状态
systemctl status mariadb

# 安全步骤（设置密码等，可以全部输入y）
mysql_secure_installation
    # 要不要把本机登录改成用 unix_socket 方式。 y可以直接sudo mysql登陆，n需要密码登陆
    Switch to unix_socket authentication [Y/n]

# 修改 127.0.0.1 -> 0.0.0.0
vim /etc/mysql/mariadb.conf.d/50-server.cnf
	bind-address            = 0.0.0.0

# 编辑galera配置文件
vim /etc/mysql/conf.d/galera.cnf
```

**galera.cnf**

```properties
[mysqld]
binlog_format=ROW
default-storage-engine=innodb
innodb_autoinc_lock_mode=2
bind-address=0.0.0.0

# Galera Provider Configuration
wsrep_on=ON
wsrep_provider=/usr/lib/galera/libgalera_smm.so

# Galera Cluster Configuration
wsrep_cluster_name="galera_cluster"
# 集群地址
wsrep_cluster_address="gcomm://172.16.1.105,172.16.1.106,172.16.1.107"

# Galera Synchronization Configuration
wsrep_sst_method=mariabackup

# 修改为当前主机的ip和name，db-107为hosts中配置
wsrep_node_address="172.16.1.107"
wsrep_node_name="db-107"
```

**启动集群**

```bash
# 分别停止所有机器上的mariadb
systemctl stop mariadb

# 用“新集群”模式启动本机的 mysqld，让这个节点成为 Primary 组件的起点
galera_new_cluster
	# 查看wsrep_cluster_size
	mysql -u root -p -e "SHOW STATUS LIKE 'wsrep_cluster_size'"
        +--------------------+-------+
        | Variable_name      | Value |
        +--------------------+-------+
        | wsrep_cluster_size | 1     |
        +--------------------+-------+
# 验证
mysql -u root -p -e "SHOW STATUS LIKE 'wsrep_cluster_size'"

# 其他机器上正常启动，就会加入集群（理论上是这样，但我都执行了一遍galera_new_cluster，stop再start）
systemctl start mariadb
	# 查看wsrep_cluster_size
	mysql -u root -p -e "SHOW STATUS LIKE 'wsrep_cluster_size'"
        +--------------------+-------+
        | Variable_name      | Value |
        +--------------------+-------+
        | wsrep_cluster_size | 3     |
        +--------------------+-------+
```

**集群验收**

```bash
-- 1) 基本健康
SHOW STATUS LIKE 'wsrep_cluster_status';     -- 应为 Primary
SHOW STATUS LIKE 'wsrep_ready';              -- ON
SHOW STATUS LIKE 'wsrep_local_state_comment';-- Synced
SHOW VARIABLES LIKE 'wsrep_sst_method';      -- mariabackup（或你设置的）
SHOW GLOBAL STATUS LIKE 'wsrep_flow_control%';
```





