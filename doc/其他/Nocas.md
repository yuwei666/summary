## Nacos

>  Nacos /nɑ:kəʊs/ 是 Dynamic Naming and Configuration Service的首字母简称 ， 一个更易于构建云原生应用的动态服务发现、[配置管理](https://so.csdn.net/so/search?q=配置管理&spm=1001.2101.3001.7020)和服务管理平台。 

优点：

易⽤： 简单的数据模型， 标准的 restfulAPI， 易用的控制台， 丰富的使用文档。
稳定： 99.9% 高可用， 脱胎于历经阿里巴巴 10 年生产验证的内部产品， 支持具有数百万服务的大规模场景， 具备企业级 SLA 的开源产品。
实时： 数据变更毫秒级推送生效； 1w 级， SLA 承诺 1w 实例上下线 1s， 99.9% 推送完成； 10w级， SLA 承诺 1w 实例上下线 3s， 99.9% 推送完成； 100w 级别， SLA 承诺 1w 实例上下线 9s，99.9% 推送完成。
规模： 十万级服务/配置， 百万级连接， 具备强大扩展性。

Nacos主要功能有以下两种：注册中心、配置中心。

### 配置中心

配置中心是一种集中化管理配置的服务。主要作用：

+ 集中管理配置信息

  配置中心将不同服务的配置信息集中管理，实现了配置中心的集中管理

+ 动态更新配置

  通过操作界面或api进行动态配置更新，消除了配置变更时重新部署应用和服务的需要，让配置管理更高效敏捷

+ 配置信息共享

  将配置信息配置在配置中心，不同的服务实例可以共享同一套配置信息

+ 配置信息安全

  对配置信息进行安全管理，权限管理

+ 配置快照

  支持配置信息的版本管理，历史记录等功能

### 注册中心

注册中心微服务架构中的一个组件，用于不同服务实例的注册与发现。主要作用：

+ 服务注册

  服务实例启动的时候，将自身信息注册到注册中心，包括服务名称，地址，端口等

+ 服务发现

  消费者向注册中心查询服务，并获取服务实例信息来访问服务

+ 服务健康检查

  定期检查服务实例健康状况，过滤不健康实例

+ 服务路由

  提供服务的路由和负载均衡功能（同nginx？？？）

+ 服务监控

  统计服务调用次数，时长等，用于监控服务状态

+ 服务更新

  当服务实例信息变更时，向注册中心发送更新信息通知

## 安装

Nacos有三种部署方式，单机，集群，多集群，[安装](https://blog.csdn.net/weixin_60781793/article/details/134541640)[Nacos](https://nacos.io/download/nacos-server/)后，本地访问localhost:8848/nacos即可访问控制台，也可以配置nginx，

```nginx
	location /nacos/ {
        proxy_pass  http://127.0.0.1:8848;
    } 
```
Nacos单机模式下默认使用的数据源是内置的嵌入式数据库Derby作为数据库，但是Derby不适合承载生产环境大规模部署，因为有以下限制： 

1. 数据存储容量只有2GB;
2. 不支持集群模式下的高可用复制;
3. 性能和并发能力有限制

 因此在生产环境中使用单机模式时，可以使用外置数据库作为数据存储，例如MySQL。 

在数据库中执行[建表sql](https://github.com/alibaba/nacos/blob/master/distribution/conf/mysql-schema.sql)后，修改nacos\conf\application.properties配置文件，重启服务即可

```properties
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=root
```

