package com.corporate.finance.ai.agent;

import com.corporate.finance.ai.service.ModelServiceFactory;
import com.corporate.finance.ai.service.WeatherService;
import com.corporate.finance.ai.tool.ActivityTool;
import com.corporate.finance.ai.tool.BudgetTool;
import com.corporate.finance.ai.tool.WeatherTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

/**
 * 活动策划 ReAct Agent
 * 
 * 该 Agent 是基于 AgentScope ReAct 范式实现的多智能体协作系统核心协调者。
 * 通过整合 BudgetTool、ActivityTool 和 WeatherTool，让大模型自主决策工具调用，
 * 生成完整的活动策划方案。
 * 
 * ReAct 工作流程：
 * 1. 接收用户需求（如："帮我策划一个 10 人的活动，预算 5000 元，地点在北京"）
 * 2. ReAct 循环开始：
 *    - Thought: 分析用户需求，识别需要调用的工具
 *    - Action: 调用 WeatherTool 查询北京天气
 *    - Observation: 获取天气信息
 *    - Thought: 需要制定预算方案
 *    - Action: 调用 BudgetTool 计算预算分配
 *    - Observation: 获取预算明细
 *    - Thought: 需要根据人数和天气安排活动
 *    - Action: 调用 ActivityTool 生成活动安排
 *    - Observation: 获取活动方案
 *    - Thought: 已收集所有必要信息，生成最终方案
 *    - Final Answer: 输出完整的活动策划方案
 * 
 * 主要功能：
 * - 基于 ReAct 范式的自主推理和工具调用
 * - 多工具协同完成复杂任务
 * - 智能化的任务分解和执行顺序规划
 * 
 * 技术特性：
 * - 使用 AgentScope Java 的 ReActAgent实现
 * - 支持流式输出思考过程（可选）
 * - 自动记忆管理（对话历史）
 * 
 * @author Corporate Finance AI Team
 * @version 2.0.0 (ReAct 版本)
 */
@Component
public class PartyPlanningAgent {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(PartyPlanningAgent.class);

    /**
     * 模型服务工厂
     * 用于获取默认的大模型服务实例
     */
    private final ModelServiceFactory modelServiceFactory;

    /**
     * 天气查询服务
     * 用于查询指定地点的天气信息
     */
//    private final WeatherService weatherService;
    private final WeatherTool weatherTool;

    /**
     * 预算计算工具
     * 提供确定性的预算分配逻辑
     */
    private final BudgetTool budgetTool;

    /**
     * 活动安排工具
     * 提供基于规则的活动安排逻辑
     */
    private final ActivityTool activityTool;

    /**
     * ReAct Agent 实例
     * 这是核心组件，负责自主推理和工具调用
     */
    private ReActAgent reactAgent;

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

    /**
     * 构造函数
     * 
     * 注入所有必要的依赖，这是 Spring Boot推荐的做法。
     * 
     * @param modelServiceFactory 模型服务工厂
     * @param weatherTool 天气查询服务
     * @param budgetTool 预算计算工具
     * @param activityTool 活动安排工具
     * @param dashscopeApiKey DashScope API Key
     * @param dashscopeBaseUrl DashScope Base URL
     * @param dashscopeModel DashScope Model
     */
    public PartyPlanningAgent(
            ModelServiceFactory modelServiceFactory,
            WeatherTool weatherTool,
            BudgetTool budgetTool,
            ActivityTool activityTool,
            @Value("${model.services.dashscope.api-key}") String dashscopeApiKey,
            @Value("${model.services.dashscope.base-url}") String dashscopeBaseUrl,
            @Value("${model.services.dashscope.model}") String dashscopeModel) {
        this.modelServiceFactory = modelServiceFactory;
        this.weatherTool = weatherTool;
        this.budgetTool = budgetTool;
        this.activityTool = activityTool;
        this.dashscopeApiKey = dashscopeApiKey;
        this.dashscopeBaseUrl = dashscopeBaseUrl;
        this.dashscopeModel = dashscopeModel;
    }

    /**
     * 初始化 ReAct Agent
     * 
     * 在 Bean 初始化后，构建并配置 ReActAgent 实例。
     * 注册所有可用的工具（BudgetTool、ActivityTool、WeatherTool）。
     */
    @PostConstruct
    public void init() {
        logger.info("正在初始化 PartyPlanning ReAct Agent...");

        try {

            // 创建 Toolkit 并注册所有工具
            Toolkit toolkit = new Toolkit();
            toolkit.registerTool(weatherTool);
            toolkit.registerTool(budgetTool);
            toolkit.registerTool(activityTool);

            // 创建 DashScopeChatModel 实例
            DashScopeChatModel chatModel = DashScopeChatModel.builder()
                    .apiKey(dashscopeApiKey)
                    .modelName(dashscopeModel)
                    .build();

            // 构建 ReActAgent
            this.reactAgent = ReActAgent.builder()
                    .name("PartyPlanner")
                    .sysPrompt("你是一个专业的活动策划助手，擅长统筹规划各类聚会活动。\n" +
                            "你可以根据用户的需求，调用以下工具来完成活动策划任务：\n" +
                            "1. get_weather(city) - 查询指定城市的天气信息\n" +
                            "2. calculate_budget(totalBudget, guests) - 根据总预算和人数计算预算分配\n" +
                            "3. plan_activities(guests, weather) - 根据人数和天气安排活动\n" +
                            "\n" +
                            "请按照以下步骤进行推理和行动：\n" +
                            "1. 首先分析用户需求，提取关键信息（地点、日期、预算、人数）\n" +
                            "2. 如果需要，查询天气信息\n" +
                            "3. 计算预算分配方案\n" +
                            "4. 根据人数和天气安排合适的活动\n" +
                            "5. 整合所有信息，生成一份详细、专业的活动策划方案\n" +
                            "\n" +
                            "注意：如果用户没有提供完整信息，请主动询问或使用合理的默认值。")
                    .model(chatModel)
                    .toolkit(toolkit)
                    .build();

            logger.info("PartyPlanning ReAct Agent 初始化成功！");
            logger.info("已注册工具：[get_weather, calculate_budget, plan_activities]");

        } catch (Exception e) {
            logger.error("初始化 PartyPlanning ReAct Agent 失败：{}", e.getMessage(), e);
            throw new RuntimeException("初始化 ReAct Agent 失败", e);
        }
    }

    /**
     * 策划活动
     * 
     * 该方法接收用户的活动需求，通过 ReAct Agent 的自主推理和工具调用，
     * 生成完整的策划方案。
     * 
     * ReAct 执行流程示例：
     * 用户输入："帮我策划一个 10 人的活动，预算 5000 元，地点在北京"
     * 
     * Agent 思考过程：
     * Thought: 用户需要策划活动，我需要先查询北京的天气
     * Action: get_weather(city="北京")
     * Observation: 北京：晴天，温度 25℃
     * 
     * Thought: 现在我知道了天气情况，接下来需要计算预算分配
     * Action: calculate_budget(totalBudget=5000, guests=10)
     * Observation: 预算分配方案：场地租赁 1500 元，餐饮费用 2000 元...
     * 
     * Thought: 我有了预算信息，现在需要根据人数和天气安排活动
     * Action: plan_activities(guests=10, weather="北京：晴天，温度 25℃")
     * Observation: 活动安排方案：开场活动、主体活动、互动游戏...
     * 
     * Thought: 我已经收集了所有必要信息，现在可以生成完整的活动方案了
     * Final Answer: [输出详细的活动策划方案]
     * 
     * @param request 用户请求字符串，包含地点、日期、预算、人数等信息
     * @return 完整的活动策划方案
     */
    public String planParty(String request) {
        logger.info("🎉 开始策划活动（ReAct 模式）：{}", request);

        try {
            // 构建用户消息
            Msg userMsg = Msg.builder()
                    .textContent(request)
                    .build();

            // 调用 ReAct Agent（阻塞等待完成）
            // ReAct Agent 会自动进行多轮推理和工具调用
            Msg response = reactAgent.call(userMsg).block();

            if (response != null && response.getTextContent() != null) {
                String result = response.getTextContent();
                logger.info("✅ 活动策划完成，方案长度：{} 字符", result.length());
                return result;
            } else {
                logger.warn("⚠️ ReAct Agent 返回空响应");
                return "抱歉，活动策划过程中出现了问题，未能生成方案。请稍后重试。";
            }

        } catch (Exception e) {
            logger.error("❌ 活动策划失败：{}", e.getMessage(), e);
            return "活动策划失败：" + e.getMessage();
        }
    }

}
