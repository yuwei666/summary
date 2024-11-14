package com.example.nacosconsumer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "payment-service", fallback = PaymentFallbackService.class)
public interface PaymentService {

    @GetMapping("/payment/{id}")
    public ResponseEntity<String> payment(@PathVariable("id") Long id);

}
