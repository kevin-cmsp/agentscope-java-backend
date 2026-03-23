package com.corporate.finance.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.corporate.finance.ai.system.dao.ConversationDao;
import com.corporate.finance.ai.system.dao.MessageDao;
import com.corporate.finance.ai.system.dao.UserProfileDao;
import com.corporate.finance.ai.system.entity.ConversationEntity;
import com.corporate.finance.ai.system.entity.MessageEntity;
import com.corporate.finance.ai.system.entity.UserProfileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户画像服务
 *
 * 基于历史对话构建用户偏好画像，实现个性化服务。
 *
 * 主要功能：
 * - 分析用户历史对话，提取兴趣标签和偏好
 * - 统计用户使用频率和常用功能
 * - 为 AI 回复提供个性化上下文
 * - 画像数据持久化到数据库和 Redis
 *
 * @author Corporate Finance AI Team
 * @version 1.0.0
 */
@Service
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    private static final String REDIS_PROFILE_KEY_PREFIX = "ai:profile:user:";
    private static final long REDIS_PROFILE_EXPIRE_HOURS = 48;

    private final UserProfileDao userProfileDao;
    private final ConversationDao conversationDao;
    private final MessageDao messageDao;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${model.services.dashscope.api-key}")
    private String dashscopeApiKey;

    @Value("${model.services.dashscope.base-url}")
    private String dashscopeBaseUrl;

    @Value("${model.services.dashscope.model}")
    private String dashscopeModel;

    public UserProfileService(UserProfileDao userProfileDao,
                              ConversationDao conversationDao,
                              MessageDao messageDao,
                              RedisTemplate<String, Object> redisTemplate) {
        this.userProfileDao = userProfileDao;
        this.conversationDao = conversationDao;
        this.messageDao = messageDao;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取用户画像（优先 Redis -> 数据库）
     */
    public UserProfileEntity getUserProfile(Long userId) {
        // 1. 从 Redis 获取
        UserProfileEntity profile = getFromRedis(userId);
        if (profile != null) {
            return profile;
        }

        // 2. 从数据库获取
        LambdaQueryWrapper<UserProfileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfileEntity::getUserId, userId);
        profile = userProfileDao.selectOne(wrapper);

        if (profile != null) {
            saveToRedis(userId, profile);
        }

        return profile;
    }

    /**
     * 获取用于 AI 上下文的个性化提示
     * 基于用户画像生成个性化提示文本
     */
    public String getPersonalizedPrompt(Long userId) {
        UserProfileEntity profile = getUserProfile(userId);
        if (profile == null || profile.getPreferenceSummary() == null) {
            return "";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("\n[用户画像信息]:\n");

        if (profile.getPreferenceSummary() != null && !profile.getPreferenceSummary().isEmpty()) {
            prompt.append("- 用户偏好: ").append(profile.getPreferenceSummary()).append("\n");
        }
        if (profile.getInterestTags() != null && !profile.getInterestTags().isEmpty()) {
            prompt.append("- 兴趣标签: ").append(profile.getInterestTags()).append("\n");
        }
        if (profile.getChatStyle() != null && !profile.getChatStyle().isEmpty()) {
            prompt.append("- 对话风格: ").append(profile.getChatStyle()).append("\n");
        }

        prompt.append("请根据以上用户画像信息，提供个性化的回复。\n");
        return prompt.toString();
    }

    /**
     * 分析并更新用户画像
     * 基于最近的对话历史重新分析用户偏好
     */
    @Transactional
    public UserProfileEntity analyzeAndUpdateProfile(Long userId) {
        logger.info("开始分析用户 {} 的画像...", userId);

        try {
            // 获取用户所有会话
            LambdaQueryWrapper<ConversationEntity> convWrapper = new LambdaQueryWrapper<>();
            convWrapper.eq(ConversationEntity::getUserId, userId);
            List<ConversationEntity> conversations = conversationDao.selectList(convWrapper);

            int totalChats = conversations.size();
            if (totalChats == 0) {
                logger.info("用户 {} 无对话历史，跳过画像分析", userId);
                return null;
            }

            // 获取最近对话的消息
            List<Long> recentConvIds = conversations.stream()
                    .map(ConversationEntity::getId)
                    .limit(10)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<MessageEntity> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.in(MessageEntity::getConversationId, recentConvIds)
                    .eq(MessageEntity::getRole, "user")
                    .orderByDesc(MessageEntity::getCreateTime)
                    .last("LIMIT 50");
            List<MessageEntity> userMessages = messageDao.selectList(msgWrapper);

            int totalMessages = userMessages.size();

            // 构建用户消息文本
            String userMessagesText = userMessages.stream()
                    .map(MessageEntity::getContent)
                    .collect(Collectors.joining("\n"));

            // 调用大模型分析用户画像
            String analysisResult = analyzeWithLLM(userMessagesText);

            // 解析分析结果并保存
            UserProfileEntity profile = getUserProfile(userId);
            if (profile == null) {
                profile = new UserProfileEntity();
                profile.setUserId(userId);
                profile.setProfileVersion(1);
            } else {
                profile.setProfileVersion(profile.getProfileVersion() + 1);
            }

            // 解析大模型返回的分析结果
            parseAnalysisResult(profile, analysisResult);
            profile.setTotalChats(totalChats);
            profile.setTotalMessages(totalMessages);

            // 保存到数据库
            if (profile.getId() == null) {
                userProfileDao.insert(profile);
            } else {
                userProfileDao.updateById(profile);
            }

            // 更新 Redis 缓存
            saveToRedis(userId, profile);

            logger.info("用户 {} 画像分析完成，版本: {}", userId, profile.getProfileVersion());
            return profile;

        } catch (Exception e) {
            logger.error("用户 {} 画像分析失败: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 增量更新统计信息（每次对话后调用）
     */
    public void incrementChatCount(Long userId) {
        UserProfileEntity profile = getUserProfile(userId);
        if (profile != null) {
            profile.setTotalChats(profile.getTotalChats() + 1);
            profile.setTotalMessages(profile.getTotalMessages() + 2); // user + assistant
            userProfileDao.updateById(profile);
            saveToRedis(userId, profile);
        }
    }

    /**
     * 调用大模型分析用户画像
     */
    private String analyzeWithLLM(String userMessages) {
        try {
            String prompt = "你是一个用户画像分析助手。请根据以下用户的对话历史，分析用户的特征和偏好。\n\n" +
                    "请按以下JSON格式输出分析结果：\n" +
                    "{\n" +
                    "  \"interestTags\": [\"标签1\", \"标签2\", \"标签3\"],\n" +
                    "  \"frequentFeatures\": [\"功能1\", \"功能2\"],\n" +
                    "  \"preferenceSummary\": \"一句话描述用户偏好\",\n" +
                    "  \"chatStyle\": \"formal/casual/technical\"\n" +
                    "}\n\n" +
                    "分析要求：\n" +
                    "1. interestTags: 提取3-5个用户感兴趣的主题标签\n" +
                    "2. frequentFeatures: 用户常用的功能（weather/calculator/data/knowledge/party/general）\n" +
                    "3. preferenceSummary: 50字以内的偏好摘要\n" +
                    "4. chatStyle: 判断用户的对话风格\n\n" +
                    "用户对话历史：\n" + userMessages + "\n\n" +
                    "分析结果：";

            return callDashScopeModel(prompt);
        } catch (Exception e) {
            logger.error("大模型分析用户画像失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析大模型返回的分析结果
     */
    private void parseAnalysisResult(UserProfileEntity profile, String analysisResult) {
        if (analysisResult == null || analysisResult.isEmpty()) {
            profile.setPreferenceSummary("暂无足够数据分析偏好");
            profile.setInterestTags("[]");
            profile.setFrequentFeatures("[]");
            profile.setChatStyle("casual");
            return;
        }

        try {
            // 尝试提取 JSON 中的字段
            String interestTags = extractJsonField(analysisResult, "interestTags");
            String frequentFeatures = extractJsonField(analysisResult, "frequentFeatures");
            String preferenceSummary = extractJsonStringField(analysisResult, "preferenceSummary");
            String chatStyle = extractJsonStringField(analysisResult, "chatStyle");

            profile.setInterestTags(interestTags != null ? interestTags : "[]");
            profile.setFrequentFeatures(frequentFeatures != null ? frequentFeatures : "[]");
            profile.setPreferenceSummary(preferenceSummary != null ? preferenceSummary : analysisResult);
            profile.setChatStyle(chatStyle != null ? chatStyle : "casual");
        } catch (Exception e) {
            // 解析失败，直接存储原始结果
            profile.setPreferenceSummary(analysisResult.length() > 500 ? analysisResult.substring(0, 500) : analysisResult);
            profile.setInterestTags("[]");
            profile.setFrequentFeatures("[]");
            profile.setChatStyle("casual");
        }
    }

    private String extractJsonField(String json, String fieldName) {
        String searchKey = "\"" + fieldName + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx + searchKey.length());
        if (colonIdx < 0) return null;
        int bracketStart = json.indexOf("[", colonIdx);
        if (bracketStart < 0) return null;
        int bracketEnd = json.indexOf("]", bracketStart);
        if (bracketEnd < 0) return null;
        return json.substring(bracketStart, bracketEnd + 1);
    }

    private String extractJsonStringField(String json, String fieldName) {
        String searchKey = "\"" + fieldName + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx + searchKey.length());
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf("\"", colonIdx + 1);
        if (quoteStart < 0) return null;
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd < 0) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    // ==================== Redis 缓存 ====================

    private void saveToRedis(Long userId, UserProfileEntity profile) {
        try {
            String key = REDIS_PROFILE_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(key, profile, REDIS_PROFILE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.warn("保存用户 {} 画像到 Redis 失败: {}", userId, e.getMessage());
        }
    }

    private UserProfileEntity getFromRedis(Long userId) {
        try {
            String key = REDIS_PROFILE_KEY_PREFIX + userId;
            Object data = redisTemplate.opsForValue().get(key);
            if (data instanceof UserProfileEntity) {
                return (UserProfileEntity) data;
            }
        } catch (Exception e) {
            logger.warn("从 Redis 获取用户 {} 画像失败: {}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 调用 DashScope 大模型
     */
    private String callDashScopeModel(String prompt) throws Exception {
        String url = dashscopeBaseUrl + "/services/aigc/text-generation/generation";

        String escapedPrompt = prompt.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        String messagesJson = "[{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}]";
        String jsonBody = "{\"model\":\"" + dashscopeModel + "\",\"input\":{\"messages\":" + messagesJson + "},\"parameters\":{\"max_tokens\":500,\"temperature\":0.3,\"top_p\":0.8}}";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + dashscopeApiKey);
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                String responseStr = response.toString();
                if (responseStr.contains("\"text\":\"")) {
                    int start = responseStr.indexOf("\"text\":\"") + 8;
                    int end = responseStr.indexOf("\"", start);
                    if (end > start) {
                        return responseStr.substring(start, end).trim()
                                .replace("\\n", "\n")
                                .replace("\\r", "");
                    }
                }
            }
        }
        return null;
    }
}
