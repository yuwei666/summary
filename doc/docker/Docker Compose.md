### Docker Compose

 Compose 项目是 Docker 官方的开源项目， 实现对。 例如要实现一个 Web 项目，除了 Web 服务容器本身，往往还需要再加上后端的数据库服务容器，甚至还包括负载均衡容器等。 

>  快速编排：站在项目角度将一组相关联容器整合在一起，对这组容器按照指定顺序进行启动。 

安装：Docker Compose 随 Docker一起安装



#### 命令

```bash
-- 所有服务上线 -d 后台
docker compose up -d

-- 所有服务下线
docker compose down

-- 启动/停止服务 服务名称
docker compose start/stop name1 name2 

-- 扩容 服务1 × 3，如之前1个实例，现在启动3个实例
docker compose scale name1=3

```



#### 示例

```dockerfile
version: "3.8"

services:
  # Nacos 服务
  nacos:
    image: nacos/nacos-server:latest
    container_name: nacos
    ports:
      - "8848:8848"  # Nacos 控制台端口
    environment:
      - MODE=standalone  # 单机模式
    volumes:
      - nacos_logs:/home/nacos/logs
    networks:
      - my-network

  # MySQL 服务
  mysql:
    image: mysql:8.0.21
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: mydb
      MYSQL_USER: myuser
      MYSQL_PASSWORD: mypassword
    volumes:
		- ./init.sql:/docker-entrypoint-initdb.d/init.sql  # 初始化脚本
      - mysql_data:/var/lib/mysql
    networks:
      - my-network

  # FastDFS 服务
  fastdfs:
    image: delron/fastdfs:latest
    container_name: fastdfs
    ports:
      - "22122:22122"  # Tracker 端口
      - "23000:23000"  # Storage 端口
    volumes:
      - fastdfs_data:/var/fdfs
    networks:
      - my-network

  # Redis 服务
  redis:
    image: redis:latest
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - my-network

  # Spring Boot 服务 1
  springboot-service1:
    build:
      context: ./springboot-service1  # Spring Boot 服务 1 的 Dockerfile 路径
      dockerfile: Dockerfile
    container_name: springboot-service1
    ports:
      - "8081:8080"  # 服务 1 的端口
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      - mysql
      - redis
      - nacos
    networks:
      - my-network

  # Spring Boot 服务 2
  springboot-service2:
    build:
      context: ./springboot-service2  # Spring Boot 服务 2 的 Dockerfile 路径
      dockerfile: Dockerfile
    container_name: springboot-service2
    ports:
      - "8082:8080"  # 服务 2 的端口
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      - mysql
      - redis
      - nacos
    networks:
      - my-network

# 定义网络
networks:
  my-network:
    driver: bridge

# 定义卷
volumes:
  nacos_logs:
  mysql_data:
  fastdfs_data:
  redis_data:
```

