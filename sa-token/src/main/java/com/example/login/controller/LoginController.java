package com.example.login.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import com.example.login.config.CaptchaProperties;
import com.example.login.constance.Constants;
import com.example.login.enums.CaptchaType;
import com.example.login.service.LoginService;
import com.example.login.utils.RsaEncryptor;
import com.example.satoken.domain.model.LoginBody;
import com.example.satoken.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final CaptchaProperties captchaProperties;
    private final LoginService loginService;

    /**
     * 获取验证码
     * @return
     */
    @GetMapping("/captchaImage")
    public Object getCode() {

        String uuid = IdUtil.fastSimpleUUID();
        String captchaKey = Constants.CAPTCHA + uuid;

        Integer length = captchaProperties.getLength();

        // 中间省略验证码生成方式，太多了，懒得弄了，自己查吧
        CodeGenerator codeGenerator = ReflectUtil.newInstance(CaptchaType.MATH.getClazz(), length);
        // ...
        // 最终得到一个验证码字符串

        String code = "7654";
        // 将验证码写入redis
        RedisUtils.setCacheObject(captchaKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));

        HashMap map = new HashMap();
        map.put("uuid", uuid);
        //map.put("img", img);

        return map;
    }

    @SaIgnore
    @PostMapping("/login")
    public Object login(@Validated @RequestBody LoginBody loginBody, HttpServletRequest request) {

        RsaEncryptor rsaEncryptor = new RsaEncryptor();
        String username = rsaEncryptor.decrypt(loginBody.getUsername());
        String password = rsaEncryptor.decrypt(loginBody.getPassword());

        loginService.login(username, password, null, null);

        return null;
    }

}
