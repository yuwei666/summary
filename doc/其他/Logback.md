## logback

### 基础介绍

 logback是log4j框架的继承者和改进版，并提供了更好的性能、可扩展性和灵活性。  与Log4j相比，Logback提供了更快的速度和更低的内存占用 。

springboot框架自带logback日志框架，只需要在resources下添加logback.xml即可，`logback.xml` 是 [Logback](https://so.csdn.net/so/search?q=Logback&spm=1001.2101.3001.7020) 日志框架的配置文件。

#### 日志级别

 Logger有五个日志级别，优先级从低到高分别是`TRACE`、`DEBUG`、`INFO`、`WARN`、`ERROR`， 在打印日志的时候，**只会打印当前日志级别高于或者等于当前日志级别的日志信息**。 



### logback.xml

logback.xml允许定义日志输出的方式、格式、目标以及日志级别等。 基本结构如下

```xml
<configuration>
    <property name="HOME" value="./log">
    
    <!-- 定义根日志级别 -->
    <root level="info">
        <appender-ref ref="STDOUT" />
    </root>
    
    <!-- 定义输出到控制台的日志格式和目标  -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <file>${HOME}/sys-console.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
 
    <!-- Loggers -->
    <logger name="LOGGER_NAME" level="LOG_LEVEL">
        <appender-ref ref="APPENDER_NAME" />
    </logger>

</configuration>
```

#### property标签

 property标签用来定义变量， 有两个属性，name和value；其中name的值是变量的名称，value的值时变量定义的值 

```xml
<property name="HOME" value="../log">

<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
	<file>${HOME}/log.log</file>
</appender>
```

#### root标签

root标签指定最基础的的日志输出级别，它只有一个level属性，可以包含 appender-ref 元素。
level属性可以选择，`ALL`、`TRACE`、`DEBUG`、`INFO`、`WARN`、`ERROR`、`NULL`、`OFF`、`INHERITED` （是不是不区分大小写）

#### appender标签

appender就是附加器，日志记录器会将输出的任务交给附加器完成，不同的附加器会将日志输出到不同的地方，例如控制台、文件、网络等 

> 几个常见的附加器：
>
> 控制台附加器：ch.qos.logback.core.ConsoleAppender
> 文件附加器：ch.qos.logback.core.FileAppender
> 滚动文件附加器：ch.qos.logback.core.rolling.RollingFileAppender

appender标签可以包含encoder元素、fileter元素等 

```
	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <filter>
            <level>info</level>
            <onMatch>DENY</onMatch>
            <onMismatch>ACCEPT</onMismatch>
        </filter>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%-5level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
```

##### encoder标签

encoder最主要的就是pattern标签，用于控制输出日志的格式 

> %d: 表示日期
> %-5level：日志级别
> %thread：表示线程名
> %logger：输出日志的类名
> logger{length}：对输出日志的类名缩写展示
> %msg：日志输出内容
> %n：换行符
> -：左对齐

##### filter标签

 ilter是过滤器，过滤器是附件器的一个组件，它是用于判断附件器是否输出日志的。  一个附件器可以包含多个过滤器。过滤器只能有三个值，不输出日志`DENY`、不决定是否输出日志`NEUTRAL`、输出日志`ACCEPT`。

 可以有三个元素，level元素、onMatch元素、onMismatch元素。

`level`：设置过滤级别
`onMatch`：用于配置符合过滤条件的操作
`onMismatch`：用于配置不符合过滤条件的操作。 

####  logger标签

在上述配置中，如果我们通过 `<root>` 配置了根日志级别为 `INFO`。这意味着只有级别为 `INFO` 及更高的日志消息才会被记录，而级别为 `DEBUG` 或 `TRACE` 的日志消息将被忽略。

如果需要配置单个日志记录器的级别，你可以使用如下的方式：

```
<logger name="com.example.MyClass" level="DEBUG" addtivity="true">
    <appender-ref ref="CONSOLE" />
</logger>
```

<loger>仅有一个name属性，一个可选的level和一个可选的addtivity属性。 

addtivity：是否向上级logger传递打印信息。默认是true。

#### 异步打印

 在 Java 中，你可以使用异步日志记录框架来异步打印日志，以提高应用程序性能，特别是在高负载环境下 。

 定义了一个异步日志记录器 `ASYNC`，它包装了一个文件输出日志记录器 `FILE`。根日志级别引用了异步日志记录器 `ASYNC。` 

```
    <!-- 异步日志记录器定义 -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="FILE" />
    </appender>
 
    <!-- 文件输出定义 -->
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>myapp.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
 
    <!-- 根日志级别和引用异步日志记录器 -->
    <root level="INFO">
        <appender-ref ref="ASYNC" />
    </root>
```



### 补充

+ 如果有多个root节点，则最后面的root节点的[日志级别](https://so.csdn.net/so/search?q=日志级别&spm=1001.2101.3001.7020)生效。

+ 如果root节点的日志级别为info，在logger节点的level配置为debug也能生效。

+ appender-ref指向的是appender，一个appender如果没有被任何引用，那么不生效



