package com.corporate.finance.ai.service;

import com.corporate.finance.ai.config.ModelServiceConfig;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 模型服务工厂
 * 
 * 该工厂类负责管理和获取不同的大模型服务实例。
 * 支持根据服务名称获取对应的模型服务，
 * 并自动调用初始化方法。
 * 
 * 主要功能：
 * - 获取默认模型服务
 * - 根据服务名称获取指定的模型服务
 * - 列出所有可用的模型服务
 * 
 * 使用方式：
 * - 通过@Autowired注入ModelServiceFactory
 * - 调用getDefaultModelService()获取默认服务
 * - 调用getModelService(serviceName)获取指定服务
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service
public class ModelServiceFactory {

    /**
     * 模型服务配置
     * 从配置文件中读取模型服务相关配置
     */
    private final ModelServiceConfig modelServiceConfig;

    /**
     * 模型服务实例集合
     * Key: 服务名称（如：dashscope、openai）
     * Value: ModelService实例
     */
    private final Map<String, ModelService> modelServices;

    /**
     * 构造函数
     * 
     * Spring 4.3+ 支持隐式构造函数注入，
     * 当类只有一个构造函数时，@Autowired注解可以省略。
     * 这是Spring Boot 2.6+ 推荐的做法。
     * 
     * @param modelServiceConfig 模型服务配置
     * @param modelServices 模型服务实例集合
     */
    public ModelServiceFactory(ModelServiceConfig modelServiceConfig, Map<String, ModelService> modelServices) {
        this.modelServiceConfig = modelServiceConfig;
        this.modelServices = modelServices;
    }

    /**
     * 获取默认模型服务
     * 
     * 根据配置文件中的default字段获取默认的模型服务。
     * 
     * @return 默认的ModelService实例
     */
    public ModelService getDefaultModelService() {
        // 从配置中获取默认服务名称
        String defaultService = modelServiceConfig.getDefaultService();
        
        // 返回对应的模型服务
        return getModelService(defaultService);
    }

    /**
     * 根据服务名称获取模型服务
     * 
     * 该方法根据服务名称获取对应的模型服务实例，
     * 并自动调用初始化方法。
     * 
     * @param serviceName 服务名称（如：dashscope、openai）
     * @return ModelService实例，如果不存在则返回null
     */
    public ModelService getModelService(String serviceName) {
        // 从集合中获取模型服务
        ModelService modelService = modelServices.get(serviceName);
        
        // 如果服务存在，调用初始化方法
        if (modelService != null) {
            modelService.init();
        }
        
        return modelService;
    }

    /**
     * 列出所有可用的模型服务
     * 
     * 返回所有已注册的模型服务实例。
     * 
     * @return 模型服务名称与实例的映射集合
     */
    public Map<String, ModelService> listModelServices() {
        return modelServices;
    }
}
