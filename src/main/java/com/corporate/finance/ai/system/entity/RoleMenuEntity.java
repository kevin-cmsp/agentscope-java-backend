package com.corporate.finance.ai.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("system_roles_menus")
public class RoleMenuEntity {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 租户ID
     */
    private Long tenantId;

}
