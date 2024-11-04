#### 自动回滚

```java
TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
```

开启了事务的方法，使用上面代码可以回滚当前提交；如果事务中嵌套了其他事务，则只回滚当前事务，嵌套事务可以正常提交。

如果此代码中未开在事务中，则报错

```java
No transaction aspect-manage transactionStatus in scope
```

