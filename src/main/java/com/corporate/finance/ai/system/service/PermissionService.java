package com.corporate.finance.ai.system.service;

import java.util.Set;

public interface PermissionService {

    /**
     * 获取用户的权限标识集合
     */
    Set<String> getUserPermissions(String username);

    /**
     * 检查用户是否拥有指定权限
     */
    boolean hasPermission(String username, String permission);

}
