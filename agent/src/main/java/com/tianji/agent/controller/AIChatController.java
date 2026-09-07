package com.tianji.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.agent.config.common.Result;
import com.tianji.agent.config.jwt.BaseContext;
import com.tianji.agent.model.dto.BatchDeleteDTO;
import com.tianji.agent.model.dto.ChatDTO;
import com.tianji.agent.model.entity.ChatHistory;
import com.tianji.agent.model.vo.ChatHistoryVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.AssistantService;
import com.tianji.agent.service.ChatHistoryService;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;

/**
 * AI对话控制器
 * <p>
 * 提供流式和非流式对话接口，支持会话记忆、知识库检索和对话历史持久化。
 * 对话历史以<b>会话级</b>存储：一次完整会话（多轮对话）作为一条数据库记录。
 * </p>
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
@Tag(name = "AI对话", description = "AI智能对话接口")
public class AIChatController {

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private ChatMemoryStore chatMemoryStore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 流式对话接口（GET方式，兼容旧版调用）
     * <p>
     * 前端需传递 memoryId 用于会话隔离，每次对话使用相同的 memoryId 即可保持上下文。
     * 此接口不持久化对话历史。
     * </p>
     */
    @GetMapping(value = "/chat", produces = "text/html;charset=utf-8")
    @Operation(summary = "流式对话（GET）", description = "与AI进行流式对话，支持会话记忆和知识库检索")
    public Flux<String> chat(
            @Parameter(description = "会话标识，用于会话记忆隔离") @RequestParam String memoryId,
            @Parameter(description = "用户消息内容") @RequestParam String message) {
        return assistantService.chat(memoryId, message);
    }

    /**
     * 发送消息获取AI回复（非流式）
     * <p>
     * 自动将用户消息和AI回复以会话级方式持久化到 MySQL：
     * 传入 sessionId 则追加到指定会话；
     * 不传 sessionId 则自动追加到用户最新活跃会话，如无活跃会话则创建新会话。
     * 一次完整会话（多轮对话）作为一条数据库记录存储。
     * </p>
     */
    @PostMapping("/chat")
    @Operation(summary = "发送消息（非流式）", description = "发送消息到AI助手，返回完整回复。传入sessionId继续指定会话，不传则自动追加到最新活跃会话。对话历史以会话级存储。")
    public Result<Map<String, String>> chatPost(@Valid @RequestBody ChatDTO chatDTO) {
        Long userId = BaseContext.getCurrentId();
        String memoryId = buildMemoryId(userId);
        String sessionId = chatDTO.getSessionId();
        String userMessage = chatDTO.getMessage();

        chatHistoryService.saveMessage(userId, sessionId, "user", userMessage);

        String result = assistantService.chatSync(memoryId, userMessage);

        chatHistoryService.saveMessage(userId, sessionId, "assistant", result);

        ChatHistory latestSession = chatHistoryService.findLatestSession(userId);
        String finalSessionId = latestSession != null ? latestSession.getSessionId() : sessionId;

        Map<String, String> response = Map.of("content", result, "sessionId", finalSessionId);
        return Result.success(response);
    }

    /**
     * 发送消息（流式响应 SSE）
     * <p>
     * 以 SSE 流式方式逐 token 返回回复，自动将完整对话以会话级方式持久化到 MySQL：
     * 传入 sessionId 则追加到指定会话；
     * 不传 sessionId 则自动追加到用户最新活跃会话，如无活跃会话则创建新会话。
     * 一次完整会话（多轮对话）作为一条数据库记录存储。
     * </p>
     * <p>响应格式：data: {"content": "token"}\n\n ... data: [DONE]\n\n</p>
     */
    @PostMapping(value = "/chat/stream", produces = "text/event-stream;charset=utf-8")
    @Operation(summary = "发送消息（流式）", description = "发送消息到AI助手，以SSE流式方式逐token返回回复。传入sessionId继续指定会话，不传则自动追加到最新活跃会话。对话历史以会话级存储。")
    public Flux<String> chatStream(@Valid @RequestBody ChatDTO chatDTO) {
        Long userId = BaseContext.getCurrentId();
        String memoryId = buildMemoryId(userId);
        String sessionId = chatDTO.getSessionId();
        String userMessage = chatDTO.getMessage();

        chatHistoryService.saveMessage(userId, sessionId, "user", userMessage);

        StringBuilder fullResponse = new StringBuilder();

        return assistantService.chat(memoryId, userMessage)
                .doOnNext(fullResponse::append)
                .map(this::toSseEvent)
                .concatWith(Flux.just(toSessionIdEvent(userId, sessionId)))
                .concatWith(Flux.just("[DONE]"))
                .doOnComplete(() -> {
                    String result = fullResponse.toString();
                    if (!result.isEmpty()) {
                        chatHistoryService.saveMessage(userId, sessionId, "assistant", result);
                    }
                });
    }

    /**
     * 查询当前用户的会话历史列表
     * <p>
     * 分页查询，按会话最后更新时间倒序排列。
     * 每条记录代表一次完整会话，包含该会话中所有轮次的对话消息。
     * </p>
     */
    @GetMapping("/history")
    @Operation(summary = "查询会话历史", description = "分页查询当前用户的会话历史列表，每条记录代表一次完整会话（包含多轮对话），按更新时间倒序排列")
    public Result<PageVO<ChatHistoryVO>> history(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = BaseContext.getCurrentId();
        PageVO<ChatHistoryVO> result = chatHistoryService.page(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 清除当前用户的会话记忆和对话历史
     * <p>
     * 同时清除 Redis 中的会话记忆和 MySQL 中的会话历史记录，开始全新对话。
     * 注意：清除后原有 sessionId 将失效，需重新创建会话。
     * </p>
     */
    @DeleteMapping("/memory/clear")
    @Operation(summary = "清除会话记忆", description = "清除当前用户的所有会话记忆（Redis）和对话历史（MySQL），开始全新对话")
    public Result<Void> clearMemory() {
        Long userId = BaseContext.getCurrentId();
        String memoryId = buildMemoryId(userId);
        chatMemoryStore.deleteMessages(memoryId);
        chatHistoryService.clearByUserId(userId);
        return Result.success(null);
    }

    /**
     * 删除会话历史
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。传递几个 ID 就删除几个，传 1 个则删 1 个。
     * </p>
     *
     * @param dto 删除请求参数，包含会话记录 ID 集合
     * @return 统一响应结果，删除成功
     */
    @PostMapping("/history/delete")
    @Operation(summary = "删除会话历史", description = "逻辑删除指定会话记录，传递 ids 集合，传几个删几个")
    public Result<Void> deleteHistory(@Valid @RequestBody BatchDeleteDTO dto) {
        chatHistoryService.deleteByIds(dto.getIds());
        return Result.success("删除成功", null);
    }

    /**
     * 构建会话记忆标识
     */
    private String buildMemoryId(Long userId) {
        return "user:" + (userId != null ? userId : "anonymous");
    }

    /**
     * 获取或创建会话标识
     * <p>
     * 如果请求中已传入 sessionId 则直接使用，否则生成新的 UUID 作为会话标识。
     * </p>
     */
    private String getOrCreateSessionId(ChatDTO chatDTO) {
        if (chatDTO.getSessionId() != null && !chatDTO.getSessionId().isEmpty()) {
            return chatDTO.getSessionId();
        }
        return UUID.randomUUID().toString();
    }

    /**
     * 将 token 文本转换为 SSE 事件格式的 JSON
     */
    private String toSseEvent(String token) {
        try {
            return objectMapper.writeValueAsString(Map.of("content", token));
        } catch (Exception e) {
            return "{\"content\":\"" + escapeJson(token) + "\"}";
        }
    }

    /**
     * 构建包含 sessionId 的 SSE 事件
     * <p>
     * 在流式响应的末尾（DONE 之前）发送 sessionId，前端可据此保存会话标识，
     * 后续请求传入该 sessionId 即可继续同一会话。
     * 如果传入的 sessionId 为 null，则查询用户最新活跃会话的 sessionId。
     * </p>
     */
    private String toSessionIdEvent(Long userId, String sessionId) {
        String finalSessionId = sessionId;
        if (finalSessionId == null || finalSessionId.isEmpty()) {
            ChatHistory latestSession = chatHistoryService.findLatestSession(userId);
            if (latestSession != null) {
                finalSessionId = latestSession.getSessionId();
            }
        }
        try {
            return objectMapper.writeValueAsString(Map.of("sessionId", finalSessionId));
        } catch (Exception e) {
            return "{\"sessionId\":\"" + finalSessionId + "\"}";
        }
    }

    /**
     * 简单的 JSON 字符串转义
     */
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}