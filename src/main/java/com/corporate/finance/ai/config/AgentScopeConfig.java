package com.corporate.finance.ai.config;

import org.springframework.context.annotation.Configuration;

/**
 * AgentScope 配置类
 * 
 * 该类负责配置 AgentScope Java 框架的核心组件。
 * 
 * 注意:
 * - InMemoryMemory 已移除，改用基于用户 ID 的 MemoryService
 * - 用户记忆现在由 MemoryService 通过 ConcurrentHashMap 管理
 * - 每个用户拥有独立的记忆空间
 * 
 * @author Corporate Finance AI Team
 * @version 2.0.0
 */
@Configuration
public class AgentScopeConfig {

    // 注意：InMemoryMemory Bean 已移除
    // 原因：
    // 1. 全局单例导致所有用户记忆混在一起
    // 2. 无法实现用户隔离
    // 3. 改用 MemoryService 的 ConcurrentHashMap 方案
}
