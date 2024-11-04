package com.example.satoken.controller;

import com.example.satoken.redis.RedisUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/redis")
public class RedisController {

    // 测试，浏览器访问： http://localhost:8081/redis/test
    @RequestMapping("/test")
    public Object testRedis() {
        RedisUtils.setCacheObject("a", "a", Duration.ofSeconds(100));
        return "test";
    }

}
