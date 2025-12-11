## Redis Cluster

Redis Cluster (去中心化)，是目前最推荐、也是最主流的方案（Redis 3.0+ 引入）。它采用了去中心化的架构，每个节点都存储数据和集群状态。

**核心原理：**

- **Hash Slot（哈希槽）：** 引入了 16384 个槽位。数据通过 `CRC16(key) % 16384` 决定放入哪个槽，而槽被分配在不同的节点上。
- **Gossip 协议：** 节点之间通过 Gossip 协议交换状态信息，感知新节点加入或故障。
- **Smart Client：** 客户端连接集群时，会缓存槽位映射关系，直接请求目标节点。

**优点：**

- **无中心架构：** 没有代理层，性能损耗小。
- **自动化：** 支持自动分片、自动故障转移（Failover）。
- **官方支持：** 社区活跃，功能更新快。

**缺点：**

- **客户端复杂：** 客户端需要支持 Cluster 协议（现在主流语言的 SDK 都支持得很好）。
- **多 Key 操作受限：** 跨槽位的事务（Transaction）和多 Key 操作（如 `MGET`）比较麻烦，通常要求 key 必须在同一个槽（可以通过 Hash Tag `{user}:1` 解决）。



### 准备工作

#### 创建目录

```bash
# 创建目录
mkdir -p /data/redis-cluster/{7001,7002}/data
# 创建配置文件
vim /data/redis-cluster/docker-compose.yml
vim /data/redis-cluster/7001/redis.conf
vim /data/redis-cluster/7002/redis.conf

# 启动
cd /data/redis-cluster
docker compose up -d
```

#### docker-compose.yml

```yaml
services:
  # 实例 1 (Master/Slave 候选)
  redis-node-1:
    image: redis:7.0
    container_name: redis-7001
    network_mode: "host"   # 直接使用宿主机网络
    volumes:
      - ./7001/redis.conf:/usr/local/etc/redis/redis.conf
      - ./7001/data:/data
    command: redis-server /usr/local/etc/redis/redis.conf
    restart: always        # 开机自启

  # 实例 2 (Master/Slave 候选)
  redis-node-2:
    image: redis:7.0
    container_name: redis-7002
    network_mode: "host"
    volumes:
      - ./7002/redis.conf:/usr/local/etc/redis/redis.conf
      - ./7002/data:/data
    command: redis-server /usr/local/etc/redis/redis.conf
    restart: always
```

#### redis.conf

##### Master

```
port 7001
# 必须绑定当前宿主机具体的局域网 IP，不要写 127.0.0.1
bind 172.16.1.105
protected-mode no
daemonize no
dir /data

# ---------------------------------------------------
# 持久化配置
# ---------------------------------------------------
appendonly yes
# AOF 刷盘策略：每秒刷盘（最高性能和数据安全性的平衡）
appendfsync everysec
# 启用 RDB 和 AOF 混合持久化 (Redis 4.0+ 推荐)
aof-use-rdb-preamble yes

# RDB 快照配置（可保留默认，作为AOF的补充）
save 900 1
save 300 100
save 60 10000

# ---------------------------------------------------
# 内存配置 ( Slave 节点不需要 maxmemory )
# ---------------------------------------------------
# 必须小于容器或宿主机的实际内存，防止 OOM Killer
maxmemory 4gb
# 内存淘汰策略：推荐 LFU（最少使用频率）
maxmemory-policy allkeys-lfu

# ---------------------------------------------------
# 集群与安全配置
# ---------------------------------------------------
# 集群配置
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
cluster-announce-ip 172.16.1.105

# 密码（必填）
masterauth "redis"
requirepass "redis"
```

##### Slave 节点

不需要设置 `maxmemory` 或 `maxmemory-policy`，因为它们的数据是 Master 复制过来的。

```
port 7002
bind 172.16.1.105
protected-mode no
daemonize no
dir /data

# ---------------------------------------------------
# 持久化配置
# ---------------------------------------------------
appendonly yes
# 新增：确保 AOF 刷盘频率
appendfsync everysec
# 新增：启用混合持久化，加速恢复
aof-use-rdb-preamble yes
# RDB 快照（可选，但推荐保留）
save 900 1
save 300 100
save 60 10000

# ---------------------------------------------------
# 集群与安全配置
# ---------------------------------------------------
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
cluster-announce-ip 172.16.1.105

masterauth "redis"
requirepass "redis"
```



#### 创建集群

```bash
# 进入任一节点
docker exec -it redis-7001 bash

# "redis" 这里使用上面的密码
redis-cli -a "redis" --cluster create \
172.16.1.105:7001 172.16.1.105:7002 \
172.16.1.106:7001 172.16.1.106:7002 \
172.16.1.107:7001 172.16.1.107:7002 \
--cluster-replicas 1
# 输入yes
```

创建信息：

```tex
>>> Performing hash slots allocation on 6 nodes...
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383
Adding replica 172.16.1.106:7002 to 172.16.1.105:7001
Adding replica 172.16.1.107:7002 to 172.16.1.106:7001
Adding replica 172.16.1.105:7002 to 172.16.1.107:7001
M: 2f4bae53cd9fb9a3ee9d1703601307366df3b320 172.16.1.105:7001
   slots:[0-5460] (5461 slots) master
S: 3b48583aec6f2266173483822a2319e93d3ad1bc 172.16.1.105:7002
   replicates 897a6c97f2e2bb833a437ea75853bdcf8577fada
M: 9a41246bf0259ca2292cc60d0fb948e065e63312 172.16.1.106:7001
   slots:[5461-10922] (5462 slots) master
S: 8c7787d240204973ae2bcda69310f02e7e65aef5 172.16.1.106:7002
   replicates 2f4bae53cd9fb9a3ee9d1703601307366df3b320
M: 897a6c97f2e2bb833a437ea75853bdcf8577fada 172.16.1.107:7001
   slots:[10923-16383] (5461 slots) master
S: 9157453e13cc6121b5be18f98747e5102f9d068c 172.16.1.107:7002
   replicates 9a41246bf0259ca2292cc60d0fb948e065e63312
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
.
>>> Performing Cluster Check (using node 172.16.1.105:7001)
M: 2f4bae53cd9fb9a3ee9d1703601307366df3b320 172.16.1.105:7001
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
M: 9a41246bf0259ca2292cc60d0fb948e065e63312 172.16.1.106:7001
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
M: 897a6c97f2e2bb833a437ea75853bdcf8577fada 172.16.1.107:7001
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: 8c7787d240204973ae2bcda69310f02e7e65aef5 172.16.1.106:7002
   slots: (0 slots) slave
   replicates 2f4bae53cd9fb9a3ee9d1703601307366df3b320
S: 9157453e13cc6121b5be18f98747e5102f9d068c 172.16.1.107:7002
   slots: (0 slots) slave
   replicates 9a41246bf0259ca2292cc60d0fb948e065e63312
S: 3b48583aec6f2266173483822a2319e93d3ad1bc 172.16.1.105:7002
   slots: (0 slots) slave
   replicates 897a6c97f2e2bb833a437ea75853bdcf8577fada
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```



#### 验证

```bash
docker exec -it redis-7001 bash
# 连接到集群
redis-cli -c -h 172.16.1.105 -p 7001 -a "redis"
# 进入 CLI 后，运行：
cluster nodes
```

```tex
9a41246bf0259ca2292cc60d0fb948e065e63312 172.16.1.106:7001@17001 master - 0 1765447236869 3 connected 5461-10922
897a6c97f2e2bb833a437ea75853bdcf8577fada 172.16.1.107:7001@17001 master - 0 1765447236000 5 connected 10923-16383
8c7787d240204973ae2bcda69310f02e7e65aef5 172.16.1.106:7002@17002 slave 2f4bae53cd9fb9a3ee9d1703601307366df3b320 0 1765447235864 1 connected
9157453e13cc6121b5be18f98747e5102f9d068c 172.16.1.107:7002@17002 slave 9a41246bf0259ca2292cc60d0fb948e065e63312 0 1765447236568 3 connected
2f4bae53cd9fb9a3ee9d1703601307366df3b320 172.16.1.105:7001@17001 myself,master - 0 1765447235000 1 connected 0-5460
3b48583aec6f2266173483822a2319e93d3ad1bc 172.16.1.105:7002@17002 slave 897a6c97f2e2bb833a437ea75853bdcf8577fada 0 1765447237000 5 connected
```

所有 6 个节点都已连接，并且拓扑结构是完美的 **3 主 3 从交叉备份**，全部 16384 个槽位被 Master 覆盖。

| **节点 IP**           | **角色**   | **负责的槽位** | **备份目标/备份源** | **交叉备份检查** |
| --------------------- | ---------- | -------------- | ------------------- | ---------------- |
| **172.16.1.105:7001** | **Master** | 0 - 5460       | 备份源：106:7002    | **✅ 安全**       |
| **172.16.1.106:7001** | **Master** | 5461 - 10922   | 备份源：107:7002    | **✅ 安全**       |
| **172.16.1.107:7001** | **Master** | 10923 - 16383  | 备份源：105:7002    | **✅ 安全**       |
| 172.16.1.106:7002     | Slave      | -              | 备份目标：105:7001  | ✅                |
| 172.16.1.107:7002     | Slave      | -              | 备份目标：106:7001  | ✅                |
| 172.16.1.105:7002     | Slave      | -              | 备份目标：107:7001  | ✅                |



#### 遇到问题

| **节点 IP:Port**      | **角色**        | **负责的 Hash Slots** | **复制目标 (Master IP)** | **HA 检查**                                      |
| --------------------- | --------------- | --------------------- | ------------------------ | ------------------------------------------------ |
| **172.16.1.105:7001** | Master (myself) | 0 - 5460              | -                        | **有 Slave 备份 (在 106)**                       |
| **172.16.1.106:7001** | Master          | 5461 - 10922          | -                        | **❌ 缺少 Slave 备份！**                          |
| **172.16.1.107:7001** | Master          | 10923 - 16383         | -                        | **❌ 缺少 Slave 备份！**                          |
| 172.16.1.106:7002     | Slave           | -                     | **172.16.1.105:7001**    | **✅ 交叉备份成功 (Slave 在 106，Master 在 105)** |

发现集群目前处于 **不完全高可用**状态，因为有两个 Slave 节点没有出现在集群视图中。以为又是防火墙的问题，被防火墙逼疯了，我直接三台服务器的防火墙全都关了。

```bash
# 清数据
docker compose down -v
rm -r /data/redis-cluster/{7001,7002}/data/*
docker compose up -d

# 重新创建集群
docker exec -it redis-7001 bash

redis-cli -a "redis" --cluster create \
172.16.1.105:7001 172.16.1.105:7002 \
172.16.1.106:7001 172.16.1.106:7002 \
172.16.1.107:7001 172.16.1.107:7002 \
--cluster-replicas 1
# 输入yes
```

重启创建集群后，发现不行，看了启动日志，发现很关键的一行

```
Could not connect to Redis at 172.16.1.200:7002: Connection refused
```

`172.16.1.200` 是keepalived使用的虚拟IP，在我的`redis.conf` 没有指定 `cluster-announce-ip`，Redis 会尝试自动选择它能找到的路由 IP。`105` 服务器当前是 Keepalived 的 Master，那么 `172.16.1.200` 这个 VIP 会绑定在 `105` 的网卡上，所以使用虚拟IP加入集群就会报错。

解决方案

```tex
cluster-announce-ip 172.16.1.105
```



#### 扩展

##### 如何保证高可用的

交叉备份 (Cross-Replication) 是指将 Master 节点和它的 Slave 节点部署在不同的物理服务器上。交叉备份的初次分配和故障转移都是 Redis Cluster 自动完成的，无需手动干预。



##### 需要手动操作的场景（维护）

| **场景**            | **目的**                                                     | **操作命令**                                                 |
| ------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **集群扩容/缩容**   | 添加/移除 Master 节点，迁移 Hash Slot。                      | `CLUSTER MEET` + `CLUSTER ADDSLOTS` 或使用 `redis-cli --cluster reshard`。 |
| **补充 Slave 节点** | Master 发生故障，Slave 晋升为 Master 后，你需要为新的 Master 补充一个新的 Slave 节点。 | `CLUSTER MEET` (加入新节点) + `CLUSTER REPLICATE <Master ID>` (指定复制目标)。 |
| **手动故障转移**    | 在计划性维护（如 Master OS 升级）前，主动触发 Master 切换。  | `CLUSTER FAILOVER`                                           |



##### 为什么是 16384 个槽位

+ 16384 个槽位需要 16384 个比特（Bit）。

- 16384 bits ÷ 8 bits/Byte = **2048 字节 (2 KB)**。

Redis 的设计者认为 **2KB** 是一个合理的大小，可以放在每个心跳包中。



##### 出现密码警告

```bash
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
```

当你直接在命令行里使用 `-a "redis"` 传递密码时，这个密码是**明文**写在命令里的。

在 Linux 系统中，任何用户如果此时输入 `ps -ef` (查看进程列表)，都可以完整地看到你刚才敲的这条命令，包括你的密码：

```bash
ps -ef
```

在极其严格的生产环境中，为了不让密码出现在 `ps` 进程列表中，通常使用 **环境变量** 的方式来传递密码：

```bash
# 先设置环境变量
export REDISCLI_AUTH="你的强密码"

# 然后执行命令（不需要再加 -a 参数）
redis-cli --cluster create 172.16.1.105:7001 ...

redis-cli -c -h 172.16.1.105 -p 7001 -a "redis" cluster nodes
```

