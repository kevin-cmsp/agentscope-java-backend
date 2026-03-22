-- 设置字符集与关闭外键检查
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for system_user
-- ----------------------------
CREATE TABLE `system_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(30) NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `nickname` varchar(30) DEFAULT NULL COMMENT '昵称',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号（唯一）',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门ID',
  `post_ids` varchar(200) DEFAULT NULL COMMENT '岗位ID列表（JSON数组字符串）',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0-正常，1-禁用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- Table structure for system_role
-- ----------------------------
CREATE TABLE `system_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(30) NOT NULL COMMENT '角色名称',
  `code` varchar(30) NOT NULL COMMENT '角色编码（如 ADMIN）',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `data_scope` varchar(20) NOT NULL DEFAULT 'ALL' COMMENT '数据权限类型（ALL/DEPT_CUSTOM/DEPT_ONLY/DEPT_AND_CHILD/SELF）',
  `data_scope_dept_ids` varchar(500) DEFAULT NULL COMMENT '自定义部门ID列表（JSON数组）',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0-正常，1-禁用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- Table structure for system_menu
-- ----------------------------
CREATE TABLE `system_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '菜单名称',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限标识（按钮必填）',
  `type` tinyint(4) NOT NULL COMMENT '类型（1-目录, 2-菜单, 3-按钮）',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父菜单ID',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `visible` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否显示（0-隐藏, 1-显示）',
  `keep_alive` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否缓存（0-否, 1-是）',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0-正常, 1-禁用）',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------
-- Table structure for system_users_roles
-- ----------------------------
CREATE TABLE `system_users_roles` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- ----------------------------
-- Table structure for system_roles_menus
-- ----------------------------
CREATE TABLE `system_roles_menus` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;




-- 设置字符集
SET NAMES utf8mb4;

-- 1. 插入超级管理员用户（admin）
INSERT INTO `system_user` (
    `id`, `username`, `password`, `nickname`, `mobile`, `email`,
    `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
    1,
    'admin',
    '$2a$10$Vc7r9KzRZyQeUfJxXWvE.eiO8uF3DqN1jBpGmS5dMlT0nYwZ1JbAe', -- BCrypt(123456)
    '超级管理员',
    '13800138000',
    'admin@example.com',
    0,
    '系统内置超级管理员',
    'system',
    NOW(),
    'system',
    NOW(),
    b'0'
);

-- 2. 插入超级管理员角色
INSERT INTO `system_role` (
    `id`, `name`, `code`, `sort`, `data_scope`, `status`,
    `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
    1,
    '超级管理员',
    'ADMIN',
    1,
    'ALL',
    0,
    '拥有所有权限',
    'system',
    NOW(),
    'system',
    NOW(),
    b'0'
);

-- 3. 绑定 admin 用户与 ADMIN 角色
INSERT INTO `system_users_roles` (`user_id`, `role_id`) VALUES (1, 1);

-- 4. 为 ADMIN 角色分配所有菜单权限（需确保 system_menu 表已有数据）
-- 注意：此语句应在菜单数据初始化之后执行
INSERT INTO `system_roles_menus` (`role_id`, `menu_id`)
SELECT 1, id FROM `system_menu` WHERE `deleted` = b'0';


-- ----------------------------
-- Table structure for ai_conversation (AI对话会话表)
-- ----------------------------
CREATE TABLE `ai_conversation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) DEFAULT NULL COMMENT '会话标题（用户首次提问前20字符）',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- ----------------------------
-- Table structure for ai_message (AI对话消息表)
-- ----------------------------
CREATE TABLE `ai_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `role` varchar(20) NOT NULL COMMENT '角色（user-用户，assistant-助手）',
  `content` text NOT NULL COMMENT '消息内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

