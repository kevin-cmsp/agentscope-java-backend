package com.corporate.finance.ai.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.corporate.finance.ai.system.dao.MenuDao;
import com.corporate.finance.ai.system.dao.RoleMenuDao;
import com.corporate.finance.ai.system.entity.MenuEntity;
import com.corporate.finance.ai.system.entity.RoleMenuEntity;
import com.corporate.finance.ai.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuDao, MenuEntity> implements MenuService {

    @Autowired
    private RoleMenuDao roleMenuDao;

    @Override
    public void createMenu(MenuEntity menu) {
        save(menu);
    }

    @Override
    public void updateMenu(MenuEntity menu) {
        updateById(menu);
    }

    @Override
    public void deleteMenu(Long menuId) {
        // 递归删除子菜单
        deleteMenuRecursive(menuId);
    }

    @Override
    public List<MenuEntity> getMenuTree() {
        // 获取所有菜单
        List<MenuEntity> allMenus = list(new QueryWrapper<MenuEntity>().orderByAsc("sort"));
        // 构建菜单树
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<MenuEntity> getUserMenus(List<Long> roleIds) {
        // 获取角色对应的菜单ID
        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<Long> roleMenuIds = roleMenuDao.selectList(new QueryWrapper<RoleMenuEntity>().eq("role_id", roleId))
                    .stream().map(RoleMenuEntity::getMenuId).toList();
            menuIds.addAll(roleMenuIds);
        }
        // 去重
        menuIds = menuIds.stream().distinct().collect(Collectors.toList());
        // 获取菜单信息
        List<MenuEntity> menus = listByIds(menuIds);
        // 构建菜单树
        return buildMenuTree(menus, 0L);
    }

    @Override
    @Transactional
    public void importMenus(List<MenuEntity> menus) {
        for (MenuEntity menu : menus) {
            save(menu);
        }
    }

    @Override
    public List<MenuEntity> exportMenus() {
        return list();
    }

    /**
     * 递归删除菜单
     */
    private void deleteMenuRecursive(Long menuId) {
        // 删除当前菜单
        removeById(menuId);
        // 查找子菜单
        List<MenuEntity> childMenus = list(new QueryWrapper<MenuEntity>().eq("parent_id", menuId));
        for (MenuEntity childMenu : childMenus) {
            deleteMenuRecursive(childMenu.getId());
        }
    }

    /**
     * 构建菜单树
     */
    private List<MenuEntity> buildMenuTree(List<MenuEntity> menus, Long parentId) {
        List<MenuEntity> tree = new ArrayList<>();
        // 按父ID分组
        Map<Long, List<MenuEntity>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(MenuEntity::getParentId));
        // 递归构建树
        buildTree(menuMap, tree, parentId);
        return tree;
    }

    /**
     * 递归构建树
     */
    private void buildTree(Map<Long, List<MenuEntity>> menuMap, List<MenuEntity> tree, Long parentId) {
        List<MenuEntity> children = menuMap.get(parentId);
        if (children != null) {
            for (MenuEntity child : children) {
                tree.add(child);
                // 递归处理子菜单
                List<MenuEntity> childTree = new ArrayList<>();
                buildTree(menuMap, childTree, child.getId());
                // TODO: 需要在MenuEntity中添加children字段
            }
        }
    }

}
