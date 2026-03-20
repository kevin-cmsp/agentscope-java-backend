package com.corporate.finance.ai.system.controller;

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
    public void createRole(@RequestBody RoleEntity role) {
        roleService.createRole(role);
    }

    /**
     * 更新角色
     */
    @PutMapping
    public void updateRole(@RequestBody RoleEntity role) {
        roleService.updateRole(role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
    }

    /**
     * 启用/禁用角色
     */
    @PutMapping("/status")
    public void updateRoleStatus(@RequestParam Long roleId, @RequestParam Integer status) {
        roleService.updateRoleStatus(roleId, status);
    }

    /**
     * 分配菜单权限给角色
     */
    @PutMapping("/assign-menus")
    public void assignMenus(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
    }

    /**
     * 获取角色的菜单权限列表
     */
    @GetMapping("/menus/{roleId}")
    public List<Long> getRoleMenus(@PathVariable Long roleId) {
        return roleService.getRoleMenus(roleId);
    }

    /**
     * 获取角色的用户列表
     */
    @GetMapping("/users/{roleId}")
    public List<Long> getRoleUsers(@PathVariable Long roleId) {
        return roleService.getRoleUsers(roleId);
    }

    /**
     * 查询角色列表
     */
    @GetMapping
    public List<RoleEntity> queryRoles(@RequestParam(required = false) String roleName, 
                                      @RequestParam(required = false) Integer status) {
        return roleService.queryRoles(roleName, status);
    }

    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/{roleId}")
    public RoleEntity getRoleById(@PathVariable Long roleId) {
        return roleService.getById(roleId);
    }

}
