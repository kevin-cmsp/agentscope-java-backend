package com.corporate.finance.ai.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户画像实体
 * 基于历史对话构建的用户偏好画像
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_user_profile")
public class UserProfileEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户兴趣标签（JSON数组，如 ["天气","财务","活动策划"]）
     */
    private String interestTags;

    /**
     * 常用功能（JSON数组，如 ["weather","calculator","party"]）
     */
    private String frequentFeatures;

    /**
     * 偏好摘要（自然语言描述用户偏好）
     */
    private String preferenceSummary;

    /**
     * 对话风格偏好（formal-正式, casual-随意, technical-技术）
     */
    private String chatStyle;

    /**
     * 累计对话次数
     */
    private Integer totalChats;

    /**
     * 累计消息数
     */
    private Integer totalMessages;

    /**
     * 画像版本号（每次更新+1）
     */
    private Integer profileVersion;
}
