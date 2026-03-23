package com.corporate.finance.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 记忆压缩服务
 *
 * 当对话历史过长时，调用大模型对历史进行摘要压缩，
 * 避免超出 token 限制，同时保留关键上下文信息。
 *
 * 工作流程：
 * 1. 检测用户记忆是否超过阈值
 * 2. 提取需要压缩的旧消息
 * 3. 调用大模型生成摘要
 * 4. 用摘要替换旧消息，保留最近的消息
 *
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service
public class MemoryCompressService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryCompressService.class);

    private final MemoryService memoryService;

    @Value("${model.services.dashscope.api-key}")
    private String dashscopeApiKey;

    @Value("${model.services.dashscope.base-url}")
    private String dashscopeBaseUrl;

    @Value("${model.services.dashscope.model}")
    private String dashscopeModel;

    public MemoryCompressService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * 检查并执行记忆压缩
     * 如果用户的记忆超过阈值，自动进行压缩
     *
     * @param userId 用户ID
     * @return 是否执行了压缩
     */
    public boolean checkAndCompress(Long userId) {
        if (!memoryService.needsCompression(userId)) {
            return false;
        }

        logger.info("用户 {} 的记忆超过阈值，开始压缩...", userId);

        try {
            // 获取需要压缩的消息文本
            String messagesText = memoryService.getMessagesForCompression(userId);
            if (messagesText == null || messagesText.isEmpty()) {
                return false;
            }

            // 调用大模型生成摘要
            String summary = generateSummary(messagesText);
            if (summary == null || summary.isEmpty()) {
                logger.warn("用户 {} 的记忆压缩失败：摘要生成为空", userId);
                return false;
            }

            // 执行压缩
            memoryService.compressMemory(userId, summary);
            logger.info("用户 {} 的记忆压缩完成", userId);
            return true;

        } catch (Exception e) {
            logger.error("用户 {} 的记忆压缩失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 调用大模型生成对话摘要
     */
    private String generateSummary(String conversationText) {
        try {
            String prompt = "你是一个对话摘要助手。请对以下对话历史进行精炼摘要，保留关键信息。\n\n" +
                    "要求：\n" +
                    "1. 提取用户的主要问题和需求\n" +
                    "2. 记录重要的回答结论\n" +
                    "3. 保留用户的偏好和习惯\n" +
                    "4. 摘要控制在 300 字以内\n" +
                    "5. 使用简洁的条目式格式\n\n" +
                    "对话历史：\n" + conversationText + "\n\n" +
                    "摘要：";

            return callDashScopeModel(prompt);

        } catch (Exception e) {
            logger.error("生成对话摘要失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 调用 DashScope 大模型
     */
    private String callDashScopeModel(String prompt) throws Exception {
        String url = dashscopeBaseUrl + "/services/aigc/text-generation/generation";

        String escapedPrompt = prompt.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        String messagesJson = "[{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}]";
        String jsonBody = "{\"model\":\"" + dashscopeModel + "\",\"input\":{\"messages\":" + messagesJson + "},\"parameters\":{\"max_tokens\":500,\"temperature\":0.3,\"top_p\":0.8}}";

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

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                String responseStr = response.toString();
                if (responseStr.contains("\"text\":\"")) {
                    int start = responseStr.indexOf("\"text\":\"") + 8;
                    int end = responseStr.indexOf("\"", start);
                    if (end > start) {
                        return responseStr.substring(start, end).trim()
                                .replace("\\n", "\n")
                                .replace("\\r", "");
                    }
                }
            }
        }
        return null;
    }
}
