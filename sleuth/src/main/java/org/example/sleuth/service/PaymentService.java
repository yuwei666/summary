package org.example.sleuth.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * url为空时，会在注册中心查找
 * url不为空时，使用指定地址
 */
@FeignClient(value = "paymentService", url = "http://localhost:8084")
public interface PaymentService {

    @GetMapping("/payment/{id}")
    public ResponseEntity<String> payment(@PathVariable("id") Long id);


}
