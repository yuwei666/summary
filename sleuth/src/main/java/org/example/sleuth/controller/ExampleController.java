package org.example.sleuth.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.sleuth.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
public class ExampleController {

    /**
     * http://localhost:8989/
     * 日志中打印
     *  2024-11-25 09:21:02.531  INFO [sleuth,1aef808b91840043,1aef808b91840043] 5076 --- [nio-8989-exec-2] o.e.sleuth.controller.ExampleController  : Hello world!
     *                     此条目对应于`[application name,trace id, span id]
     */
    @RequestMapping("/")
    public String home() {
        log.info("Hello world!");
        return "Hello World!";
    }

    @Resource
    private PaymentService paymentService;

    /**
     * http://localhost:8989/2
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<String> test(@PathVariable("id") Long id) {
        return paymentService.payment(id);
    }

}
