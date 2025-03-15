package com.example.nacosconsumer.config;

import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NacosConfig {

    private static final Logger logger = LoggerFactory.getLogger(NacosConfig.class);

    @Bean
    public NacosRegistrationCustomizer nacosRegistrationCustomizer() {
        return registration -> {
            // 打印注册时的参数
            logger.info("Registering service with Nacos, parameters: {}", registration);
            logger.info("Service Name: {}", registration.getServiceId());
            logger.info("IP: {}", registration.getHost());
            logger.info("Port: {}", registration.getPort());
            logger.info("Metadata: {}", registration.getMetadata());
        };
    }
}