package com.example.nacosprovider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@RefreshScope
public class PaymentController {

    @Value("${server.port}")
    private String serverPort;

    /**
     * 从Nacos配置中心获取
     */
    @Value("${config.info}")
    private String configInfo;

    @GetMapping("/{id}")
    public ResponseEntity<String> payment(@PathVariable("id") Long id) {
        // 验证链路追踪时，让其抛出异常
        // int i = 1/0;
        return ResponseEntity.ok("订单号 = " + id + "，支付成功，server.port" + serverPort);
    }

    /**
     * http://localhost:8084/payment/config/info
     */
    @GetMapping("/config/info")
    public String getConfigInfo() {
        return configInfo;
    }

}
