### 常用MQ

#### activemq

万级并发，基于主从架构实现高可用。有低概率丢失数据。每秒几十万并发量不适用。非常成熟，功能强大，国内大量的公司及项目在使用。

社区不活跃，官网对activemq5.x维护越来越少。

#### rabbitmq

万级并发，基于erlang开发，性能好，延迟低（消息在mq中停留的时间短 ），基于主从架构实现高可用。适用中小公司，开源项目提供了非常完备的管理界面。

社区活跃高。

缺点：基础erlang开发，定制不容易，扩展集群复杂。

#### rocketmq

十万级并发，topic可以达到几百，可用性非常高。分布式架构，扩展起来方便些。经过配置，可以做到消息0丢失。alibaba开源，java开发，可以定制功能。开源社区黄了，自己也能接手。

社区活跃高。

#### kafka

单机十万级，topic多的时候，吞吐量会下降。经过配置，可以做到消息0丢失。

功能简单，吞吐量高，可以任意扩展。支持简单的mq功能，在大数据领域的实时计算以及日志采集被大规模使用。

总结：中小型公司建议rabbitmq，大型公司建议rocketmq，有实力投入一组工程师研究源码。大数据领域使用kafka，已成为业内标准。

#### 使用场景

异步处理场景：用户加入纪检条线后，需要发注册邮件和建行推送消息；工作流程发送到下一环节后，发送邮件和推送消息到当前处理人。



### 为什么要用MQ（优点）

解耦

传统系统之间耦合性太强。使用消息中间件，将消息写入队列，需要消息的系统自己订阅，不需要原来系统做任何修改。

异步

传统系统的逻辑以同步的方式运行，太消耗时间。使用中间件，非必要的逻辑以异步的形式运行，加快相应速度。

 削峰

传统模式，当并发量大的时候，所有请求直接怼到数据库，造成程序库异常。使用中间件，可以按照数据库能处理的并发量，从消息队列中拉取消息。在生产中，短暂的高峰期挤压是允许的。

### 缺点

系统可用性降低

系统引入的外部依赖越多，越容易挂掉。本来系统直接调用其他系统的接口就好了，现在加个MQ，MQ挂了，整套系统就崩溃了，风险增加了，系统可用性降低了。

系统复杂性增加

要多考虑很多方面的问题，比如如何保证处理消息丢失的情况，如何保证消息传递的顺序性，如何保证消息没有重复消费。

一致性问题

A系统处理完了直接返回成功了，BCD三个系统，有一个系统写库失败了，就产生了数据不一致的问题。

消息队列是一种非常复杂的架构，引入它有很多好处，但是也要针对坏处做各种额外的技术方案和架构来规避掉，系统复杂度提升了一个数量级。



### 如何保证高可用

#### rabbitmq

##### 普通集群模式

在一台实例上创建queue时，其他实例会保存这个queue的元数据。消费者可以连接集群上任何一台实例，消费指定queue时，如果连接实例中没有queue，会去拥有该queue的实例拿到消息，并返回给消费者。

缺点：

1. 可能会有rabbitmq集群内部产生大量的数据传输

2. 可用性无法保证，如果节点宕机，会导致queue数据丢失

##### 镜像集群模式

在一台实例上创建queue，其他实例会同时创建queue，在生产消息时，插入到queue的消息会同步到集群其他实例上（所有机器上都有queue的所有数据，只要queue数据更新，就会同步到其他所有机器上）。

优点：一台实例宕机，也不影响其他实例的使用。

问：如果queue的数量很大，达到了机器容量的上线，该怎么办？

 	在扩展机器也解决不了问题，唯有分布式存储

问：怎么开启镜像集群模式？

​	在rabbitmq中管理平台中，新增镜像集群策略。创建queue时，使用此策略，就会自动将数据同步到其他的节点上了。

##### 总结

rabbitmq的镜像集群相比普通集群，性能上会有所下降。节点之间同步的数据数据量较大的话内部网络带宽加重。消费的时候从任何一个节点都能消费到数据。 普通集群模式queue没有备份的初衷是想在增加机器的时候线性的增加性能 

#### kafka

基于副本和主从机制实现了高可用，每个partion的数据都在其从节点上存在副本，而主节点负责读写。当主节点宕机后，从节点会选举成主节点继续提供服务。 

kafka是分布式的，数据并不是都集中在一台机器上，可以分多个机器保存，这个和RabbitMQ不一样。 kafka的topic的partition多台机器上都有，并且同样的partition机器中，会有一台被选举为leader，其他的为follow。数据的交互式通过leader这台机器交互的，如果leader这台机器宕机了，follow机器会被选举为leader，然后这个继续运行。这样实现了高可用 

注： RocketMQ 介绍的就是借鉴 kafka 的思路实现的 

[topic、nameServer、broker相关概念的解释](https://blog.csdn.net/yuanchangliang/article/details/119155557)



### 如何保证消息不被重复消费（保证消息的幂等性）

#### 为什么会出现重复消费

 kafka消费端可能出现重复消费的问题：通过offset来记录数据的时序性，消费者会定期的返回数据处理到的具体位置到kafka，可能消费者处理了数据，但是还没来得及告诉给kafka，导致kafka以为数据没有消费。那么当消费者重启的时候，kafka就会把已经处理了的数据再次发给消费者，导致重复消费 

消息在使用后不会删除。你可以通过每个主题的配置设置来定义Kafka应该保留你的消息多长时间，之后旧的消息将被丢弃。 

#### 如何保证

主要思路是根据每次请求生成一个唯一key，然后进行判断和去重。 可以使用内存Set，或者redis，或者数据库唯一键。



### 如何保证消息不丢失（保证消息可靠性）

#### 为什么会丢失数据

##### rabbitmq

1. 生产者端。消息因为网络问题丢失或者发送到rabbitmq时出错了。

2. rabbitmq服务端。未做持久化。
3. 消费者端。打开了autoAck，在未完成消费之前就自动回复了。 

###### 解决

1. 生产者端。通过confirm模式异步确认消息发送成功，在失败后的回调函数中处理失败的逻辑。 
2.  服务端。打开持久化机制。这里涉及到两个参数，一个是建立queue的时候，持久化那个queue。另外一个是生产者发送消息的时候，把deliveryMode设置为2，让MQ把这条数据也给持久化。但是尽管如此，如果在极端情况下，在rabbitmq中内存写成功，但是还没来及持久化时，rabbitmq宕机，这部分在内存里面的数据也会丢失，不过几率很小。 
3. 在消费者端，去掉autoAck，在自己完成逻辑后手动提交ack。 

##### kafka

1. 生产者端：和rabbitmq类似，如果没能确认写成功，也没有重发那么也会丢失。 
2. 服务端：如果未来得及和从节点同步数据就宕机了，那么这部分数据就会丢失。 
3. 消费者端：和rabbitmq类似，如果自动提交offset依旧会出现丢失。 

###### 解决

1. 生产者端：设置参数（ ack=all ），要求每个从节点都写成功后才任务成功，另外如果发送失败，重试次数设置一个很大的值。
2. 服务端：设置参数，要求从节点起码大于1（ min.insync.raplicas参数值大于1 ），且至少有一个能被感知到。 
3. 消费者端：取消掉自动回复。 不过，强一致的保证消息不丢失，必然会影响到吞吐量。



### 如何保证消息按顺序执行

#### 为什么未按照顺序执行

##### rabbitmq

一个queue按顺序写入，消费者按顺序获取到后，写入库的时间不可控，导致顺序不一致。

###### 解决

一个queue对应一个消费者即可，但是存在单点问题

##### kafka

生产者在写入数据的数据，可以指定一个key，比如说某个订单id，这个订单相关的数据都会分发到一个patition中去，而且这个patition中数据是保存顺序的。

kafka原理是一个消费者消费一个patition，不存在多个消费者消费一个patition。所以消费者得到的消息也是有顺序的。但是如果消费者内部使用多线程进行处理，就有可能造成消息顺序错乱。

###### 解决

流程和rabbitmq相同，但是一般消费内部需要用多线程去消费才能有可观的吞吐量（单线程吞吐量太低了），所以如果是多线程的情况下，可以用内存队列（ 还可以是Redis或者其它数据库 ）来进行一次再排序。将对应的消息放到对应的内存队列中去，接着对应的工作线程去顺序消费该消息即可。压测数据：4核8G，32线程近千吞吐量。



### 大量消息长时间在消息队列中积压了怎么处理

#### 为什么会出现大量积压

为什么会出现大量积压

消费者程序出现bug，消费者依赖的其他系统挂掉了，导致消费者也挂掉了，导致消息在消息队列中积压。当消费者恢复之后，需要快速消费掉积压的消息。

#### kafka

3个消费者1s是3000条，当积压消息是1000万条时，也需要1个小时才能恢复正常。这时候只能操作临时紧急扩容了，步骤如下

1. 恢复消费者，确保其恢复消费速度，然后将现有消费者都停掉
2. 临时建立原来10倍或20倍的queue（新建一个topic，patition是原来的10倍）
3. 然后写一个临时分发数据的cusumer程序，这个程序部署上去消费积压的消息，消费之后不做耗时的处理，直接均匀轮询写入临时建立好的10倍数量的queue
4. 接着临时征用10倍的机器来部署consumer，每一批cusumer消费一个临时queue的数据。
5. 当处理完积压消息后，把临时增加的消费者下掉，恢复原来部署架构继续消费

#### rabbitmq

如果消息设置了过期时间（TTL），如果消息在queue中积压超过一定的时间就会被清理掉，消息就丢失了。解决方案是用户人数少的时候，查出来再手动导入。但是如果是消费信息，第三方有过期时间，就完蛋了，所还是别设置这个。 

如果是挤压到磁盘满了：直接丢掉或者往其他地方写，快速消费掉，再想办法补偿。 



### 如果让你设计一个消息中间件架构该如何设计

以上这些点说到即可