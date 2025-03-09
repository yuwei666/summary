package org.example.feignapi.service;

import feign.Param;
import org.example.feignapi.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 使用@FeignClient 定义远程服务
 */
@FeignClient(
        name = "user-service", // 服务名
        url = "http://localhost:8080", // 目标服务 URL
        path = "/api/users", // 请求路径前缀
        configuration = FeignConfig.class, // 自定义配置
        fallback = RemoteServiceFallback.class // 熔断器回退类
)
public interface RemoteService {

    @GetMapping("/api/data/{id}")
    String getData(@Param("id") String id);


    @PostMapping("/api/data")
    String createData(String requestBody);

}
