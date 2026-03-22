package com.corporate.finance.ai.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_menu")
public class MenuEntity extends BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
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
     * 类型（1-目录，2-菜单，3-按钮）
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 父菜单 ID
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
     * 是否显示（0-隐藏，1-显示）
     */
    private Integer visible;

    /**
     * 是否缓存（0-否，1-是）
     */
    private Integer keepAlive;

    /**
     * 状态（0-正常，1-禁用）
     */
    private Integer status;

    /**
     * 子菜单
     */
    @TableField(exist = false)
    private List<MenuEntity> children;

    /**
     * 逻辑删除标记
     */
    private Boolean deleted;

}
