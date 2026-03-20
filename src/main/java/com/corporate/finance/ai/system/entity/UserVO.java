package com.corporate.finance.ai.system.entity;

import lombok.Data;

/**
 * 用户视图对象，用于向前端返回用户信息（不包含敏感字段）
 */
@Data
public class UserVO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 所属部门 ID
     */
    private Long deptId;

    /**
     * 岗位 ID 列表
     */
    private String postIds;

    /**
     * 状态（0-正常，1-禁用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新者
     */
    private String updater;

    /**
     * 更新时间
     */
    private String updateTime;
}
