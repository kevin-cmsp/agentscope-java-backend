package com.corporate.finance.ai.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.corporate.finance.ai.system.dao.MenuDao;
import com.corporate.finance.ai.system.dao.RoleMenuDao;
import com.corporate.finance.ai.system.dao.UserRoleDao;
import com.corporate.finance.ai.system.entity.MenuEntity;
import com.corporate.finance.ai.system.entity.RoleMenuEntity;
import com.corporate.finance.ai.system.entity.UserRoleEntity;
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

    @Autowired
    private UserRoleDao userRoleDao;

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
        // 递归逻辑删除子菜单
        deleteMenuRecursive(menuId);
    }

    @Override
    public List<MenuEntity> getMenuTree() {
        // 获取所有菜单（排除已删除）
        List<MenuEntity> allMenus = list(new QueryWrapper<MenuEntity>()
                .eq("deleted", 0)
                .orderByAsc("sort"));
        // 构建菜单树
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<MenuEntity> getUserMenus(List<Long> roleIds) {
        System.out.println("=== MenuServiceImpl.getUserMenus 开始执行 ===");
        System.out.println("角色 ID 列表：" + roleIds);
        
        // 获取角色对应的菜单 ID
        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            System.out.println("查询角色 " + roleId + " 的菜单：SELECT * FROM role_menu WHERE role_id = " + roleId);
            List<RoleMenuEntity> roleMenus = roleMenuDao.selectList(new QueryWrapper<RoleMenuEntity>().eq("role_id", roleId));
            System.out.println("角色 " + roleId + " 的菜单数量：" + roleMenus.size());
            
            List<Long> roleMenuIds = roleMenus.stream()
                    .map(RoleMenuEntity::getMenuId)
                    .toList();
            menuIds.addAll(roleMenuIds);
        }
        
        System.out.println("所有菜单 ID（去重前）: " + menuIds);
        
        // 去重
        menuIds = menuIds.stream().distinct().collect(Collectors.toList());
        System.out.println("去重后的菜单 ID: " + menuIds);
        
        if (menuIds.isEmpty()) {
            System.out.println("没有任何菜单权限，返回空列表");
            return new ArrayList<>();
        }
        
        // 获取菜单信息
        System.out.println("查询菜单详情：SELECT * FROM menu WHERE id IN " + menuIds);
        List<MenuEntity> menus = listByIds(menuIds);
        System.out.println("获取到菜单数量：" + (menus != null ? menus.size() : 0));
        
        if (menus != null) {
            for (MenuEntity menu : menus) {
                System.out.println("  - 菜单：" + menu.getName() + " (ID: " + menu.getId() + ", Type: " + menu.getType() + ")");
            }
        }
        
        // 构建菜单树
        System.out.println("构建菜单树...");
        List<MenuEntity> tree = buildMenuTree(menus, 0L);
        System.out.println("菜单树构建完成，根节点数量：" + tree.size());
        
        return tree;
    }

    @Override
    public List<MenuEntity> getUserMenusByUserId(Long userId) {
        System.out.println("=== MenuServiceImpl.getUserMenusByUserId 开始执行 ===");
        System.out.println("用户 ID: " + userId);
        
        // 获取用户的所有角色
        System.out.println("查询用户角色：SELECT * FROM user_role WHERE user_id = " + userId);
        List<UserRoleEntity> userRoles = userRoleDao.selectList(new QueryWrapper<UserRoleEntity>().eq("user_id", userId));
        System.out.println("用户角色数量：" + userRoles.size());
        
        List<Long> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());
        
        System.out.println("角色 ID 列表：" + roleIds);
        
        if (roleIds.isEmpty()) {
            System.out.println("用户没有任何角色，返回空菜单列表");
            return new ArrayList<>();
        }
        
        System.out.println("调用 getUserMenus 获取菜单，角色 ID: " + roleIds);
        List<MenuEntity> menus = getUserMenus(roleIds);
        System.out.println("获取到菜单数量：" + (menus != null ? menus.size() : 0));
        
        return menus;
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
     * 递归逻辑删除菜单
     */
    private void deleteMenuRecursive(Long menuId) {
        // 使用 removeById 触发 MyBatis-Plus 的逻辑删除
        removeById(menuId);
        
        // 查找子菜单并递归删除
        List<MenuEntity> childMenus = list(new QueryWrapper<MenuEntity>()
                .eq("parent_id", menuId));
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
                MenuEntity node = new MenuEntity();
                node.setId(child.getId());
                node.setName(child.getName());
                node.setPermission(child.getPermission());
                node.setType(child.getType());
                node.setSort(child.getSort());
                node.setParentId(child.getParentId());
                node.setPath(child.getPath());
                node.setComponent(child.getComponent());
                node.setIcon(child.getIcon());
                node.setVisible(child.getVisible());
                node.setKeepAlive(child.getKeepAlive());
                node.setStatus(child.getStatus());
                
                // 递归处理子菜单
                List<MenuEntity> childTree = new ArrayList<>();
                buildTree(menuMap, childTree, child.getId());
                node.setChildren(childTree);
                
                tree.add(node);
            }
        }
    }

}
