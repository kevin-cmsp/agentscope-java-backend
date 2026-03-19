package com.corporate.finance.ai.config;

import io.agentscope.core.memory.InMemoryMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgentScope配置类
 * 
 * 该类负责配置AgentScope Java框架的核心组件，
 * 包括记忆管理、工具注册等基础功能。
 * 
 * 主要功能：
 * - 配置InMemoryMemory作为默认记忆存储
 * - 提供AgentScope核心组件的Bean定义
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Configuration
public class AgentScopeConfig {

    /**
     * 创建InMemoryMemory Bean
     * 
     * InMemoryMemory是AgentScope Java内置的内存级记忆存储实现，
     * 用于存储对话历史和上下文信息。
     * 
     * 特点：
     * - 数据存储在内存中，重启后丢失
     * - 适用于开发和测试环境
     * - 生产环境建议使用持久化存储方案
     * 
     * @return InMemoryMemory实例
     */
    @Bean
    public InMemoryMemory memory() {
        return new InMemoryMemory();
    }
}
