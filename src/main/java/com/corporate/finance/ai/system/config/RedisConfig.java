package com.corporate.finance.ai.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author wuzh
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 使用字符串序列化
        template.setKeySerializer(new StringRedisSerializer());

        // Value 使用 JSON 序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash 的 Key 使用字符串序列化
        template.setHashKeySerializer(new StringRedisSerializer());

        // Hash 的 Value 使用 JSON 序列化
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
