package org.example.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * 防重复提交与请求参数防篡改
 */
public class TamperAndResubmitFilter extends ZuulFilter {

    /**
     * 过滤器具体的业务逻辑
     */
    @Override
    public Object run() {

        // 防重复提交使用拦截器配合redis实现（参考rate模块RepeatSubmitAspect.class）
        return null;
    }

    /**
        四种过滤器类型，详细见readme.md
         1、pre：可以在请求被路由之前调用；
         2、route：在路由请求时候被调用；
         3、post：在route和error过滤器之后被调用；
         4、error：处理请求时发生错误时被调用；
     */
    @Override
    public String filterType() {
        // FilterConstants.PRE_TYPE;
        // FilterConstants.ROUTE_TYPE;
        // FilterConstants.ERROR_TYPE;
        // FilterConstants.POST_TYPE;
        return FilterConstants.PRE_TYPE;
    }

    /**
     * 该过滤器在所有过滤器的执行顺序值
     * 值越小，优先级越高
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 该过滤器是否生效
     * 返回true执行该过滤器，返回false不执行该过滤器
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

}
