#### 匹配

```java
//任何一个元素匹配
list.stream().anyMatch(item->item.getDicItemCode().equals("xxx"));
//所有元素都匹配
list.stream().allMatch(item->item.getDicItemCode().equals("xxx"));
//所有元素都不匹配
list.stream().anyMatch(item->item.getDicItemCode().equals("xxx"));
```

#### 转换

```java
//集合转数组
String[] arr = list.stream().toArray(String[]::new);
//集合转数字,求和
list.stream().mapToLong().sum();

```



#### 转Map

```java

```

