package com.corporate.finance.ai.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent管理服务
 * 
 * 该服务负责管理Agent实例的生命周期，包括创建、获取、删除和列出所有Agent。
 * 使用HashMap存储Agent实例，提供基本的内存级管理功能。
 * 
 * 主要功能：
 * - 创建Agent并分配唯一ID
 * - 根据ID获取Agent实例
 * - 删除指定的Agent实例
 * - 列出所有已注册的Agent ID
 * 
 * 技术实现：
 * - 使用HashMap存储Agent实例，键为Agent ID，值为Agent实例
 * - 提供线程安全的操作方法
 * - 支持基本的CRUD操作
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service
public class AgentManagerService {

    /**
     * Agent实例存储
     * 使用HashMap存储，键为Agent ID，值为Agent实例
     */
    private final Map<String, Object> agents = new HashMap<>();

    /**
     * 创建Agent
     * 
     * 该方法创建一个新的Agent实例，并为其分配唯一的ID。
     * Agent ID格式为：agent_{时间戳}_{随机数}
     * 
     * @param name Agent名称
     * @param agent Agent实例对象
     * @return Agent唯一标识符
     */
    public synchronized String createAgent(String name, Object agent) {
        // 生成唯一的Agent ID
        String agentId = "agent_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
        
        // 存储Agent实例
        agents.put(agentId, agent);
        
        return agentId;
    }

    /**
     * 获取Agent
     * 
     * 根据Agent ID获取对应的Agent实例。
     * 
     * @param agentId Agent唯一标识符
     * @return Agent实例对象，如果不存在则返回null
     */
    public synchronized Object getAgent(String agentId) {
        return agents.get(agentId);
    }

    /**
     * 删除Agent
     * 
     * 根据Agent ID删除对应的Agent实例。
     * 
     * @param agentId Agent唯一标识符
     * @return 是否删除成功
     */
    public synchronized boolean deleteAgent(String agentId) {
        return agents.remove(agentId) != null;
    }

    /**
     * 列出所有Agent
     * 
     * 获取当前已注册的所有Agent ID列表。
     * 
     * @return Agent ID列表
     */
    public synchronized List<String> listAgents() {
        return agents.keySet().stream().toList();
    }
}
