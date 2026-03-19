package com.corporate.finance.ai.tool;

import com.corporate.finance.ai.service.WeatherService;
import io.agentscope.core.tool.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 天气查询工具类
 *
 * 将 WeatherService 封装为符合 AgentScope 规范的 Tool，
 * 使 ReAct Agent 能够自主调用。
 */
@Component
public class WeatherTool {

    private final WeatherService weatherService;

    public WeatherTool(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * 查询城市天气
     *
     * @param city 城市名称
     * @return 天气信息字符串
     * @throws IOException 当天气查询失败时抛出
     */
    @Tool(name = "get_weather", description = "查询指定城市的天气信息")
    public String getWeather(@io.agentscope.core.tool.ToolParam(name = "city", description = "城市名称") String city) throws IOException {
        return weatherService.getWeather(city);
    }
}
