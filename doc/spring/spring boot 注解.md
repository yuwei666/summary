@ConfigurationProperties

 在Spring Boot框架中，配置管理是一个核心功能。  Spring Boot提供了多种方式来处理外部配置， 通过使用@ConfigurationProperties注解，开发者可以将配置文件（如`application.properties`或`application.yml`）中的属性值自动映射到Java类的字段上，从而实现配置的集中管理和类型安全。 

1. 定义配置类 

首先，定义一个Java类，用于绑定配置属性。使用`@ConfigurationProperties`注解标记该类，并指定前缀（prefix）。

```java
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app") //指定配置属性的前缀为app。
@Validated
public class AppProperties {
	@NotNull
    private String name;
    @NotEmpty
    private String version;
    private boolean enabled;
	private List<String> features;
    // getters and setters
}
```

2. 配置文件 

在`application.properties`或`application.yml`文件中定义配置属性。 

```properties
app.name=MyApp
app.version=1.0.0
app.enabled=true
app.features[0]=feature1
app.features[1]=feature2
```

```yml
app:
  name: MyApp
  version: 1.0.0
  enabled: true
  features:
  	- feature1
  	- feature2
```

3. 启用配置属性支持 

 在Spring Boot应用的**主类**或**配置类**上，使用`@EnableConfigurationProperties`注解启用配置属性支持。 

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MyAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyAppApplication.class, args);
    }
}
```

```java
@Configuration
@EnableConfigurationProperties(XxxConfig.class)
public class XxxConfig{
}
```

4. 配置校验 

 结合`@Validated注解，可以实现配置属性的校验，确保配置的有效性。









