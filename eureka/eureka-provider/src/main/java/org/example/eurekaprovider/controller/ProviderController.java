package org.example.eurekaprovider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {

    @Value("${server.port}")
    private String port;

    @RequestMapping("/getPort")
    public Object getPort() {
        return "服务：eureka-provider，port：" + port;
    }

}
