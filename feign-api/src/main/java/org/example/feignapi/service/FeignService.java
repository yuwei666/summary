package org.example.feignapi.service;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * 使用 @Feign手动构造，定义远程服务
 *
 */
public interface FeignService {

    @RequestLine("GET /api/data/{id}")
    @Headers("Content-Type: application/json")
    String getData(@Param("id") String id);

    @RequestLine("POST /api/data")
    @Headers("Content-Type: application/json")
    String createData(String requestBody);

}
