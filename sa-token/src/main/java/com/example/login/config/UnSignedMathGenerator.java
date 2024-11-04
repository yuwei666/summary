package com.example.login.config;

import cn.hutool.captcha.generator.CodeGenerator;

public class UnSignedMathGenerator implements CodeGenerator {
    @Override
    public String generate() {
        return null;
    }

    @Override
    public boolean verify(String s, String s1) {
        return false;
    }
}
