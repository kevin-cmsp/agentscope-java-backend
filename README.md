# AgentScope Java Backend

企业级 AI 智能助手后端服务，基于 Spring Boot 3 + AgentScope Java + DashScope（通义千问）构建，提供多智能体协作、对话管理、记忆系统和用户画像等功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.4 | 核心框架 |
| JDK | 21 | 运行环境 |
| AgentScope Java | 1.0.9 | 多智能体框架 |
| DashScope SDK | 2.0.0 | 通义千问大模型 API |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 5.7+ | 关系型数据库 |
| Redis | 6.0+ | 缓存与会话管理 |
| Spring Security | 6.x | 安全认证框架 |
| JWT (jjwt) | 0.11.5 | Token 认证 |
| OkHttp | 4.12.0 | HTTP 客户端 |
| Hutool | 5.8.25 | 工具库 |

## 项目结构

```
src/main/java/com/corporate/finance/ai/
├── Application.java                        # 应用启动入口
├── agent/                                  # AI Agent 实现
│   ├── PartyPlanningAgent.java            # ReAct 模式活动策划 Agent
│   ├── PartyPlanningAgentLevel.java       # 层级式协作活动策划 Agent
│   ├── BudgetAgent.java                   # 预算管理 Agent
│   └── ActivityAgent.java                 # 活动安排 Agent
├── tool/                                   # Agent 工具集
│   ├── CalculatorTool.java                # 计算工具（加减乘除、百分比）
│   ├── WeatherTool.java                   # 天气查询工具
│   ├── BudgetTool.java                    # 预算分配工具
│   └── ActivityTool.java                  # 活动安排工具
├── config/                                 # 配置类
│   ├── AgentScopeConfig.java              # AgentScope 框架配置
│   └── ModelServiceConfig.java            # 模型服务配置
├── service/                                # 业务服务层
│   ├── MemoryService.java                 # 记忆管理服务（三级存储）
│   ├── MemoryCompressService.java         # 记忆压缩服务
│   ├── UserProfileService.java            # 用户画像服务
│   ├── AgentManagerService.java           # Agent 生命周期管理
│   ├── SkillService.java                  # 技能管理服务
│   ├── WeatherService.java               # 天气查询服务（高德 API）
│   ├── ModelService.java                  # 模型服务接口
│   ├── ModelServiceFactory.java           # 模型服务工厂
│   └── DashScopeModelService.java         # DashScope 模型实现
├── controller/                             # 控制层
│   ├── AgentController.java               # Agent 管理 + 聊天统一接口
│   └── ChatHistoryController.java         # 对话历史管理接口
└── system/                                 # 系统管理模块
    ├── common/Result.java                 # 统一响应封装
    ├── config/                            # 系统配置
    │   ├── RedisConfig.java               # Redis 配置
    │   ├── WebConfig.java                 # Web 配置
    │   └── MyMetaObjectHandler.java       # MyBatis-Plus 自动填充
    ├── security/                          # 安全认证
    │   ├── SecurityConfig.java            # Spring Security 配置
    │   ├── JwtFilter.java                 # JWT 过滤器
    │   └── PermissionService.java         # 权限检查服务
    ├── entity/                            # 数据实体
    │   ├── BaseEntity.java                # 基础实体（审计字段）
    │   ├── UserEntity.java                # 用户实体
    │   ├── RoleEntity.java                # 角色实体
    │   ├── MenuEntity.java                # 菜单实体
    │   ├── ConversationEntity.java        # AI 对话会话实体
    │   ├── MessageEntity.java             # AI 对话消息实体
    │   ├── UserProfileEntity.java         # 用户画像实体
    │   ├── UserRoleEntity.java            # 用户-角色关联
    │   └── RoleMenuEntity.java            # 角色-菜单关联
    ├── dao/                               # 数据访问层
    │   ├── UserDao.java
    │   ├── RoleDao.java
    │   ├── MenuDao.java
    │   ├── ConversationDao.java
    │   ├── MessageDao.java
    │   ├── UserProfileDao.java
    │   ├── UserRoleDao.java
    │   └── RoleMenuDao.java
    ├── service/                           # 系统服务
    │   ├── ChatHistoryService.java        # 对话历史服务
    │   ├── AuthService.java               # 认证服务
    │   ├── UserService.java               # 用户管理服务
    │   ├── RoleService.java               # 角色管理服务
    │   ├── MenuService.java               # 菜单管理服务
    │   └── impl/                          # 服务实现类
    ├── controller/                        # 系统控制器
    │   ├── AuthController.java            # 认证接口
    │   ├── UserController.java            # 用户管理接口
    │   ├── RoleController.java            # 角色管理接口
    │   └── MenuController.java            # 菜单管理接口
    └── utils/                             # 工具类
        ├── JwtUtils.java                  # JWT 工具
        ├── RedisUtils.java                # Redis 工具
        └── AesDecryptor.java              # AES 解密工具
```

## 核心功能

### 1. 统一聊天接口

通过意图识别自动路由到对应的处理逻辑，支持 6 种意图类型：

| 意图类型 | 说明 | 触发示例 |
|----------|------|----------|
| `weather` | 天气查询 | 北京天气怎么样？ |
| `calculator` | 数学计算 | 10 加 5 等于多少？ |
| `data` | 智能查数 | 上个月销量是多少？ |
| `knowledge` | 知识库查询 | 报销流程是什么？ |
| `party` | 活动策划 | 帮我策划一场团建活动 |
| `general` | 通用问答 | 你好，介绍下华为 |

意图识别采用 **大模型 + 关键词** 双重策略，大模型识别失败时自动回退到关键词匹配。

### 2. 记忆管理系统（三级存储）

```
内存（ConcurrentHashMap）→ Redis（24h 过期）→ 数据库（永久存储）
```

- **内存层**：基于用户 ID 隔离，ConcurrentHashMap 线程安全存储
- **Redis 层**：服务重启后从 Redis 恢复记忆，24 小时过期
- **数据库层**：从 `ai_message` 表加载历史对话，支持跨会话记忆

### 3. 记忆压缩

当用户对话历史超过 50 条时自动触发压缩：
- 调用大模型对旧消息生成摘要（300字以内）
- 保留最近 10 条原始消息
- 后续对话时在上下文中注入摘要，保持连贯性

### 4. 用户画像

基于历史对话自动构建用户偏好画像：
- **兴趣标签**：自动提取用户关注的主题
- **常用功能**：统计用户使用频率最高的功能
- **对话风格**：识别用户偏好的交互风格（正式/随意/技术）
- **个性化服务**：画像信息注入到 AI 回复上下文中

### 5. 多智能体协作

- **ReAct 模式**（PartyPlanningAgent）：大模型自主决策工具调用链路
- **层级式协作**（PartyPlanningAgentLevel）：多个专业 Agent 协调工作

### 6. RBAC 权限管理

- 用户-角色-菜单三级权限模型
- JWT Token + Redis 会话管理
- AES 密码加密传输 + BCrypt 存储

## API 接口

### AI 聊天

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/chat` | 统一聊天接口 |
| GET | `/api/chat/conversations` | 获取会话列表 |
| POST | `/api/chat/conversations` | 创建新会话 |
| GET | `/api/chat/conversations/{id}/messages` | 获取会话消息 |
| DELETE | `/api/chat/conversations/{id}` | 删除会话 |

### 记忆管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/memory` | 获取记忆内容 |
| POST | `/api/memory/clear` | 清除记忆 |
| GET | `/api/memory/stats` | 获取记忆统计信息 |

### 用户画像

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/profile` | 获取用户画像 |
| POST | `/api/profile/analyze` | 触发画像分析 |

### 工具能力

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/weather` | 天气查询 |
| POST | `/api/skills/calculator` | 计算器 |
| POST | `/api/skills/register` | 注册技能 |
| GET | `/api/skills` | 列出技能 |

### Agent 管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/agents` | 创建 Agent |
| GET | `/api/agents` | 列出所有 Agent |
| GET | `/api/agents/{agentId}` | 获取 Agent 信息 |
| POST | `/api/agents/{agentId}/execute` | 执行 Agent 任务 |
| DELETE | `/api/agents/{agentId}` | 删除 Agent |

### 系统管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 用户登出 |
| GET | `/api/auth/captcha` | 获取验证码 |
| GET | `/api/auth/userInfo` | 获取用户信息 |

## 数据库设计

### 数据表

| 表名 | 说明 |
|------|------|
| `system_user` | 用户表 |
| `system_role` | 角色表 |
| `system_menu` | 菜单表 |
| `system_users_roles` | 用户-角色关联表 |
| `system_roles_menus` | 角色-菜单关联表 |
| `ai_conversation` | AI 对话会话表 |
| `ai_message` | AI 对话消息表 |
| `ai_user_profile` | 用户画像表 |
| `ai_memory_summary` | 记忆摘要表 |

初始化脚本位于 `doc/database-mysql5.7.sql`。

## 环境要求

- JDK 21+
- MySQL 5.7+
- Redis 6.0+
- Maven 3.8+

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p agentscope < doc/database-mysql5.7.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，配置以下信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:3306/agentscope
    username: your-username
    password: your-password
  data:
    redis:
      host: your-redis-host
      port: 6379
      password: your-redis-password

model:
  services:
    dashscope:
      api-key: your-dashscope-api-key

weather:
  api-key: your-amap-api-key
```

### 3. 构建运行

```bash
mvn clean package -DskipTests
java -jar target/agentscope-java-backend-1.0-SNAPSHOT.jar
```

服务默认启动在 `http://localhost:8080`。

## 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8080 |
| `model.services.dashscope.api-key` | DashScope API Key | - |
| `model.services.dashscope.model` | 模型名称 | qwen-turbo |
| `weather.api-key` | 高德天气 API Key | - |
| `system.login.max-fail-count` | 最大登录失败次数 | 5 |
| `system.login.lock-time` | 账号锁定时间（分钟） | 30 |
| `system.aes.key` | AES 加密密钥 | - |
| `system.aes.iv` | AES 初始向量 | - |

## 架构设计

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Controller  │────>│   Service    │────>│    DAO      │
│   (API层)    │     │  (业务逻辑)   │     │ (数据访问)   │
└─────────────┘     └──────────────┘     └─────────────┘
       │                   │                     │
       │            ┌──────┴──────┐              │
       │            │             │              │
  ┌────┴────┐  ┌────┴────┐  ┌────┴────┐   ┌────┴────┐
  │ Security│  │  Memory │  │ Profile │   │  MySQL  │
  │  (JWT)  │  │ (Redis) │  │  (AI)   │   │         │
  └─────────┘  └─────────┘  └─────────┘   └─────────┘
                     │
              ┌──────┴──────┐
              │  DashScope  │
              │ (通义千问 AI) │
              └─────────────┘
```
