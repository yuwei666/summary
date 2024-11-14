package com.example.nacosconsumer.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PaymentFallbackService implements PaymentService {

    @Override
    public ResponseEntity<String> payment(Long id) {
        return new ResponseEntity<String>("feign调用，异常降级方法", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
