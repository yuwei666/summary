Spring的事件监听本质上是观察者模式的实现，有三部分组成，事件（ApplicationEvent），监听器（ApplicationListener），事件发布操作。

**事件**

事件需要继承ApplicationEvent。

```
public class HelloEvent extends ApplicationEvent {

    private String name;

    public HelloEvent(Object source, String name) {
        super(source);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```

**事件监听器**

事件监听器需要实现ApplicationListener<T>接口，泛型类型就是事件类型。监听器需要加入spring容器进行管理。

```
@Component
public class HelloEventListener implements ApplicationListener<HelloEvent> {
    @Override
    public void onApplicationEvent(HelloEvent event) {
        logger.info("receive {} say hello!",event.getName());
    }
}

```

**事件发布**

```
applicationContext.publishEvent(new HelloEvent(this,"lgb"));
```



### @EventListener注解

项目启动时，ApplicationStartedEvent会发布事件，事件会被执行。这里loadAddresses()加入了事务。

```java
@Component
public class AddressLoader {

  @Autowired
  AreaRepository areaRepository;

  @Autowired
  AddressRepository addressRepository;

  private ThreadLocalRandom random = ThreadLocalRandom.current();

  @EventListener
  public void onApplicationStarted(ApplicationStartedEvent event) {
    loadAddresses();
  }

  @Transactional
  public void loadAddresses() {
    addressRepository.deleteAll();
    areaRepository.findAll().forEach(area -> {
      int num = random.nextInt(1, 5);
      for (int i = 0; i < num; i++) {
        Address address = createInArea(area, i);
        addressRepository.save(address);
      }
    });
  }

  private Address createInArea(Area area, int seq) {
    BigDecimal lat = adjustValue(area.getLat());
    BigDecimal lng = adjustValue(area.getLng());
    String addressLine = area.getName() + "-" + seq;
    Address address = new Address();
    address.setArea(area);
    address.setAddressLine(addressLine);
    address.setLat(lat);
    address.setLng(lng);
    return address;
  }

  private BigDecimal adjustValue(BigDecimal location) {
    return location.add(BigDecimal.valueOf(random.nextInt(-100, 100) * 0.000001));
  }
}
```

