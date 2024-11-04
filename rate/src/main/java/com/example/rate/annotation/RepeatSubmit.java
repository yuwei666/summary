package com.example.rate.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    int interval() default 5000;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    String message() default "{repeat.submit.message}";

}
