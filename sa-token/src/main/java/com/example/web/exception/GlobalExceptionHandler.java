package com.example.web.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;


/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public SaResult handlerNotPermissionException(NotPermissionException e) {
        e.printStackTrace();
        //log.error打印日志
        return SaResult.error(e.getMessage());
    }

    @ExceptionHandler({NotLoginException.class})
    public SaResult handlerNotLoginException(NotLoginException e, HttpServletRequest request) {
        //log.error打印日志
        String requestURI = request.getRequestURI();
        System.out.println("请求地址" + requestURI + "认证失败，" + e.getMessage() + "无法访问资源");

        return SaResult.error("认证失败");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SaResult handle(MethodArgumentNotValidException e) {
        return SaResult.error(e.getBindingResult().getFieldError().getDefaultMessage());
    }

}
