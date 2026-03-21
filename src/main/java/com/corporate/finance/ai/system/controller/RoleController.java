package com.corporate.finance.ai.system.controller;

import com.corporate.finance.ai.system.common.Result;
import com.corporate.finance.ai.system.entity.RoleEntity;
import com.corporate.finance.ai.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    public Result<Void> createRole(@RequestBody RoleEntity role) {
        try {
            roleService.createRole(role);
            return Result.success("创建成功", null);
        } catch (Exception e) {
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping
    public Result<Void> updateRole(@RequestBody RoleEntity role) {
        try {
            roleService.updateRole(role);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        try {
            roleService.deleteRole(roleId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用角色
     */
    @PutMapping("/status")
    public Result<Void> updateRoleStatus(@RequestParam Long roleId, @RequestParam Integer status) {
        try {
            roleService.updateRoleStatus(roleId, status);
            return Result.success("操作成功", null);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 分配菜单权限给角色
     */
    @PutMapping("/assign-menus")
    public Result<Void> assignMenus(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        try {
            roleService.assignMenus(roleId, menuIds);
            return Result.success("分配成功", null);
        } catch (Exception e) {
            return Result.error("分配失败：" + e.getMessage());
        }
    }

    /**
     * 获取角色的菜单权限列表
     */
    @GetMapping("/menus/{roleId}")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        try {
            List<Long> result = roleService.getRoleMenus(roleId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取角色菜单失败");
        }
    }

    /**
     * 获取角色的用户列表
     */
    @GetMapping("/users/{roleId}")
    public Result<List<Long>> getRoleUsers(@PathVariable Long roleId) {
        try {
            List<Long> result = roleService.getRoleUsers(roleId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取角色用户失败");
        }
    }

    /**
     * 查询角色列表
     */
    @GetMapping
    public Result<List<RoleEntity>> queryRoles(@RequestParam(required = false) String roleName, 
                                      @RequestParam(required = false) Integer status) {
        try {
            List<RoleEntity> result = roleService.queryRoles(roleName, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("查询角色失败");
        }
    }

    /**
     * 根据 ID 获取角色信息
     */
    @GetMapping("/{roleId}")
    public Result<RoleEntity> getRoleById(@PathVariable Long roleId) {
        try {
            RoleEntity result = roleService.getById(roleId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取角色详情失败");
        }
    }

}
