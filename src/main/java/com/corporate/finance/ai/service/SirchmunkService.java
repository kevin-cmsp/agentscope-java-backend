package com.corporate.finance.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Sirchmunk 知识库服务
 *
 * 提供与 Sirchmunk 知识库系统的集成，
 * 支持智能搜索查询和结果解析。
 *
 * 主要功能：
 * - 调用 Sirchmunk 搜索 API
 * - 解析搜索响应
 * - 缓存搜索结果
 * - 错误处理和重试机制
 *
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service
public class SirchmunkService {

    private static final Logger logger = LoggerFactory.getLogger(SirchmunkService.class);

    @Value("${sirchmunk.base-url}")
    private String baseUrl;

    @Value("${sirchmunk.search-endpoint}")
    private String searchEndpoint;

    @Value("${sirchmunk.mode:FAST}")
    private String searchMode;

    @Value("${sirchmunk.max-depth:10}")
    private Integer maxDepth;

    @Value("${sirchmunk.top-k-files:20}")
    private Integer topKFiles;

    @Value("${sirchmunk.enable-dir-scan:true}")
    private Boolean enableDirScan;

    @Value("${sirchmunk.max-loops:8}")
    private Integer maxLoops;

    @Value("${sirchmunk.max-token-budget:131072}")
    private Integer maxTokenBudget;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_INTERVAL_MS = 1000;

    /**
     * 搜索知识库
     *
     * @param query 搜索查询
     * @return 搜索结果，包含 summary 和 context
     * @throws IOException 异常
     */
    public SirchmunkSearchResult search(String query) throws IOException {
        logger.info("Sirchmunk 知识库搜索：{}", query);

        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                String url = baseUrl + searchEndpoint;

                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("query", query);
                requestBody.put("mode", searchMode);
                requestBody.put("max_depth", maxDepth);
                requestBody.put("top_k_files", topKFiles);
                requestBody.put("enable_dir_scan", enableDirScan);
                requestBody.put("max_loops", maxLoops);
                requestBody.put("max_token_budget", maxTokenBudget);
                requestBody.put("return_context", true);

                // 序列化为 JSON
                String jsonBody = objectMapper.writeValueAsString(requestBody);

                logger.debug("Sirchmunk 请求 URL: {}", url);
                logger.debug("Sirchmunk 请求体：{}", jsonBody);

                // 创建请求
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonBody, JSON);

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Accept", "application/json; charset=utf-8")
                        .build();

                // 执行请求
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 打印原始响应报文（截取前1000字符避免日志过长）
                    String truncatedResponse = responseBody.length() > 1000
                            ? responseBody.substring(0, 1000) + "...(truncated, total length: " + responseBody.length() + ")"
                            : responseBody;
                    logger.info("Sirchmunk 原始响应报文：{}", truncatedResponse);

                    // 解析响应
                    SirchmunkSearchResult result = parseSearchResponse(responseBody);

                    if (result != null && result.isSuccess()) {
                        logger.info("Sirchmunk 搜索成功，hasContent={}，summary={}，context类型={}",
                                result.hasContent(),
                                result.getSummary() != null ? "长度" + result.getSummary().length() : "null",
                                result.getContext() != null ? result.getContext().getClass().getSimpleName() : "null");
                        return result;
                    } else {
                        logger.warn("Sirchmunk 搜索返回失败，result={}", result);
                        return null;
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    logger.error("Sirchmunk API 错误：{} {}, 详细信息：{}",
                            response.code(), response.message(), errorBody);

                    retryCount++;
                    if (retryCount < MAX_RETRY_COUNT) {
                        Thread.sleep(RETRY_INTERVAL_MS * retryCount);
                    }
                }
            } catch (Exception e) {
                retryCount++;
                logger.warn("Sirchmunk 搜索失败 ({}次): {}", retryCount, e.getMessage());
                if (retryCount < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new IOException("Sirchmunk 搜索失败：" + e.getMessage(), e);
                }
            }
        }

        return null;
    }

    /**
     * 解析搜索响应
     *
     * @param responseBody 响应体 JSON 字符串
     * @return 解析后的搜索结果对象
     * @throws IOException 解析异常
     */
    private SirchmunkSearchResult parseSearchResponse(String responseBody) throws IOException {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(
                    responseBody,
                    new TypeReference<Map<String, Object>>() {}
            );

            Boolean success = (Boolean) responseMap.get("success");
            if (success == null || !success) {
                logger.warn("Sirchmunk 响应中 success 字段为 false，success={}", success);
                return new SirchmunkSearchResult(false, null, null);
            }

            Map<String, Object> data = null;
            if (responseMap.containsKey("data")) {
                data = objectMapper.convertValue(
                        responseMap.get("data"),
                        new TypeReference<Map<String, Object>>() {}
                );
            }

            if (data == null) {
                logger.warn("Sirchmunk 响应中 data 字段为空");
                return new SirchmunkSearchResult(true, null, null);
            }

            logger.info("Sirchmunk 响应顶层字段：{}，data 字段包含的 key：{}", responseMap.keySet(), data.keySet());

            String summary = (String) data.get("summary");
            Object context = data.get("context");

            logger.info("Sirchmunk 解析结果 - summary: {}，context类型: {}，context前200字符: {}",
                    summary != null ? "长度=" + summary.length() + "，前100字符=[" + summary.substring(0, Math.min(100, summary.length())) + "]" : "null",
                    context != null ? context.getClass().getSimpleName() : "null",
                    context != null ? context.toString().substring(0, Math.min(200, context.toString().length())) : "null");

            return new SirchmunkSearchResult(true, summary, context);

        } catch (Exception e) {
            logger.error("解析 Sirchmunk 响应失败：{}", e.getMessage());
            throw new IOException("解析 Sirchmunk 响应失败：" + e.getMessage(), e);
        }
    }

    /**
     * Sirchmunk 搜索结果内部类
     */
    public static class SirchmunkSearchResult {
        private final boolean success;
        private final String summary;
        private final Object context;

        public SirchmunkSearchResult(boolean success, String summary, Object context) {
            this.success = success;
            this.summary = summary;
            this.context = context;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getSummary() {
            return summary;
        }

        public Object getContext() {
            return context;
        }

        public boolean hasContent() {
            if (summary != null && !summary.trim().isEmpty()) {
                return true;
            }
            // FAST 模式下 summary 可能为空，但 context 中有实际内容
            if (context != null) {
                String contextStr = context.toString().trim();
                return !contextStr.isEmpty() && !"{}".equals(contextStr) && !"[]".equals(contextStr);
            }
            return false;
        }
    }
}
