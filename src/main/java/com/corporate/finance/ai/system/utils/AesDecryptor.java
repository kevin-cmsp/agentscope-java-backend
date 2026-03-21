package com.corporate.finance.ai.system.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 密码解密工具类
 */
@Component
public class AesDecryptor {

    private static final String SECRET_KEY = "your-32-character-secret-key-here"; // 32 字符密钥
    private static final String IV = "your-16-character-iv-here"; // 16 字符向量

    /**
     * 解密前端传来的 AES 加密密码
     * @param encryptedPassword 前端传来的加密密码 (Base64 编码)
     * @return 解密后的原始密码
     * @throws Exception 解密失败时抛出异常
     */
    public static String decryptPassword(String encryptedPassword) throws Exception {
        try {
            // 将密钥和向量转换为字节数组
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = IV.getBytes(StandardCharsets.UTF_8);

            // 创建密钥规范和 IV 参数规范
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            // 创建 Cipher 实例，使用 AES/CBC/PKCS5Padding
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // Base64 解码
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);

            // 解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // 返回解密后的字符串
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("密码解密失败", e);
        }
    }
}

