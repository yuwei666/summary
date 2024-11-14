#### 介绍

Spring Boot中使用ENC（Environment-Neutral Configuration）主要是为了将配置信息从应用程序代码中分离出来，以提高安全性和可维护性。ENC的主要优点包括：

+ 安全性增强：敏感信息（如数据库密码、API密钥等）不应硬编码在代码中，而是应该使用加密的方式存储在配置文件中，然后通过ENC进行解密和使用，从而减少泄露风险。
+ 可维护性：将配置信息与代码分离，使得配置可以独立地修改和管理，而不需要重新编译和部署应用程序。这样可以降低维护成本，并使应用程序更易于管理。
+ 灵活性：使用ENC可以根据不同的环境（开发、测试、生产等）提供不同的配置，而不需要修改应用程序代码，从而提高了部署的灵活性和可移植性。



#### 使用

##### 引入pom

```java
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

##### 盐值

整合SpringBoot时，`盐值`的配置最好式写到`配置中心`的文件中，`盐值`写到本地文件，泄露后也容易被进行解密 

```
jasypt:
  encryptor:
  	# password值任意，最好随机字符
    password: hhX4FzbwcT
```

##### 工具类

使用工具类对需要处理的明文数据进行加密处理，再将加密结果写入到配置文件中

注意：工具类使用完成后，应该删除`加密盐`

```java
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class JasyptTest {

    /**
     * 加密盐值，使用完成后进行删除，或者不能提交到`生产环境`，比如：
     */
    private final static String PASSWORD = "hhX4FzbwcT";

    public static void main(String[] args) {

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();

        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        // 用于设置加密密钥。密钥是用于加密和解密字符串的关键信息。
        config.setPassword(PASSWORD);
        // 加密算法的名称,jasypt-3.0.5版本后默认的加密方式
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        // 用于设置加密时迭代次数的数量，增加迭代次数可以使攻击者更难进行密码破解。
        config.setKeyObtentionIterations("1000");
        // 加密器池的大小。池是一组加密器实例，可确保加密操作的并发性。
        config.setPoolSize("1");
        // 用于设置JCE（Java Cryptography Extension）提供程序的名称。
        config.setProviderName("SunJCE");
        // 用于设置生成盐的类名称。在此配置中，我们使用了org.jasypt.salt.RandomSaltGenerator，表示使用随机生成的盐。
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        // 用于设置Jasypt使用的初始化向量（IV）生成器的类名。初始化向量是在加密过程中使用的一个固定长度的随机数，用于加密数据块，使每个数据块的加密结果都是唯一的。在此配置中，我们使用了org.jasypt.iv.RandomIvGenerator类，该类是一个随机生成器，用于生成实时随机IV的实例。这样可以确保每次加密的IV都是唯一的，从而增加加密强度。
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        // 指定加密输出类型。在此配置中，我们选择了base64输出类型。
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        // 明文1
        String name_encrypt = "root";
        // 明文2
        String password_encrypt = "123456";

        // 明文加密
        String encrypt1 = encryptor.encrypt(name_encrypt);
        String encrypt2 = encryptor.encrypt(password_encrypt);
        System.out.println("明文加密1：" + encrypt1);
        System.out.println("明文加密2：" + encrypt2);

        // 密文解密
        String decrypt1 = encryptor.decrypt(encrypt1);
        String decrypt2 = encryptor.decrypt(encrypt2);
        System.out.println("密文解密1：" + decrypt1);
        System.out.println("密文解密2：" + decrypt2);
    }
}
```

##### 加密配置使用(yml)

```yml
sys:
 name: ENC(Yt36hceu3xGXEzrz2jCPjvalaXQ5yIHE04SVT6lIkcktrxqtBZrlivkAkA9/9oZ2)
 password: ENC(0Ci6irPOko9IG+hBZJAGoguIuE52gF/XiigCV4DwLm6NfkoyvV4Etgc9FzKK3MYl)
```

##### 测试

```
@RestController
public class TestController {

    @Value("${sys.name}")
    private String name;

    @Value("${sys.password}")
    private String password;

    @GetMapping("/test")
    public void test() {
        System.out.println("name = " + name);
        System.out.println("password = " + password);
    }
}
```

