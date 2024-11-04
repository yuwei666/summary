```java
//名称占位符
@Query("select t from Device t where t.deviceSn=:deviceSn and t.deviceType =:deviceType and t.deleteFlag=1")
Device findExistDevice(@Param("deviceSn") String deviceSn,@Param("deviceType")Integer deviceType);

//位置占位符
@Query("select t from Device t where t.deviceSn=?1 and t.deviceType = ?2 and t.deleteFlag=1")
Device findDevice(String deviceSn,Integer deviceType);

//修改
@Modifying
@Query("update Device t set t.userName =:userName where t.id =:userId")
User updateUserName(@Param("userId") Long userId,@Param("userName") String userName);

//本地查询
@Query(value="select * from t_user",nativeQuery=true)
List<User> findByUserName(String userName);

```

SQL上使用占位符的两种方式，第一种是使用":"后加变量的名称，第二种是使用"?"后加方法参数的位置。如果使用":"的话，需要使用`@Param`注解来指定变量名；如果使用"?"就需要注意参数的位置。

SQL语句中直接用实体类代表表名，因为在实体类中使用了`@Table`注解，将该实体类和表进行了关联。



```java
@Transient		//忽略字段
@Query			//查询
@Modifying		//如果@Query涉及到update就必须同时加上@Modifying注解
```

#### 其他

##### 求和

```
countByPkId(String pkId);
```



#### 一些常用注解

```java
//持久化前操作，例如添加时间，主键等
@PrePersist
public void generateId() {
    if (pkId == null) {
        pkId = UUID.randomUUID().toString();
    }
}
```



#### 查询

| 关键字            | 方法命名                              | sql where字句                         |
| ----------------- | ------------------------------------- | :------------------------------------ |
| And               | findByNameAndPwd                      | where name = ? and pwd= ?             |
| Or                | findByNameOrSex                       | where name = ? or sex =?              |
| Between           | findByIdBetween                       | where id between ? and ?              |
| LessThan          | findByIdLessThan                      | where id <?                           |
| LessThanEqual     | findByIdLessThanEqual                 | where id <=?                          |
| GreaterThan       | findByIdGreaterThan                   | where id > ?                          |
| GreaterThanEqual  | findByIdGreaterThanEqual              | where id >= ?                         |
| After             | findByIdAfter                         | where id > ?                          |
| Before            | find ByIdBefore                       | where id < ?                          |
| IsNull            | findByNameIsNull                      | where name is null                    |
| IsNotNull,NotNull | findByNameIsNotNull findByNameNotNull | where name is not null                |
| Like              | findByNameLike                        | where name like ?                     |
| NotLike           | findByNameNotLike                     | where name not like ?                 |
| StartingWith      | findByNameStartingWith                | where name like '?%'                  |
| EndingWith        | findByNameEndingWith                  | where name like '%?'                  |
| Containing        | findByNameContaining                  | where name like '%?%'                 |
| OrderBy           | findByIdOrderByAgeDescAndIdAsc        | where id = ? order by age desc,id asc |
| Not               | findByNameNot                         | where name <> ?                       |
| In                | findByNameIn                          | where name in (?)                     |
| NotIn             | findByIdNotIn                         | where id not in (?)                   |
| True              | findByDelStatusTrue                   | where delStatus = true                |
| False             | findByDelStatusFalse                  | where delStatus = false               |
| IgnoreCase        | findByNameIgnoreCase                  | where UPPER(name) = UPPER(?)          |