package com.corporate.finance.ai.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("system_users_roles")
public class UserRoleEntity {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色 ID
     */
    private Long roleId;


}
