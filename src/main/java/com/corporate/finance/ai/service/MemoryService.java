package com.corporate.finance.ai.service;

import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import org.springframework.stereotype.Service;

/**
 * 记忆管理服务
 * 
 * 该服务封装了AgentScope Java的记忆管理功能，
 * 提供对话历史和上下文信息的存储、查询和清除功能。
 * 
 * 主要功能：
 * - 存储对话消息到记忆系统
 * - 获取所有记忆内容
 * - 清除记忆系统中的所有数据
 * 
 * 技术实现：
 * - 基于AgentScope Java的InMemoryMemory
 * - 数据存储在内存中，重启后丢失
 * - 适用于开发和测试环境
 * 
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service
public class MemoryService {

    /**
     * InMemoryMemory实例
     * 通过依赖注入获取，由AgentScopeConfig配置
     */
    private final InMemoryMemory memory;

    /**
     * 构造函数
     * 
     * Spring 4.3+ 支持隐式构造函数注入，
     * 当类只有一个构造函数时，@Autowired注解可以省略。
     * 这是Spring Boot 2.6+ 推荐的做法。
     * 
     * @param memory InMemoryMemory实例
     */
    public MemoryService(InMemoryMemory memory) {
        this.memory = memory;
    }

    /**
     * 存储消息到记忆系统
     * 
     * 该方法将Msg对象添加到记忆系统中，
     * 用于保存对话历史和上下文信息。
     * 
     * @param message Msg消息对象
     */
    public void storeMessage(Msg message) {
        memory.addMessage(message);
    }

    /**
     * 存储消息到记忆系统（简化版）
     * 
     * 该方法提供简化的消息存储接口，
     * 接收发送者和内容参数。
     * 
     * 注意：由于Msg构造器是private的，
     * 该方法目前未实现实际存储功能。
     * 
     * @param sender 消息发送者（如：user、assistant）
     * @param content 消息内容
     */
    public void storeMessage(String sender, String content) {
        // 由于Msg构造器是private的，这里我们暂时不存储到memory
        // 后续可以通过其他方式实现
    }

    /**
     * 获取所有记忆内容
     * 
     * 该方法返回记忆系统中存储的所有消息列表。
     * 
     * @return 记忆内容的字符串表示
     */
    public String getAllMessages() {
        return memory.getMessages().toString();
    }

    /**
     * 清除记忆系统中的所有数据
     * 
     * 该方法清空记忆系统中的所有对话历史和上下文信息。
     * 操作不可逆，请谨慎使用。
     */
    public void clearMemory() {
        memory.clear();
    }
}
