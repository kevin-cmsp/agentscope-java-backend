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
        System.out.println("=== MenuController.getUserMenusByUserId 开始执行 ===");
        System.out.println("用户 ID: " + userId);
        try {
            System.out.println("调用 menuService.getUserMenusByUserId(" + userId + ")");
            List<MenuEntity> result = menuService.getUserMenusByUserId(userId);
            System.out.println("返回菜单数量：" + (result != null ? result.size() : 0));
            if (result != null) {
                for (MenuEntity menu : result) {
                    System.out.println("  - 菜单：" + menu.getName() + " (ID: " + menu.getId() + ")");
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            System.out.println("=== 获取用户菜单失败 ===");
            System.out.println("错误信息：" + e.getMessage());
            e.printStackTrace();
            return Result.serverError("获取用户菜单失败：" + e.getMessage());
        }
    }

    /**
     * 批量导入菜单
     */
    @PostMapping("/import")
    public Result<Void> importMenus(@RequestBody List<MenuEntity> menus) {
        try {
            menuService.importMenus(menus);
            return Result.success("导入成功", null);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 导出菜单
     */
    @GetMapping("/export")
    public Result<List<MenuEntity>> exportMenus() {
        try {
            List<MenuEntity> result = menuService.exportMenus();
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("导出菜单失败");
        }
    }

    /**
     * 根据 ID 获取菜单信息
     */
    @GetMapping("/{menuId}")
    public Result<MenuEntity> getMenuById(@PathVariable Long menuId) {
        try {
            MenuEntity result = menuService.getById(menuId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.serverError("获取菜单详情失败");
        }
    }

}
