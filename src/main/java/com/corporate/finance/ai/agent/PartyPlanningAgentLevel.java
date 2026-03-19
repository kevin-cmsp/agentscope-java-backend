package com.corporate.finance.ai.agent;

import com.corporate.finance.ai.service.ModelServiceFactory;
import com.corporate.finance.ai.service.WeatherService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 派对策划层级式多智能体协作 Agent
 * 
 * 该 Agent 是层级式多智能体协作系统的核心协调者（Manager Agent），
 * 通过协调 BudgetAgent 和 ActivityAgent 两个子 Agent（Worker Agent），
 * 生成完整的派对策划方案。
 * 
 * 层级式协作架构：
 * ┌─────────────────────────────┐
 * │  PartyPlanningAgentLevel    │  ← Manager Agent（协调者）
 * │  (层级式协作核心)            │
 * └──────────┬──────────────────┘
 *            │ 协调调用
 *     ┌──────┴──────┐
 *     ▼             ▼
 * ┌─────────   ┌──────────┐
 * │BudgetAgent│   │ActivityAgent│  ← Worker Agents（执行者）
 * │(ReAct)   │   │(ReAct)    │
 * └────┬────┘   └────┬─────┘
 *      │             │
 *      ▼             ▼
 * ┌─────────┐   ┌──────────┐
 * │BudgetTool│   │ActivityTool│  ← Tools
 * └─────────┘   └──────────┘
 * 
 * 工作流程：
 * 1. 接收用户需求（如："帮我策划一个 10 人的生日派对，预算 5000 元，地点在北京"）
 * 2. Manager Agent 解析需求，提取关键信息（地点、日期、预算、人数）
 * 3. Manager Agent 查询天气信息（调用 WeatherService）
 * 4. Manager Agent 调用 BudgetAgent（子 Agent）制定预算方案
 *    - BudgetAgent 内部进行 ReAct 推理和工具调用
 * 5. Manager Agent 调用 ActivityAgent（子 Agent）制定活动方案
 *    - ActivityAgent 内部进行 ReAct 推理和工具调用
 * 6. Manager Agent 整合所有信息，生成最终策划方案
 * 
 * 主要功能：
 * - 层级式多智能体协作调度
 * - 任务分解和子 Agent 协调
 * - 信息整合和方案生成
 * - 天气因素考虑
 * 
 * 技术特性：
 * - Manager-Worker 架构模式
 * - 子 Agent 独立运行（ReAct 模式）
 * - 支持并发调用子 Agent（可选）
 * - 完整的错误处理和日志记录
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0 (层级式协作版本)
 */
@Component
public class PartyPlanningAgentLevel {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(PartyPlanningAgentLevel.class);

    /**
     * 模型服务工厂
     * 用于获取默认的大模型服务实例
     */
    @Autowired
    private ModelServiceFactory modelServiceFactory;

    /**
     * 预算管理子 Agent
     * 负责制定预算分配方案（ReAct Agent）
     */
    @Autowired
    private BudgetAgent budgetAgent;

    /**
     * 活动安排子 Agent
     * 负责制定活动安排方案（ReAct Agent）
     */
    @Autowired
    private ActivityAgent activityAgent;

    /**
     * 天气查询服务
     * 用于查询指定地点的天气信息
     */
    @Autowired
    private WeatherService weatherService;

    /**
     * JSON 对象映射器
     * 用于解析大模型返回的 JSON 响应
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化方法
     * 
     * 验证所有子 Agent 是否已正确初始化。
     */
    @PostConstruct
    public void init() {
        logger.info("正在初始化 PartyPlanningAgentLevel（层级式协作）...");
        
        if (budgetAgent == null) {
            logger.error("BudgetAgent 未注入，请检查 Spring 配置");
            throw new RuntimeException("BudgetAgent 注入失败");
        }
        
        if (activityAgent == null) {
            logger.error("ActivityAgent 未注入，请检查 Spring 配置");
            throw new RuntimeException("ActivityAgent 注入失败");
        }
        
        logger.info("✅ PartyPlanningAgentLevel 初始化成功！");
        logger.info("已连接的子 Agent：[BudgetAgent(ReAct), ActivityAgent(ReAct)]");
    }

    /**
     * 策划派对
     * 
     * 该方法接收用户的派对需求，通过层级式多智能体协作生成完整的策划方案。
     * 
     * Manager Agent 工作流程：
     * 1. 解析用户需求，提取关键信息
     * 2. 查询指定地点的天气信息
     * 3. 调用 BudgetAgent（子 Agent）制定预算方案
     * 4. 调用 ActivityAgent（子 Agent）制定活动方案
     * 5. 整合所有信息，生成最终策划方案
     * 
     * 子 Agent 执行流程（ReAct 模式）：
     * - BudgetAgent: 接收预算和人数 → ReAct 推理 → 调用 BudgetTool → 生成预算方案
     * - ActivityAgent: 接收人数和天气 → ReAct 推理 → 调用 ActivityTool → 生成活动方案
     * 
     * @param request 用户请求字符串，包含地点、日期、预算、人数等信息
     * @return 完整的派对策划方案
     */
    public String planParty(String request) {
        logger.info("🎉 开始策划活动（层级式协作模式）：{}", request);

        try {
            // 1. Manager Agent 解析用户需求
            logger.info("📋 步骤 1/5: 解析用户需求...");
            Map<String, String> requirements = parseRequirements(request);

            // 2. 提取关键信息
            String location = requirements.getOrDefault("location", "济南");
            String date = requirements.getOrDefault("date", "今天");
            int budget = Integer.parseInt(requirements.getOrDefault("budget", "1000"));
            int guests = Integer.parseInt(requirements.getOrDefault("guests", "10"));

            logger.info("✅ 需求解析完成：地点={}, 日期={}, 预算={}元，人数={}", 
                    location, date, budget, guests);

            // 3. Manager Agent 查询天气
            logger.info("🌤️  步骤 2/5: 查询 {} 的天气...", location);
            String weatherInfo = checkWeather(location, date);
            logger.info("✅ 天气信息：{}", weatherInfo);

            // 4. Manager Agent 调用 BudgetAgent（子 Agent）
            logger.info("💰 步骤 3/5: 调用 BudgetAgent 制定预算方案...");
            String budgetPlan = budgetAgent.planBudget(budget, guests);
            logger.info("✅ 预算方案生成完成，长度：{} 字符", budgetPlan.length());

            // 5. Manager Agent 调用 ActivityAgent（子 Agent）
            logger.info("🎮 步骤 4/5: 调用 ActivityAgent 制定活动方案...");
            String activityPlan = activityAgent.planActivities(guests, weatherInfo);
            logger.info("✅ 活动方案生成完成，长度：{} 字符", activityPlan.length());

            // 6. Manager Agent 整合所有信息，生成最终方案
            logger.info("📝 步骤 5/5: 整合信息，生成最终策划方案...");
            String finalPlan = generatePartyPlan(requirements, weatherInfo, budgetPlan, activityPlan);
            
            logger.info("✅ 派对策划完成（层级式协作），方案总长度：{} 字符", finalPlan.length());
            return finalPlan;

        } catch (Exception e) {
            logger.error("❌ 派对策划失败：{}", e.getMessage(), e);
            return "派对策划失败：" + e.getMessage();
        }
    }

    /**
     * 解析用户需求
     *
     * 从用户请求中提取关键信息，包括地点、日期、预算、人数等。
     * 使用大模型进行自然语言理解和信息提取，并解析返回的 JSON 响应。
     *
     * @param request 用户请求字符串
     * @return 包含关键信息的 Map 对象
     */
    private Map<String, String> parseRequirements(String request) {
        String prompt = "请从以下用户需求中提取关键信息，包括：地点、日期、预算、人数。返回 JSON 格式：\n" + request;
        String response = modelServiceFactory.getDefaultModelService().generate("你是一个信息提取助手，擅长从自然语言中提取结构化信息。", prompt);

        // 使用 Jackson 解析 JSON 响应
        Map<String, String> requirements = new java.util.HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);

            // 提取地点字段
            if (rootNode.has("地点")) {
                String location = rootNode.get("地点").asText();
                if (location != null && !location.isEmpty()) {
                    requirements.put("location", location);
                }
            }

            // 提取日期字段
            if (rootNode.has("日期")) {
                String date = rootNode.get("日期").asText();
                if (date != null && !date.isEmpty()&& !"null".equals(date)) {
                    requirements.put("date", date);
                }else {
                    requirements.put("date", "今天");
                }
            }

            // 提取预算字段（移除"元"等单位）
            if (rootNode.has("预算")) {
                String budget = rootNode.get("预算").asText();
                if (budget != null && !budget.isEmpty()) {
                    // 移除货币单位和非数字字符
                    String cleanBudget = budget.replaceAll("[^0-9]", "");
                    if (!cleanBudget.isEmpty()) {
                        requirements.put("budget", cleanBudget);
                    }
                }
            }

            // 提取人数字段（移除"人"等单位）
            if (rootNode.has("人数")) {
                String guests = rootNode.get("人数").asText();
                if (guests != null && !guests.isEmpty()) {
                    // 移除人数单位和非数字字符
                    String cleanGuests = guests.replaceAll("[^0-9]", "");
                    if (!cleanGuests.isEmpty()) {
                        requirements.put("guests", cleanGuests);
                    }
                }
            }

            logger.info("解析用户需求成功：{}", requirements);
        } catch (Exception e) {
            logger.error("解析 JSON 响应失败：{}", e.getMessage());
            logger.debug("原始响应：{}", response);

            // 解析失败时使用默认值
            requirements.put("location", "济南");
            requirements.put("date", "今天");
            requirements.put("budget", "1000");
            requirements.put("guests", "10");
        }

        return requirements;
    }

    /**
     * 查询天气
     * 
     * 调用 WeatherService 查询指定地点的天气信息。
     * 包含异常处理，确保天气查询失败不影响整体流程。
     * 
     * @param location 地点名称
     * @param date 日期（目前未使用）
     * @return 天气信息字符串
     */
    private String checkWeather(String location, String date) {
        try {
            logger.debug("查询 {} 的天气", location);
            return weatherService.getWeather(location);
        } catch (IOException e) {
            logger.error("查询天气失败：{}", e.getMessage());
            return "天气查询失败，请稍后再试。";
        }
    }

    /**
     * 生成派对方案
     * 
     * 整合所有信息，生成完整的派对策划方案。
     * 包括派对主题、时间安排、场地布置、活动流程、餐饮安排、预算明细等。
     * 
     * @param requirements 用户需求
     * @param weatherInfo 天气信息
     * @param budgetPlan 预算规划
     * @param activityPlan 活动安排
     * @return 完整的派对策划方案
     */
    private String generatePartyPlan(Map<String, String> requirements, String weatherInfo, 
                                     String budgetPlan, String activityPlan) {
        StringBuilder plan = new StringBuilder();
        
        plan.append("🎉 **活动策划方案** 🎉\n\n");
        plan.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        
        // 一、基本信息
        plan.append("📋 **一、基本信息**\n");
        plan.append("- 地点：").append(requirements.get("location")).append("\n");
        plan.append("- 日期：").append(requirements.get("date")).append("\n");
        plan.append("- 预算：").append(requirements.get("budget")).append("元\n");
        plan.append("- 人数：").append(requirements.get("guests")).append("人\n\n");
        
        // 二、天气情况
        plan.append("🌤️ **二、天气情况**\n");
        plan.append(weatherInfo).append("\n\n");
        
        // 三、预算分配
        plan.append("💰 **三、预算分配**\n");
        plan.append(budgetPlan).append("\n\n");
        
        // 四、活动安排
        plan.append("🎮 **四、活动安排**\n");
        plan.append(activityPlan).append("\n\n");
        
        // 五、综合建议
        plan.append("💡 **五、综合建议**\n");
        
        // 根据天气给出建议
        if (weatherInfo.contains("雨") || weatherInfo.contains("雪")) {
            plan.append("1. ⚠️ 由于天气原因，建议准备雨具或调整活动为室内\n");
        } else {
            plan.append("1. ✅ 天气良好，可安排室内外结合的活动\n");
        }
        
        // 根据人数给出建议
        int guests = Integer.parseInt(requirements.get("guests"));
        if (guests <= 10) {
            plan.append("2. 👥 小型聚会，建议选择温馨的室内场所\n");
        } else if (guests <= 50) {
            plan.append("2. 👥 中型活动，建议租用专业活动场地\n");
        } else {
            plan.append("2. 👥 大型活动，建议提前预订大型场馆并做好人员分流\n");
        }
        
        // 根据预算给出建议
        int budget = Integer.parseInt(requirements.get("budget"));
        int perPerson = budget / guests;
        plan.append("3. 💰 人均预算：").append(perPerson).append("元，建议合理分配各项支出\n\n");
        
        // 结束语
        plan.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        plan.append("✨ 祝您活动圆满成功！✨\n");
        
        return plan.toString();
    }
}
