package com.example.rate.aspectj;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.rate.annotation.RateLimiter;
import com.example.rate.enums.LimitType;
import com.example.rate.exception.ServiceException;
import com.example.rate.utils.ServletUtils;
import com.example.utils.MessageUtils;
import com.example.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RateType;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    /**
     * 定义SpEL表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 定义SpEL解析模板
     */
    private final ParserContext parserContext = new TemplateParserContext();

    /**
     * 定义SpEL上下文对象进行解析
     */
    private final EvaluationContext context = new StandardEvaluationContext();

    /**
     * 方法参数解析器
     */
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();


/*    @Around("@annotation(rateLimiter)")
    public Object doAround(ProceedingJoinPoint point, RateLimiter rateLimiter) throws Throwable {
        // 业务代码1
        Object proceed = point.proceed();
        // 业务代码2
        return proceed;
    }*/

    /**
     * 方法前执行
     * @param point
     * @param rateLimiter
     */
    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        int time = rateLimiter.time();
        int count = rateLimiter.count();
        // combineKey: rate_limit/rate/test:rate
        // 针对每个接口进行限流
        String combineKey = getCombineKey(rateLimiter, point);
        try {
            RateType rateType = RateType.OVERALL;
            if (rateLimiter.limitType() == LimitType.CLUSTER) {
                rateType = RateType.PER_CLIENT; // 单实例
            }

            long number = RedisUtils.rateLimiter(combineKey, rateType, count, time);
            if (number == -1) {
                String message = rateLimiter.message();
                // 这块应该是把{}替换了，替换成了啥？
                // (百度找的原因)项目中使用到了thymeleaf作为模板渲染页面
                if (StrUtil.startWith(message, "{") && StrUtil.endWith(message, "}")) {
                    message = MessageUtils.message(StrUtil.sub(message, 1, message.length() - 1));
                }
                throw new ServiceException(message);
            }
            log.info("限制令牌 => {}, 剩余令牌 => {}, 缓存key => '{}'", count, number, combineKey);

        } catch (Exception e) {
            if(e instanceof ServiceException) {
                throw e;
            } else {
                throw new RuntimeException("服务器限流异常，请稍后再试");
            }
        }

    }

    /**
     *
     * @param rateLimiter
     * @param point
     * @return
     */
    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        String key = rateLimiter.key();
        // 获取方法（通过方法签名获取）
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();

        Class<?> targetClass = method.getDeclaringClass();
        if(StrUtil.containsAny(key, "#")) {
            Object[] args = point.getArgs();
            String[] parameterNames = pnd.getParameterNames(method);
            if(ArrayUtil.isEmpty(parameterNames)) {
                throw new ServiceException("限流key解析异常，请联系管理员");
            }
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
            // 解析返回给key
            try {
                key = parser.parseExpression(key, parserContext).getValue(context, String.class) + ":";
            } catch (Exception e) {
                throw new ServiceException("限流key解析异常，请联系管理员");
            }
        }

        StringBuilder stringBuilder = new StringBuilder("rate_limit");
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        stringBuilder.append(requestAttributes.getRequest().getRequestURI()).append(":");
        if(rateLimiter.limitType() == LimitType.IP) {
            stringBuilder.append(ServletUtils.getClientIP()).append(":");
        } else if(rateLimiter.limitType() == LimitType.CLUSTER){
            stringBuilder.append(RedisUtils.getClient().getId()).append(":");
        }

        return stringBuilder.append(key).toString();
    }

}
