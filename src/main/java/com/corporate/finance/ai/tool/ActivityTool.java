package com.corporate.finance.ai.tool;

import io.agentscope.core.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 活动安排工具类
 * 
 * 该类提供基于人数和天气情况的活动安排功能，
 * 为活动生成合适的活动建议。
 * 
 * 主要功能：
 * - 根据天气情况推荐室内外活动
 * - 根据人数规模设计不同类型的游戏
 * - 提供完整的活动流程建议
 * 
 * 使用场景：
 * - PartyPlanningAgent 调用生成活动安排
 * - 直接作为技能被 Agent 调用
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Component
public class ActivityTool {

    /**
     * 计划活动安排
     * 
     * 根据参与人数和天气情况，生成适合的活动安排方案。
     * 该方法通过@Tool 注解标记为 AgentScope 工具，可被 ReActAgent 自动调用。
     * 
     * @param guests 参与人数
     * @param weather 天气信息字符串
     * @return 活动安排方案的详细描述
     */
    @Tool(name = "plan_activities", description = "根据人数和天气安排活动")
    public String planActivities(int guests, String weather) {
        // 根据人数和天气情况生成活动安排
        StringBuilder activities = new StringBuilder();
        activities.append("活动安排方案：\n");
        
        if (weather.contains("雨") || weather.contains("雪")) {
            activities.append("由于天气原因，安排室内活动：\n");
        } else {
            activities.append("天气良好，可安排室内外结合的活动：\n");
        }
        
        activities.append("1. 开场活动：签到、合影\n");
        
        if (guests <= 10) {
            activities.append("2. 主体活动：桌游、卡拉 OK\n");
            activities.append("3. 互动游戏：真心话大冒险、谁是卧底\n");
        } else {
            activities.append("2. 主体活动：分组游戏、团队挑战\n");
            activities.append("3. 互动游戏：接力赛、猜词游戏\n");
        }
        
        activities.append("4. 活动仪式：剪彩、开香槟、切蛋糕\n");
        activities.append("5. 结束活动：交换礼物、合影留念\n");
        
        return activities.toString();
    }
}
