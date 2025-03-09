package org.example.feignapi.service;

import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

import java.util.concurrent.TimeUnit;

/**
 * 手动构建Feign客户端代码
 */
public class FeignClientBuilder {

    public static RemoteService buildRemoteService(String baseUrl) {
        // 设置连接超时和读取超时
        int connTimeout = 5; // 连接超时时间（秒）
        int readTimeout = 10; // 读取超时时间（秒）

        return Feign.builder()
                .encoder(new JacksonEncoder()) // 使用Jackson编码器
                .decoder(new JacksonDecoder()) // 使用Jackson解码器
                .options(new Request.Options(connTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
                .logger(new Slf4jLogger(RemoteService.class)) // 使用SLF4J日志
                .logLevel(Logger.Level.FULL) // 设置日志级别为FULL（记录请求和响应的详细信息）
                .target(RemoteService.class, baseUrl); // 目标接口和目标URL
    }

    public static void main(String[] args) {
        // 构建Feign客户端
        RemoteService service = buildRemoteService("http://example.com");

        // 调用远程服务
        String response = service.getData("123");
        System.out.println("Response: " + response);

        // 发送POST请求
        String requestBody = "{\"name\":\"test\",\"value\":\"123\"}";
        String postResponse = service.createData(requestBody);
        System.out.println("POST Response: " + postResponse);
    }
}
