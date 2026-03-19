package com.corporate.finance.ai.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 模型服务配置类
 * 
 * 该类用于从配置文件（application.yml）中读取模型服务相关配置。
 * 使用@ConfigurationProperties 注解实现配置绑定。
 * 
 * 配置示例：
 * model:
 *   default: dashscope
 *   services:
 *     dashscope:
 *       api-key: your-api-key
 *       base-url: https://dashscope.aliyuncs.com/api/v1
 *       model: qwen-turbo
 * 
 * 主要功能：
 * - 读取默认模型服务名称
 * - 读取各个模型服务的配置参数
 * - 提供配置访问接口
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "model")
public class ModelServiceConfig {

    /**
     * 默认模型服务名称
     * 对应配置文件中的 model.default 字段
     */
    private String defaultService;

    /**
     * 模型服务配置集合
     * Key: 服务名称（如：dashscope、openai）
     * Value: 服务配置属性
     */
    private Map<String, ModelServiceProperties> services;

    /**
     * 默认构造函数
     * Spring Boot 会通过反射创建实例并注入配置
     */
    public ModelServiceConfig() {
    }

    /**
     * PostConstruct 初始化方法
     * 在依赖注入完成后执行，用于验证配置是否正确加载
     */
    @PostConstruct
    private void init() {
        if (defaultService == null || defaultService.isEmpty()) {
            System.err.println("警告：未找到默认模型服务配置，使用默认值：dashscope");
            defaultService = "dashscope";
        }
    }

    /**
     * 获取默认模型服务名称
     * 
     * @return 默认服务名称
     */
    public String getDefaultService() {
        return defaultService;
    }

    /**
     * 设置默认模型服务名称
     * 
     * Spring Boot 会通过此方法将配置文件中的 default 字段值注入到 defaultService 字段
     * 
     * @param defaultService 默认服务名称
     */
    public void setDefaultService(String defaultService) {
        this.defaultService = defaultService;
    }

    /**
     * 获取所有模型服务配置
     * 
     * @return 服务名称与配置属性的映射集合
     */
    public Map<String, ModelServiceProperties> getServices() {
        return services;
    }

    /**
     * 设置模型服务配置集合
     * 
     * @param services 服务名称与配置属性的映射集合
     */
    public void setServices(Map<String, ModelServiceProperties> services) {
        this.services = services;
    }

    /**
     * 模型服务属性内部类
     * 
     * 该类封装了单个模型服务的配置属性，
     * 包括 API 密钥、基础 URL 和模型名称。
     */
    public static class ModelServiceProperties {

        /**
         * API 密钥
         * 用于访问模型服务的认证密钥
         */
        private String apiKey;

        /**
         * 基础 URL
         * 模型服务的 API 端点地址
         */
        private String baseUrl;

        /**
         * 模型名称
         * 具体使用的模型标识符
         */
        private String model;

        /**
         * 获取 API 密钥
         * 
         * @return API 密钥
         */
        public String getApiKey() {
            return apiKey;
        }

        /**
         * 设置 API 密钥
         * 
         * @param apiKey API 密钥
         */
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * 获取基础 URL
         * 
         * @return 基础 URL
         */
        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * 设置基础 URL
         * 
         * @param baseUrl 基础 URL
         */
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * 获取模型名称
         * 
         * @return 模型名称
         */
        public String getModel() {
            return model;
        }

        /**
         * 设置模型名称
         * 
         * @param model 模型名称
         */
        public void setModel(String model) {
            this.model = model;
        }
    }
}
