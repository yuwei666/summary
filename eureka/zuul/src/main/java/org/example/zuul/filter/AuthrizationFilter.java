package org.example.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

public class AuthrizationFilter extends ZuulFilter {

    /**
     * 拦截类型,4种类型 pre route error post
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    /**
     * 该过滤器在所有过滤器的执行顺序值
     * 值越小，越前面执行
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
        //获取上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 建议用这种方式
        if (!requestContext.sendZuulResponse()) {
            // 返回false 表示不执行该拦截器
            return false;
        }
//		另外一种方式，不让执行该拦截器
//		boolean ifContinue = (boolean) requestContext.get("ifContinue");
//		if (ifContinue){
//			return true;
//		}else {
//			return false;
//		}

        return true;// 返回true 表示执行该拦截器
    }

    /**
     * 过滤器具体的业务逻辑
     */
    @Override
    public Object run() {

        //获取上下文（重要，贯穿 所有filter，包含所有参数）
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token)) {

            // 具体鉴权可使用saToken框架（sa-token模块有代码），或自己实现均可，这里只写思路
            boolean result = false;
            if (result) {
                System.out.println("鉴权成功,进入下一个阶段");
                return null;
            }

        }

        // 不往下走，还走剩下的过滤器，但是不向后面的服务转发。
        // 这里不让他过 里面设置false就可以
        requestContext.setSendZuulResponse(false);
        requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        requestContext.setResponseBody("auth fail");

//      设置全局 其他过滤器should中出现该值是false就后面不执行了
//        requestContext.set("ifContinue",false);

        return null;
    }

}
