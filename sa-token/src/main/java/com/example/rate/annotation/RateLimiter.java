package com.example.rate.annotation;

import com.example.rate.enums.LimitType;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) //一般如果需要在运行时去动态获取注解信息，那只能用 RUNTIME 注解
@Documented     //一个标记注解，用于指示将被注解的元素包含在生成的Java文档中
public @interface RateLimiter {

    String key() default "";

    /**
     * 限流时间，单位秒
     */
    int time() default 60;

    /**
     * 限流次数
     */
    int count() default 100;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 提示消息 支持国际化 格式为{code}
     * todo 国际化是咋实现的
     * @return
     */
    String message() default "{rate.limiter.message}";

}
