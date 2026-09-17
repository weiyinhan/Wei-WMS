package org.jeecg.modules.airag.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 Redis 的 Spring AI ChatMemory 实现。
 * <p>
 * 直接使用 conversationId 作为 Redis key 的一部分，以 JSON 形式存储 Spring AI Message 列表，
 * 与 MessageChatMemoryAdvisor 的调用契约（传入纯 conversationId 字符串）保持一致。
 * </p>
 */
@Slf4j
@Component
public class RedisChatMemory implements ChatMemory {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "airag:chat:memory:";

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        String redisKey = PREFIX + conversationId;
        try {
            // 取出已有历史消息，追加新消息后整体写回
            List<Message> allMessages = new ArrayList<>(get(conversationId));
            allMessages.addAll(messages);
            List<SpringAIMsg> msgList = allMessages.stream()
                    .map(SpringAIMsg::new)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(msgList));
        } catch (Exception e) {
            log.error("[RedisChatMemory] 保存消息失败, conversationId={}", conversationId, e);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        String redisKey = PREFIX + conversationId;
        String json = redisTemplate.opsForValue().get(redisKey);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<SpringAIMsg> msgList = JSON.parseObject(json, new TypeReference<List<SpringAIMsg>>() {
            });
            return msgList.stream()
                    .map(SpringAIMsg::toMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[RedisChatMemory] 读取消息失败, conversationId={}", conversationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        String redisKey = PREFIX + conversationId;
        redisTemplate.delete(redisKey);
    }
}
