package com.corporate.finance.ai.agent;

import com.corporate.finance.ai.service.ModelServiceFactory;
import com.corporate.finance.ai.tool.ActivityTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 活动安排 ReAct Agent
 * 
 * 该 Agent 是基于 AgentScope ReAct 范式实现的活动策划智能体。
 * 通过整合 ActivityTool，让大模型自主决策工具调用，
 * 生成详细的活动流程方案。
 * 
 * ReAct 工作流程：
 * 1. 接收用户需求（如："为 10 人安排活动，天气晴朗"）
 * 2. ReAct 循环开始：
 *    - Thought: 分析人数和天气情况
 *    - Action: 调用 plan_activities 生成活动安排
 *    - Observation: 获取活动方案
 *    - Thought: 可能需要根据天气调整室内外活动
 *    - Action: 再次调用 plan_activities 优化方案
 *    - Observation: 获取优化后的方案
 *    - Thought: 已收集所有信息，生成最终方案
 *    - Final Answer: 输出详细的活动安排
 * 
 * 主要功能：
 * - 基于 ReAct 范式的自主推理和工具调用
 * - 智能化的活动安排和天气适应
 * - 专业的活动策划建议
 * 
 * @author Corporate Finance AI Team
 * @version 2.0.0 (ReAct 版本)
 */
@Component
public class ActivityAgent {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ActivityAgent.class);

    /**
     * 活动安排工具
     */
    private final ActivityTool activityTool;

    /**
     * ReAct Agent 实例
     */
    private ReActAgent reactAgent;

    /**
     * DashScope API Key
     */
    private final String dashscopeApiKey;

    /**
     * DashScope Model
     */
    private final String dashscopeModel;

    /**
     * 构造函数
     */
    public ActivityAgent(
            ActivityTool activityTool,
            @Value("${model.services.dashscope.api-key}") String dashscopeApiKey,
            @Value("${model.services.dashscope.model}") String dashscopeModel) {
        this.activityTool = activityTool;
        this.dashscopeApiKey = dashscopeApiKey;
        this.dashscopeModel = dashscopeModel;
    }

    /**
     * 初始化 ReAct Agent
     */
    @PostConstruct
    public void init() {
        logger.info("正在初始化 Activity ReAct Agent...");

        try {
            // 创建 Toolkit 并注册工具
            Toolkit toolkit = new Toolkit();
            toolkit.registerTool(activityTool);

            // 创建 DashScopeChatModel 实例
            DashScopeChatModel chatModel = DashScopeChatModel.builder()
                    .apiKey(dashscopeApiKey)
                    .modelName(dashscopeModel)
                    .build();

            // 构建 ReActAgent
            this.reactAgent = ReActAgent.builder()
                    .name("ActivityPlanner")
                    .sysPrompt("你是一位专业的活动策划师，擅长为各类聚会设计有趣的活动安排。\n" +
                            "你可以根据用户的需求，调用以下工具：\n" +
                            "1. plan_activities(guests, weather) - 根据人数和天气安排活动\n" +
                            "\n" +
                            "请按照以下步骤进行：\n" +
                            "1. 分析用户提供的参与人数和天气信息\n" +
                            "2. 调用 plan_activities 生成活动安排方案\n" +
                            "3. 根据天气情况调整室内外活动（雨天安排室内活动）\n" +
                            "4. 生成详细、专业的活动流程建议\n" +
                            "\n" +
                            "注意：活动安排要适合参与人数，考虑不同年龄层的需求。")
                    .model(chatModel)
                    .toolkit(toolkit)
                    .build();

            logger.info("Activity ReAct Agent 初始化成功！");
            logger.info("已注册工具：[plan_activities]");

        } catch (Exception e) {
            logger.error("初始化 Activity ReAct Agent 失败：{}", e.getMessage(), e);
            throw new RuntimeException("初始化 ReAct Agent 失败", e);
        }
    }

    /**
     * 计划活动安排
     * 
     * @param guests 参与人数
     * @param weatherInfo 天气信息字符串
     * @return 活动安排方案的详细描述
     */
    public String planActivities(int guests, String weatherInfo) {
        logger.info("🎮 开始活动安排（ReAct 模式）：人数={}, 天气={}", guests, weatherInfo);

        try {
            // 构建用户消息
            String request = String.format("请为一场%d人的集体活动制定活动安排方案。\n\n天气信息：%s\n\n请根据人数和天气情况，设计适合的活动安排。", guests, weatherInfo);
            
            Msg userMsg = Msg.builder()
                    .textContent(request)
                    .build();

            // 调用 ReAct Agent
            Msg response = reactAgent.call(userMsg).block();

            if (response != null && response.getTextContent() != null) {
                String result = response.getTextContent();
                logger.info("✅ 活动安排完成，方案长度：{} 字符", result.length());
                return result;
            } else {
                logger.warn("⚠️ ReAct Agent 返回空响应");
                return "抱歉，活动安排过程中出现了问题，未能生成方案。请稍后重试。";
            }

        } catch (Exception e) {
            logger.error("❌ 活动安排失败：{}", e.getMessage(), e);
            return "活动安排失败：" + e.getMessage();
        }
    }
}
