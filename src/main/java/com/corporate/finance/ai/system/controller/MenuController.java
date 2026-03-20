package com.corporate.finance.ai.system.controller;

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
    public void createMenu(@RequestBody MenuEntity menu) {
        menuService.createMenu(menu);
    }

    /**
     * 更新菜单
     */
    @PutMapping
    public void updateMenu(@RequestBody MenuEntity menu) {
        menuService.updateMenu(menu);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{menuId}")
    public void deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
    }

    /**
     * 获取菜单树形结构
     */
    @GetMapping("/tree")
    public List<MenuEntity> getMenuTree() {
        return menuService.getMenuTree();
    }

    /**
     * 根据用户角色获取菜单权限
     */
    @PostMapping("/user-menus")
    public List<MenuEntity> getUserMenus(@RequestBody List<Long> roleIds) {
        return menuService.getUserMenus(roleIds);
    }

    /**
     * 批量导入菜单
     */
    @PostMapping("/import")
    public void importMenus(@RequestBody List<MenuEntity> menus) {
        menuService.importMenus(menus);
    }

    /**
     * 导出菜单
     */
    @GetMapping("/export")
    public List<MenuEntity> exportMenus() {
        return menuService.exportMenus();
    }

    /**
     * 根据ID获取菜单信息
     */
    @GetMapping("/{menuId}")
    public MenuEntity getMenuById(@PathVariable Long menuId) {
        return menuService.getById(menuId);
    }

}
