package com.corporate.finance.ai.tool;

import io.agentscope.core.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 预算计算工具类
 * 
 * 该类提供活动预算分配的计算功能，
 * 根据总预算按照固定比例分配各项费用。
 * 
 * 主要功能：
 * - 场地租赁预算分配（30%）
 * - 餐饮费用预算分配（40%）
 * - 装饰布置预算分配（15%）
 * - 活动物料预算分配（10%）
 * - 其他费用预算分配（5%）
 * 
 * 使用场景：
 * - PartyPlanningAgent 调用生成预算方案
 * - 直接作为技能被 Agent 调用
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Component
public class BudgetTool {

    /**
     * 计算预算分配
     * 
     * 根据总预算和参与人数，按照预设比例分配各项费用。
     * 该方法通过@Tool 注解标记为 AgentScope 工具，可被 ReActAgent 自动调用。
     * 
     * @param totalBudget 总预算（单位：元）
     * @param guests 参与人数（目前未使用，预留扩展）
     * @return 预算分配方案的详细描述
     */
    @Tool(name = "calculate_budget", description = "计算活动预算分配")
    public String calculateBudget(int totalBudget, int guests) {
        // 简单的预算分配逻辑
        int venueBudget = totalBudget * 30 / 100;
        int foodBudget = totalBudget * 40 / 100;
        int decorBudget = totalBudget * 15 / 100;
        int activityBudget = totalBudget * 10 / 100;
        int otherBudget = totalBudget * 5 / 100;

        return String.format("预算分配方案：\n" +
                "总预算：%d元\n" +
                "场地租赁：%d元 (30%%)\n" +
                "餐饮费用：%d元 (40%%)\n" +
                "装饰布置：%d元 (15%%)\n" +
                "活动物料：%d元 (10%%)\n" +
                "其他费用：%d元 (5%%)",
                totalBudget, venueBudget, foodBudget, decorBudget, activityBudget, otherBudget);
    }
}
