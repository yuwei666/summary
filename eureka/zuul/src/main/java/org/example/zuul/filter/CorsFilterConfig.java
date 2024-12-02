package org.example.zuul.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 解决跨域
 */
@Configuration
public class CorsFilterConfig {

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        // 允许cookies跨域
        config.setAllowCredentials(true);

        /*List<String> allowedOrigins = new ArrayList<>();
        allowedOrigin = appConfig.getCorsAllowedOrigin();
        // 允许向该服务器提交请求的URI，*表示全部允许。。这里尽量限制来源域，比如http://xxxx:8080
        if (StringUtils.isEmpty(allowedOrigin)) {
//        	config.addAllowedOrigin("*");
            config.addAllowedOriginPattern("*");//spring web5.3.x再启用
        } else {
            String[] split = allowedOrigin.split(",");
            for (String string : split) {
                allowedOrigins.add(string);
            }
//	        config.setAllowedOrigins(allowedOrigins);
            config.setAllowedOriginPatterns(allowedOrigins);//spring web5.3.x再启用
        }*/

        // 允许访问的头信息,*表示全部
        config.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        config.setMaxAge(18000L);
        // 允许提交请求的方法，*表示全部允许，也可以单独设置GET、PUT等
        //config.addAllowedMethod("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        List<String> exposedHeaders = new ArrayList<>();
        exposedHeaders.add("timestamp");
        exposedHeaders.add("Content-Disposition");
        config.setExposedHeaders(exposedHeaders);

        source.registerCorsConfiguration("/**", config);
        return new org.springframework.web.filter.CorsFilter(source);
    }

}
