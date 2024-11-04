```xml
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.24</version>
        </dependency>
```



@NoArgsConstrutor：无参构造方法；@NoArgsConstructor(access = AccessLevel.*PRIVATE*) 私有的构造函数

@RequiredArgsConstructor：有参构造方法，使用了@NonNull约束的属性； 生成包含final和@NonNull修饰的属性的构造方法

@AllArgsContructor：全参构造方法；

@Data：setter/getter,tostring,hashcode,equals,requiredArgsConstructor，@Setter,@Getter:setter/getter

@Accessors(chain=true)：settter的链式调用

@Builder：将类转变为建造者模式

@EqualsAndHashCode：生成equals和hashCode方法

@Slf4j：生成一个log变量，生private static final修饰，配合日志框架使用； 针对不同的日志实现产品，有不同的日志注解，使用 @Log表示使用Java自带的日志功能，除了 @Log ，还可以使用@[Log4j](https://so.csdn.net/so/search?q=Log4j&spm=1001.2101.3001.7020) 、 @Log4j2 、 @Slf4j 等注解 

@NonNull：用于方法参数前，表示调用该方法时参数不能为null；用于属性上方，表示为该属性赋值时值不能为null。如果为null，抛出NullPointException。

@CleanUp  自动关闭资源，如IO流对象。 

@SneakyThrows  对方法中异常进行捕捉并抛出 