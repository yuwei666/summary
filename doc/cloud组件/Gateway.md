### 基本概念

Spring Gateway网关框架，Spring Cloud中的组件。底层使用reactor-netty响应式编程组件，底层使用netty通讯框架（和dubbo、Redis一样?）。

Spring Cloud Gateway 包含三个重要的部分

#### 路由（Route）

网关的基础部分，路由包含 ID、URI、一组谓语和一组过滤器，通过谓语返回 `true` 来匹配路由。

#### 谓语（Predicate）

通过 Java 8 的 `Predicate` 接口实现，可以通过任何方式匹配路由，例如请求头、参数等。

#### 过滤器（Filter）

通常以过滤器链的形式存在，可以访问和修改请求和响应，业务逻辑一般在过滤器中实现。

#### 工作流程

当客户端请求 Spring Cloud Gateway，首先通过网关的 Handler Mapping 匹配路由，匹配通过后该请求会发送到网关的 Web Handler，这个 Handler 会运行一组特定的过滤器链。 



### Gateway和Nginx区别

初识Gateway，觉得它和nginx做的工作差不多，但是具体使用场景还是有区别，在技术选型时，选择 Nginx 和 Spring Cloud Gateway（或简称为 Gateway）主要取决于具体应用场景和技术需求。下面是两者的一些关键差异和适用场景：

#### Nginx

1.  **定位与功能：** Nginx 是一个高性能的 Web 服务器和反向代理服务器，常被用作静态内容的服务器和[负载均衡器](https://cloud.tencent.com/product/clb?from_column=20065&from=20065)。它支持HTTP、HTTPS、SMTP、POP3和IMAP协议，适合处理静态资源、SSL终止、HTTP压缩等任务。 
2. 使用场景：
   - **静态资源服务：** 直接提供静态文件如HTML、图片、CSS等。
   - **反向代理：** 将客户端请求转发给后端服务器，隐藏后端架构细节。
   - [**负载均衡**](https://cloud.tencent.com/product/clb?from_column=20065&from=20065)**：** 分配请求到多个后端服务器，提高系统的可用性和扩展性。
   - **安全控制：** 实现基本的访问控制、SSL/TLS加密等安全措施。

#### Spring Cloud Gateway

1.  **定位与功能：** Spring Cloud Gateway 是Spring Cloud生态中的API网关，专为[微服务架构](https://cloud.tencent.com/product/tse?from_column=20065&from=20065)设计。它提供了动态路由、过滤器机制以及集成Spring Cloud DiscoveryClient的服务发现能力，便于实现复杂的API管理需求。 
2. 使用场景：
   - **微服务架构：** 在微服务环境中作为统一的API入口，负责路由、转发、过滤和鉴权等。
   - **动态路由：** 支持根据请求内容动态路由到不同服务，适用于复杂的服务调用逻辑。
   - **高级API管理：** 利用过滤器机制实现API限流、熔断、日志记录、鉴权等高级功能。
   - **与Spring Cloud生态集成：** 紧密集成Spring Cloud服务发现，方便管理和发现微服务实例。

#### 选择建议

- 如果项目是一个传统的Web应用，或者需要处理大量的静态内容，同时需要基础的反向代理和负载均衡功能，Nginx可能是更好的选择。
- 对于基于微服务架构的系统，特别是那些已经采用Spring Cloud全家桶的项目，Spring Cloud Gateway因其强大的API管理功能和与Spring生态的紧密集成而更为适合。
- 如果应用场景需要在API层面实现复杂逻辑，如动态路由、细粒度的鉴权和过滤等，Spring Cloud Gateway能够提供更灵活的解决方案。
- 在某些情况下，两者也可以结合使用，Nginx作为最外层的反向代理处理静态内容和初步的负载均衡，而Spring Cloud Gateway则部署在内部作为微服务的入口，处理更精细的API管理任务。



