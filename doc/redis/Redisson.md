Redisson和Redis区别：

1. Redis是一个开源的内存数据库，支持多种数据类型，如字符串、哈希、列表、集合和有序集合等，常用于缓存、消息队列、计数器、分布式锁和搜索等应用场景。
2. Redisson则是一个基于Redis实现的Java驻内存数据网格，提供了一系列分布式的Java常用对象和多种锁机制，以及分布式服务，使开发者能更集中于处理业务逻辑。
3. Redisson不支持字符串操作、排序、事务、管道、分区等Redis特性，但Redisson 提供了许多其他功能，如分布式闭锁、分布式计数器、分布式信号量等。



来自知乎

提供了异步和响应的支持，很好的支持了高并发和阻塞的场景， 提供了批量操作来提升性能，还支持分布式特性 

##### 配置

支持Redis多种连接模式，集群、单例、哨兵、主从

现有项目中单例配置如下，应该是配置了redis，redisson自然就能用了。项目中还是配置了redisson，配置如下 （在redisson中，连接池起着非常重要的作用）

```yml
redisson:
  # redis key前缀
  keyPrefix:
  # 线程池数量
  threads: 4
  # netty连接池数量
  nettyThreads: 8
  # 单节点配置
  singleServerConfig:
    clientName: aaa
    # 连接池最小空闲数
    connectionMinimumIdleSize: 8
    # 连接池大小
    connectionPoolSize: 32
    # 空闲超时，单位毫秒
    idleConnectionTimeout: 10000
    # 命令等待超时，单位毫秒
    timeout: 3000
    # 发布和订阅连接池大小
    subscriptConnectionPoolSize: 50
```

另外一个系统中存在redisson的Bean配置，后续补充

##### 分布式对象

含通用对象桶（Object Bucket）、二进制流（Binary Stream）、地理空间对象桶（Geospatial Bucket）、BitSet、原子整长形（AtomicLong）、原子双精度浮点数（AtomicDouble）、话题（订阅分发）、模糊话题、布隆过滤器（Bloom Filter）、基数估计算法（HyperLogLog）


```java
// 正常设置值
RBucket<T> bucket = CLIENT.getBucket(key);
bucket.set(value);
bucket.expire(duration);
bucket.setAndKeepTTL(value);

// 管道，批处理，异步，可以设置多条
RBatch batch = CLIENT.createBatch();
RBucketAsync<T> bucket = batch.getBucket(key);
bucket.setAsync(value);		// 设置值
bucket.expireAsync(duration);	// 设置过期时间
batch.execute();
```



### 限流

##### 令牌桶

- 令牌以固定速率生成；
- 生成的令牌放入令牌桶中存放，如果令牌桶满了则多余的令牌会直接丢弃，当请求到达时，会尝试从令牌桶中取令牌，取到了令牌的请求可以执行；
- 如果桶空了，那么尝试取令牌的请求会被直接丢弃。

#### 使用

```java
// 1、 声明一个限流器
RRateLimiter rateLimiter = redissonClient.getRateLimiter("token");
 
// 2、 设置速率，30秒中产生10个令牌
rateLimiter.trySetRate(RateType.OVERALL, 10, 30, RateIntervalUnit.SECONDS);
 
// 3、试图获取一个令牌，获取到返回true
rateLimiter.tryAcquire(3)
```

1. 首先要进行令牌数初始化，如果未初始化，会直接返回“RateLimiter is not initialized”
2. 将令牌数、过期时间和令牌桶type(全局还是单实例)三个参数放入一个key为“token”的Hash表中 数据 ：hsetnx key value
3. 校验: 如果许可令牌数(rate)小于ARGV1 ，返回单次请求的令牌数不能超过一个时间窗口内产生的令牌数
4. 获取当前可用令牌数({token}:value)，如果拿不到说明是首次获取，先设置当前key的可用令牌数 set {test}:value rate， 然后当前线程任务进入zset，可用令牌数({token}:value)做减操作。如果可以拿到，则先回收zsort中的当前时间戳过去interval秒之前的令牌，可用令牌数({token}:value)+之前所有请求的令牌数，**然后zset移除**，然后判断可用令牌数是否足够，足够则入队(sort set)，{token}:value 减减，不可用就返回等待时间。
   原文链接：https://blog.csdn.net/qq_37477317/article/details/139993448

 `redission`分布式限流采用令牌桶思想和固定时间窗口，`trySetRate`方法设置桶的大小，利用`redis key`过期机制达到时间窗口目的，控制固定时间窗口内允许通过的请求量。 

#### 实现原理：

KEYS[1]  my_limiter						限流器名称
KEYS[2] { my_limiter}:value 			当前可用令牌数 key
KEYS[3] {my_limiter}:实例id 		(type = 1 才需要， RateType.OVERALL ，{ my_limiter}:value和{ my_limiter}:permits会加一个:实例id)
KEYS[4] {my_limiter}:permits（授权记录有序集合的 key）
ARGV[1] 本次请求的令牌数  		3 
ARGV[2] 当前时间戳 					System.currentTimeMillis()
ARGV[3] 一个随机字符串 			ThreadLocalRandom.current().nextBytes(random);

1. 声明限流器，及设置限流器速率

   将令牌数、过期时间和令牌桶type三个参数放入一个key为“KEYS[1]”的Hash表中

   ```
   redis.call('hsetnx', KEYS[1], 'rate', ARGV[1]);
   redis.call('hsetnx', KEYS[1], 'interval', ARGV[2]);
   return redis.call('hsetnx', KEYS[1], 'type', ARGV[3]);
   ```

2. 试图获取一个令牌 rateLimiter.tryAcquire(3)

   ```
   local rate = redis.call("hget", KEYS[1], "rate")  # 10 令牌个数
   local interval = redis.call("hget", KEYS[1], "interval")  # 30000 时间间隔
   local type = redis.call("hget", KEYS[1], "type")  # 0 模式
   assert(rate ~= false and interval ~= false and type ~= false, "RateLimiter is not initialized")
   local valueName = KEYS[2]      # {my_limiter}:value 用来存储剩余许可数量
   local permitsName = KEYS[4]    # {my_limiter}:permits 记录了所有许可发出的时间戳  
   # 如果是单实例模式，name信息后面就需要拼接上clientId来区分出来了
   if type == "1" then
       valueName = KEYS[3]        # {my_limiter}:value:b474c7d5-862c-4be2-9656-f4011c269d54
       permitsName = KEYS[5]      # {my_limiter}:permits:b474c7d5-862c-4be2-9656-f4011c269d54
   end
   # 对参数校验 
   assert(tonumber(rate) >= tonumber(ARGV[1]), "Requested permits amount could not exceed defined rate")
   # 获取当前还有多少许可 
   local currentValue = redis.call("get", valueName)   
   local res
   # 如果有记录当前还剩余多少许可 
   if currentValue ~= false then
       # 查询已过期的许可数量  	查找（当前时间-间隔时间）到 0 区间的数据
       local expiredValues = redis.call("zrangebyscore", permitsName, 0, tonumber(ARGV[2]) - interval)
       local released = 0
       for i, v in ipairs(expiredValues) do
           local random, permits = struct.unpack("Bc0I", v)
           released = released + permits
       end
       # 移除已过期的许可记录，并重新计算当前可用令牌数
       if released > 0 then
           redis.call("zremrangebyscore", permitsName, 0, tonumber(ARGV[2]) - interval)
           # 如果当前令牌数+过期令牌数 > 设定令牌总数
           if tonumber(currentValue) + released > tonumber(rate) then
           	# 当前令牌数 = 总数 - 发出的令牌			# Zcard 计算集合中元素的数量 
               currentValue = tonumber(rate) - redis.call("zcard", permitsName)
           else
           	# 当前令牌数 = 当前令牌数 + 过期回收的令牌数
               currentValue = tonumber(currentValue) + released
           end
           # 设置当前令牌数
           redis.call("set", valueName, currentValue)
       end
       # ARGV  permit  timestamp  random， random是一个随机的8字节
       # 如果剩余许可不够，需要在res中返回下个许可需要等待多长时间 
       # 当前令牌数 < 请求的令牌数
       if tonumber(currentValue) < tonumber(ARGV[1]) then
       	# 查找最小的一条数据，带有分数
           local firstValue = redis.call("zrange", permitsName, 0, 0, "withscores")
           // 计算剩余时间 = 3 + 令牌刷新的时间间隔 - （当前时间 - 令牌发出的时间） 
           // 3的意思应该是程序消耗时间，用于补足，不至于是0？？？？
           res = 3 + interval - (tonumber(ARGV[2]) - tonumber(firstValue[2]))
       else
       	# 增加一个令牌在发出的令牌有序集合中
           redis.call("zadd", permitsName, ARGV[2], struct.pack("Bc0I", string.len(ARGV[3]), ARGV[3], ARGV[1]))
           # 减小可用许可量 
           redis.call("decrby", valueName, ARGV[1])
           res = nil
       end
   else # 反之，记录到还有多少许可，说明是初次使用或者之前已记录的信息已经过期了，就将配置rate写进去，并减少许可数 
       redis.call("set", valueName, rate)
       redis.call("zadd", permitsName, ARGV[2], struct.pack("Bc0I", string.len(ARGV[3]), ARGV[3], ARGV[1]))
       redis.call("decrby", valueName, ARGV[1])
       res = nil
   end
   local ttl = redis.call("pttl", KEYS[1])
   # 重置
   if ttl > 0 then
       redis.call("pexpire", valueName, ttl)
       redis.call("pexpire", permitsName, ttl)
   end
   return res
   ```

   

### 分布式锁

#### 应用场景

效率：使用分布式锁可以避免不同节点重复相同的工作，这些工作会浪费资源。比如用户付了钱之后有可能不用节点会发出多封短信。（这个是怎么产生的）

正确性：加分布式锁同样可以避免破坏正确性的发生，如果两个节点在同一条数据上面操作，可能导致数据不对。

```java
RLock lock = redisson.getLock("myLock");
try {
    // 尝试获取锁，最多等待100秒，锁定之后最多持有锁10秒
    boolean isLocked = lock.tryLock(100, 10, TimeUnit.SECONDS);
    if (isLocked) {
        // 业务逻辑
        System.out.println("Lock acquired");
        // 处理完业务逻辑之后释放锁
    } else {
        // 如果未能获取锁，可以做其他事情
        System.out.println("Lock not acquired");
    }
} catch (InterruptedException e) {
    e.printStackTrace();
} finally {
    // 确保释放锁资源
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
        System.out.println("Lock released");
    }
}

// 关闭RedissonClient
redisson.shutdown();
```

















