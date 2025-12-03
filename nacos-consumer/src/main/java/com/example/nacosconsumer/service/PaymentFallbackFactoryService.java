package com.example.nacosconsumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @date 2025/11/25
 */
@Slf4j
@Component
public class PaymentFallbackFactoryService implements FallbackFactory<PaymentService> {

    @Override
    public PaymentService create(Throwable cause) {

        log.error("调用 RMS 服务失败，原因：{}", cause.getMessage(), cause);
        return new PaymentService() {
            @Override
            public ResponseEntity<String> payment(Long id) {
                return ResponseEntity.status(500).build();
            }

            @Override
            public ResponseEntity<String> payment(Object id) {
                return ResponseEntity.status(500).build();
            }
        };
    }
}
