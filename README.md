# AgentScope-Java 后端项目

## 项目概述

这是一个基于 **AgentScope-Java** 构建的AI Agent开发平台后端项目。该项目使用Spring Boot 3.x、JDK 21和Maven构建，提供了完整的Agent管理、技能管理、天气查询和多智能体协作功能。

### 主要功能

- **天气查询**：支持中文城市名称，使用高德天气API
- **技能管理**：注册、列出、执行和卸载技能
- **记忆管理**：存储和管理Agent对话历史
- **Agent管理**：创建、查询、执行和删除Agent（基础功能）
- **多智能体协作**：基于ReAct模式的活动策划，支持自主推理和工具调用

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 后端框架 |
| JDK | 21 | Java开发环境 |
| Maven | 3.9+ | 依赖管理和构建工具 |
| OkHttp | 4.12.0 | HTTP客户端 |
| Jackson | 2.15.2 | JSON解析 |
| AgentScope | 1.0.9 | Agent开发框架 |

## 项目结构

```
agentscope-java-backend/
├── src/
│   ├── main/
│   │   ├── java/com/corporate/finance/ai/
│   │   │   ├── agent/            # Agent实现
│   │   │   │   ├── ActivityAgent.java          # 活动安排Agent
│   │   │   │   ├── BudgetAgent.java             # 预算管理Agent
│   │   │   │   ├── PartyPlanningAgent.java      # 活动策划Agent（ReAct模式）
│   │   │   │   └── PartyPlanningAgentLevel.java # 活动策划Agent（层级式协作模式）
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/       # REST API控制器
│   │   │   ├── service/          # 业务逻辑服务
│   │   │   ├── tool/             # 工具类
│   │   │   │   ├── ActivityTool.java           # 活动安排工具
│   │   │   │   ├── BudgetTool.java              # 预算计算工具
│   │   │   │   ├── CalculatorTool.java          # 计算器工具
│   │   │   │   └── WeatherTool.java             # 天气查询工具
│   │   │   └── Application.java  # 应用入口
│   │   └── resources/
│   │       └── application.yml   # 配置文件
│   └── test/                     # 测试代码
├── pom.xml                       # Maven配置文件
└── README.md                     # 项目文档
```

## 快速开始

### 1. 环境准备

- **JDK 21**：确保安装并配置了JDK 21
- **Maven 3.9+**：确保安装了Maven 3.9或更高版本
- **高德天气API Key**：注册高德开放平台账号并获取API Key
- **DashScope API Key**：注册阿里云账号并获取DashScope API Key（用于大模型服务）

### 2. 配置API Key

编辑 `src/main/resources/application.yml` 文件，配置相关API Key：

```yaml
# 天气查询API配置（高德天气）
weather:
  api-key: your_amap_api_key_here  # 替换为你的高德天气API Key
  base-url: https://restapi.amap.com/v3/weather/weatherInfo

# 大模型服务配置
model:
  services:
    dashscope:
      api-key: your_dashscope_api_key_here  # 替换为你的DashScope API Key
      base-url: https://dashscope.aliyuncs.com/api/v1
      model: qwen-turbo
```

### 3. 构建项目

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

### 4. 服务管理（PowerShell）

#### 4.1 启动服务

```powershell
# 在项目目录下启动服务
cd agentscope-java-backend
mvn spring-boot:run

# 编译并启动服务
cd agentscope-java-backend
mvn clean compile spring-boot:run

# 后台启动服务（Windows）
# 注意：Windows PowerShell不直接支持后台运行，建议使用新的终端窗口
Start-Process powershell -ArgumentList "cd agentscope-java-backend; mvn spring-boot:run"
```

#### 4.2 停止服务

```powershell
# 查看端口8080占用情况
netstat -ano | findstr :8080

# 终止占用端口的进程（替换 <进程ID> 为实际的进程ID）
taskkill /PID <进程ID> /F

# 或者在启动服务的终端窗口中按 Ctrl+C 停止服务
```

#### 4.3 查看服务状态

```powershell
# 检查服务是否运行
netstat -ano | findstr :8080

# 测试服务是否正常响应
$body = @{ message = "你好" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"
```

## API接口文档

### 1. 统一聊天接口（推荐）

**接口**：`POST /api/chat`

**请求体**：
```json
{
  "message": "济南的天气怎么样？"  // 用户消息
}
```

**响应**：
```json
{
  "status": "success",
  "content": "城市: 山东省 济南市\n发布时间: 2026-03-18 08:03:01\n\n=== 今天天气 ===\n日期: 2026-03-18 (周三)\n白天: 阴, 温度 10°C, 东北风 4级\n夜间: 多云, 温度 1°C, 东北风 4级\n\n=== 明天预报 ===\n日期: 2026-03-19 (周四)\n白天: 多云, 温度 15°C\n夜间: 多云, 温度 6°C\n\n=== 后天预报 ===\n日期: 2026-03-20 (周五)\n白天: 晴, 温度 19°C\n夜间: 晴, 温度 10°C\n\n=== 大后天预报 ===\n日期: 2026-03-21 (周六)\n白天: 晴, 温度 21°C\n夜间: 晴, 温度 12°C"
}
```

**PowerShell调用示例**：

```powershell
# 示例1：查询指定城市天气
$body = @{ message = "北京的天气怎么样？" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"

# 示例2：查询默认城市天气（无需指定城市）
$body = @{ message = "今天天气怎么样？" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"

# 示例3：计算请求
$body = @{ message = "10加5等于多少？" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"

# 示例4：智能查数
$body = @{ message = "上个月的销量是多少？" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"

# 示例5：知识库查询
$body = @{ message = "公司的规章制度是什么？" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"

# 示例6：通用问题
$body = @{ message = "你好，今天过得怎么样？" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"

# 示例7：活动策划
$body = @{ message = "帮我策划一场生日派对，地点在北京，预算5000元，大约10人参加" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"
```

#### 1.1 意图识别系统

系统使用**DashScope API**（通义千问）进行智能意图识别，支持更复杂的语义理解。当大模型调用失败时，会自动回退到基于关键词的识别方案。

**支持的意图类型**：
- **weather**：天气查询（如："北京的天气怎么样？"、"今天会下雨吗？"）
- **calculator**：计算请求（如："10加5等于多少？"、"3乘以4是多少？"）
- **data**：智能查数（如："上个月的销量是多少？"、"今年的收入情况？"）
- **knowledge**：知识库查询（如："公司的规章制度是什么？"、"如何使用系统？"）
- **party**：活动策划（如："帮我策划一场生日派对"、"我想举办一个聚会"）
- **general**：其他通用问题（如："你好，今天过得怎么样？"）

### 2. 天气查询接口

**接口**：`POST /api/weather`

**请求体**：
```json
{
  "city": "济南"  // 城市名称（支持中文）
}
```

**响应**：
```
城市: 山东省 济南市
发布时间: 2026-03-18 08:03:01

=== 今天天气 ===
日期: 2026-03-18 (周三)
白天: 阴, 温度 10°C, 东北风 4级
夜间: 多云, 温度 1°C, 东北风 4级

=== 明天预报 ===
日期: 2026-03-19 (周四)
白天: 多云, 温度 15°C
夜间: 多云, 温度 6°C

=== 后天预报 ===
日期: 2026-03-20 (周五)
白天: 晴, 温度 19°C
夜间: 晴, 温度 10°C

=== 大后天预报 ===
日期: 2026-03-21 (周六)
白天: 晴, 温度 21°C
夜间: 晴, 温度 12°C
```

**PowerShell调用示例**：
```powershell
# 调用天气查询接口
$body = @{ city = "济南" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/weather" -Method POST -Body $body -ContentType "application/json"
```

### 3. 技能管理接口

#### 3.1 注册技能

**接口**：`POST /api/skills/register`

**请求体**：
```json
{
  "skillName": "calculator"  // 技能名称
}
```

**响应**：
```
技能注册成功：calculator
```

#### 3.2 列出所有技能

**接口**：`GET /api/skills`

**响应**：
```json
{
  "skills": ["calculator"],
  "count": 1
}
```

#### 3.3 执行计算器技能

**接口**：`POST /api/skills/calculator`

**请求体**：
```json
{
  "operation": "add",  // 操作类型：add, subtract, multiply, divide, percentage
  "a": 10,             // 第一个数
  "b": 5               // 第二个数
}
```

**响应**：
```
10.00 + 5.00 = 15.00
```

### 4. 记忆管理接口

#### 4.1 获取记忆内容

**接口**：`GET /api/memory`

**响应**：
```
User: 查询城市天气：济南
Assistant: 天气信息：城市: 济南
温度: 11°C (体感: 10°C)
湿度: 46%
天气: 多云
风向: 东北风
风力: 3级
```

#### 4.2 清除记忆

**接口**：`POST /api/memory/clear`

**响应**：
```
记忆已清除
```

### 5. Agent管理接口

> **注意**：Agent创建功能已暂时移除，需要根据具体业务需求重新实现。

#### 5.1 创建Agent

**接口**：`POST /api/agents`

**请求体**：
```json
{
  "name": "test-agent",  // Agent名称
  "sysPrompt": "你是一个测试助手"  // 系统提示词
}
```

**响应**：
```
Agent创建功能已暂时移除，需要根据具体业务需求重新实现
```

#### 5.2 列出所有Agent

**接口**：`GET /api/agents`

**响应**：
```json
[]
```

### 6. 多智能体协作接口

#### 6.1 活动策划接口（ReAct模式）

系统实现了基于ReAct模式的多智能体协作活动策划功能，通过PartyPlanningAgent协调多个工具完成复杂的活动策划任务。

**工作流程**：
1. 用户发送活动策划请求（包含地点、预算、人数等信息）
2. PartyPlanningAgent（ReAct模式）自主推理：
   - 分析用户需求，识别需要调用的工具
   - 调用WeatherTool查询指定地点的天气信息
   - 调用BudgetTool计算预算分配
   - 调用ActivityTool根据人数和天气安排活动
   - 整合所有信息，生成完整的活动策划方案

**调用示例**：
```powershell
# 策划生日派对
$body = @{ message = "帮我策划一场生日派对，地点在北京，预算5000元，大约10人参加" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -Body $body -ContentType "application/json"
```

**响应示例**：
```
生日派对策划方案

一、基本信息
- 地点：北京
- 日期：今天
- 预算：5000元
- 预计人数：10人

二、天气情况
城市: 北京市
发布时间: 2026-03-18 08:03:01
今天白天: 晴, 温度 25°C, 南风 2级
今天夜间: 晴, 温度 15°C, 南风 2级

三、预算分配
总预算：5000元
场地租赁：1500元 (30%)
餐饮费用：2000元 (40%)
装饰布置：750元 (15%)
活动物料：500元 (10%)
其他费用：250元 (5%)

四、活动安排
天气良好，可安排室内外结合的活动：
1. 开场活动：签到、合影
2. 主体活动：桌游、卡拉OK
3. 互动游戏：真心话大冒险、谁是卧底
4. 活动仪式：剪彩、开香槟、切蛋糕
5. 结束活动：交换礼物、合影留念

五、建议
1. 由于天气晴朗，建议安排部分户外活动
2. 场地选择建议在交通便利的市中心
3. 餐饮建议选择适合10人份的套餐
4. 装饰以生日主题为主，营造温馨氛围
5. 活动安排时间建议在下午2点开始，晚上8点结束
```

#### 6.2 活动级别策划接口

**接口**：`POST /api/api/party/level`

**请求体**：
```json
{
  "message": "帮我策划一场高端商务活动，地点在上海，预算10000元，大约20人参加"  // 用户消息
}
```

**响应**：
根据活动级别生成相应的策划方案

## 多智能体协作系统（ReAct模式）

### 核心组件

1. **PartyPlanningAgent**：基于ReAct模式的核心协调者，负责自主推理和工具调用
2. **WeatherTool**：提供天气查询功能
3. **BudgetTool**：提供预算计算功能
4. **ActivityTool**：提供活动安排功能

### ReAct工作流程

```
用户输入 → ReAct Agent → 思考(Thought) → 行动(Action) → 观察(Observation) → 思考(Thought) → ... → 最终答案(Final Answer)
```

**示例流程**：
1. 用户输入："帮我策划一个10人的活动，预算5000元，地点在北京"
2. ReAct Agent思考：需要先查询北京的天气
3. 行动：调用WeatherTool查询北京天气
4. 观察：获取天气信息（北京：晴天，温度25℃）
5. ReAct Agent思考：现在需要计算预算分配
6. 行动：调用BudgetTool计算预算
7. 观察：获取预算明细（场地1500元，餐饮2000元等）
8. ReAct Agent思考：需要根据人数和天气安排活动
9. 行动：调用ActivityTool安排活动
10. 观察：获取活动方案
11. ReAct Agent思考：已收集所有必要信息，生成最终方案
12. 最终答案：输出完整的活动策划方案

## 天气API切换说明

### 从和风天气切换到高德天气

**原因**：
- 和风天气对中文城市名称支持存在问题
- 高德天气API对中文城市名称支持更好，无需额外的城市编码映射
- 高德天气API响应速度更快（国内服务器）
- 高德天气提供更丰富的天气数据

**使用方法**：
1. 注册高德开放平台账号：https://lbs.amap.com/
2. 创建应用并获取Web服务API Key
3. 在 `application.yml` 中配置API Key
4. 重启服务

## 常见问题

### 1. 天气查询返回"查询天气失败，错误信息：INVALID_USER_KEY"

**原因**：高德天气API Key不正确或未激活
**解决方法**：检查API Key是否正确，确保已经在高德开放平台激活并启用了Web服务API

### 2. 天气查询返回"查询天气失败，错误信息：DAILY_QUERY_OVER_LIMIT"

**原因**：API Key使用次数超限
**解决方法**：高德天气免费版每天有调用限制，检查使用频率或升级套餐

### 3. 服务启动失败，端口8080被占用

**解决方法**：
```powershell
# 查找占用端口的进程
netstat -ano | findstr :8080

# 终止占用端口的进程
taskkill /PID <进程ID> /F
```

### 4. 大模型调用失败

**原因**：DashScope API Key不正确或网络问题
**解决方法**：检查API Key是否正确，确保网络连接正常

### 5. 活动策划返回空响应

**原因**：ReAct Agent执行过程中出现错误
**解决方法**：检查DashScope API Key配置，确保网络连接正常，或尝试简化请求内容

## 开发指南

### 添加新技能

1. 在 `tool` 包下创建新的工具类
2. 使用 `@Component` 注解标记为Spring组件
3. 使用 `@Tool` 注解标记工具方法
4. 在 `AgentController` 的 `getToolByName` 方法中添加技能映射
5. 重启服务后即可注册和使用新技能

### 扩展多智能体协作功能

1. 在 `agent` 包下创建新的Agent类
2. 实现相应的业务逻辑
3. 在 `PartyPlanningAgent` 中添加新Agent的协作逻辑
4. 重启服务后即可使用新的协作功能

### 扩展ReAct Agent能力

1. 在 `tool` 包下创建新的工具类
2. 在 `PartyPlanningAgent` 的 `init` 方法中注册新工具
3. 更新 `sysPrompt` 以包含新工具的使用说明
4. 重启服务后ReAct Agent即可使用新工具

## 版本历史

### v1.0.0
- 初始化项目，集成AgentScope-Java
- 实现天气查询功能（使用OpenWeatherMap API）
- 实现技能管理和记忆管理功能

### v1.1.0
- 切换到和风天气API，支持中文城市名称
- 移除不再需要的WeatherAgent和WeatherTool
- 优化代码结构，提高可维护性
- 更新项目文档

### v1.2.0
- 新增统一聊天接口（`POST /api/chat`）
- 实现基于DashScope API的智能意图识别
- 添加默认城市天气查询功能
- 优化响应格式和错误处理

### v1.3.0
- 切换到高德天气API，解决中文城市名称支持问题
- 优化WeatherService代码结构
- 更新项目文档

### v1.4.0
- 新增多智能体协作功能
- 实现PartyPlanningAgent、BudgetAgent和ActivityAgent
- 添加BudgetTool和ActivityTool工具类
- 扩展意图识别系统，支持派对策划意图
- 更新API接口文档

### v2.0.0
- 升级到ReAct模式的多智能体协作
- 新增WeatherTool工具类
- 实现PartyPlanningAgent的自主推理和工具调用
- 优化活动策划流程，支持更复杂的任务分解
- 更新项目文档，添加ReAct模式说明

## 许可证

本项目使用 MIT 许可证。

## 联系方式

- **项目维护者**：Corporate Finance AI Team
- **邮箱**：contact@corporatefinance.ai
- **GitHub**：https://github.com/corporate-finance-ai/agentscope-java-backend