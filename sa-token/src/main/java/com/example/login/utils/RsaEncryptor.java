package com.example.login.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.example.login.constance.Constants;
import jodd.util.Base64;

/**
 * RSA算法实现
 */
public class RsaEncryptor{

    private final RSA rsa;

    public RsaEncryptor() {
        this.rsa = SecureUtil.rsa(Base64.decode(Constants.PRIVATE_SECRET), Base64.decode(Constants.PUBLIC_SECRET));
    }

    /**
     * 加密
     * @param value
     * @return
     */
    public String encrypt(String value) {
        return rsa.encryptBase64(value, KeyType.PublicKey);
    }

    /**
     * 解密
     * @param value
     * @return
     */
    public String decrypt(String value) {
        return rsa.decryptStr(value, KeyType.PrivateKey);
    }
}
