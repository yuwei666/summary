package com.example.rate.controller;

import com.example.rate.annotation.RateLimiter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateController {

    // 测试，浏览器访问： http://localhost:8081/rate/test2
    @RequestMapping("/test")
    @RateLimiter(key = "rate", time = 3, count = 3)
    public Object testRate() {
        return "ok";
    }

    @RequestMapping("/test2")
    @RateLimiter(key = "rate2", time = 4, count = 4)
    public Object testRate2() {
        return "ok";
    }
}
