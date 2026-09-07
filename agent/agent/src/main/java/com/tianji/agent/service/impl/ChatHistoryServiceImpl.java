package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tianji.agent.mapper.ChatHistoryMapper;
import com.tianji.agent.model.entity.ChatHistory;
import com.tianji.agent.model.vo.ChatHistoryVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对话历史服务实现（会话级存储）
 * <p>
 * 以会话为单位存储对话历史，每次会话（多轮对话）作为一条数据库记录。
 * 核心逻辑：
 * <ul>
 *   <li>新增消息时，先查询 sessionId 对应的会话是否存在</li>
 *   <li>存在则解析 messages JSON 数组，追加新消息后更新</li>
 *   <li>不存在则创建新会话，首条用户消息作为会话标题</li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryMapper chatHistoryMapper;
    private final ObjectMapper objectMapper;

    /** ISO 8601 日期格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatHistoryServiceImpl(ChatHistoryMapper chatHistoryMapper) {
        this.chatHistoryMapper = chatHistoryMapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 向会话中追加一条消息
     * <p>
     * 如果 sessionId 对应的会话不存在，则创建新会话记录；
     * 如果已存在，则解析现有 messages JSON 数组，追加新消息后更新。
     * 如果 sessionId 为 null 或空，则自动查找该用户最新的活跃会话进行追加；
     * 如果用户没有任何活跃会话，则创建新会话。
     * 首条用户消息自动作为会话标题。
     * </p>
     */
    @Override
    public void saveMessage(Long userId, String sessionId, String role, String content) {
        log.info("保存会话消息: userId={}, sessionId={}, role={}", userId, sessionId, role);

        ChatHistory existingSession = null;

        // 如果传入了 sessionId，则根据 sessionId 查找会话
        if (sessionId != null && !sessionId.isEmpty()) {
            existingSession = chatHistoryMapper.selectOne(
                    new LambdaQueryWrapper<ChatHistory>().eq(ChatHistory::getSessionId, sessionId));
            
            // 如果 sessionId 不存在，直接创建新会话（不查找旧会话）
            if (existingSession == null) {
                createNewSession(userId, sessionId, role, content);
                return;
            }
        } else {
            // 如果没有传入 sessionId，则查找用户最新的活跃会话
            existingSession = findLatestActiveSession(userId);
        }

        // 如果找到了现有会话，则追加消息
        if (existingSession != null) {
            appendToSession(existingSession, role, content);
        } else {
            // 没有找到现有会话，创建新会话
            createNewSession(userId, sessionId, role, content);
        }
    }

    /**
     * 查找用户最新的活跃会话
     * <p>
     * 按更新时间倒序排列，返回第一条未删除的会话记录。
     * </p>
     */
    private ChatHistory findLatestActiveSession(Long userId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId)
                .orderByDesc(ChatHistory::getUpdateTime)
                .last("LIMIT 1");
        return chatHistoryMapper.selectOne(wrapper);
    }

    @Override
    public PageVO<ChatHistoryVO> page(Long userId, int page, int pageSize) {
        log.info("分页查询会话历史: userId={}, page={}, pageSize={}", userId, page, pageSize);

        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId)
                .orderByDesc(ChatHistory::getUpdateTime);

        Page<ChatHistory> pageResult = chatHistoryMapper.selectPage(
                new Page<>(page, pageSize), wrapper);

        List<ChatHistoryVO> records = pageResult.getRecords().stream()
                .map(ChatHistoryVO::fromEntity)
                .collect(Collectors.toList());

        return PageVO.<ChatHistoryVO>builder()
                .list(records)
                .page((long) pageResult.getCurrent())
                .pageSize((long) pageResult.getSize())
                .total((long) pageResult.getTotal())
                .build();
    }

    @Override
    public void clearByUserId(Long userId) {
        log.info("清除用户所有会话历史: userId={}", userId);

        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId);
        chatHistoryMapper.delete(wrapper);

        log.info("用户会话历史已清除: userId={}", userId);
    }

    @Override
    public ChatHistory findLatestSession(Long userId) {
        return findLatestActiveSession(userId);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        log.info("逻辑删除会话: ids={}", ids);
        chatHistoryMapper.deleteBatchIds(ids);
        log.info("会话删除完成: 数量={}", ids.size());
    }

    /**
     * 创建新会话
     * <p>
     * 首条消息写入 messages JSON 数组，若为首条用户消息则设为会话标题。
     * </p>
     */
    private void createNewSession(Long userId, String sessionId, String role, String content) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildMessageItem(role, content));

        String messagesJson = toJson(messages);
        String title = "user".equals(role) ? truncateTitle(content) : "新会话";
        String finalSessionId = (sessionId != null && !sessionId.isEmpty()) ? sessionId : UUID.randomUUID().toString();

        ChatHistory session = ChatHistory.builder()
                .userId(userId)
                .sessionId(finalSessionId)
                .title(title)
                .messages(messagesJson)
                .messageCount(1)
                .build();

        chatHistoryMapper.insert(session);
        log.info("新会话已创建: sessionId={}, title={}", finalSessionId, title);
    }

    /**
     * 向已有会话追加消息
     * <p>
     * 解析现有 messages JSON 数组，追加新消息，更新 messageCount 和 updateTime。
     * </p>
     */
    private void appendToSession(ChatHistory session, String role, String content) {
        List<Map<String, String>> messages = parseMessages(session.getMessages());
        messages.add(buildMessageItem(role, content));

        session.setMessages(toJson(messages));
        session.setMessageCount(messages.size());
        session.setUpdateTime(LocalDateTime.now());

        chatHistoryMapper.updateById(session);
        log.info("消息已追加到会话: sessionId={}, 当前消息数={}", session.getSessionId(), messages.size());
    }

    /**
     * 构建单条消息的 Map 结构
     */
    private Map<String, String> buildMessageItem(String role, String content) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("role", role);
        item.put("content", content);
        item.put("time", LocalDateTime.now().format(DATE_FORMATTER));
        return item;
    }

    /**
     * 将消息列表序列化为 JSON 字符串
     */
    private String toJson(List<Map<String, String>> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (Exception e) {
            log.error("消息序列化失败", e);
            return "[]";
        }
    }

    /**
     * 解析 messages JSON 字符串为消息列表
     */
    private List<Map<String, String>> parseMessages(String messagesJson) {
        try {
            if (messagesJson == null || messagesJson.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(messagesJson, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            log.error("消息解析失败，将返回空列表", e);
            return new ArrayList<>();
        }
    }

    /**
     * 截断标题，超过 30 个字符追加省略号
     */
    private String truncateTitle(String content) {
        if (content == null) {
            return "新会话";
        }
        return content.length() > 30 ? content.substring(0, 30) + "..." : content;
    }
}