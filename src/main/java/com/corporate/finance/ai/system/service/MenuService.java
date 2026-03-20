package com.corporate.finance.ai.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.corporate.finance.ai.system.entity.MenuEntity;
import java.util.List;

public interface MenuService extends IService<MenuEntity> {

    /**
     * 创建菜单
     */
    void createMenu(MenuEntity menu);

    /**
     * 更新菜单
     */
    void updateMenu(MenuEntity menu);

    /**
     * 删除菜单
     */
    void deleteMenu(Long menuId);

    /**
     * 获取菜单树形结构
     */
    List<MenuEntity> getMenuTree();

    /**
     * 根据用户角色获取菜单权限
     */
    List<MenuEntity> getUserMenus(List<Long> roleIds);

    /**
     * 批量导入菜单
     */
    void importMenus(List<MenuEntity> menus);

    /**
     * 导出菜单
     */
    List<MenuEntity> exportMenus();

}
