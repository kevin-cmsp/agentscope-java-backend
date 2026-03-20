package com.corporate.finance.ai.system.service;

import com.corporate.finance.ai.system.entity.UserEntity;
import java.util.Map;

public interface AuthService {

    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password, String captcha, String captchaKey);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 获取用户信息
     */
    UserEntity getUserInfo(String username);

    /**
     * 生成验证码
     */
    Map<String, Object> generateCaptcha();

}
