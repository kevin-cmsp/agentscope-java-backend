package com.corporate.finance.ai.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.corporate.finance.ai.system.dao.RoleDao;
import com.corporate.finance.ai.system.dao.RoleMenuDao;
import com.corporate.finance.ai.system.dao.UserRoleDao;
import com.corporate.finance.ai.system.entity.RoleEntity;
import com.corporate.finance.ai.system.entity.RoleMenuEntity;
import com.corporate.finance.ai.system.entity.UserRoleEntity;
import com.corporate.finance.ai.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleDao, RoleEntity> implements RoleService {

    @Autowired
    private RoleMenuDao roleMenuDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Override
    public void createRole(RoleEntity role) {
        save(role);
    }

    @Override
    public void updateRole(RoleEntity role) {
        updateById(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        removeById(roleId);
    }

    @Override
    public void updateRoleStatus(Long roleId, Integer status) {
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        role.setStatus(status);
        updateById(role);
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有菜单关联
        roleMenuDao.delete(new QueryWrapper<RoleMenuEntity>().eq("role_id", roleId));
        // 添加新菜单关联
        for (Long menuId : menuIds) {
            RoleMenuEntity roleMenu = new RoleMenuEntity();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setTenantId(1L); // 临时设置租户ID
            roleMenuDao.insert(roleMenu);
        }
    }

    @Override
    public List<Long> getRoleMenus(Long roleId) {
        return roleMenuDao.selectList(new QueryWrapper<RoleMenuEntity>().eq("role_id", roleId))
                .stream().map(RoleMenuEntity::getMenuId).toList();
    }

    @Override
    public List<Long> getRoleUsers(Long roleId) {
        return userRoleDao.selectList(new QueryWrapper<UserRoleEntity>().eq("role_id", roleId))
                .stream().map(UserRoleEntity::getUserId).toList();
    }

    @Override
    public List<RoleEntity> queryRoles(String roleName, Integer status) {
        QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            queryWrapper.like("name", roleName);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        return list(queryWrapper);
    }

}
