package com.corporate.finance.ai.controller;

import com.corporate.finance.ai.system.common.Result;
import com.corporate.finance.ai.system.entity.ConversationEntity;
import com.corporate.finance.ai.system.entity.MessageEntity;
import com.corporate.finance.ai.system.service.ChatHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 对话历史管理控制器
 *
 * 提供对话会话和消息的CRUD接口。
 *
 * API端点：
 * - GET  /api/chat/conversations              - 获取当前用户的会话列表
 * - POST /api/chat/conversations              - 创建新会话
 * - GET  /api/chat/conversations/{id}/messages - 获取指定会话的消息列表
 * - DELETE /api/chat/conversations/{id}        - 删除指定会话
 */
@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    public ChatHistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ConversationEntity>> getConversations() {
        try {
            List<ConversationEntity> list = chatHistoryService.getConversationList();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建新会话
     */
    @PostMapping("/conversations")
    public Result<ConversationEntity> createConversation(@RequestBody(required = false) Map<String, String> request) {
        try {
            String title = request != null ? request.get("title") : "新对话";
            if (title == null || title.isEmpty()) {
                title = "新对话";
            }
            ConversationEntity conversation = chatHistoryService.createConversation(title);
            return Result.success(conversation);
        } catch (Exception e) {
            return Result.error("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取指定会话的消息列表
     */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<MessageEntity>> getMessages(@PathVariable Long id) {
        try {
            List<MessageEntity> messages = chatHistoryService.getMessages(id);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取消息失败：" + e.getMessage());
        }
    }

    /**
     * 删除指定会话
     */
    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        try {
            chatHistoryService.deleteConversation(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除会话失败：" + e.getMessage());
        }
    }
}
