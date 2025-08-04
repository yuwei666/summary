package org.example.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.DataSource;

@RestController
public class TestController {

    @Autowired
    DataSource dataSource;


    @RequestMapping("/test")
    public String test() {
        return "test";
    }

    

}
