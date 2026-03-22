package com.corporate.finance.ai.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.corporate.finance.ai.system.dao.UserDao;
import com.corporate.finance.ai.system.dao.UserRoleDao;
import com.corporate.finance.ai.system.entity.UserEntity;
import com.corporate.finance.ai.system.entity.UserRoleEntity;
import com.corporate.finance.ai.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${system.password.reset-default}")
    private String resetDefaultPassword;

    @Override
    @Transactional
    public void createUser(UserEntity user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认状态（如果未设置）
        if (user.getStatus() == null) {
            user.setStatus(0);
        }
        // 设置默认删除标记（如果未设置）
        if (user.getDeleted() == null) {
            user.setDeleted(false);
        }
        // 保存用户（createTime, updateTime, creator, updater 会自动填充）
        save(user);
    }

    @Override
    public void updateUser(UserEntity user) {
        // 更新用户信息时，不更新密码字段（密码重置使用专门的方法）
        user.setPassword(null);
        updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        // 逻辑删除用户
        removeById(userId);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public void resetPassword(Long userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(resetDefaultPassword));
        updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色关联
        userRoleDao.delete(new QueryWrapper<UserRoleEntity>().eq("user_id", userId));
        // 添加新角色关联
        for (Long roleId : roleIds) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleDao.insert(userRole);
        }
    }

    @Override
    public List<Long> getUserRoles(Long userId) {
        return userRoleDao.selectList(new QueryWrapper<UserRoleEntity>().eq("user_id", userId))
                .stream().map(UserRoleEntity::getRoleId).toList();
    }

    @Override
    public List<UserEntity> queryUsers(String username, String mobile, Integer status) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        if (mobile != null && !mobile.isEmpty()) {
            queryWrapper.like("mobile", mobile);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        return list(queryWrapper);
    }

}
