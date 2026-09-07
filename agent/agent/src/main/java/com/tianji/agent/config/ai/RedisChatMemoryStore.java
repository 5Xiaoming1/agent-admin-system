package com.tianji.agent.config.ai;

import com.tianji.agent.config.redis.RedisUtils;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Redis会话记忆存储
 * 实现ChatMemoryStore接口，使用Redis持久化会话记忆
 * <p>
 * 会话记忆的TTL与JWT Token过期时间保持一致，用户登录过期后会话记忆同步失效。
 * 每次读取记忆时刷新TTL（滑动过期），确保活跃用户的会话不会中断。
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final RedisUtils redisUtils;

    /** 会话记忆过期时间（秒），与JWT user-ttl保持一致，默认7200秒=2小时 */
    @Value("${agent.knowledgebase.memory.ttl-seconds:7200}")
    private long memoryTtlSeconds;

    /**
     * 获取会话记忆，同时刷新TTL（滑动过期）
     * @param memoryId 会话标识
     * @return 会话记忆列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = memoryId.toString();
        Object value = redisUtils.get(key);
        if (value == null) {
            return List.of();
        }
        redisUtils.expire(key, memoryTtlSeconds);
        String json = value.toString();
        List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(json);
        return messages;
    }

    /**
     * 更新会话记忆，设置TTL过期时间
     * @param memoryId 会话标识
     * @param list 会话记忆列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String json = ChatMessageSerializer.messagesToJson(list);
        redisUtils.set(memoryId.toString(), json, memoryTtlSeconds);
    }

    /**
     * 删除会话记忆
     * @param memoryId 会话标识
     */
    @Override
    public void deleteMessages(Object memoryId) {
        redisUtils.delete(memoryId.toString());
    }
}