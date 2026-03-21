package com.corporate.finance.ai.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("system_roles_menus")
public class RoleMenuEntity {

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 菜单 ID
     */
    private Long menuId;


}
