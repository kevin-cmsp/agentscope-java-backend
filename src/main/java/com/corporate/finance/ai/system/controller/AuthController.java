package com.corporate.finance.ai.system.controller;

import com.corporate.finance.ai.system.common.Result;
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
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String captcha = params.get("captcha");
            String captchaKey = params.get("captchaKey");
            Map<String, Object> result = authService.login(username, password, captcha, captchaKey);
            return Result.success("登录成功", result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.serverError("登录失败，请稍后重试");
        }
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            authService.logout(token);
            return Result.success("退出成功", null);
        } catch (Exception e) {
            return Result.error("退出失败：" + e.getMessage());
        }
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String, Object>> getCaptcha() {
        try {
            Map<String, Object> result = authService.generateCaptcha();
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取验证码失败");
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String username = "admin";
            UserEntity user = authService.getUserInfo(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(convertToUserVO(user));
        } catch (Exception e) {
            return Result.serverError("获取用户信息失败");
        }
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
