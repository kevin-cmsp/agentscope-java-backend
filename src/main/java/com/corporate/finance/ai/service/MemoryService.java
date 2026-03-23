package com.corporate.finance.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.corporate.finance.ai.system.dao.ConversationDao;
import com.corporate.finance.ai.system.dao.MessageDao;
import com.corporate.finance.ai.system.entity.ConversationEntity;
import com.corporate.finance.ai.system.entity.MessageEntity;
import io.agentscope.core.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 记忆管理服务
 *
 * 提供对话历史和上下文信息的存储、查询和清除功能。
 * 支持三级存储：内存 -> Redis -> 数据库
 *
 * 主要功能:
 * - 存储对话消息到记忆系统（内存 + Redis）
 * - 从数据库加载历史对话到内存
 * - 获取所有记忆内容
 * - 记忆压缩（对话摘要）
 * - 清除记忆系统中的所有数据
 *
 * @author Corporate Finance AI Team
 * @version 3.0.0
 */
@Service
public class MemoryService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryService.class);

    /** Redis Key 前缀 */
    private static final String REDIS_MEMORY_KEY_PREFIX = "ai:memory:user:";
    /** Redis 记忆过期时间（小时） */
    private static final long REDIS_MEMORY_EXPIRE_HOURS = 24;
    /** 内存中最大保留消息数 */
    private static final int MAX_MEMORY_SIZE = 100;
    /** 触发压缩的消息阈值 */
    private static final int COMPRESS_THRESHOLD = 50;
    /** 压缩后保留的最近消息数 */
    private static final int KEEP_RECENT_COUNT = 10;

    /** 用户记忆存储 Map */
    private final Map<Long, List<Msg>> userMemories = new ConcurrentHashMap<>();

    /** 用户压缩摘要存储 */
    private final Map<Long, String> userMemorySummaries = new ConcurrentHashMap<>();

    private final MessageDao messageDao;
    private final ConversationDao conversationDao;
    private final RedisTemplate<String, Object> redisTemplate;

    public MemoryService(MessageDao messageDao,
                         ConversationDao conversationDao,
                         RedisTemplate<String, Object> redisTemplate) {
        this.messageDao = messageDao;
        this.conversationDao = conversationDao;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 存储消息到指定用户的记忆中（内存 + Redis 双写）
     */
    public void storeMessage(Long userId, Msg message) {
        // 1. 写入内存
        userMemories.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
        // 2. 写入 Redis
        persistToRedis(userId);
    }

    /**
     * 存储消息到指定用户的记忆中 (简化版)
     */
    public void storeMessage(Long userId, String sender, String content) {
        Msg message = Msg.builder()
                .textContent("[" + sender + "]: " + content)
                .build();
        storeMessage(userId, message);
    }

    /**
     * 获取指定用户的所有记忆（优先内存 -> Redis -> 数据库）
     */
    public List<Msg> getAllMessages(Long userId) {
        // 1. 尝试从内存获取
        List<Msg> messages = userMemories.get(userId);
        if (messages != null && !messages.isEmpty()) {
            return messages;
        }

        // 2. 尝试从 Redis 恢复
        messages = loadFromRedis(userId);
        if (messages != null && !messages.isEmpty()) {
            userMemories.put(userId, messages);
            logger.info("从 Redis 恢复用户 {} 的记忆，共 {} 条", userId, messages.size());
            return messages;
        }

        // 3. 从数据库加载
        messages = loadFromDatabase(userId);
        if (!messages.isEmpty()) {
            userMemories.put(userId, messages);
            persistToRedis(userId);
            logger.info("从数据库加载用户 {} 的记忆，共 {} 条", userId, messages.size());
        }

        return messages;
    }

    /**
     * 获取指定用户最近的 N 条记忆（用于上下文构建）
     * 如果存在摘要，会在返回列表头部加入摘要信息
     */
    public List<Msg> getRecentMessages(Long userId, int limit) {
        List<Msg> messages = getAllMessages(userId);
        List<Msg> result = new ArrayList<>();

        // 如果有压缩摘要，先加入摘要
        String summary = getMemorySummary(userId);
        if (summary != null && !summary.isEmpty()) {
            Msg summaryMsg = Msg.builder()
                    .textContent("[历史对话摘要]: " + summary)
                    .build();
            result.add(summaryMsg);
        }

        // 获取最近 N 条消息
        if (!messages.isEmpty()) {
            int size = messages.size();
            int fromIndex = Math.max(0, size - limit);
            result.addAll(messages.subList(fromIndex, size));
        }

        return result;
    }

    /**
     * 从数据库加载用户最近的历史对话到内存
     * 加载该用户最近的会话及其消息
     */
    public List<Msg> loadFromDatabase(Long userId) {
        List<Msg> messages = new ArrayList<>();
        try {
            // 获取用户最近的会话（最多5个）
            LambdaQueryWrapper<ConversationEntity> convWrapper = new LambdaQueryWrapper<>();
            convWrapper.eq(ConversationEntity::getUserId, userId)
                    .orderByDesc(ConversationEntity::getUpdateTime)
                    .last("LIMIT 5");
            List<ConversationEntity> conversations = conversationDao.selectList(convWrapper);

            if (conversations.isEmpty()) {
                return messages;
            }

            // 获取这些会话的消息ID列表
            List<Long> conversationIds = conversations.stream()
                    .map(ConversationEntity::getId)
                    .collect(Collectors.toList());

            // 查询这些会话的所有消息（按时间倒序，最多取最近100条）
            LambdaQueryWrapper<MessageEntity> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.in(MessageEntity::getConversationId, conversationIds)
                    .orderByDesc(MessageEntity::getCreateTime)
                    .last("LIMIT " + MAX_MEMORY_SIZE);
            List<MessageEntity> messageEntities = messageDao.selectList(msgWrapper);

            // 反转为正序（按时间升序）
            for (int i = messageEntities.size() - 1; i >= 0; i--) {
                MessageEntity entity = messageEntities.get(i);
                Msg msg = Msg.builder()
                        .textContent("[" + entity.getRole() + "]: " + entity.getContent())
                        .build();
                messages.add(msg);
            }

            logger.info("从数据库加载用户 {} 的历史记录，共 {} 条消息", userId, messages.size());
        } catch (Exception e) {
            logger.error("从数据库加载用户 {} 的历史记录失败: {}", userId, e.getMessage(), e);
        }
        return messages;
    }

    /**
     * 加载指定会话的历史消息到内存
     */
    public List<Msg> loadConversationFromDatabase(Long userId, Long conversationId) {
        List<Msg> messages = new ArrayList<>();
        try {
            LambdaQueryWrapper<MessageEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MessageEntity::getConversationId, conversationId)
                    .orderByAsc(MessageEntity::getCreateTime);
            List<MessageEntity> messageEntities = messageDao.selectList(wrapper);

            for (MessageEntity entity : messageEntities) {
                Msg msg = Msg.builder()
                        .textContent("[" + entity.getRole() + "]: " + entity.getContent())
                        .build();
                messages.add(msg);
            }

            // 合并到用户记忆中
            List<Msg> existingMessages = userMemories.computeIfAbsent(userId, k -> new ArrayList<>());
            existingMessages.addAll(messages);
            persistToRedis(userId);

            logger.info("加载会话 {} 的历史消息，共 {} 条", conversationId, messages.size());
        } catch (Exception e) {
            logger.error("加载会话 {} 的历史消息失败: {}", conversationId, e.getMessage(), e);
        }
        return messages;
    }

    // ==================== Redis 持久化 ====================

    /**
     * 将用户记忆持久化到 Redis
     */
    private void persistToRedis(Long userId) {
        try {
            String key = REDIS_MEMORY_KEY_PREFIX + userId;
            List<Msg> messages = userMemories.get(userId);
            if (messages == null || messages.isEmpty()) {
                return;
            }
            // 转换为可序列化的格式
            List<String> serialized = messages.stream()
                    .map(Msg::getTextContent)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(key, serialized, REDIS_MEMORY_EXPIRE_HOURS, TimeUnit.HOURS);

            // 同时持久化摘要
            String summary = userMemorySummaries.get(userId);
            if (summary != null && !summary.isEmpty()) {
                String summaryKey = REDIS_MEMORY_KEY_PREFIX + userId + ":summary";
                redisTemplate.opsForValue().set(summaryKey, summary, REDIS_MEMORY_EXPIRE_HOURS, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            logger.warn("持久化用户 {} 的记忆到 Redis 失败: {}", userId, e.getMessage());
        }
    }

    /**
     * 从 Redis 恢复用户记忆
     */
    @SuppressWarnings("unchecked")
    private List<Msg> loadFromRedis(Long userId) {
        try {
            String key = REDIS_MEMORY_KEY_PREFIX + userId;
            Object data = redisTemplate.opsForValue().get(key);
            if (data instanceof List) {
                List<String> serialized = (List<String>) data;
                List<Msg> messages = new ArrayList<>();
                for (String text : serialized) {
                    messages.add(Msg.builder().textContent(text).build());
                }

                // 恢复摘要
                String summaryKey = REDIS_MEMORY_KEY_PREFIX + userId + ":summary";
                Object summaryData = redisTemplate.opsForValue().get(summaryKey);
                if (summaryData instanceof String) {
                    userMemorySummaries.put(userId, (String) summaryData);
                }

                return messages;
            }
        } catch (Exception e) {
            logger.warn("从 Redis 恢复用户 {} 的记忆失败: {}", userId, e.getMessage());
        }
        return null;
    }

    // ==================== 记忆压缩 ====================

    /**
     * 检查是否需要压缩记忆
     */
    public boolean needsCompression(Long userId) {
        List<Msg> messages = userMemories.get(userId);
        return messages != null && messages.size() >= COMPRESS_THRESHOLD;
    }

    /**
     * 压缩用户记忆：将旧消息替换为摘要，保留最近的消息
     *
     * @param userId  用户ID
     * @param summary 由大模型生成的摘要文本
     */
    public void compressMemory(Long userId, String summary) {
        List<Msg> messages = userMemories.get(userId);
        if (messages == null || messages.size() <= KEEP_RECENT_COUNT) {
            return;
        }

        // 保存摘要
        userMemorySummaries.put(userId, summary);

        // 只保留最近的消息
        int size = messages.size();
        List<Msg> recentMessages = new ArrayList<>(messages.subList(size - KEEP_RECENT_COUNT, size));
        userMemories.put(userId, recentMessages);

        // 同步到 Redis
        persistToRedis(userId);

        logger.info("用户 {} 的记忆已压缩，摘要长度: {}, 保留最近 {} 条消息",
                userId, summary.length(), recentMessages.size());
    }

    /**
     * 获取用户的记忆摘要
     */
    public String getMemorySummary(Long userId) {
        return userMemorySummaries.get(userId);
    }

    /**
     * 获取待压缩的消息文本（用于发送给大模型生成摘要）
     */
    public String getMessagesForCompression(Long userId) {
        List<Msg> messages = userMemories.get(userId);
        if (messages == null || messages.size() <= KEEP_RECENT_COUNT) {
            return null;
        }
        // 获取需要压缩的旧消息（排除最近的消息）
        int compressEnd = messages.size() - KEEP_RECENT_COUNT;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < compressEnd; i++) {
            sb.append(messages.get(i).getTextContent()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取当前记忆大小
     */
    public int getMemorySize(Long userId) {
        List<Msg> messages = userMemories.get(userId);
        return messages != null ? messages.size() : 0;
    }

    // ==================== 清除操作 ====================

    /**
     * 清除指定用户的记忆（内存 + Redis）
     */
    public void clearMemory(Long userId) {
        userMemories.remove(userId);
        userMemorySummaries.remove(userId);
        try {
            redisTemplate.delete(REDIS_MEMORY_KEY_PREFIX + userId);
            redisTemplate.delete(REDIS_MEMORY_KEY_PREFIX + userId + ":summary");
        } catch (Exception e) {
            logger.warn("清除用户 {} 的 Redis 记忆失败: {}", userId, e.getMessage());
        }
    }

    /**
     * 清除所有用户的记忆
     */
    public void clearAllMemory() {
        userMemories.clear();
        userMemorySummaries.clear();
    }
}
