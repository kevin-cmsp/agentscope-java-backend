package com.corporate.finance.ai.system.controller;

import com.corporate.finance.ai.system.common.Result;
import com.corporate.finance.ai.system.entity.MenuEntity;
import com.corporate.finance.ai.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取菜单树形结构
     */
    @GetMapping("/tree")
    public Result<List<MenuEntity>> getMenuTree() {
        try {
            List<MenuEntity> result = menuService.getMenuTree();
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取菜单失败");
        }
    }

    /**
     * 创建菜单
     */
    @PostMapping
    public Result<Void> createMenu(@RequestBody MenuEntity menu) {
        try {
            menuService.createMenu(menu);
            return Result.success("创建成功", null);
        } catch (Exception e) {
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping
    public Result<Void> updateMenu(@RequestBody MenuEntity menu) {
        try {
            menuService.updateMenu(menu);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{menuId}")
    public Result<Void> deleteMenu(@PathVariable Long menuId) {
        try {
            menuService.deleteMenu(menuId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户角色获取菜单权限
     */
    @PostMapping("/user-menus")
    public Result<List<MenuEntity>> getUserMenus(@RequestBody List<Long> roleIds) {
        try {
            List<MenuEntity> result = menuService.getUserMenus(roleIds);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取用户菜单失败");
        }
    }

    /**
     * 根据用户 ID 获取菜单权限
     */
    @GetMapping("/user/{userId}")
    public Result<List<MenuEntity>> getUserMenusByUserId(@PathVariable Long userId) {
        try {
            List<MenuEntity> result = menuService.getUserMenusByUserId(userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取用户菜单失败");
        }
    }

}
