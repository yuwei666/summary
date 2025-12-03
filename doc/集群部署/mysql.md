**需求**

在172.16.1.105，172.16.1.106，172.16.1.107上部署InnoDB Cluster。

**方案**

部署方式为Docker，MySql版本为8.0，106 同时部署 mysqlsh 与 Router。

```
     +----------------------+
     |      MySQL Router    |
     +----------+-----------+
                |
   +------------+-------------+
   |            |             |
+------+    +------+     +------+
| node1 |   | node2 |    | node3 |
| (Primary) |(Replica)|  (Replica)|
+------+    +------+     +------+
```

**环境准备**

```bash
sudo mkdir -p /opt/mysql/{data,conf,init,logs}
# 只在 106（放 Router）额外建：
sudo mkdir -p /opt/router/{conf,run,logs}

# 放行端口
sudo firewall-cmd --add-port=3306/tcp --permanent
sudo firewall-cmd --add-port=33061/tcp --permanent
sudo firewall-cmd --add-port=6446/tcp --permanent
sudo firewall-cmd --add-port=6447/tcp --permanent
sudo firewall-cmd --reload

# 拉取镜像
sudo docker pull docker.1ms.run/mysql:8.0 \
  && docker tag docker.1ms.run/mysql:8.0 mysql:8.0 \
  && docker rmi docker.1ms.run/mysql:8.0
sudo docker pull docker.1ms.run/mysql:8.0 \
  && docker tag docker.1ms.run/mysql:8.0 mysql:8.0 \
  && docker rmi docker.1ms.run/mysql:8.0
# 时间同步：确保三台机器启用 NTP/chrony，同步良好（MGR 对时钟敏感）。
```

**生成UUID**

```bash
python3 - <<'PY'
import uuid; print(str(uuid.uuid4()))
PY
```

**生成my.cnf**

```bash
cat | sudo tee /opt/mysql/conf/my.cnf >/dev/null <<'CNF'
[mysqld]
server_id=105		# 这个需要更改为服务器ip
gtid_mode=ON
enforce_gtid_consistency=ON
log_bin=binlog
binlog_format=ROW
relay_log_recovery=ON
master_info_repository=TABLE
relay_log_info_repository=TABLE

# Group Replication 基础
transaction_write_set_extraction=XXHASH64
binlog_checksum=CRC32
disabled_storage_engines=""
plugin_load_add='group_replication.so'

# 组复制参数（注意全部节点 group_name 一致）
loose-group_replication_group_name="11111111-2222-3333-4444-555555555555"
loose-group_replication_start_on_boot=OFF
loose-group_replication_local_address="172.16.1.105:33061"	# 这个需要更改为服务器ip
loose-group_replication_group_seeds="172.16.1.105:33061,172.16.1.106:33061,172.16.1.107:33061"
loose-group_replication_bootstrap_group=OFF

# 建议
innodb_flush_log_at_trx_commit=1
sync_binlog=1

# 日志
log_error=/var/log/mysql/error.log
CNF
```

**初始化脚本**

```bash
cat | sudo tee /opt/mysql/init/01_init.sql >/dev/null <<'SQL'
-- 创建管理员账户（用于建集群）
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'StrongAdmin!123';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%' WITH GRANT OPTION;

-- 便于 Shell 检查
CREATE USER IF NOT EXISTS 'clustercheck'@'%' IDENTIFIED BY 'ClusterCheck!123';
GRANT SELECT, PROCESS, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'clustercheck'@'%';
FLUSH PRIVILEGES;
SQL
```

**赋予权限**

```bash
# 这两个目录需要容器内 mysql(999:999) 可写
sudo chown -R 999:999 /opt/mysql/data /opt/mysql/logs
sudo chmod -R u+rwX,g+rwX /opt/mysql/data /opt/mysql/logs

# 这两个目录只需可读
sudo chown -R root:root /opt/mysql/conf /opt/mysql/init
sudo chmod -R 755 /opt/mysql/conf /opt/mysql/init

# 确保父目录也可进入
sudo chmod 755 /opt/mysql
```

**启动MySql**

```bash
sudo docker run -d --name mysql-105 --restart unless-stopped \
  --network host \
  -e MYSQL_ROOT_PASSWORD='StrongRoot!123' \
  -v /opt/mysql/data:/var/lib/mysql \
  -v /opt/mysql/conf:/etc/mysql/conf.d \
  -v /opt/mysql/init:/docker-entrypoint-initdb.d \
  -v /opt/mysql/logs:/var/log/mysql \
  mysql:8.0
  
# 查看
docker logs mysql-105 | tail
```

**启动mysqlsh**

```bash
sudo docker run -it --rm --network host --name mysqlsh \
  -v /root/.mysqlsh:/root/.mysqlsh \
  mysql/mysql-shell:8.0 \
  mysqlsh
```

