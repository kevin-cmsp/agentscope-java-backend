package com.corporate.finance.ai.agent;

import com.corporate.finance.ai.service.ModelServiceFactory;
import com.corporate.finance.ai.tool.BudgetTool;
import com.corporate.finance.ai.tool.CalculatorTool;
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
 * 预算管理 ReAct Agent
 * 
 * 该 Agent 是基于 AgentScope ReAct 范式实现的预算管理智能体。
 * 通过整合 BudgetTool 和 CalculatorTool，让大模型自主决策工具调用，
 * 生成专业的预算分配方案。
 * 
 * ReAct 工作流程：
 * 1. 接收用户需求（如："为 10 人的活动制定 5000 元预算方案"）
 * 2. ReAct 循环开始：
 *    - Thought: 分析预算总额和人数
 *    - Action: 调用 calculate_budget 计算预算分配
 *    - Observation: 获取各项费用明细
 *    - Thought: 可能需要计算人均预算
 *    - Action: 调用 divide 计算人均费用
 *    - Observation: 获取人均预算
 *    - Thought: 已收集所有信息，生成最终方案
 *    - Final Answer: 输出详细的预算分配方案
 * 
 * 主要功能：
 * - 基于 ReAct 范式的自主推理和工具调用
 * - 智能预算分配和计算
 * - 专业的财务规划建议
 * 
 * @author Corporate Finance AI Team
 * @version 2.0.0 (ReAct 版本)
 */
@Component
public class BudgetAgent {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(BudgetAgent.class);

    /**
     * 预算计算工具
     */
    private final BudgetTool budgetTool;

    /**
     * 计算器工具
     */
    private final CalculatorTool calculatorTool;

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
    public BudgetAgent(
            BudgetTool budgetTool,
            CalculatorTool calculatorTool,
            @Value("${model.services.dashscope.api-key}") String dashscopeApiKey,
            @Value("${model.services.dashscope.model}") String dashscopeModel) {
        this.budgetTool = budgetTool;
        this.calculatorTool = calculatorTool;
        this.dashscopeApiKey = dashscopeApiKey;
        this.dashscopeModel = dashscopeModel;
    }

    /**
     * 初始化 ReAct Agent
     */
    @PostConstruct
    public void init() {
        logger.info("正在初始化 Budget ReAct Agent...");

        try {
            // 创建 Toolkit 并注册工具
            Toolkit toolkit = new Toolkit();
            toolkit.registerTool(budgetTool);
            toolkit.registerTool(calculatorTool);

            // 创建 DashScopeChatModel 实例
            DashScopeChatModel chatModel = DashScopeChatModel.builder()
                    .apiKey(dashscopeApiKey)
                    .modelName(dashscopeModel)
                    .build();

            // 构建 ReActAgent
            this.reactAgent = ReActAgent.builder()
                    .name("BudgetPlanner")
                    .sysPrompt("你是一位专业的财务规划师，擅长为各类活动制定合理的预算方案。\n" +
                            "你可以根据用户的需求，调用以下工具：\n" +
                            "1. calculate_budget(totalBudget, guests) - 计算预算分配\n" +
                            "2. divide(a, b) - 计算人均费用\n" +
                            "3. percentage(part, total) - 计算百分比\n" +
                            "\n" +
                            "请按照以下步骤进行：\n" +
                            "1. 分析用户的预算总额和人数\n" +
                            "2. 调用 calculate_budget 计算各项费用分配\n" +
                            "3. 根据需要计算人均预算或百分比\n" +
                            "4. 生成详细、专业的预算方案\n" +
                            "\n" +
                            "注意：提供合理的分配比例建议，确保预算使用高效。")
                    .model(chatModel)
                    .toolkit(toolkit)
                    .build();

            logger.info("Budget ReAct Agent 初始化成功！");
            logger.info("已注册工具：[calculate_budget, divide, percentage]");

        } catch (Exception e) {
            logger.error("初始化 Budget ReAct Agent 失败：{}", e.getMessage(), e);
            throw new RuntimeException("初始化 ReAct Agent 失败", e);
        }
    }

    /**
     * 规划预算分配
     * 
     * @param totalBudget 总预算（单位：元）
     * @param guests 参与人数
     * @return 预算分配方案的详细描述
     */
    public String planBudget(int totalBudget, int guests) {
        logger.info("💰 开始预算规划（ReAct 模式）：总预算={}, 人数={}", totalBudget, guests);

        try {
            // 构建用户消息
            String request = String.format("请为%d人的活动制定预算分配方案，总预算为%d元。", guests, totalBudget);
            
            Msg userMsg = Msg.builder()
                    .textContent(request)
                    .build();

            // 调用 ReAct Agent
            Msg response = reactAgent.call(userMsg).block();

            if (response != null && response.getTextContent() != null) {
                String result = response.getTextContent();
                logger.info("✅ 预算规划完成，方案长度：{} 字符", result.length());
                return result;
            } else {
                logger.warn("⚠️ ReAct Agent 返回空响应");
                return "抱歉，预算规划过程中出现了问题，未能生成方案。请稍后重试。";
            }

        } catch (Exception e) {
            logger.error("❌ 预算规划失败：{}", e.getMessage(), e);
            return "预算规划失败：" + e.getMessage();
        }
    }
}
