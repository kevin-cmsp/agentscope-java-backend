package com.corporate.finance.ai.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corporate.finance.ai.system.dao.MenuDao;
import com.corporate.finance.ai.system.dao.RoleMenuDao;
import com.corporate.finance.ai.system.dao.UserDao;
import com.corporate.finance.ai.system.dao.UserRoleDao;
import com.corporate.finance.ai.system.entity.MenuEntity;
import com.corporate.finance.ai.system.entity.RoleMenuEntity;
import com.corporate.finance.ai.system.entity.UserEntity;
import com.corporate.finance.ai.system.entity.UserRoleEntity;
import com.corporate.finance.ai.system.service.PermissionService;
import com.corporate.finance.ai.system.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RoleMenuDao roleMenuDao;

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Set<String> getUserPermissions(String username) {
        String cacheKey = "permissions:" + username;
        @SuppressWarnings("unchecked")
        Set<String> permissions = (Set<String>) redisUtils.get(cacheKey);
        if (permissions != null) {
            return permissions;
        }

        UserEntity user = userDao.selectOne(new QueryWrapper<UserEntity>().eq("username", username));
        if (user == null) {
            return new HashSet<>();
        }

        List<Long> roleIds = userRoleDao.selectList(new QueryWrapper<UserRoleEntity>().eq("user_id", user.getId()))
                .stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());

        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<Long> roleMenuIds = roleMenuDao.selectList(new QueryWrapper<RoleMenuEntity>().eq("role_id", roleId))
                    .stream().map(RoleMenuEntity::getMenuId).collect(Collectors.toList());
            menuIds.addAll(roleMenuIds);
        }

        permissions = new HashSet<>();
        if (!menuIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<MenuEntity> menus = (List<MenuEntity>) menuDao.selectBatchIds(menuIds);
            for (MenuEntity menu : menus) {
                if (menu.getPermission() != null && !menu.getPermission().isEmpty()) {
                    permissions.add(menu.getPermission());
                }
            }
        }

        redisUtils.set(cacheKey, permissions, 1, java.util.concurrent.TimeUnit.HOURS);

        return permissions;
    }

    @Override
    public boolean hasPermission(String username, String permission) {
        Set<String> permissions = getUserPermissions(username);
        return permissions.contains(permission);
    }

}
