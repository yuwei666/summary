package com.example.mybatisplus.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

/**
 * @author yuwei
 * @desc
 * @date 2025/5/27
 */
public class JasyptUtil {

    /**
     * 加密
     * @param key   加解密使用的key
     * @param value 需要加密的字符串
     * @return
     */
    public static String encrypt(String key, String value) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        // 加密key，读取配置 jasypt.encryptor.password=1234567890
        // 推荐：启动时增加参数： JASYPT_ENCRYPTOR_PASSWORD=your_key java -jar app.jar
        encryptor.setPassword(key);
        // 加密算法
        encryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor.encrypt(value);
    }

    /**
     * 解密
     * @param key   加解密使用的key
     * @param encrypted 需要解密的字符串
     * @return
     */
    public static String decrypt(String key, String encrypted) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(key);
        encryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor.decrypt(encrypted);
    }

    public static void main(String[] args) {
        String encrypt = JasyptUtil.encrypt("1234567890", "Philips@2022");
        System.out.println(encrypt);
    }
}
