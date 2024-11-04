package com.example.config;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动 @AspectJ 支持
 * exposeProxy = true 表示通过aop框架暴露该代理对象，AopContext能够访问
 */
@EnableAspectJAutoProxy(exposeProxy = true)
public class ApplicationConfig {
}
