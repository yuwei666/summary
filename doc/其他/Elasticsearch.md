## 部署单点es

[部署参考](https://blog.csdn.net/Ying_ph/article/details/136586656) [设置修改账号密码](https://blog.csdn.net/Ying_ph/article/details/136737346)

```shell

# 创建网络
docker network create es-net
# 拉取镜像 docker.domys.cc是国内镜像源
docker pull docker.domys.cc/elasticsearch:7.17.28
# 运行容器 --privileged 授予逻辑卷访问权
docker run -d \
	--name es \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -e "discovery.type=single-node" \
    -v es-data:/usr/share/elasticsearch/data \
    -v es-plugins:/usr/share/elasticsearch/plugins \
    --privileged \
    --network es-net \
    -p 9200:9200 \
    -p 9300:9300 \
elasticsearch:7.17.28
```

启动成功后，访问 ip:9200，收到返回值

```
{
  "name" : "6068d86e1781",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "pHB6rsqSRu6VBAUNWfJSwQ",
  "version" : {
    "number" : "7.17.28",
    "build_flavor" : "default",
    "build_type" : "docker",
    "build_hash" : "139cb5a961d8de68b8e02c45cc47f5289a3623af",
    "build_date" : "2025-02-20T09:05:31.349013687Z",
    "build_snapshot" : false,
    "lucene_version" : "8.11.3",
    "minimum_wire_compatibility_version" : "6.8.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```

## 部署kibana

需要部署[kibana](https://so.csdn.net/so/search?q=kibana&spm=1001.2101.3001.7020)容器，因此需要让es和kibana容器互联。  kibana可以给我们提供一个elasticsearch的可视化界面，便于我们学习。 

```
# docker.domys.cc是国内镜像源
docker pull docker.domys.cc/kibana:7.12.1

# 运行容器
docker run -d \
--name kibana \
-e ELASTICSEARCH_HOSTS=http://es:9200 \
--network=es-net \
-p 5601:5601  \
docker.domys.cc/kibana:7.17.28

# 查看日志
docker logs kibana | grep "5601"
```

看到结果："message":"http server running at http://0.0.0.0:5601"} 启动成功，访问 http://ip:5601 



## 安装分词器

```shell
# 查询卷，没有安装任何插件
docker volume inspect es-plugins
```

```
[
    {
        "CreatedAt": "2025-03-03T16:28:44+08:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/es-plugins/_data",
        "Name": "es-plugins",
        "Options": null,
        "Scope": "local"
    }
]
```



```shell
# 安装插件
docker exec -it es ./bin/elasticsearch-plugin  install  https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-7.17.28.zip

# 重启服务
docker restart es

# 查看日志
docker logs es | grep "analysis-ik"
```

成功

```
{"type": "server", "timestamp": "2025-03-03T09:49:27,352Z", "level": "INFO", "component": "o.e.p.PluginsService", "cluster.name": "docker-cluster", "node.name": "6068d86e1781", "message": "loaded plugin [analysis-ik]" }
```

### 测试分词器

http://ip:5601/app/dev_tools#/console

#### 标准分词

```
POST /_analyze
{
  "analyzer":"standard",
  "text":"你曾爱我"
}
```

```
{
  "tokens" : [
    {
      "token" : "你",
      "start_offset" : 0,
      "end_offset" : 1,
      "type" : "<IDEOGRAPHIC>",
      "position" : 0
    },
    {
      "token" : "曾",
      "start_offset" : 1,
      "end_offset" : 2,
      "type" : "<IDEOGRAPHIC>",
      "position" : 1
    },
    {
      "token" : "经",
      "start_offset" : 2,
      "end_offset" : 3,
      "type" : "<IDEOGRAPHIC>",
      "position" : 2
    },
    {
      "token" : "不",
      "start_offset" : 3,
      "end_offset" : 4,
      "type" : "<IDEOGRAPHIC>",
      "position" : 3
    },
    {
      "token" : "爱",
      "start_offset" : 4,
      "end_offset" : 5,
      "type" : "<IDEOGRAPHIC>",
      "position" : 4
    },
    {
      "token" : "我",
      "start_offset" : 5,
      "end_offset" : 6,
      "type" : "<IDEOGRAPHIC>",
      "position" : 5
    }
  ]
}
```



#### 最细粒度查询 

```
POST /_analyze
{
  "analyzer":"ik_max_word",
  "text":"你曾经说过爱我的"
}
```

```
{
  "tokens" : [
    {
      "token" : "你",
      "start_offset" : 0,
      "end_offset" : 1,
      "type" : "CN_CHAR",
      "position" : 0
    },
    {
      "token" : "曾经",
      "start_offset" : 1,
      "end_offset" : 3,
      "type" : "CN_WORD",
      "position" : 1
    },
    {
      "token" : "不爱",
      "start_offset" : 3,
      "end_offset" : 5,
      "type" : "CN_WORD",
      "position" : 2
    },
    {
      "token" : "爱我",
      "start_offset" : 4,
      "end_offset" : 6,
      "type" : "CN_WORD",
      "position" : 3
    }
  ]
}

```







