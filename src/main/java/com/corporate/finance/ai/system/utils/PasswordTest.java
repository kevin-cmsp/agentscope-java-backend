package com.corporate.finance.ai.system.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 测试密码 "admin123"
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("原始密码：" + rawPassword);
        System.out.println("BCrypt 加密后：" + encodedPassword);
        System.out.println("验证结果：" + encoder.matches(rawPassword, encodedPassword));
    }
}
