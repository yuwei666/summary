package com.example.login.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.login.constance.Constants;
import com.example.login.entity.User;
import com.example.login.exception.UserException;
import com.example.satoken.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class LoginService {

    public String login(String username, String password, String code, String uuid) {
        User user = loadUserByUsername(username);
        // 校验密码是否正确
        checkLogin(username, () -> !BCrypt.checkpw(password, user.getPassword()));

        StpUtil.login(username);
        String tokenValue = StpUtil.getTokenInfo().getTokenValue();
        return tokenValue;
    }

    private void checkLogin(String username, Supplier<Boolean> supplier) {
        String errorKey = Constants.PWD_ERR_CNT_KEY + username;
        String maxRetryCount = "5";
        Integer lockTime = 5;
        // 获取登录错误次数，如果超过最大次数，则不允许继续登录
        Integer errorNumber = RedisUtils.getCacheObject(errorKey);
        if(ObjectUtil.isNotEmpty(errorNumber) && errorNumber.equals(maxRetryCount)) {
            // 记录登录信息
            recordLoginInformation(username, "不允许登录", "不允许登录");
            throw new UserException();
        }

        if(!supplier.get()) {
            errorNumber = ObjectUtil.isNull(errorNumber) ? 1 : errorNumber + 1;
            if(errorNumber.equals(maxRetryCount)) {
                RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
                // 记录登录错误次数信息
                recordLoginInformation(username, "记录登录错误次数信息", "记录登录错误次数信息");
                throw new UserException();
            } else {
                RedisUtils.setCacheObject(errorKey, errorNumber);
                // 记录登录错误次数信息
                recordLoginInformation(username, "记录登录错误次数信息", "记录登录错误次数信息");
                throw new UserException();
            }
        }
        RedisUtils.deleteObject(errorKey);
    }

    public void recordLoginInformation(String username, String status, String message) {

    }

    private User loadUserByUsername(String username) {
        User user = new User();
        // 加密后的
        user.setPassword("xxxx");
        return user;
    }

}
