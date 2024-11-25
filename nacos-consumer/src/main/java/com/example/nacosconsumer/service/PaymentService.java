package com.example.nacosconsumer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @FeignClient 注解用于声明一个 Feign 客户端，该客户端可以用来调用远程服务
 *  fallbackFactory 和 fallback 是两个重要的属性，用于处理服务调用的失败情况，确保在远程服务不可用时客户端能够优雅地处理错误情况
 *  fallback 属性
 *  定义：fallback 属性用于指定一个降级处理类（即一个实现了接口的类），当 Feign 客户端调用失败时，自动回退到这个类中定义的处理逻辑。
 */
@FeignClient(value = "payment-service", fallback = PaymentFallbackService.class)
public interface PaymentService {

    @GetMapping("/payment/{id}")
    public ResponseEntity<String> payment(@PathVariable("id") Long id);

}
