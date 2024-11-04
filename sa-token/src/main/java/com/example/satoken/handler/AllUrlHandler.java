package com.example.satoken.handler;

import cn.hutool.core.util.ReUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.regex.Pattern;

@Data
@Component
public class AllUrlHandler implements InitializingBean {

    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    private List<String> urls = new ArrayList<>();

    /**
     * 替换占位符RequestMapping中占位符{xxx} 为 *
     * 如：/tokenInfo/{id} -> /tokenInfo/*
     */
    @Override
    public void afterPropertiesSet() {
        Set<String> set = new HashSet<>();
        RequestMappingHandlerMapping mapping = SpringUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        // 获取所有请求路径集合 key:{ [/user/doLogin]}  value: com.example.satoken.controller.UserController#doLogin(String, String)
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        map.keySet().forEach(info -> {
            Objects.requireNonNull(info.getPathPatternsCondition().getPatterns())
                    .forEach(url -> set.add(ReUtil.replaceAll(url.getPatternString(), PATTERN, "*")));
        });
        urls.addAll(set);
    }
}
