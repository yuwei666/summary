package com.example.nacosconsumer.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * fallback 必须是一个实现了与 Feign 客户端相同接口的类。
 */
public class PaymentFallbackService implements PaymentService {

    @Override
    public ResponseEntity<String> payment(Long id) {
        return new ResponseEntity<String>("feign调用，异常降级方法", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
