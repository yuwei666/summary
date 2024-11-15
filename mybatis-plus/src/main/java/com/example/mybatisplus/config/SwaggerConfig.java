package com.example.mybatisplus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;

@Configuration
public class SwaggerConfig {

    //创建swagger实例
    /*@Bean
    public Docket docket() {
        Docket docket=
                new Docket(DocumentationType.SWAGGER_2)
                        .apiInfo(getInfo())//设置接口文档的信息
                        .select()
                        .apis(RequestHandlerSelectors.basePackage("com.example.controller")) //指定为那些路径下得到类生成接口文档
                        .build()
                ;

        return docket;
    }

    private ApiInfo getInfo(){
        Contact DEFAULT_CONTACT = new Contact("李四", "http://www.ldw.com", "110@qq.com");
        ApiInfo DEFAULT = new ApiInfo("用户管理系统API", "该系统中的接口专门操作用户的", "v1.0", "http://www.baidu.com",
                DEFAULT_CONTACT, "与相似", "http://www.jd.com", new ArrayList<VendorExtension>());
        return DEFAULT;
    }*/
}
