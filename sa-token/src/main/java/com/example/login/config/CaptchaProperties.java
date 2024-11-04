package com.example.login.config;

import com.example.login.enums.CaptchaType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "captcha")
@Data
public class CaptchaProperties {

    private CaptchaType type;

    private Integer charLength;

    private Integer length;

}
