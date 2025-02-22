### Docker Compose

 Compose 项目是 Docker 官方的开源项目， 实现对。 例如要实现一个 Web 项目，除了 Web 服务容器本身，往往还需要再加上后端的数据库服务容器，甚至还包括负载均衡容器等。 

>  快速编排：站在项目角度将一组相关联容器整合在一起，对这组容器按照指定顺序进行启动。 

安装：Docker Compose 随 Docker一起安装





#### 命令

```
-- 所有服务上线 -d 后台
docker compose up -d

-- 所有服务下线
docker compose down

-- 启动/停止服务 服务名称
docker compose start/stop name1 name2 

-- 扩容 服务1 × 3，如之前1个实例，现在启动3个实例
docker compose scale name1=3

```

