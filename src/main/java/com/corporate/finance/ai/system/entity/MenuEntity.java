package com.corporate.finance.ai.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_menu")
public class MenuEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 类型（1-目录, 2-菜单, 3-按钮）
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 是否显示（0-隐藏, 1-显示）
     */
    private Integer visible;

    /**
     * 是否缓存（0-否, 1-是）
     */
    private Integer keepAlive;

    /**
     * 状态（0-正常, 1-禁用）
     */
    private Integer status;

}
