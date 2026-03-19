package com.corporate.finance.ai.tool;

import io.agentscope.core.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 计算器工具类
 * 
 * 该类提供基本的数学计算功能，包括加减乘除运算。
 * 通过@Tool注解标记为AgentScope工具，可被ReActAgent调用。
 * 
 * 主要功能：
 * - 两数相加
 * - 两数相减
 * - 两数相乘
 * - 两数相除
 * 
 * 使用场景：
 * - Agent需要进行数学计算时自动调用
 * - 用户请求计算任务时使用
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Component
public class CalculatorTool {

    /**
     * 两数相加
     * 
     * 该方法通过@Tool注解标记为AgentScope工具，
     * 可被ReActAgent自动调用进行加法运算。
     * 
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 两数之和的字符串表示
     */
    @Tool(name = "add", description = "计算两个数字的和")
    public String add(double a, double b) {
        double result = a + b;
        return String.format("%.2f + %.2f = %.2f", a, b, result);
    }

    /**
     * 两数相减
     * 
     * 该方法通过@Tool注解标记为AgentScope工具，
     * 可被ReActAgent自动调用进行减法运算。
     * 
     * @param a 被减数
     * @param b 减数
     * @return 两数之差的字符串表示
     */
    @Tool(name = "subtract", description = "计算两个数字的差")
    public String subtract(double a, double b) {
        double result = a - b;
        return String.format("%.2f - %.2f = %.2f", a, b, result);
    }

    /**
     * 两数相乘
     * 
     * 该方法通过@Tool注解标记为AgentScope工具，
     * 可被ReActAgent自动调用进行乘法运算。
     * 
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 两数之积的字符串表示
     */
    @Tool(name = "multiply", description = "计算两个数字的积")
    public String multiply(double a, double b) {
        double result = a * b;
        return String.format("%.2f × %.2f = %.2f", a, b, result);
    }

    /**
     * 两数相除
     * 
     * 该方法通过@Tool注解标记为AgentScope工具，
     * 可被ReActAgent自动调用进行除法运算。
     * 包含除零检查。
     * 
     * @param a 被除数
     * @param b 除数
     * @return 两数之商的字符串表示，如果除数为0则返回错误信息
     */
    @Tool(name = "divide", description = "计算两个数字的商")
    public String divide(double a, double b) {
        if (b == 0) {
            return "错误：除数不能为零";
        }
        double result = a / b;
        return String.format("%.2f ÷ %.2f = %.2f", a, b, result);
    }

    /**
     * 计算百分比
     * 
     * 该方法计算一个数占另一个数的百分比。
     * 
     * @param part 部分值
     * @param total 总值
     * @return 百分比字符串表示
     */
    @Tool(name = "percentage", description = "计算部分占总体的百分比")
    public String percentage(double part, double total) {
        if (total == 0) {
            return "错误：总值不能为零";
        }
        double result = (part / total) * 100;
        return String.format("%.2f 占 %.2f 的百分比为：%.2f%%", part, total, result);
    }
}
