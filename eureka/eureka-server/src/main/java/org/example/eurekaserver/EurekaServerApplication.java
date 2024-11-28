package org.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    /**
     * 访问eureka页面：http://localhost:8761/
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
