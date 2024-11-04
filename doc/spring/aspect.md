



环绕

```
// 注意，@annotation(rateLimiter)中放的是参数对象，而不是类
@Around("@annotation(rateLimiter)")
public Object doAround(ProceedingJoinPoint point, RateLimiter rateLimiter) throws Throwable {
    // 业务代码1
    Object proceed = point.proceed();
    // 业务代码2
    return proceed;
}

```

