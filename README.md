# AgentScope-Java 后端项目

## 项目概述

基于 **AgentScope-Java** 构建的 AI Agent 开发平台后端服务。项目采用 Spring Boot 3.x + JDK 21 构建，集成通义千问大模型，提供智能聊天、多智能体协作、天气查询、技能管理等 AI 能力，同时包含完整的 RBAC 权限管理系统。

### 核心功能

- **智能聊天**：基于 DashScope（通义千问）的意图识别与多场景对话
- **多智能体协作**：支持 ReAct 模式和层级式协作两种 Agent 编排方式
- **天气查询**：集成高德天气 API，支持中文城市名称查询
- **技能管理**：支持动态注册、执行和卸载 Agent 技能
- **系统管理**：完整的用户、角色、菜单 RBAC 权限管理体系
- **安全认证**：JWT + Redis 的无状态认证，支持验证码、账户锁定等安全策略

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.4 | 后端框架 |
| JDK | 21 | Java 运行环境 |
| Maven | 3.9+ | 构建工具 |
| AgentScope Java | 1.0.9 | Agent 开发框架 |
| DashScope SDK | 2.0.0 | 通义千问大模型 API |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 5.7+ | 关系型数据库 |
| Redis | - | 缓存与会话存储 |
| Spring Security | - | 安全认证框架 |
| OkHttp | 4.12.0 | HTTP 客户端 |
| Hutool | 5.8.25 | 工具函数库 |

## 项目结构

```
agentscope-java-backend/
├── src/main/java/com/corporate/finance/ai/
│   ├── Application.java                    # 应用入口
│   ├── agent/                              # AI Agent 实现
│   │   ├── PartyPlanningAgent.java         #   活动策划 Agent（ReAct 模式）
│   │   ├── PartyPlanningAgentLevel.java    #   活动策划 Agent（层级式协作）
│   │   ├── BudgetAgent.java                #   预算管理 Agent
│   │   └── ActivityAgent.java              #   活动安排 Agent
│   ├── tool/                               # Agent 工具
│   │   ├── CalculatorTool.java             #   计算器工具
│   │   ├── WeatherTool.java                #   天气查询工具
│   │   ├── BudgetTool.java                 #   预算计算工具
│   │   └── ActivityTool.java               #   活动安排工具
│   ├── config/                             # 配置类
│   │   ├── AgentScopeConfig.java           #   AgentScope 配置
│   │   ├── ModelServiceConfig.java         #   模型服务配置
│   │   ├── RedisConfig.java                #   Redis 配置
│   │   └── WebConfig.java                  #   Web 配置
│   ├── controller/                         # REST 控制器
│   │   └── AgentController.java            #   Agent 管理与聊天接口
│   ├── service/                            # 业务服务
│   │   ├── AgentManagerService.java        #   Agent 管理服务
│   │   ├── SkillService.java               #   技能管理服务
│   │   ├── MemoryService.java              #   记忆管理服务
│   │   ├── WeatherService.java             #   天气查询服务
│   │   ├── ModelService.java               #   模型服务接口
│   │   ├── ModelServiceFactory.java        #   模型服务工厂
│   │   └── DashScopeModelService.java      #   DashScope 实现
│   └── system/                             # 系统管理模块
│       ├── common/Result.java              #   统一响应封装
│       ├── entity/                         #   数据实体
│       │   ├── BaseEntity.java             #     基础实体（审计字段）
│       │   ├── UserEntity.java             #     用户实体
│       │   ├── RoleEntity.java             #     角色实体
│       │   ├── MenuEntity.java             #     菜单实体
│       │   ├── UserRoleEntity.java         #     用户角色关联
│       │   ├── RoleMenuEntity.java         #     角色菜单关联
│       │   └── UserVO.java                 #     用户 DTO
│       ├── dao/                            #   数据访问层
│       ├── service/                        #   系统业务服务
│       │   └── impl/                       #     服务实现
│       ├── controller/                     #   系统控制器
│       │   ├── AuthController.java         #     认证接口
│       │   ├── UserController.java         #     用户管理接口
│       │   ├── RoleController.java         #     角色管理接口
│       │   └── MenuController.java         #     菜单管理接口
│       ├── security/                       #   安全认证
│       │   ├── SecurityConfig.java         #     Security 配置
│       │   ├── JwtFilter.java              #     JWT 过滤器
│       │   └── PermissionService.java      #     权限检查服务
│       └── utils/                          #   工具类
│           ├── JwtUtils.java               #     JWT 工具
│           ├── RedisUtils.java             #     Redis 工具
│           └── AesDecryptor.java           #     AES 解密工具
├── src/main/resources/
│   └── application.yml                     # 应用配置
├── doc/
│   └── database-mysql5.7.sql               # 数据库初始化脚本
└── pom.xml                                 # Maven 配置
```

## 快速开始

### 1. 环境准备

- JDK 21
- Maven 3.9+
- MySQL 5.7+
- Redis

### 2. 初始化数据库

```bash
mysql -h <host> -u root -p agentscope < doc/database-mysql5.7.sql
```

数据库包含以下表：

| 表名 | 说明 |
|------|------|
| `system_user` | 用户表 |
| `system_role` | 角色表 |
| `system_menu` | 菜单表 |
| `system_users_roles` | 用户角色关联表 |
| `system_roles_menus` | 角色菜单关联表 |

初始化数据包含超级管理员用户 `admin`（密码：`123456`）。

### 3. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
# 数据库连接
spring:
  datasource:
    url: jdbc:mysql://<host>:<port>/agentscope?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: <your_password>

# Redis 连接
  data:
    redis:
      host: <redis_host>
      port: <redis_port>
      password: <redis_password>

# DashScope 大模型
model:
  services:
    dashscope:
      api-key: <your_dashscope_api_key>
      model: qwen-turbo

# 高德天气
weather:
  api-key: <your_amap_api_key>
```

### 4. 构建与运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package
java -jar target/agentscope-java-backend-1.0-SNAPSHOT.jar
```

服务启动后访问 `http://localhost:8080`。

## 系统架构

### 整体架构

```
客户端 (Web/Mobile)
       │ HTTP/REST
       ▼
┌──────────────────────────────────────────┐
│            API 层 (Controllers)          │
│  AgentController / AuthController / ...  │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│           业务逻辑层 (Services)           │
│  AgentManager / Skill / Memory / Weather │
│  Auth / User / Role / Menu / Permission  │
└─────┬─────────────────┬─────────────────┘
      │                 │
      ▼                 ▼
┌───────────┐   ┌──────────────┐
│ AgentScope│   │ 数据访问层    │
│ Agents &  │   │ (MyBatis-    │
│ Tools     │   │  Plus DAO)   │
└─────┬─────┘   └──────┬───────┘
      │                │
      ▼                ▼
┌───────────┐   ┌──────────────┐
│ DashScope │   │    MySQL     │
│ 高德API   │   │    Redis     │
└───────────┘   └──────────────┘
```

### 多智能体协作

**ReAct 模式**（PartyPlanningAgent）：Agent 自主进行"思考-行动-观察"循环，自动调用工具完成任务。

```
用户输入 → Thought → Action(调用工具) → Observation → Thought → ... → Final Answer
```

**层级式协作模式**（PartyPlanningAgentLevel）：Manager Agent 解析需求后，协调 BudgetAgent 和 ActivityAgent 分别完成子任务，最终整合结果。

### 安全认证

- JWT Token 认证，有效期 24 小时
- AES 加密传输密码，BCrypt 哈希存储
- Redis 存储 Token 和验证码
- 登录失败 5 次后锁定账户 30 分钟
- 验证码 15 分钟有效，一次性使用

### RBAC 权限模型

```
用户(User) ──N:M── 角色(Role) ──N:M── 菜单(Menu)
```

菜单类型：目录（type=1）、菜单（type=2）、按钮（type=3）

数据权限支持：ALL / DEPT_CUSTOM / DEPT_ONLY / DEPT_AND_CHILD / SELF

## API 接口文档

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

---

### 1. 智能聊天接口

#### 1.1 统一聊天

**POST** `/api/chat`

通过意图识别自动分发到对应处理逻辑。

**请求体**：
```json
{
  "message": "用户消息"
}
```

**响应**：
```json
{
  "status": "success",
  "content": "响应内容"
}
```

**支持的意图类型**：

| 意图 | 触发示例 | 说明 |
|------|---------|------|
| `weather` | "北京的天气怎么样？" | 天气查询 |
| `calculator` | "10加5等于多少？" | 数学计算 |
| `data` | "上个月的销量是多少？" | 企业数据查询 |
| `knowledge` | "公司的规章制度是什么？" | 知识库查询 |
| `party` | "帮我策划一场生日派对" | 活动策划 |
| `general` | "你好" | 通用对话 |

意图识别优先使用 DashScope 大模型语义识别，失败时回退到关键词匹配。

#### 1.2 天气查询

**POST** `/api/weather`

```json
// 请求
{ "city": "北京" }

// 响应（文本格式）
城市: 北京市
发布时间: 2026-03-22 08:00:00

=== 今天天气 ===
日期: 2026-03-22 (周日)
白天: 晴, 温度 20°C, 南风 2级
夜间: 多云, 温度 10°C, 北风 3级
...
```

缓存策略：30 分钟内不重复查询同一城市。调用失败最多重试 3 次。

#### 1.3 活动策划（层级式协作）

**POST** `/api/api/party/level`

```json
// 请求
{ "message": "帮我策划一场生日派对，地点在北京，预算5000元，10人参加" }

// 响应
{
  "status": "success",
  "content": "完整的活动策划方案..."
}
```

#### 1.4 技能管理

| 操作 | 方法 | 路径 |
|------|------|------|
| 注册技能 | POST | `/api/skills/register` |
| 技能列表 | GET | `/api/skills` |
| 卸载技能 | DELETE | `/api/skills/{skillName}` |
| 执行计算 | POST | `/api/skills/calculator` |

**计算器请求**：
```json
{
  "operation": "add",   // add / subtract / multiply / divide / percentage
  "a": 10,
  "b": 5
}
```

#### 1.5 记忆管理

| 操作 | 方法 | 路径 |
|------|------|------|
| 获取对话历史 | GET | `/api/memory` |
| 清空记忆 | POST | `/api/memory/clear` |

#### 1.6 Agent 管理

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建 Agent | POST | `/api/agents` | 已禁用 |
| 获取 Agent | GET | `/api/agents/{agentId}` | |
| 执行 Agent | POST | `/api/agents/{agentId}/execute` | |
| Agent 列表 | GET | `/api/agents` | |
| 删除 Agent | DELETE | `/api/agents/{agentId}` | |

---

### 2. 认证接口

#### 2.1 登录

**POST** `/api/auth/login`

```json
// 请求
{
  "username": "admin",
  "password": "AES加密后的密码",
  "captcha": "ABCD",
  "captchaKey": "captcha:xxx"
}

// 响应
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "status": 0
    }
  }
}
```

认证流程：获取验证码 -> 前端 AES 加密密码 -> 发送登录请求 -> 服务端验证 -> 返回 JWT Token。

后续请求需在 Header 中携带：`Authorization: Bearer <token>`

#### 2.2 登出

**POST** `/api/auth/logout`

#### 2.3 获取验证码

**GET** `/api/auth/captcha`

```json
{
  "captchaKey": "captcha:123456",
  "captcha": "ABCD"
}
```

#### 2.4 获取当前用户信息

**GET** `/api/auth/userinfo`

```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员",
  "mobile": "13800138000",
  "email": "admin@example.com",
  "deptId": 1,
  "status": 0
}
```

---

### 3. 用户管理接口

所有接口前缀：`/api/system/user`

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户列表 | GET | `/` | 支持 username/mobile/status 查询 |
| 用户详情 | GET | `/{userId}` | |
| 创建用户 | POST | `/` | |
| 更新用户 | PUT | `/` | |
| 删除用户 | DELETE | `/{userId}` | |
| 更新状态 | PUT | `/status` | 参数：userId, status(0正常/1禁用) |
| 重置密码 | PUT | `/reset-password/{userId}` | 重置为默认密码 123456 |
| 分配角色 | PUT | `/assign-roles` | 参数：userId, body: [roleId...] |
| 用户角色 | GET | `/roles/{userId}` | 返回角色 ID 列表 |

**创建用户请求**：
```json
{
  "username": "user01",
  "password": "123456",
  "nickname": "用户01",
  "mobile": "13900139000",
  "email": "user@example.com",
  "deptId": 1,
  "status": 0
}
```

---

### 4. 角色管理接口

所有接口前缀：`/api/system/role`

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 角色列表 | GET | `/` | 支持 roleName/status 查询 |
| 角色详情 | GET | `/{roleId}` | |
| 创建角色 | POST | `/` | |
| 更新角色 | PUT | `/` | |
| 删除角色 | DELETE | `/{roleId}` | |
| 更新状态 | PUT | `/status` | 参数：roleId, status |
| 分配菜单 | PUT | `/assign-menus` | 参数：roleId, body: [menuId...] |
| 角色菜单 | GET | `/menus/{roleId}` | 返回菜单 ID 列表 |
| 角色用户 | GET | `/users/{roleId}` | 返回用户 ID 列表 |

**创建角色请求**：
```json
{
  "name": "管理员",
  "code": "ADMIN",
  "sort": 1,
  "dataScope": "ALL",
  "status": 0,
  "remark": "系统管理员"
}
```

---

### 5. 菜单管理接口

所有接口前缀：`/api/system/menu`

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 菜单树 | GET | `/tree` | 返回树形菜单结构 |
| 菜单详情 | GET | `/{menuId}` | |
| 创建菜单 | POST | `/` | |
| 更新菜单 | PUT | `/` | |
| 删除菜单 | DELETE | `/{menuId}` | |
| 用户菜单 | POST | `/user-menus` | body: [roleId...] |
| 批量导入 | POST | `/import` | body: [MenuEntity...] |
| 导出菜单 | GET | `/export` | |

**创建菜单请求**：
```json
{
  "name": "用户管理",
  "permission": "sys:user:manage",
  "type": 2,
  "sort": 1,
  "parentId": 1,
  "path": "/system/user",
  "component": "system/User",
  "icon": "User",
  "visible": 1,
  "keepAlive": 0,
  "status": 0
}
```

菜单类型说明：
- `type=1`：目录，用于分组
- `type=2`：菜单，关联前端页面组件
- `type=3`：按钮，用于按钮级权限控制

## 数据模型

### 用户表 (system_user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键（雪花算法） |
| username | varchar(30) | 用户名（唯一） |
| password | varchar(100) | BCrypt 加密密码 |
| nickname | varchar(30) | 昵称 |
| mobile | varchar(20) | 手机号（唯一） |
| email | varchar(50) | 邮箱 |
| dept_id | bigint | 所属部门 ID |
| post_ids | varchar(200) | 岗位 ID 列表（JSON） |
| status | tinyint | 状态（0-正常，1-禁用） |
| remark | varchar(500) | 备注 |
| creator / create_time | - | 创建人 / 创建时间 |
| updater / update_time | - | 更新人 / 更新时间 |
| deleted | bit | 逻辑删除（0-未删除，1-已删除） |

### 角色表 (system_role)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(30) | 角色名称 |
| code | varchar(30) | 角色编码（唯一，如 ADMIN） |
| sort | int | 排序 |
| data_scope | varchar(20) | 数据权限（ALL/DEPT_CUSTOM/DEPT_ONLY/DEPT_AND_CHILD/SELF） |
| data_scope_dept_ids | varchar(500) | 自定义部门 ID 列表 |
| status | tinyint | 状态 |

### 菜单表 (system_menu)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(50) | 菜单名称 |
| permission | varchar(100) | 权限标识 |
| type | tinyint | 类型（1-目录，2-菜单，3-按钮） |
| sort | int | 排序 |
| parent_id | bigint | 父菜单 ID |
| path | varchar(200) | 路由地址 |
| component | varchar(200) | 前端组件路径 |
| icon | varchar(50) | 图标 |
| visible | tinyint | 是否显示（0-隐藏，1-显示） |
| keep_alive | tinyint | 是否缓存 |

### 关联表

- `system_users_roles`：用户角色关联（user_id, role_id），联合主键
- `system_roles_menus`：角色菜单关联（role_id, menu_id），联合主键

## 配置说明

### application.yml 关键配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8080 |
| `spring.datasource.*` | 数据库连接 | - |
| `spring.data.redis.*` | Redis 连接 | - |
| `model.defaultService` | 默认模型服务 | dashscope |
| `model.services.dashscope.api-key` | DashScope API Key | - |
| `model.services.dashscope.model` | 模型名称 | qwen-turbo |
| `weather.api-key` | 高德天气 API Key | - |
| `system.login.max-fail-count` | 最大登录失败次数 | 5 |
| `system.login.lock-time` | 账户锁定时间（分钟） | 30 |
| `system.password.reset-default` | 默认重置密码 | 123456 |
| `system.aes.key` | AES 加密密钥（32位） | - |
| `system.aes.iv` | AES 初始化向量（16位） | - |

### MyBatis-Plus 配置

- 逻辑删除字段：`deleted`（0-未删除，1-已删除）
- 自动填充：creator、createTime、updater、updateTime
- SQL 日志：StdOutImpl

## 开发指南

### 添加新的 Agent 工具

1. 在 `tool` 包下创建工具类，使用 `@Component` 标记
2. 使用 `@Tool` 注解标记工具方法，`@ToolParam` 标记参数
3. 在 `AgentController.getToolByName()` 中添加映射

```java
@Component
public class MyTool {
    @Tool(name = "my_tool", description = "工具描述")
    public String execute(
        @ToolParam(name = "param1", description = "参数说明") String param1) {
        return "结果";
    }
}
```

### 扩展多智能体协作

1. 在 `agent` 包下创建新的 Agent 类
2. 在 `PartyPlanningAgent.init()` 中注册新工具到 Toolkit
3. 更新 `sysPrompt` 说明新工具的使用方式

### 添加系统管理功能

按照以下分层创建：
1. `system/entity/` - 数据实体
2. `system/dao/` - DAO 接口（继承 BaseMapper）
3. `system/service/` - 服务接口与实现
4. `system/controller/` - REST 控制器

## 常见问题

**天气查询返回 INVALID_USER_KEY**
检查高德天气 API Key 是否正确配置和激活。

**天气查询返回 DAILY_QUERY_OVER_LIMIT**
高德免费版有每日调用限制，请降低调用频率或升级套餐。

**端口 8080 被占用**
```powershell
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F
```

**大模型调用失败**
检查 DashScope API Key 是否正确，确保网络能访问 `dashscope.aliyuncs.com`。

**登录时提示账号被锁定**
Redis 中设置了 30 分钟锁定，等待过期或手动清除 Redis 中 `login:lock:<username>` 键。

## 版本历史

| 版本 | 说明 |
|------|------|
| v2.1.0 | 新增系统管理模块：用户认证（JWT+Redis）、用户/角色/菜单管理、RBAC 权限控制 |
| v2.0.0 | 升级到 ReAct 模式多智能体协作，Agent 自主推理与工具调用 |
| v1.4.0 | 新增多智能体协作、BudgetAgent/ActivityAgent、活动策划意图识别 |
| v1.3.0 | 切换到高德天气 API |
| v1.2.0 | 新增统一聊天接口、DashScope 意图识别 |
| v1.1.0 | 切换到和风天气 API |
| v1.0.0 | 项目初始化，集成 AgentScope-Java |

## 许可证

MIT License
