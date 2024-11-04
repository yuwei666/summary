## 命令

### Hash 哈希

+ HSETNX	用于为哈希表中不存在的的字段赋值。

  如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。设置成功，返回1。 如果给定字段已经存在且没有操作被执行，返回0。 

  ```
  HSETNX myhash field1 "foo"  # key:myhash Hash表中的key：field1 Hash表中的value："foo"
  (integer) 1
  
  HGET myhash field1	# 获取Hash表中的key
  "foo"
  ```

+ INCR	 为键`key`对应的数字字符串（整数）加上一。  

  如果键`key`不存在， 那么它的值会先被初始化为 `0` ， 然后再执行INCR命令。 

   NCR命令是一个针对字符串的操作。 因为Redis并没有专用的整数类型， 所以键`key`对应的字符串在执行INCR命令时会被解释为十进制（`64`位有符号整数）。 

  ```
  > set number 10
  ok
  > incr number
  (integer) 11
  ```

+  INCRBY key increment 

   为键`key`对应的数字字符串（整数）加上 `increment` 。 

  ```
  > get number
  (nil)
  > incrby number 90
  "90"
  ```

+ INCRBYFLOAT

   为键`key`对应的值（浮点数）加上浮点数 `increment`  

+ DECR

  为键`key`对应的数字字符串（整数） 减去一。 

+  DECRBY 

   将键`key`对应的数字值（整数）减去`decrement` 。 

  

### Sorted set 有序集合（ZSet）

+  zrangebyscore  

   返回有序集合中指定分数区间的成员列表。有序集成员按分数值递增(从小到大)次序排列。 

  ```
  > ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
  ```

+ ZREMRANGEBYSCORE  用于移除有序集中，指定分数（score）区间内的所有成员。 返回被移除成员的数量。

  ```
  > ZREMRANGEBYSCORE key min max
  ```

+ ZRANGE  指定区间内，带有分数值(可选)的有序集成员的列表。 

  ```
  > ZRANGE key start stop [WITHSCORES]
  
  # 特殊例子：查找最小的一条数据，带有分数
  > zrange sorted_set 0 0 withscores
  ```

+ ZAdd

   向有序集合添加一个或多个成员，或者更新已存在成员的分数 

+ Zcard  计算集合中元素的数量 

  ```
  > ZCARD KEY_NAME
  (integer) 1
  ```

+  Zrem

  命令用于移除有序集中的一个或多个成员，不存在的成员将被忽略。 

  ```
  > ZREM key member [member ...]
  ```

  

## 配置

混合持久化， 既能够快速重启（通过RDB），又能够保证数据的安全性（通过AOF） 

```
# redis4.0 以上版本支持
config set aof-use-rdb-preamble yes
```



