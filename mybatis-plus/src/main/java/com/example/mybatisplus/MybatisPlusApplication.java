package com.example.mybatisplus;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import com.baomidou.dynamic.datasource.toolkit.CryptoUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("${mybatis-plus.mapperPackage}")
public class MybatisPlusApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MybatisPlusApplication.class, args);
//        printKey();
    }

    public static void printKey() throws Exception {
        //CryptoUtils 为自带工具 keySize 密钥越长，安全性越高
        String[] strings = CryptoUtils.genKeyPair(512);
        //公钥用于加密，私钥用于解密，基于Rsa算法进行的加密
        String publicKey, primaryKey;
        System.out.println("公钥：" + (publicKey = strings[0]));
        System.out.println("私钥：" + (primaryKey = strings[1]));

//        publicKey = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAnxSCQwl8ez5e9rOsm71asEY8Q1cFnBaogTyJ0eoZ/vgrteZoSt2+HauSPiNomClJcqSrn0ULpe9BqivW1diM/QIDAQABAj9V1ZVN3fIN3XfuqXl0616ramANPQjFpWvEPwePg4JAMa/uXYwYgQrCdR15QUrsRFpNfpnJ54i4CdPQVkyC7vkCIQDcOiWmfxpOY66no8IzBGP/gw5QLbJeo7Y0WPT9xtVPCwIhALjrpNxYlKqDcA3Kq3DL4dqINNuGwULaTZxjI3+PXzkXAiEAmxAyLG54VlOnyY/rkEJ6KpHbxSa33h1FkMxGDF4xGYsCIBE2Pb4UsKG+YiumdZamI3uHugPE4zApyZeI0sEIw0A5AiB0Xc3jTUfvrssr8tLLsAPgH2c5z04pK8KcJYuYQ03tpQ==";
//        primaryKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJ8UgkMJfHs+XvazrJu9WrBGPENXBZwWqIE8idHqGf74K7XmaErdvh2rkj4jaJgpSXKkq59FC6XvQaor1tXYjP0CAwEAAQ==";

        // 公钥加密
        String encrypt = CryptoUtils.encrypt(publicKey, "root");
        System.out.println("密码加密为：" + encrypt);
        // 私钥进行解密
        System.out.println("密码解密为：" + CryptoUtils.decrypt(primaryKey, encrypt));

        System.out.println(CryptoUtils.encrypt("root"));
    }

}
