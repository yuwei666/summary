### 使用

Sa-Token官方文档](https://sa-token.cc/doc.html#/) 已经写的很全面了，在实际项目中，登录认证，权限认证都是使用此框架，所以补充一些在项目中的具体使用代码。

#### 引入依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>sa-token</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>sa-token</name>
    <description>sa-token</description>
    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <spring-boot.version>2.7.6</spring-boot.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- SpringBoot Web模块 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- 生成配置的元数据信息，即META-INF目录下的spring-configuration-metadata.json文件，从而告诉spring这个jar包中有哪些自定义的配置 -->
        <!-- 在配置类上添加 @ConfigurationProperties，编译后即可在application.yml中使用配置提示 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Sa-Token 权限认证，在线文档：https://sa-token.cc -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot-starter</artifactId>
            <version>1.39.0</version>
        </dependency>
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-jwt</artifactId>
            <version>1.39.0</version>
        </dependency>
        <!-- Sa-Token 整合 Redis （使用 jdk 默认序列化方式） -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-redis</artifactId>
            <version>1.39.0</version>
        </dependency>
        <!-- Sa-Token插件：权限缓存与业务缓存分离 -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-alone-redis</artifactId>
            <version>1.39.0</version>
        </dependency>
        <!-- 提供Redis连接池 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>

        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>3.20.0</version>
        </dependency>

        <!-- Java工具库，提供了许多实用的工具类和方法 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.7.14</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.24</version>
        </dependency>

    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <configuration>
                    <mainClass>com.example.satoken.SaTokenApplication</mainClass>
                    <skip>true</skip>
                </configuration>
                <executions>
                    <execution>
                        <id>repackage</id>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

#### yml配置文件

```yml
server:
  # 端口号
  port: 8081

############## Sa-Token 配置 (文档: https://sa-token.cc) ##############
sa-token:
  # token 名称（同时也是 cookie 名称）
  token-name: sa-token
  # token 有效期（单位：秒） 默认30天，-1 代表永久有效
  timeout: 2592000
  # token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
  active-timeout: -1
  # 是否允许同一账号多地同时登录 （为 true 时允许一起登录, 为 false 时新登录挤掉旧登录）
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个 token, 为 false 时每次登录新建一个 token）
  is-share: true
  # token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
  token-style: uuid
  # 是否输出操作日志
  is-log: true
  # Token前缀 与 Token值 之间必须有一个空格。
  #一旦配置了 Token前缀，则前端提交 Token 时，必须带有前缀，否则会导致框架无法读取 Token。
  #由于Cookie中无法存储空格字符，所以配置 Token 前缀后，Cookie 模式将会失效，此时只能将 Token 提交到header里进行传输。
  # token-prefix: Bearer
  jwt-secret-key: xxxxx

security:
  excludes:
    # 静态资源
    - /*.html
    - /**/*.html
    - /**/*.css
    - /**/*.js
    # 公共路径
    - /favicon.ico
    - /error
    # swagger 文档配置
    # actuator 监控配置
```

#### 实现权限加载接口实现类

```java
/**
 * 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object o, String s) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询权限
        // 用户信息保存在session中，包含角色和权限，每次从缓存中获取
        List<String> list = new ArrayList<String>();
        list.add("101");
        list.add("user.add");
        list.add("user.update");
        list.add("user.get");
        // list.add("user.delete");
        list.add("art.*");
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object o, String s) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询角色
        List<String> list = new ArrayList<String>();
        list.add("admin");
        list.add("super-admin");
        return list;
    }
}
```

#### 注解鉴权

想使用注解鉴权，需要配置全局拦截器，默认拦截器处于关闭状态，需要手动注册

```java
@RequiredArgsConstructor
@Slf4j
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SaTokenConfigure implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> {
                    AllUrlHandler allUrlHandler = SpringUtil.getBean(AllUrlHandler.class);
                    SaRouter.match(allUrlHandler.getUrls())
                        	// 写在这里可以动态化
                        	.notMatch(securityProperties.getExcludes())
                            .check(() -> {
                                StpUtil.checkLogin();
                            });
                }))
                .addPathPatterns("/**")
            	// 只会在启动时执行一次
                .excludePathPatterns(securityProperties.getExcludes());
    }

    /**
     * Sa-Token 整合 jwt (Simple 简单模式)
     * 需要同时引入 jwt 与 Redis
     * @return
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

}
```

这里的SecurityProperties.getExcludes()如下，通过配置去属性注入

```java
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 排除路径
     */
    private String[] excludes;

}
```

AllUrlHandler

```java
@Data
@Component
public class AllUrlHandler implements InitializingBean {

    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    private List<String> urls = new ArrayList<>();

    /**
     * 替换占位符RequestMapping中占位符{xxx} 为 *
     * 如：/tokenInfo/{id} -> /tokenInfo/*
     */
    @Override
    public void afterPropertiesSet() {
        Set<String> set = new HashSet<>();
        RequestMappingHandlerMapping mapping = SpringUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        // 获取所有请求路径集合 key:{ [/user/doLogin]}  value: com.example.satoken.controller.UserController#doLogin(String, String)
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        map.keySet().forEach(info -> {
            Objects.requireNonNull(info.getPathPatternsCondition().getPatterns())
                    .forEach(url -> set.add(ReUtil.replaceAll(url.getPatternString(), PATTERN, "*")));
        });
        urls.addAll(set);
    }
}
```

| 功能点                | Simple 简单模式 | Mixin 混入模式        | Stateless 无状态模式   |
| --------------------- | --------------- | --------------------- | ---------------------- |
| Token风格             | jwt风格         | jwt风格               | jwt风格                |
| 登录数据存储          | Redis中存储     | Token中存储           | Token中存储            |
| Session存储           | Redis中存储     | Redis中存储           | 无Session              |
| 注销下线              | 前后端双清数据  | 前后端双清数据        | 前端清除数据           |
| 踢人下线API           | 支持            | 不支持                | 不支持                 |
| 顶人下线API           | 支持            | 不支持                | 不支持                 |
| 登录认证              | 支持            | 支持                  | 支持                   |
| 角色认证              | 支持            | 支持                  | 支持                   |
| 权限认证              | 支持            | 支持                  | 支持                   |
| timeout 有效期        | 支持            | 支持                  | 支持                   |
| active-timeout 有效期 | 支持            | 支持                  | 不支持                 |
| id反查Token           | 支持            | 支持                  | 不支持                 |
| 会话管理              | 支持            | 部分支持              | 不支持                 |
| 注解鉴权              | 支持            | 支持                  | 支持                   |
| 路由拦截鉴权          | 支持            | 支持                  | 支持                   |
| 账号封禁              | 支持            | 支持                  | 不支持                 |
| 身份切换              | 支持            | 支持                  | 支持                   |
| 二级认证              | 支持            | 支持                  | 支持                   |
| 模式总结              | Token风格替换   | jwt 与 Redis 逻辑混合 | 完全舍弃Redis，只用jwt |

```java
    // 测试登录，浏览器访问： http://localhost:8081/user/doLogin?username=zhang&password=123456
    @RequestMapping("doLogin")
    @SaIgnore
    public String doLogin(String username, String password) {

        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if("zhang".equals(username) && "123456".equals(password)) {
            //StpUtil.login(10001);
            // 登录10001账号，并为生成的 Token 追加扩展参数name
            StpUtil.login(10001, SaLoginConfig.setExtra("name", "ZhangSan").setExtra("age", 18));
            //还有一种写法，可以使用SaLoginModel
            //SaLoginModel model = new SaLoginModel();
            //StpUtil.login(10001, model.setExtra(USER_KEY, user_id));
            // 获取扩展参数
            String name = (String) StpUtil.getExtra("name");
            // 获取任意 Token 的扩展参数
            //String name = StpUtil.getExtra("tokenValue", "name");
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return "登录成功\n" + tokenInfo;
        }
        return "登录失败";
    }
```

因为整合了jwt，所以token风格为jwt风格，额外参数只在jwt模式下生效

`SA [INFO] -->: 账号 10001 登录成功 (loginType=login), 会话凭证 token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb2dpbiIsImxvZ2luSWQiOjEwMDAxLCJyblN0ciI6Im12eWlPd1VsODhFY2tVR3NxaUNSZGdka3h0SGlBMXlyIn0.Ut0Z0-1VGAPH8jWHj9HjlSQkAXd0GKMvfag51IcxlvY`

#### 在多账户模式中集成 jwt

 sa-token-jwt 插件默认只为 `StpUtil` 注入 `StpLogicJwtFoxXxx` 实现，自定义的 `StpUserUtil` 是不会自动注入的，我们需要帮其手动注入： 

```java
/**
 * 为 StpUserUtil 注入 StpLogicJwt 实现 
 */
@PostConstruct
public void setUserStpLogic() {
    StpUserUtil.setStpLogic(new StpLogicJwtForSimple(StpUserUtil.TYPE));
}
```



### 自定义DAO层：基于Redis

 SaTokenDao是Sa-Token 持久层接口，sa-token本身封装了基于内存的默认实现，因为不满足持久化的需求所以不适用。 项目中实现 SaTokenDao接口并重写一系列方法，基于Redission客户端封装的RedisUtils，RedisUtils是RuoYi-Vue-Plus封装的工具类，基于jackson实现序列化，覆盖了大部分Redis的使用场景。 

这里就要介绍Sa-Token内部的几个类：

- `SaHolder`：上下文持有类，用于快速获取SaRequest、SaResponse、SaStorage。
- `SaTokenContext`：上下文。可以共享部分数据。
- `SaStorage`：在一次请求的作用域内读写值，可以在不同方法间隐式传参

Sa-Token在登陆后将token存储在Storage和Dao层，采用的存储策略是多级缓存。

```java
/**
 * Sa-Token持久层接口
 * sa-token本身封装了基于内存的默认实现，因为不满足持久化的需求所以不适用
 * 项目中需要实现SaTokenDao接口重写一系列方法
 */
public class PlusSaTokenDao implements SaTokenDao {

    @Override
    public String get(String key) {
        return RedisUtils.getCacheObject(key);
    }

    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        // 如果永不过期
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, value);
        } else {
            RedisUtils.setCacheObject(key, value, Duration.ofSeconds(timeout));
        }
    }

    /**
     * 修改指定key-value键值对（过期时间不变）
     *
     * @param key   键名称
     * @param value 值
     */
    @Override
    public void update(String key, String value) {
        long expire = getTimeout(key);
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.set(key, value, expire);
    }

    @Override
    public void delete(String key) {
        RedisUtils.deleteObject(key);
    }

    @Override
    public long getTimeout(String key) {
        long timeout = RedisUtils.getTimeToLive(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getTimeout(key);
            if (expire == SaTokenDao.NEVER_EXPIRE) {
                // 如果已经被设置为永久，则不做处理
            } else {
                // 再设置一次
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }

    @Override
    public Object getObject(String key) {
        return RedisUtils.getCacheObject(key);
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        // 如果永不过期
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, object);
        } else {
            RedisUtils.setCacheObject(key, object, Duration.ofSeconds(timeout));
        }
    }

    @Override
    public void updateObject(String key, Object object) {
        long expire = getObjectTimeout(key);
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.setObject(key, object, expire);
    }

    @Override
    public void deleteObject(String key) {
        RedisUtils.deleteObject(key);
    }

    @Override
    public long getObjectTimeout(String key) {
        long timeout = RedisUtils.getTimeToLive(key);
        return timeout <= 0 ? timeout : timeout / 1000;
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getTimeout(key);
            if (expire == SaTokenDao.NEVER_EXPIRE) {
                // 如果已经被设置为永久，则不做处理
            } else {
                // 再设置一次
                this.setObject(key, this.getObject(key), timeout);
            }
            return;
        }
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 搜索数据
     * @param prefix 前缀
     * @param keyword 关键字
     * @param start 开始处索引
     * @param size 获取数量  (-1代表从 start 处一直取到末尾)
     * @param sortType 排序类型（true=正序，false=反序）
     *
     * @return
     */
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        Collection<String> keys = RedisUtils.keys(prefix + "*" + keyword + "*");
        List<String> list = new ArrayList<>(keys);
        return SaFoxUtil.searchList(list, start, size, sortType);
    }
}
```

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisUtils {

    private static final RedissonClient CLIENT = SpringUtil.getBean(RedissonClient.class);

    public static <T> T getCacheObject(String key) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        return bucket.get();
    }

    /**
     * 缓存基本的对象，保留TTL有效期
     * @param key
     * @param value
     * @param isSaveTtl
     * @Since Redis 6.X以上使用SetAndKeepTTL 兼容5.X 方案
     */
    public static <T> void setCacheObject(final String key, final T value, final boolean isSaveTtl) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        if(isSaveTtl) {
            try {
                bucket.setAndKeepTTL(value);
            } catch (Exception e) {
                long timeToLive = bucket.remainTimeToLive();
                setCacheObject(key, value, Duration.ofMillis(timeToLive));
            }
        } else {
            bucket.set(value);
        }
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     * @param key
     * @param value
     * @param duration
     */
    public static <T> void setCacheObject(final String key, final T value, final Duration duration) {
        RBatch batch = CLIENT.createBatch();
        RBucketAsync<T> bucket = batch.getBucket(key);
        bucket.setAsync(value);
        bucket.expireAsync(duration);
        batch.execute();
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     * @param key
     * @param value
     */
    public static void setCacheObject(final String key, final String value) {
        setCacheObject(key, value, false);
    }

    /**
     * 删除单个对象
     * @param key
     * @return
     */
    public static boolean deleteObject(final String key) {
        return CLIENT.getBucket(key).delete();
    }

    /**
     * 删除集合对象
     * @param collection
     * @return
     */
    public static void deleteObject(final Collection collection) {
        RBatch batch = CLIENT.createBatch();
        collection.forEach( t -> {
            batch.getBucket(t.toString()).deleteAsync();
        });
        batch.execute();
    }

    public static <T> long getTimeToLive(final String key) {
        RBucket<T> rBucket = CLIENT.getBucket(key);
        return rBucket.remainTimeToLive();
    }

    /**
     * 设置有效期
     * @param key
     * @param duration 超时时间
     * @return 成功/失败
     */
    public static boolean expire(final String key, final Duration duration) {
        RBucket<Object> rBucket = CLIENT.getBucket(key);
        return rBucket.expire(duration);
    }

    public static <T> void setCacheObject(final String key, final T value) {
        setCacheObject(key, value, false);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public static Collection<String> keys(final String pattern) {
        Stream<String> stream = CLIENT.getKeys().getKeysStreamByPattern(pattern);
        return stream.collect(Collectors.toList());
    }
}
```



### 登录

登录时，涉及到比较多的步骤

 + 验证码的生成

 + 账户密码验证码等的加密解密

 + 密码校验

 + 账号登录次数验证等

 + 生成令牌

   

#### 验证码生成

生成的关键，是要将这个验证码存到redis中，key为uuid，然后将uuid返回给前端，前端拿到输入的验证码和uuid再验证结果的正确性。

```java
   /**
     * 获取验证码
     * @return
     */
    @GetMapping("/captchaImage")
    public Object getCode() {

        String uuid = IdUtil.fastSimpleUUID();
        String captchaKey = Constants.CAPTCHA + uuid;

        Integer length = captchaProperties.getLength();

        // 中间省略验证码生成方式，太多了，懒得弄了，自己查吧
        CodeGenerator codeGenerator = ReflectUtil.newInstance(CaptchaType.MATH.getClazz(), length);
        // ...
        // 最终得到一个验证码字符串
        String code = "7654";
        
        // 将验证码写入redis
        RedisUtils.setCacheObject(captchaKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));

        HashMap map = new HashMap();
        map.put("uuid", uuid);
        //map.put("img", img);

        return map;
    }
```



#### 账户密码验证码等的加密解密

前端带着用户的登录信息访问login接口，肯定不能是明文，这时候就需要对隐私信息进行RSA加密

>  RSA非对称加密，也称为公钥加密，他有一个公钥和密钥，公的用于加密，而密的用于解密，公钥可以公开给任何人使用，而私钥则只有密钥的持有者可以访问，这种机制确保了只有持有私钥的人才能解密由公钥加密的数据，从而提供了高度的安全性； 

```
public class RsaEncryptor{

    private final RSA rsa;
    
    public RsaEncryptor(RSA rsa) {
        this.rsa = SecureUtil.rsa(Base64.decode(Constants.PRIVATE_SECRET), Base64.decode(Constants.PUBLIC_SECRET));
    }

    /**
     * 加密
     * @param value
     * @return
     */
    public String encrypt(String value) {
        return rsa.encryptBase64(value, KeyType.PublicKey);
    }

    /**
     * 解密
     * @param value
     * @return
     */
    public String decrypt(String value) {
        return rsa.decryptStr(value, KeyType.PrivateKey);
    }
}
```



#### 密码校验

密码的加密是不可逆的，数据库中存储的是密文，密码只能通过加密后和数据库中对比，一致为正确，不一致为错误 。MD5和BCrypt比较流行。相对来说，BCrypt比MD5更安全，BCrypt算法的优点是计算速度慢，你没看错，我说的就是计算速度慢，它还可以通过参数调节速度，要多慢有多慢 

```
    public String login(String username, String password, String code, String uuid) {
        User user = loadUserByUsername(username);
        // 校验密码是否正确
        checkLogin(username, () -> !BCrypt.checkpw(password, user.getPassword()));

		//登录，生成令牌
        StpUtil.login(username);
        String tokenValue = StpUtil.getTokenInfo().getTokenValue();
        return tokenValue;
    }
```



#### 账号登录次数验证等

密码重复一定次数后，就要进行验证了，这个过程依赖缓存来完成（对，还是redis！！）

```
	private void checkLogin(String username, Supplier<Boolean> supplier) {
        String errorKey = Constants.PWD_ERR_CNT_KEY + username;
        String maxRetryCount = "5";
        Integer lockTime = 5;
        // 获取登录错误次数，如果超过最大次数，则不允许继续登录
        Integer errorNumber = RedisUtils.getCacheObject(errorKey);
        if(ObjectUtil.isNotEmpty(errorNumber) && errorNumber.equals(maxRetryCount)) {
            // 记录登录信息
            recordLoginInformation(username, "登录状态", "不允许登录");
            throw new UserException();
        }

        if(!supplier.get()) {
            errorNumber = ObjectUtil.isNull(errorNumber) ? 1 : errorNumber + 1;
            if(errorNumber.equals(maxRetryCount)) {
                RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
                // 记录登录错误次数信息
                recordLoginInformation(username, "登录状态", "记录登录错误次数信息");
                throw new UserException();
            } else {
                RedisUtils.setCacheObject(errorKey, errorNumber);
                // 记录登录错误次数信息
                recordLoginInformation(username, "登录状态", "记录登录错误次数信息");
                throw new UserException();
            }
        }
        RedisUtils.deleteObject(errorKey);
    }
```



#### 生成令牌

生成令牌的过程就是使用sa-token，这里不再重复

```java
 	public String login(String username, String password, String code, String uuid) {
        User user = loadUserByUsername(username);
        // 校验密码是否正确
        checkLogin(username, () -> !BCrypt.checkpw(password, user.getPassword()));
		recordLoginInformation(username, "登录状态", "登录成功");
		//登录，生成令牌
        StpUtil.login(username);
        String tokenValue = StpUtil.getTokenInfo().getTokenValue();
        return tokenValue;
    }
```





























