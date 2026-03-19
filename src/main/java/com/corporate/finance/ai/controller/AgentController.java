package com.corporate.finance.ai.controller;

import com.corporate.finance.ai.agent.PartyPlanningAgent;
import com.corporate.finance.ai.agent.PartyPlanningAgentLevel;
import com.corporate.finance.ai.service.AgentManagerService;
import com.corporate.finance.ai.service.MemoryService;
import com.corporate.finance.ai.service.SkillService;
import com.corporate.finance.ai.service.WeatherService;
import com.corporate.finance.ai.tool.ActivityTool;
import com.corporate.finance.ai.tool.BudgetTool;
import com.corporate.finance.ai.tool.CalculatorTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * Agent REST API控制器
 * 
 * 该控制器提供Agent管理和天气查询的RESTful API接口。
 * 所有接口都以/api为前缀。
 * 
 * 主要功能：
 * - 统一聊天接口（处理所有用户请求）
 * - 天气查询接口
 * - Agent创建、查询、执行、删除接口
 * - 记忆管理接口
 * - 技能管理接口
 * 
 * API端点：
 * - POST /api/chat - 统一聊天接口（推荐）
 * - POST /api/weather - 查询天气
 * - POST /api/agents - 创建Agent
 * - GET /api/agents/{agentId} - 获取Agent信息
 * - POST /api/agents/{agentId}/execute - 执行Agent任务
 * - GET /api/agents - 列出所有Agent
 * - DELETE /api/agents/{agentId} - 删除Agent
 * - GET /api/memory - 获取记忆内容
 * - POST /api/memory/clear - 清除记忆
 * - POST /api/skills/register - 注册技能
 * - GET /api/skills - 列出所有技能
 * - DELETE /api/skills/{skillName} - 卸载技能
 * - POST /api/skills/calculator - 执行计算器技能
 * 
 * 技术实现：
 * - 使用构造函数注入实现依赖注入（Spring Boot推荐做法）
 * - 所有依赖均为final，确保不可变性
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
public class AgentController {

    /**
     * Agent管理服务实例
     */
    private final AgentManagerService agentManagerService;

    /**
     * 天气查询服务实例
     */
    private final WeatherService weatherService;

    /**
     * 记忆管理服务实例
     */
    private final MemoryService memoryService;

    /**
     * 技能管理服务实例
     */
    private final SkillService skillService;

    /**
     * 计算器工具实例
     */
    private final CalculatorTool calculatorTool;

    /**
     * 预算工具实例
     */
    private final BudgetTool budgetTool;

    /**
     * 活动安排工具实例
     */
    private final ActivityTool activityTool;

    /**
     * 活动策划Agent实例
     */
    private final PartyPlanningAgent partyPlanningAgent;

    /**
     * DashScope API Key
     */
    private final String dashscopeApiKey;

    /**
     * DashScope Base URL
     */
    private final String dashscopeBaseUrl;

    /**
     * DashScope Model
     */
    private final String dashscopeModel;

    @Autowired
    private PartyPlanningAgentLevel partyPlanningAgentLevel;

    /**
     * 构造函数
     * 
     * 通过构造函数注入所有依赖，这是Spring Boot推荐的做法。
     * 优点：
     * - 依赖明确，易于测试
     * - 字段可以声明为final，确保不可变性
     * - 避免使用@Autowired字段注入
     * 
     * @param agentManagerService Agent管理服务
     * @param weatherService 天气查询服务
     * @param memoryService 记忆管理服务
     * @param skillService 技能管理服务
     * @param calculatorTool 计算器工具
     * @param budgetTool 预算工具
     * @param activityTool 活动安排工具
     * @param partyPlanningAgent 活动策划Agent
     * @param dashscopeApiKey DashScope API Key
     * @param dashscopeBaseUrl DashScope Base URL
     * @param dashscopeModel DashScope Model
     */
    public AgentController(AgentManagerService agentManagerService,
                          WeatherService weatherService,
                          MemoryService memoryService,
                          SkillService skillService,
                          CalculatorTool calculatorTool,
                          BudgetTool budgetTool,
                          ActivityTool activityTool,
                          PartyPlanningAgent partyPlanningAgent,
                          @Value("${model.services.dashscope.api-key}") String dashscopeApiKey,
                          @Value("${model.services.dashscope.base-url}") String dashscopeBaseUrl,
                          @Value("${model.services.dashscope.model}") String dashscopeModel) {
        this.agentManagerService = agentManagerService;
        this.weatherService = weatherService;
        this.memoryService = memoryService;
        this.skillService = skillService;
        this.calculatorTool = calculatorTool;
        this.budgetTool = budgetTool;
        this.activityTool = activityTool;
        this.partyPlanningAgent = partyPlanningAgent;
        this.dashscopeApiKey = dashscopeApiKey;
        this.dashscopeBaseUrl = dashscopeBaseUrl;
        this.dashscopeModel = dashscopeModel;
    }

    /**
     * 统一聊天接口
     * 
     * 该接口处理所有用户请求，通过意图识别确定需要调用的服务。
     * 支持的请求类型：
     * - 天气查询：包含"天气"、"温度"、"下雨"等关键词
     * - 计算器：包含"计算"、"加"、"减"、"乘"、"除"等关键词
     * - 智能查数：包含"数据"、"报表"、"统计"等关键词
     * - 知识库：包含"知识"、"文档"、"资料"等关键词
     * - 其他：直接返回大模型回答
     * 
     * @param request 请求体，包含message字段（用户消息）
     * @return 统一格式的响应
     */
    @PostMapping(value = "/chat", produces = "application/json;charset=utf-8")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        // 获取用户消息
        String message = request.get("message");
        
        // 参数校验
        if (message == null || message.isEmpty()) {
            return createResponse("error", "错误：消息内容不能为空");
        }

        try {
            // 存储用户消息到记忆
            memoryService.storeMessage("user", message);
            
            // 识别意图并处理
            String intent = identifyIntent(message);
            String response;
            
            switch (intent) {
                case "weather":
                    response = handleWeatherRequest(message);
                    break;
                case "calculator":
                    response = handleCalculatorRequest(message);
                    break;
                case "data":
                    response = handleDataRequest(message);
                    break;
                case "knowledge":
                    response = handleKnowledgeRequest(message);
                    break;
                case "party":
                    response = handlePartyRequest(message);
                    break;
                default:
                    response = handleGeneralRequest(message);
                    break;
            }
            
            // 存储响应到记忆
            memoryService.storeMessage("assistant", response);
            
            return createResponse("success", response);
        } catch (Exception e) {
            String errorMessage = "处理请求失败：" + e.getMessage();
            memoryService.storeMessage("assistant", errorMessage);
            return createResponse("error", errorMessage);
        }
    }

    /**
     * 根据活动级别策划活动
     *
     * 该接口接收用户请求，通过PartyPlanningAgentLevel服务根据不同的活动级别
     * 生成相应的活动策划方案。
     *
     * @param request 请求体，应包含message字段（用户消息内容）
     * @return 活动策划方案字符串
     * @see PartyPlanningAgentLevel
     */
    @PostMapping(value = "/api/party/level", produces = "application/json;charset=utf-8")
    public Map<String, Object> planParty(@RequestBody Map<String, String> request) {
        String message = partyPlanningAgentLevel.planParty(request.get("message"));
        return createResponse("success", message);
    }

    /**
     * 识别用户意图（基于大模型）
     * 
     * 使用大模型进行意图识别，支持更复杂的语义理解。
     * 
     * @param message 用户消息
     * @return 意图类型
     */
    private String identifyIntent(String message) {
        try {
            // 构造提示词
            String prompt = "请识别以下用户消息的意图，并返回对应的意图类型。\n" +
                           "意图类型包括：\n" +
                           "1. weather - 天气查询（如：北京的天气怎么样？今天会下雨吗？）\n" +
                           "2. calculator - 计算请求（如：10加5等于多少？3乘以4是多少？）\n" +
                           "3. data - 智能查数（如：上个月的销量是多少？今年的收入情况？）\n" +
                           "4. knowledge - 知识库查询（如：公司的规章制度是什么？如何使用系统？）\n" +
                           "5. party - 活动策划（如：帮我策划一场商务活动，我需要一个活动计划）\n" +
                           "6. general - 其他通用问题（如：你好，今天过得怎么样？）\n" +
                           "\n" +
                           "用户消息：" + message + "\n" +
                           "请只返回意图类型，不要返回其他内容。";
            
            // 调用大模型（这里使用DashScope API）
            String intent = callDashScopeModel(prompt);
            
            // 验证意图类型
            if (intent != null && Arrays.asList("weather", "calculator", "data", "knowledge", "party", "general").contains(intent)) {
                return intent;
            }
        } catch (Exception e) {
            // 大模型调用失败时，回退到关键词匹配
            e.printStackTrace();
        }
        
        // 回退到关键词匹配
        return identifyIntentByKeywords(message);
    }

    /**
     * 调用DashScope大模型
     * 
     * @param prompt 提示词
     * @return 大模型返回的意图类型
     * @throws Exception 异常
     */
    private String callDashScopeModel(String prompt) throws Exception {
        // DashScope API 配置（从配置文件读取）
        String url = dashscopeBaseUrl + "/services/aigc/text-generation/generation";

        // 构建消息列表（使用 messages 格式）
        String messagesJson = "[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}]";

        // 构建完整的请求体
        String jsonBody = "{\"model\":\"" + dashscopeModel + "\",\"input\":{\"messages\":" + messagesJson + "},\"parameters\":{\"max_tokens\":100,\"temperature\":0.7,\"top_p\":0.8}}";

        // 发送 HTTP 请求
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + dashscopeApiKey);
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // 解析响应：output.text
                String responseStr = response.toString();
                if (responseStr.contains("\"text\":\"")) {
                    int start = responseStr.indexOf("\"text\":\"") + 8;
                    int end = responseStr.indexOf("\"", start);
                    if (end > start) {
                        return responseStr.substring(start, end).trim();
                    }
                }
            }
        } else {
            // 读取错误响应
            try (BufferedReader errorIn = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((errorLine = errorIn.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                System.err.println("DashScope API 错误 (HTTP " + responseCode + "): " + errorResponse.toString());
            }
        }

        // 如果 API 调用失败，返回默认值
        return "general";
    }

    /**
     * JSON 字符串转义
     *
     * @param text 原始文本
     * @return 转义后的 JSON 字符串
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 基于关键词的意图识别（作为回退方案）
     * 
     * @param message 用户消息
     * @return 意图类型
     */
    private String identifyIntentByKeywords(String message) {
        message = message.toLowerCase();
        
        // 天气相关关键词
        if (message.contains("天气") || message.contains("温度") || message.contains("下雨") ||
            message.contains("晴天") || message.contains("多云") || message.contains("刮风")) {
            return "weather";
        }
        
        // 计算器相关关键词
        if (message.contains("计算") || message.contains("加") || message.contains("减") ||
            message.contains("乘") || message.contains("除") || message.contains("等于") ||
            message.contains("+") || message.contains("-") || message.contains("*") || message.contains("/")) {
            return "calculator";
        }
        
        // 智能查数相关关键词
        if (message.contains("数据") || message.contains("报表") || message.contains("统计") ||
            message.contains("销量") || message.contains("收入") || message.contains("利润")) {
            return "data";
        }
        
        // 知识库相关关键词
        if (message.contains("知识") || message.contains("文档") || message.contains("资料") ||
            message.contains("手册") || message.contains("指南")) {
            return "knowledge";
        }
        
        // 活动策划相关关键词
        if (message.contains("活动") || message.contains("生日") || message.contains("策划") ||
            message.contains("聚会") || message.contains("活动") || message.contains("安排")) {
            return "party";
        }
        
        // 默认意图
        return "general";
    }

    /**
     * 处理天气查询请求
     * 
     * 从用户消息中提取城市名称并查询天气。
     * 如果未指定城市，使用默认城市（济南）。
     * 
     * @param message 用户消息
     * @return 天气信息
     * @throws Exception 异常
     */
    private String handleWeatherRequest(String message) throws Exception {
        // 提取城市名称（简单实现，实际项目中可以使用NLP）
        String city = extractCityName(message);
        
        // 如果未指定城市，使用默认城市
        if (city == null || city.isEmpty()) {
            city = getDefaultCity();
        }
        
        return weatherService.getWeather(city);
    }

    /**
     * 从消息中提取城市名称
     * 
     * @param message 用户消息
     * @return 城市名称
     */
    private String extractCityName(String message) {
        // 简单实现：查找常见城市名称
        String[] cities = {"北京", "上海", "广州", "深圳", "济南", "南京", "杭州", "成都", "武汉", "西安"};
        for (String city : cities) {
            if (message.contains(city)) {
                return city;
            }
        }
        return null;
    }

    /**
     * 获取默认城市
     * 
     * 在实际项目中，这里可以根据用户IP地址或登录信息获取用户所在城市。
     * 目前返回固定默认值：济南。
     * 
     * @return 默认城市名称
     */
    private String getDefaultCity() {
        // 实际项目中可以通过IP地址查询、用户设置等方式获取
        // 这里为了演示，返回固定值
        return "济南";
    }

    /**
     * 处理计算器请求
     * 
     * 从用户消息中提取计算表达式并执行计算。
     * 
     * @param message 用户消息
     * @return 计算结果
     */
    private String handleCalculatorRequest(String message) {
        // 简单实现：处理基本的数学表达式
        try {
            // 这里可以添加更复杂的表达式解析
            if (message.contains("+") || message.contains("-") || message.contains("*") || message.contains("/")) {
                // 示例：处理 "10加5" 这样的表达式
                if (message.contains("加")) {
                    String[] parts = message.split("加");
                    if (parts.length == 2) {
                        double a = Double.parseDouble(parts[0].replaceAll("\\D", ""));
                        double b = Double.parseDouble(parts[1].replaceAll("\\D", ""));
                        return calculatorTool.add(a, b);
                    }
                }
                // 可以添加更多操作符的处理
            }
            return "请提供具体的计算表达式，例如：10加5";
        } catch (Exception e) {
            return "计算失败：" + e.getMessage();
        }
    }

    /**
     * 处理智能查数请求
     * 
     * @param message 用户消息
     * @return 查数结果
     */
    private String handleDataRequest(String message) {
        // 模拟智能查数功能
        return "智能查数功能正在开发中，敬请期待！";
    }

    /**
     * 处理知识库请求
     * 
     * @param message 用户消息
     * @return 知识库查询结果
     */
    private String handleKnowledgeRequest(String message) {
        // 模拟知识库查询功能
        return "知识库功能正在开发中，敬请期待！";
    }

    /**
     * 处理活动策划请求
     * 
     * @param message 用户消息
     * @return 活动策划方案
     */
    private String handlePartyRequest(String message) {
        return partyPlanningAgent.planParty(message);
    }

    /**
     * 处理通用请求
     * 
     * @param message 用户消息
     * @return 通用回答
     */
    private String handleGeneralRequest(String message) {
        // 模拟大模型回答
        return "这是一个通用回答。您的问题是：" + message + "\n\n如果您需要查询天气、进行计算、查看数据或查询知识库，请明确说明您的需求。";
    }

    /**
     * 创建统一格式的响应
     * 
     * @param status 状态：success 或 error
     * @param content 内容
     * @return 统一格式的响应
     */
    private Map<String, Object> createResponse(String status, String content) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("content", content);
        return response;
    }

    /**
     * 查询城市天气
     * 
     * 该接口直接调用WeatherService查询天气信息，
     * 并将查询记录存储到记忆系统中。
     * 
     * @param request 请求体，包含city字段（城市名称）
     * @return 天气信息字符串
     */
    @PostMapping(value = "/weather", produces = "text/plain;charset=utf-8")
    public String getWeather(@RequestBody Map<String, String> request) {
        // 获取城市名称
        String city = request.get("city");
        
        // 参数校验
        if (city == null || city.isEmpty()) {
            return "错误：城市名称不能为空";
        }

        try {
            // 调用WeatherService查询天气
            String weather = weatherService.getWeather(city);
            
            // 存储到记忆中
            memoryService.storeMessage("user", "查询城市天气：" + city);
            memoryService.storeMessage("assistant", "天气信息：" + weather);
            
            return weather;
        } catch (Exception e) {
            return "查询天气失败：" + e.getMessage();
        }
    }

    /**
     * 创建Agent
     * 
     * 该接口创建一个新的Agent实例，并注册到AgentManagerService中。
     * 目前返回提示信息，因为Agent创建功能需要根据具体业务需求实现。
     * 
     * @param request 请求体，包含name（Agent名称）和sysPrompt（系统提示词）字段
     * @return 创建结果，包含提示信息
     */
    @PostMapping("/agents")
    public String createAgent(@RequestBody Map<String, Object> request) {
        // 获取请求参数
        String agentName = (String) request.get("name");
        String sysPrompt = (String) request.get("sysPrompt");
        
        // 参数校验
        if (agentName == null || agentName.isEmpty()) {
            return "错误：Agent名称不能为空";
        }

        try {
            // 由于WeatherAgent已移除，返回提示信息
            return "Agent创建功能已暂时移除，需要根据具体业务需求重新实现";
        } catch (Exception e) {
            return "创建Agent失败：" + e.getMessage();
        }
    }

    /**
     * 获取Agent信息
     * 
     * 根据Agent ID获取Agent的基本信息。
     * 
     * @param agentId Agent唯一标识符
     * @return Agent信息字符串
     */
    @GetMapping("/agents/{agentId}")
    public String getAgent(@PathVariable String agentId) {
        try {
            // 从AgentManagerService获取Agent
            Object agent = agentManagerService.getAgent(agentId);
            
            if (agent != null) {
                return "Agent信息：" + agentId;
            } else {
                return "Agent不存在";
            }
        } catch (Exception e) {
            return "获取Agent失败：" + e.getMessage();
        }
    }

    /**
     * 执行Agent任务
     * 
     * 该接口让指定的Agent执行特定任务，
     * 并将执行记录存储到记忆系统中。
     * 
     * @param agentId Agent唯一标识符
     * @param request 请求体，包含task字段（任务内容）
     * @return 任务执行结果
     */
    @PostMapping("/agents/{agentId}/execute")
    public String executeAgent(@PathVariable String agentId, @RequestBody Map<String, String> request) {
        // 获取任务内容
        String task = request.get("task");
        
        // 参数校验
        if (task == null || task.isEmpty()) {
            return "错误：任务内容不能为空";
        }

        try {
            // 获取Agent实例
            Object agent = agentManagerService.getAgent(agentId);
            
            if (agent != null) {
                // 执行任务（目前返回模拟结果）
                String result = "执行任务：" + task;
                
                // 存储到记忆中
                memoryService.storeMessage("user", task);
                memoryService.storeMessage("assistant", result);
                
                return result;
            } else {
                return "Agent不存在";
            }
        } catch (Exception e) {
            return "执行Agent任务失败：" + e.getMessage();
        }
    }

    /**
     * 列出所有Agent
     * 
     * 获取当前已注册的所有Agent ID列表。
     * 
     * @return Agent ID列表
     */
    @GetMapping("/agents")
    public List<String> listAgents() {
        return agentManagerService.listAgents();
    }

    /**
     * 删除Agent
     * 
     * 根据Agent ID删除指定的Agent实例。
     * 
     * @param agentId Agent唯一标识符
     * @return 删除结果
     */
    @DeleteMapping("/agents/{agentId}")
    public String deleteAgent(@PathVariable String agentId) {
        try {
            // 从AgentManagerService删除Agent
            boolean deleted = agentManagerService.deleteAgent(agentId);
            
            if (deleted) {
                return "Agent删除成功";
            } else {
                return "Agent不存在";
            }
        } catch (Exception e) {
            return "删除Agent失败：" + e.getMessage();
        }
    }

    /**
     * 获取记忆内容
     * 
     * 获取当前记忆系统中存储的所有对话历史。
     * 
     * @return 记忆内容字符串
     */
    @GetMapping("/memory")
    public String getMemory() {
        return memoryService.getAllMessages();
    }

    /**
     * 清除记忆
     * 
     * 清空当前记忆系统中的所有对话历史。
     * 
     * @return 操作结果
     */
    @PostMapping("/memory/clear")
    public String clearMemory() {
        memoryService.clearMemory();
        return "记忆已清除";
    }

    /**
     * 注册技能
     * 
     * 该接口将指定的工具注册为技能，
     * 使其可以被Agent调用。
     * 
     * @param request 请求体，包含skillName（技能名称）字段
     * @return 注册结果
     */
    @PostMapping("/skills/register")
    public String registerSkill(@RequestBody Map<String, String> request) {
        String skillName = request.get("skillName");
        
        if (skillName == null || skillName.isEmpty()) {
            return "错误：技能名称不能为空";
        }

        try {
            // 根据技能名称获取对应的工具实例
            Object toolInstance = getToolByName(skillName);
            
            if (toolInstance != null) {
                // 注册技能
                skillService.registerSkill(skillName, toolInstance);
                return "技能注册成功：" + skillName;
            } else {
                return "错误：未找到指定的技能：" + skillName;
            }
        } catch (Exception e) {
            return "注册技能失败：" + e.getMessage();
        }
    }

    /**
     * 列出所有已注册的技能
     * 
     * 获取当前已注册的所有技能列表。
     * 
     * @return 技能名称列表
     */
    @GetMapping("/skills")
    public Map<String, Object> listSkills() {
        Map<String, Object> result = new HashMap<>();
        result.put("skills", skillService.listSkills().keySet());
        result.put("count", skillService.listSkills().size());
        return result;
    }

    /**
     * 卸载技能
     * 
     * 从技能集合中移除指定的技能。
     * 
     * @param skillName 技能名称
     * @return 操作结果
     */
    @DeleteMapping("/skills/{skillName}")
    public String unregisterSkill(@PathVariable String skillName) {
        try {
            skillService.unregisterSkill(skillName);
            return "技能卸载成功：" + skillName;
        } catch (Exception e) {
            return "卸载技能失败：" + e.getMessage();
        }
    }

    /**
     * 执行计算器技能
     * 
     * 该接口演示如何直接调用计算器工具进行数学运算。
     * 
     * @param request 请求体，包含operation（操作类型）、a（第一个数）、b（第二个数）字段
     * @return 计算结果
     */
    @PostMapping("/skills/calculator")
    public String executeCalculator(@RequestBody Map<String, Object> request) {
        String operation = (String) request.get("operation");
        Double a = ((Number) request.get("a")).doubleValue();
        Double b = ((Number) request.get("b")).doubleValue();

        if (operation == null || operation.isEmpty()) {
            return "错误：操作类型不能为空";
        }

        if (a == null || b == null) {
            return "错误：参数a和b不能为空";
        }

        try {
            String result;
            switch (operation.toLowerCase()) {
                case "add":
                    result = calculatorTool.add(a, b);
                    break;
                case "subtract":
                    result = calculatorTool.subtract(a, b);
                    break;
                case "multiply":
                    result = calculatorTool.multiply(a, b);
                    break;
                case "divide":
                    result = calculatorTool.divide(a, b);
                    break;
                case "percentage":
                    result = calculatorTool.percentage(a, b);
                    break;
                default:
                    return "错误：不支持的操作类型：" + operation;
            }
            
            // 存储到记忆中
            memoryService.storeMessage("user", String.format("计算：%s(%.2f, %.2f)", operation, a, b));
            memoryService.storeMessage("assistant", result);
            
            return result;
        } catch (Exception e) {
            return "执行计算失败：" + e.getMessage();
        }
    }

    /**
     * 根据技能名称获取对应的工具实例
     * 
     * 该方法根据技能名称返回对应的工具实例。
     * 目前支持：calculator, budget, activity
     * 
     * @param skillName 技能名称
     * @return 工具实例对象，如果不存在则返回null
     */
    private Object getToolByName(String skillName) {
        switch (skillName.toLowerCase()) {
            case "calculator":
                return calculatorTool;
            case "budget":
                return budgetTool;
            case "activity":
                return activityTool;
            default:
                return null;
        }
    }
}
