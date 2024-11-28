package org.example.eurekaserver7001;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServer7001Application {

    /**
     * http://eureka7001.com:7001/
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer7001Application.class, args);
    }

}
