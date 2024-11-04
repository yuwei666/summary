package com.example.login.enums;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import com.example.login.config.UnSignedMathGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaptchaType {

    MATH(UnSignedMathGenerator.class),

    CHAR(RandomGenerator.class);

    /**
     * 我擦，这块留个伏笔
     */
    private final Class<? extends CodeGenerator> clazz;

}
