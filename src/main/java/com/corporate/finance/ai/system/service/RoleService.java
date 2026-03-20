package com.corporate.finance.ai.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.corporate.finance.ai.system.entity.RoleEntity;
import java.util.List;

public interface RoleService extends IService<RoleEntity> {

    /**
     * 创建角色
     */
    void createRole(RoleEntity role);

    /**
     * 更新角色
     */
    void updateRole(RoleEntity role);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 启用/禁用角色
     */
    void updateRoleStatus(Long roleId, Integer status);

    /**
     * 分配菜单权限给角色
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色的菜单权限列表
     */
    List<Long> getRoleMenus(Long roleId);

    /**
     * 获取角色的用户列表
     */
    List<Long> getRoleUsers(Long roleId);

    /**
     * 根据条件查询角色
     */
    List<RoleEntity> queryRoles(String roleName, Integer status);

}
