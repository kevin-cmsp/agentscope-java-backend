package com.corporate.finance.ai.service;

import io.agentscope.core.message.Msg;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记忆管理服务
 * 
 * 该服务封装了 AgentScope Java 的记忆管理功能，
 * 提供对话历史和上下文信息的存储、查询和清除功能。
 * 
 * 主要功能:
 * - 存储对话消息到记忆系统
 * - 获取所有记忆内容
 * - 清除记忆系统中的所有数据
 * 
 * 技术实现:
 * - 基于用户 ID 隔离记忆数据
 * - 使用 ConcurrentHashMap 实现线程安全的记忆存储
 * - 支持与数据库会话历史同步
 * 
 * @author Corporate Finance AI Team
 * @version 2.0.0
 */
@Service
public class MemoryService {

    /**
     * 用户记忆存储 Map
     * Key: 用户 ID
     * Value: 该用户的记忆列表
     */
    private final Map<Long, List<Msg>> userMemories = new ConcurrentHashMap<>();

    /**
     * 存储消息到指定用户的记忆中
     * 
     * @param userId 用户 ID
     * @param message Msg 消息对象
     */
    public void storeMessage(Long userId, Msg message) {
        userMemories.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    /**
     * 存储消息到指定用户的记忆中 (简化版)
     * 
     * @param userId 用户 ID
     * @param sender 消息发送者 (如:user、assistant)
     * @param content 消息内容
     */
    public void storeMessage(Long userId, String sender, String content) {
        // 使用 Map 存储消息，包含角色和内容
        Map<String, Object> messageData = new ConcurrentHashMap<>();
        messageData.put("role", sender);
        messageData.put("content", content);
        
        // 创建 Msg 对象
        Msg message = Msg.builder()
                .textContent("[" + sender + "]: " + content)
                .build();
        
        storeMessage(userId, message);
    }

    /**
     * 获取指定用户的所有记忆
     * 
     * @param userId 用户 ID
     * @return 记忆列表
     */
    public List<Msg> getAllMessages(Long userId) {
        return userMemories.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * 获取指定用户最近的 N 条记忆 (用于上下文)
     * 
     * @param userId 用户 ID
     * @param limit 最大数量
     * @return 最近的记忆列表
     */
    public List<Msg> getRecentMessages(Long userId, int limit) {
        List<Msg> messages = getAllMessages(userId);
        if (messages.isEmpty()) {
            return messages;
        }
        int size = messages.size();
        int fromIndex = Math.max(0, size - limit);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }

    /**
     * 清除指定用户的记忆
     * 
     * @param userId 用户 ID
     */
    public void clearMemory(Long userId) {
        userMemories.remove(userId);
    }

    /**
     * 清除所有用户的记忆
     */
    public void clearAllMemory() {
        userMemories.clear();
    }

    /**
     * 获取所有记忆内容 (已废弃，请使用 getAllMessages(Long userId))
     * 
     * @return 记忆内容的字符串表示
     * @deprecated 改用 {@link #getAllMessages(Long)} 支持用户隔离
     */
    @Deprecated
    public String getAllMessages() {
        return "错误：该方法已废弃，请使用 getAllMessages(Long userId)";
    }

    /**
     * 清除记忆系统中的所有数据 (已废弃，请使用 clearMemory(Long userId))
     * 
     * @deprecated 改用 {@link #clearMemory(Long)} 支持用户隔离
     */
    @Deprecated
    public void clearMemory() {
        // 已废弃，改用 clearMemory(Long userId)
    }
}
