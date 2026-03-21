package com.corporate.finance.ai.system.controller;

import com.corporate.finance.ai.system.common.Result;
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
    public Result<Void> createUser(@RequestBody UserEntity user) {
        try {
            userService.createUser(user);
            return Result.success("创建成功", null);
        } catch (Exception e) {
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Result<Void> updateUser(@RequestBody UserEntity user) {
        try {
            userService.updateUser(user);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/status")
    public Result<Void> updateUserStatus(@RequestParam Long userId, @RequestParam Integer status) {
        try {
            userService.updateUserStatus(userId, status);
            return Result.success("操作成功", null);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/reset-password/{userId}")
    public Result<Void> resetPassword(@PathVariable Long userId) {
        try {
            userService.resetPassword(userId);
            return Result.success("重置成功", null);
        } catch (Exception e) {
            return Result.error("重置失败：" + e.getMessage());
        }
    }

    /**
     * 分配角色给用户
     */
    @PutMapping("/assign-roles")
    public Result<Void> assignRoles(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        try {
            userService.assignRoles(userId, roleIds);
            return Result.success("分配成功", null);
        } catch (Exception e) {
            return Result.error("分配失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/roles/{userId}")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        try {
            List<Long> result = userService.getUserRoles(userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取用户角色失败");
        }
    }

    /**
     * 查询用户列表
     */
    @GetMapping
    public Result<List<UserEntity>> queryUsers(@RequestParam(required = false) String username, 
                                      @RequestParam(required = false) String mobile, 
                                      @RequestParam(required = false) Integer status) {
        try {
            List<UserEntity> result = userService.queryUsers(username, mobile, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("查询用户失败");
        }
    }

    /**
     * 根据 ID 获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserEntity> getUserById(@PathVariable Long userId) {
        try {
            UserEntity result = userService.getById(userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取用户详情失败");
        }
    }

}
