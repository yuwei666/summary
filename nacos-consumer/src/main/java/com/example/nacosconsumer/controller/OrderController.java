package com.example.nacosconsumer.controller;

import com.example.nacosconsumer.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
public class OrderController {

    public static final String SERVICE_URL = "http://payment-service";

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/lb/{id}")
    public ResponseEntity<String> consumer_ribbon(@PathVariable("id") Integer id){
        String result = restTemplate.getForObject(SERVICE_URL + "/payment/" + id, String.class);
        return ResponseEntity.ok(result);
    }

    //OpenFeign
    @Resource
    private PaymentService paymentService;

    @GetMapping(value = "/feign/{id}")
    public ResponseEntity<String> consumer_feign(@PathVariable("id") Long id) {
        return paymentService.payment(id);
    }

}
