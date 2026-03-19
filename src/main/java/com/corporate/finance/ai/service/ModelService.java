package com.corporate.finance.ai.service;

/**
 * 模型服务接口
 * 
 * 该接口定义了大模型服务的基本操作，
 * 包括初始化、生成响应和关闭服务等方法。
 * 
 * 主要功能：
 * - 初始化模型服务
 * - 调用模型生成响应
 * - 关闭模型服务
 * 
 * 实现类：
 * - DashScopeModelService：通义千问模型服务实现
 * - OpenAIModelService：OpenAI模型服务实现（待开发）
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
public interface ModelService {

    /**
     * 初始化模型服务
     * 
     * 该方法在获取模型服务时自动调用，
     * 用于完成模型服务的初始化工作。
     */
    void init();

    /**
     * 调用模型生成响应
     * 
     * 该方法接收用户提示词，调用大模型生成响应。
     * 
     * @param prompt 用户提示词
     * @return 模型生成的响应文本
     */
    String generate(String prompt);

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
    String generate(String systemPrompt, String userPrompt);

    /**
     * 关闭模型服务
     * 
     * 该方法用于释放模型服务占用的资源，
     * 在服务不再使用时调用。
     */
    void close();
}
