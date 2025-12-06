### 准备工作

```
# 加入overlay网络（前提是已经swarm init）
docker network create -d overlay --attachable nacos_network

# 新建数据库nacos，并创建登陆用户
CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
# [建表](https://github.com/alibaba/nacos/blob/master/distribution/conf/mysql-schema.sql)
CREATE USER IF NOT EXISTS 'nacos'@'%' IDENTIFIED BY 'nacos';
GRANT ALL PRIVILEGES ON nacos.* TO 'nacos'@'%';
FLUSH PRIVILEGES;
```



### 启动容器

分别在三台服务器上执行，设置环境变量即可，无需挂载目录；三台设备注意开放端口。

具体参数参考[中文文档](https://github.com/nacos-group/nacos-docker/blob/master/README_ZH.md)

#### 不开启鉴权

```bash
docker run -d \
  --name nacos \
  --restart unless-stopped \
  --network nacos_network \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  -e MODE=cluster \
  -e NACOS_SERVERS=172.16.1.105:8848,172.16.1.106:8848,172.16.1.107:8848 \
  -e NACOS_SERVER_IP=172.16.1.105 \
  -e SPRING_DATASOURCE_PLATFORM=mysql \
  -e MYSQL_SERVICE_HOST=172.16.1.105 \
  -e MYSQL_SERVICE_PORT=4002 \
  -e MYSQL_SERVICE_DB_NAME=nacos \
  -e MYSQL_SERVICE_USER=nacos \
  -e MYSQL_SERVICE_PASSWORD=nacos \
  -e NACOS_AUTH_ENABLE=true \
  -e NACOS_AUTH_TOKEN=8e9f27a83e8d4e9b8a7f6e5d4c3b2a1e8e9f27a83e8d4e9b8a7f6e5d4c3b2a1e \
  -e NACOS_AUTH_IDENTITY_KEY=nacos \
  -e NACOS_AUTH_IDENTITY_VALUE=nacos \
  -e TZ=Asia/Shanghai \
  nacos/nacos-server:v2.3.0
```

+ -e NACOS_SERVER_IP=172.16.1.105

  这里需要更改为三台设备的ip

  

#### 开启鉴权

```bash
# 启动docker，开启鉴权
docker run -d \
  --name nacos \
  --restart unless-stopped \
  --network nacos_network \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  -e MODE=cluster \
  -e NACOS_SERVERS=172.16.1.105:8848,172.16.1.106:8848,172.16.1.107:8848 \
  -e NACOS_SERVER_IP=172.16.1.107 \
  -e SPRING_DATASOURCE_PLATFORM=mysql \
  -e MYSQL_SERVICE_HOST=172.16.1.105 \
  -e MYSQL_SERVICE_PORT=4002 \
  -e MYSQL_SERVICE_DB_NAME=nacos \
  -e MYSQL_SERVICE_USER=nacos \
  -e MYSQL_SERVICE_PASSWORD=nacos \
  -e NACOS_AUTH_ENABLE=true \
  -e NACOS_AUTH_TOKEN=8e9f27a83e8d4e9b8a7f6e5d4c3b2a1e8e9f27a83e8d4e9b8a7f6e5d4c3b2a1e \
  -e NACOS_AUTH_IDENTITY_KEY=nacos \
  -e NACOS_AUTH_IDENTITY_VALUE=nacos \
  -e TZ=Asia/Shanghai \
  nacos/nacos-server:v2.3.0
```

+ -e NACOS_AUTH_TOKEN #支持自定义，需要>32位



#### 登陆提示报错

开启鉴权后，使用nacos/nacos若无法登陆，需要在数据库中添加用户和角色

```
INSERT INTO nacos.users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 1);

-- 给用户分配 ROLE_ADMIN 角色
INSERT INTO nacos.roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');
```



