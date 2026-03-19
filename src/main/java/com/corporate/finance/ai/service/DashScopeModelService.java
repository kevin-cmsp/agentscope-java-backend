package com.corporate.finance.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * DashScope 大模型服务实现类
 * 
 * 该类实现了 ModelService 接口，提供对阿里云 DashScope（通义千问）大模型的访问。
 * 支持文本生成、对话等功能。
 * 
 * 主要功能：
 * - 调用 DashScope API 生成文本响应
 * - 支持系统提示词和用户提示词
 * - 处理 JSON 格式的请求和响应
 * 
 * 技术实现：
 * - 使用 HttpURLConnection 发送 HTTP 请求
 * - 使用 Jackson ObjectMapper 解析 JSON 响应
 * - 包含异常处理和错误日志
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service("dashscope")
public class DashScopeModelService implements ModelService {

    /**
     * DashScope API Key
     * 从配置文件中读取
     */
    @Value("${model.services.dashscope.api-key}")
    private String apiKey;

    /**
     * DashScope Base URL
     * 从配置文件中读取
     */
    @Value("${model.services.dashscope.base-url}")
    private String baseUrl;

    /**
     * DashScope 模型名称
     * 从配置文件中读取
     */
    @Value("${model.services.dashscope.model}")
    private String model;

    /**
     * JSON 对象映射器
     * 用于解析 JSON 响应
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化模型服务
     * 
     * 该方法在获取模型服务时自动调用，
     * 用于完成模型服务的初始化工作。
     */
    @Override
    public void init() {
        // 初始化逻辑
    }

    /**
     * 调用模型生成响应
     * 
     * 该方法接收用户提示词，调用大模型生成响应。
     * 使用默认的系统提示词。
     * 
     * @param prompt 用户提示词
     * @return 模型生成的响应文本
     */
    @Override
    public String generate(String prompt) {
        return generate("你是一个智能助手，能够回答各种问题。", prompt);
    }

    /**
     * 调用模型生成响应，带有系统提示
     * 
     * 该方法接收系统提示词和用户提示词，
     * 调用大模型生成响应。
     * 
     * 系统提示词用于定义模型的角色和行为，
     * 用户提示词是具体的任务或问题。
     * 
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return 模型生成的响应文本
     */
    @Override
    public String generate(String systemPrompt, String userPrompt) {
        try {
            String url = baseUrl + "/services/aigc/text-generation/generation";

            // 构建消息列表（使用 messages 格式）
            String messagesJson = "[{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},{\"role\":\"user\",\"content\":\"" + escapeJson(userPrompt) + "\"}]";

            // 构建完整的请求体
            String jsonBody = "{\"model\":\"" + model + "\",\"input\":{\"messages\":" + messagesJson + "},\"parameters\":{\"max_tokens\":1000,\"temperature\":0.7,\"top_p\":0.8}}";

            URI obj = new URI(url);
            HttpURLConnection con = (HttpURLConnection) obj.toURL().openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // 使用 Jackson 正确解析 JSON 响应
                    String responseStr = response.toString();
                    return extractTextFromResponse(responseStr);
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
                    System.err.println("DashScope API 错误 (HTTP " + responseCode + "): " + errorResponse);
                }
            }

            return "生成响应失败";
        } catch (Exception e) {
            e.printStackTrace();
            return "生成响应失败：" + e.getMessage();
        }
    }

    /**
     * 从 DashScope API 响应中提取 text 字段的值
     * 
     * 响应格式示例：
     * {"output":{"finish_reason":"stop","text":"{\n  \"地点\": \"济南\",\n  \"日期\": null,\n  \"预算\": 1000,\n  \"人数\": 10\n}"},"usage":{...}}
     * 
     * @param jsonResponse API 返回的 JSON 响应字符串
     * @return 提取的 text 字段值
     * @throws Exception 解析失败时抛出异常
     */
    private String extractTextFromResponse(String jsonResponse) throws Exception {
        try {
            // 使用 Jackson 解析 JSON
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // 导航到 output.text
            JsonNode outputNode = rootNode.get("output");
            if (outputNode != null && outputNode.has("text")) {
                return outputNode.get("text").asText();
            }
            
            throw new Exception("JSON 响应中未找到 output.text 字段");
        } catch (Exception e) {
            System.err.println("解析 JSON 响应失败：" + e.getMessage());
            System.err.println("原始响应：" + jsonResponse);
            throw e;
        }
    }

    /**
     * 关闭模型服务
     * 
     * 该方法用于释放模型服务占用的资源，
     * 在服务不再使用时调用。
     */
    @Override
    public void close() {
        // 关闭资源
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
}
