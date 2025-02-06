package org.example.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * 限流
 */
@Configuration
public class LimitFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        // 限流应该是所有过滤器之前，所以该值越小越好
        return -100;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    // RRateLimiter rateLimiter = Redisson.getRateLimiter();

    /**
     * 限流参考sa-token模块的RateLimiterAspect.class
     */
    @Override
    public Object run() throws ZuulException {

        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        // 以下皆为伪代码
        /*
        log.info("剩余令牌：{}", rateLimiter.availablePermits());

        if(rateLimiter.tryAcquire()) {
            // 放行
            return null;
        } else {
            return false;
        }*/
        return null;
    }
}
