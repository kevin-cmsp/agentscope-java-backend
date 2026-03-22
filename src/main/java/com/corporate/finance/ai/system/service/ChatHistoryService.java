package com.corporate.finance.ai.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.corporate.finance.ai.system.dao.ConversationDao;
import com.corporate.finance.ai.system.dao.MessageDao;
import com.corporate.finance.ai.system.dao.UserDao;
import com.corporate.finance.ai.system.entity.ConversationEntity;
import com.corporate.finance.ai.system.entity.MessageEntity;
import com.corporate.finance.ai.system.entity.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatHistoryService {

    private final ConversationDao conversationDao;
    private final MessageDao messageDao;
    private final UserDao userDao;

    public ChatHistoryService(ConversationDao conversationDao, MessageDao messageDao, UserDao userDao) {
        this.conversationDao = conversationDao;
        this.messageDao = messageDao;
        this.userDao = userDao;
    }

    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserEntity::getUsername, username);
            UserEntity user = userDao.selectOne(wrapper);
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }

    /**
     * 创建新会话
     */
    @Transactional
    public ConversationEntity createConversation(String title) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        ConversationEntity entity = new ConversationEntity();
        entity.setUserId(userId);
        entity.setTitle(title != null && title.length() > 20 ? title.substring(0, 20) : title);
        conversationDao.insert(entity);
        return entity;
    }

    /**
     * 获取当前用户的所有会话列表（按更新时间降序）
     */
    public List<ConversationEntity> getConversationList() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        LambdaQueryWrapper<ConversationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationEntity::getUserId, userId)
               .orderByDesc(ConversationEntity::getUpdateTime);
        return conversationDao.selectList(wrapper);
    }

    /**
     * 获取指定会话的所有消息
     */
    public List<MessageEntity> getMessages(Long conversationId) {
        LambdaQueryWrapper<MessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageEntity::getConversationId, conversationId)
               .orderByAsc(MessageEntity::getCreateTime);
        return messageDao.selectList(wrapper);
    }

    /**
     * 保存消息到会话
     */
    @Transactional
    public MessageEntity saveMessage(Long conversationId, String role, String content) {
        MessageEntity entity = new MessageEntity();
        entity.setConversationId(conversationId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setCreateTime(LocalDateTime.now());
        messageDao.insert(entity);

        // 如果是用户第一条消息，更新会话标题
        if ("user".equals(role)) {
            ConversationEntity conversation = conversationDao.selectById(conversationId);
            if (conversation != null && (conversation.getTitle() == null || "新对话".equals(conversation.getTitle()))) {
                String title = content.length() > 20 ? content.substring(0, 20) : content;
                conversation.setTitle(title);
                conversationDao.updateById(conversation);
            }
        }

        return entity;
    }

    /**
     * 删除会话（逻辑删除会话，物理删除消息）
     */
    @Transactional
    public void deleteConversation(Long conversationId) {
        conversationDao.deleteById(conversationId);
        LambdaQueryWrapper<MessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageEntity::getConversationId, conversationId);
        messageDao.delete(wrapper);
    }

    /**
     * 获取会话详情
     */
    public ConversationEntity getConversation(Long conversationId) {
        return conversationDao.selectById(conversationId);
    }
}
