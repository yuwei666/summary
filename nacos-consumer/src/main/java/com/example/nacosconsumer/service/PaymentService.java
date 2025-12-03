package com.example.nacosconsumer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @FeignClient 注解用于声明一个 Feign 客户端，该客户端可以用来调用远程服务
 *  fallbackFactory 和 fallback 是两个重要的属性，用于处理服务调用的失败情况，确保在远程服务不可用时客户端能够优雅地处理错误情况
 *  fallback 属性
 *  定义：fallback 属性用于指定一个降级处理类（即一个实现了接口的类），当 Feign 客户端调用失败时，自动回退到这个类中定义的处理逻辑。
 */
@FeignClient(value = "payment-service", fallbackFactory = PaymentFallbackFactoryService.class)
public interface PaymentService {

    @GetMapping("/payment/{id}")
    ResponseEntity<String> payment(@PathVariable("id") Long id);

    /*
    * Feign 默认会将方法参数作为请求体（@RequestBody）处理，如果方法有多个参数，Feign 会报错
    * 多个参数时使用 @RequestParam 注解明确指定：
    * */
    ResponseEntity<String> payment(Object id);

    /**
     * 文件上传，使用 @RequestPart 注解，指定consumes
     * @param file
     * @param group
     * @return
     */
    @PostMapping(value = "/internal/file/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("group") String group);

}
