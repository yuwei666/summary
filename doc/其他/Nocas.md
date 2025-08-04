## Nacos介绍

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

启动命令

```
cd F:\java\nacos1.4.7\bin

# windows下启动
./startup.cmd
```

Docker 部署nacos
+ 拉取镜像

```docker
docker pull nacos/nacos-server:latest 
```

+ 启动容器

启动容器需要对3个端口进行映射，分别是8848,9848,9849。
如果不开启三个端口，那么服务无法进行注册








## bootstrap.yml的作用

在**Nacos**项目中，`bootstrap.yml` 用于加载应用程序启动时的配置，`application.yml`  用于加载应用程序运行时的配置，所以`bootstrap.yml` 优先级更高。 如果 `bootstrap.yml` 中已经包含了所有必要的配置，那么 `application.yml` 就不是必需的。 

Nacos 作为一个配置中心，通常会在 `bootstrap.yml` 中配置 Nacos 服务器的地址、命名空间、分组等信息。应用启动时，会从 Nacos 中拉取配置，这些配置会覆盖或补充本地配置（如 `application.yml`）。 

在微服务架构中，通常会将配置集中管理在 Nacos 中，而不是分散在各个服务的 `application.yml` 中。因此，`bootstrap.yml` 的作用就是告诉应用从哪里加载配置。 

非Nacos项目，**没有 Spring Cloud 依赖时**：`bootstrap.yml` 不会被加载，所有配置都需要定义在 `application.yml` 中。

### **什么情况下需要 `application.yml`？**

虽然可以只有 `bootstrap.yml`，但在以下情况下，你可能仍然需要 `application.yml`：

- **本地开发和测试**：在本地开发时，可能不需要连接 Nacos，可以直接在 `application.yml` 中定义配置。
- **Nacos 不可用时的 fallback 配置**：如果 Nacos 配置中心不可用，可以在 `application.yml` 中定义一些默认配置。
- **本地特有的配置**：某些配置可能只适用于本地环境，不适合放在 Nacos 中。

 `bootstrap.yml` 的特性（如配置中心） 

bootstrap.yml是 Spring Cloud 项目中，



### 使用

`bootstrap.yml`和`application.yml`使用一样，当指定了profiles

```yml
spring:
  application:
    name: payment-service
  profiles:
    active: dev
```

会依次读取

1. `bootstrap.yml`（或 `bootstrap.properties`）
2. `bootstrap-{profile}.yml`（如 `bootstrap-dev.yml`）
3. `application.yml`
4. `application-{profile}.yml`

如果存在 `bootstrap-dev.yml` 且激活了 `dev` 环境，`bootstrap-dev.yml` 会覆盖 `bootstrap.yml` 中的相同配置项。

## 常用注解

| 注解                            | 作用                        | 使用场景                      |
| :------------------------------ | :-------------------------- | :---------------------------- |
| `@EnableDiscoveryClient`        | 启用服务注册与发现          | 主启动类上启用 Nacos 服务发现 |
| `@NacosValue`                   | 注入 Nacos 配置值           | 动态获取配置项                |
| `@NacosConfigurationProperties` | 绑定 Nacos 配置到 Java 对象 | 将多个配置项映射到对象        |
| `@NacosConfigListener`          | 监听 Nacos 配置变化         | 配置变化时执行自定义逻辑      |
| `@FeignClient`                  | 声明 Feign 客户端           | 远程服务调用                  |
| `@RefreshScope`                 | 支持配置动态刷新            | 配置变化时重新加载 Bean       |

通过合理使用这些注解，可以轻松实现 Nacos 的服务注册与发现、动态配置管理等功能。





发布-订阅模型

```java
package com.rld.operation.support;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.api.naming.listener.EventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuwei
 * @desc
 * @date 2025/7/8
 */
@Component
@Slf4j
public class NacosEventSubscriber extends Subscriber<InstancesChangeEvent>
{

    @Resource
    private NacosServiceManager nacosServiceManager;

    @Resource
    private NacosServiceDiscovery nacosServiceDiscovery;


    @PostConstruct
    public void registerToNotifyCenter(){
        // 全局注册
        NotifyCenter.registerSubscriber(this);
        
        // 订阅指定服务名称
        NamingService namingService = nacosServiceManager.getNamingService();
        try {
            List<String> services = nacosServiceDiscovery.getServices();
            for (String service : services) {
                // 基于 Nacos 的 NamingService 服务订阅（服务粒度监听）。
                namingService.subscribe(service, new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        log.info("监听nacos的服务实例变化情况: {}", JSON.toJSONString(event));
                    }
                });
            }

        } catch (NacosException e) {
            log.error("监听nacos的服务实例变化情况失败", e);
        }
    }

    /**
     * 监听全局所有服务的实例变更事件
     * 基于 Nacos 的 NotifyCenter 发布-订阅模型
     * 任何服务发生实例变化（上线/下线/更新）时自动触发。
     * 通过 NotifyCenter.registerSubscriber(this) 全局注册。
     * 强类型订阅（只接收 InstancesChangeEvent）。
     */
    @Override
    public void onEvent(InstancesChangeEvent event) {
        log.info("监听nacos的服务实例变化情况: {}", JSON.toJSONString(event));
    }

    @Override
    public Class<? extends com.alibaba.nacos.common.notify.Event> subscribeType() {
        return InstancesChangeEvent.class;
    }

}

```















