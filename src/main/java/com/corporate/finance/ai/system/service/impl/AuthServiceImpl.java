package com.corporate.finance.ai.system.service.impl;

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
        if (!passwordEncoder.matches(password, user.getPassword())) {
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
        // 生成验证码
        String captcha = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        
        // 存储验证码到 Redis，5 分钟过期
        String redisKey = "captcha:" + captchaKey;
        redisUtils.set(redisKey, captcha, 5, TimeUnit.MINUTES);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        // 注意：实际项目中这里应该返回验证码图片的 Base64 编码
        // 前端使用<img src="data:image/png;base64,{base64Image}"/>显示
        // 测试阶段先返回明文，生产环境务必删除这行
        result.put("captcha", captcha); 
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

}
