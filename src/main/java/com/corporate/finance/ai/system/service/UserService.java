package com.corporate.finance.ai.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.corporate.finance.ai.system.entity.UserEntity;
import java.util.List;

public interface UserService extends IService<UserEntity> {

    /**
     * 创建用户
     */
    void createUser(UserEntity user);

    /**
     * 更新用户
     */
    void updateUser(UserEntity user);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long userId);

    /**
     * 启用/禁用用户
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId);

    /**
     * 分配角色给用户
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色列表
     */
    List<Long> getUserRoles(Long userId);

    /**
     * 根据条件查询用户
     */
    List<UserEntity> queryUsers(String username, String mobile, Integer status);

}
