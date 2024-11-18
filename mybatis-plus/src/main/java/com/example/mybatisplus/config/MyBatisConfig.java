package com.example.mybatisplus.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.example.mybatisplus.interceptor.PlusDataPermissionInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("${mybatis-plus.mapperPackage}")
public class MyBatisConfig {

    /**
     * 分页插件
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){

        // 初始化 MybatisPlusInterceptor 核心插件
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 数据权限拦截器
        interceptor.addInnerInterceptor(plusDataPermissionInterceptor());
        // 添加自动分页插件
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(optimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 数据权限拦截器
     * @return
     */
    public PlusDataPermissionInterceptor plusDataPermissionInterceptor() {
        return new PlusDataPermissionInterceptor();
    }

    /**
     * 分页插件，自动识别数据类型
     */
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置分页单页最大限制为无限制，默认500
        paginationInnerInterceptor.setMaxLimit(-1L);
        // 分页合理化
        paginationInnerInterceptor.setOverflow(true);
        return paginationInnerInterceptor;
    }

    /**
     * 乐观锁插件
     */
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }

}
