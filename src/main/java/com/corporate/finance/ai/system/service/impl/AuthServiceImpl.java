package com.corporate.finance.ai.system.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corporate.finance.ai.system.dao.UserDao;
import com.corporate.finance.ai.system.entity.UserEntity;
import com.corporate.finance.ai.system.entity.UserVO;
import com.corporate.finance.ai.system.service.AuthService;
import com.corporate.finance.ai.system.utils.JwtUtils;
import com.corporate.finance.ai.system.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${system.login.max-fail-count}")
    private int maxFailCount;

    @Value("${system.login.lock-time}")
    private int lockTime;

    @Value("${system.aes.key}")
    private String aesKey;

    @Value("${system.aes.iv}")
    private String aesIv;

    @Override
    public Map<String, Object> login(String username, String password, String captcha, String captchaKey) {
        // 检查用户是否被锁定
        String lockKey = "login:lock:" + username;
        if (redisUtils.exists(lockKey)) {
            throw new RuntimeException("账号已被锁定，请稍后再试");
        }

        // 验证验证码
        if (captchaKey == null || captcha == null || captcha.trim().isEmpty()) {
            handleLoginFail(username);
            throw new RuntimeException("验证码不能为空");
        }
        
        String redisCaptchaKey = "captcha:" + captchaKey;
        String correctCaptcha = (String) redisUtils.get(redisCaptchaKey);
        if (correctCaptcha == null || !correctCaptcha.equalsIgnoreCase(captcha)) {
            handleLoginFail(username);
            throw new RuntimeException("验证码错误");
        }
        // 验证码使用后立即删除，防止重复使用
        redisUtils.delete(redisCaptchaKey);

        // 解密密码
        String rawPassword;
        try {
            rawPassword = decryptPassword(password, aesKey, aesIv);
        } catch (Exception e) {
            handleLoginFail(username);
            throw new RuntimeException("密码解密失败", e);
        }

        // 查询用户
        UserEntity user = userDao.selectOne(new QueryWrapper<UserEntity>().eq("username", username));
        if (user == null) {
            handleLoginFail(username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 验证密码
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            handleLoginFail(username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 登录成功，清除失败计数
        String failKey = "login:fail:" + username;
        redisUtils.delete(failKey);

        // 生成 JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId());
        String token = jwtUtils.generateToken(claims);

        // 存储 token 到 Redis，实现单点登录
        String tokenKey = "token:" + user.getUsername();
        redisUtils.set(tokenKey, token, 24, TimeUnit.HOURS);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", convertToUserVO(user));
        return result;
    }

    @Override
    public void logout(String token) {
        // 从token中获取用户名
        String username = jwtUtils.getUsernameFromToken(token);
        // 删除Redis中的token
        String tokenKey = "token:" + username;
        redisUtils.delete(tokenKey);
    }

    @Override
    public UserEntity getUserInfo(String username) {
        return userDao.selectOne(new QueryWrapper<UserEntity>().eq("username", username));
    }

    /**
     * 将 UserEntity 转换为 UserVO，过滤敏感字段
     */
    private UserVO convertToUserVO(UserEntity user) {
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

    @Override
    public Map<String, Object> generateCaptcha() {
        // 生成验证码 Key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        
        // 使用 Hutool 生成验证码图片 (LineCaptcha - 线段干扰)
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 80, 4, 50);
        
        // 获取验证码文本
        String captchaText = captcha.getCode();
        
        // 存储验证码到 Redis，5 分钟过期
        String redisKey = "captcha:" + captchaKey;
        redisUtils.set(redisKey, captchaText, 5, TimeUnit.MINUTES);

        // 将验证码图片转换为 Base64 编码
        byte[] imageBytes = captcha.getImageBytes();
        String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
        
        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captcha", "data:image/png;base64," + base64Image);
        return result;
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFail(String username) {
        String failKey = "login:fail:" + username;
        Long failCount = redisUtils.increment(failKey, 1);
        if (failCount == 1) {
            // 第一次失败，设置过期时间为30分钟
            redisUtils.expire(failKey, 30, TimeUnit.MINUTES);
        } else if (failCount >= maxFailCount) {
            // 达到最大失败次数，锁定账号
            String lockKey = "login:lock:" + username;
            redisUtils.set(lockKey, "1", lockTime, TimeUnit.MINUTES);
        }
    }

    /**
     * 解密 AES 加密的密码
     * @param encryptedPassword 加密的密码 (Base64 编码)
     * @param key AES 密钥 (32 字符)
     * @param iv AES 向量 (16 字符)
     * @return 解密后的原始密码
     * @throws Exception 解密失败
     */
    private String decryptPassword(String encryptedPassword, String key, String iv) throws Exception {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("密码解密失败", e);
        }
    }

}
