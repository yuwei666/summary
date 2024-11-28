### 基础概念

gateway目的是代替Sleuth，但是项目中确实真实使用到了Sleuth，所以还是学习一下。

#### Sleuth

Spring Cloud Sleuth提供了一套完整的服务跟踪的解决方案。  他会将服务与服务之间的调用给记录起来。可以快速的知道调用 用户服务，到底涉及到了哪些微服务，方便我们快速排查问题！ 

具体功能：

1. 将跟踪和跨度 ID 添加到 Slf4J，因此您可以从日志聚合器中的给定跟踪或跨度中提取所有日志。
2.  检测来自 Spring 应用程序的公共入口和出口点（`servlet filter`, `rest template`, `scheduled actions`, `message channels`, `feign client`）。 
3.  如果`spring-cloud-sleuth-zipkin`可用，则应用程序将通过 HTTP生成和报告与Zipkin兼容的跟踪。默认情况下，它将它们发送到 localhost（端口 9411）上的 Zipkin 收集器服务。使用`spring.zipkin.baseUrl`配置Zipkin 服务的位置。  

#### Zipkin

Spring Cloud Sleuth对于分布式链路的跟踪仅仅是生成一些数据，这些数据不便于人类阅读，所以我们一般把这种跟踪数据上传给Zipkin Server，由Zipkin通过UI页面统一进行数据的展示。 

#### 链路监控相关术语

- **span（跨度）** ：工作的基本单位。例如，发送 RPC 是一个新的跨度，发送响应到 RPC 也是如此。Span还有其他数据，例如描述、时间戳事件、键值注释（标签）、导致它们的 Span 的 ID 和进程 ID（通常是 IP 地址）。跨度可以启动和停止，并且它们会跟踪它们的时间信息。创建跨度后，您必须在将来的某个时间点停止它。
- **Trace** ：一组跨度形成树状结构。
- **Annotation/Event** ：用于及时记录某个事件的存在，有如下事件类型：
- **cs** ：客户端发送。客户已提出请求。此注释指示跨度的开始。

- **sr** ：Server Received：服务器端收到请求并开始处理它。cs从此时间戳中减去时间戳揭示了网络延迟。
- **ss** ：服务器发送。在请求处理完成时注释（当响应被发送回客户端时）。从这个时间戳中减去sr时间戳，可以看出服务器端处理请求所需的时间。
- **cr** : 客户收到。表示跨度的结束。客户端已成功收到服务器端的响应。cs从这个时间戳中减去时间戳，可以看出客户端从服务器接收响应所需的全部时间。

当发送一个请求后，Sleuth日志如下，其中 [sleuth,1aef808b91840043,1aef808b91840043]  此条目对应于[application name,trace id, span id]

```
2024-11-25 09:21:02.531  INFO [sleuth,1aef808b91840043,1aef808b91840043] 5076 --- [nio-8989-exec-2] o.e.sleuth.controller.ExampleController  : Hello world!
```



[Sleuth](https://developer.aliyun.com/article/1203201#slide-0)