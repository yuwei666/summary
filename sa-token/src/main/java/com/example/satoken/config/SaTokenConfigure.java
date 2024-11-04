package com.example.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.example.satoken.config.properties.SecurityProperties;
import com.example.satoken.handler.AllUrlHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Slf4j
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SaTokenConfigure implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> {
                    AllUrlHandler allUrlHandler = SpringUtil.getBean(AllUrlHandler.class);
                    SaRouter.match(allUrlHandler.getUrls())
                            // 写在这里可以动态化
                            .notMatch(securityProperties.getExcludes())
                            .check(() -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/user/**")
                // 只会在启动时执行一次
                .excludePathPatterns(securityProperties.getExcludes());
    }

    /**
     * Sa-Token 整合 jwt (Simple 简单模式)
     * 需要同时引入 jwt 与 Redis
     * @return
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

}
