package com.corporate.finance.ai.system.controller;

import com.corporate.finance.ai.system.entity.UserEntity;
import com.corporate.finance.ai.system.entity.UserVO;
import com.corporate.finance.ai.system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String captcha = params.get("captcha");
        String captchaKey = params.get("captchaKey"); // 新增：验证码 key
        return authService.login(username, password, captcha, captchaKey);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public Map<String, Object> getCaptcha() {
        return authService.generateCaptcha();
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public UserVO getUserInfo(@RequestHeader("Authorization") String token) {
        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // TODO: 从 token 中获取用户名
        String username = "admin"; // 临时测试
        UserEntity user = authService.getUserInfo(username);
        return convertToUserVO(user);
    }

    /**
     * 将 UserEntity 转换为 UserVO
     */
    private UserVO convertToUserVO(UserEntity user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setMobile(user.getMobile());
        userVO.setEmail(user.getEmail());
        userVO.setDeptId(user.getDeptId());
        userVO.setPostIds(user.getPostIds());
        userVO.setStatus(user.getStatus());
        userVO.setRemark(user.getRemark());
        userVO.setCreator(user.getCreator());
        
        // 格式化日期时间
        if (user.getCreateTime() != null) {
            userVO.setCreateTime(user.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (user.getUpdateTime() != null) {
            userVO.setUpdateTime(user.getUpdateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return userVO;
    }

}
