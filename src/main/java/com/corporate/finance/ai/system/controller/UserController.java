package com.corporate.finance.ai.system.controller;

import com.corporate.finance.ai.system.entity.UserEntity;
import com.corporate.finance.ai.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public void createUser(@RequestBody UserEntity user) {
        userService.createUser(user);
    }

    /**
     * 更新用户
     */
    @PutMapping
    public void updateUser(@RequestBody UserEntity user) {
        userService.updateUser(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/status")
    public void updateUserStatus(@RequestParam Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/reset-password/{userId}")
    public void resetPassword(@PathVariable Long userId) {
        userService.resetPassword(userId);
    }

    /**
     * 分配角色给用户
     */
    @PutMapping("/assign-roles")
    public void assignRoles(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/roles/{userId}")
    public List<Long> getUserRoles(@PathVariable Long userId) {
        return userService.getUserRoles(userId);
    }

    /**
     * 查询用户列表
     */
    @GetMapping
    public List<UserEntity> queryUsers(@RequestParam(required = false) String username, 
                                      @RequestParam(required = false) String mobile, 
                                      @RequestParam(required = false) Integer status) {
        return userService.queryUsers(username, mobile, status);
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{userId}")
    public UserEntity getUserById(@PathVariable Long userId) {
        return userService.getById(userId);
    }

}
