package org.example.feignapi.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // 设置日志级别为 FULL
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 添加自定义请求头
            requestTemplate.header("Authorization", "Bearer token");
        };
    }
}