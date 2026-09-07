package com.tianji.agent.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * AI助手服务接口
 * 使用@AiService声明式编程，自动集成Prompt模板、记忆管理、RAG检索
 * <p>
 * chatModel 用于非流式同步调用（返回 String），
 * streamingChatModel 用于流式异步调用（返回 Flux&lt;String&gt;）。
 * </p>
 */
@AiService(
        chatModel = "openAiChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",
        streamingChatModel = "openAiStreamingChatModel",
        tools = {"systemBusinessTool"}
)
public interface AssistantService {

    /**
     * 流式对话
     * @param memoryId 会话标识（用于会话记忆隔离）
     * @param message  用户消息
     * @return 流式响应
     */
    @SystemMessage(fromResource = "prompts/system-prompt.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);

    /**
     * 非流式同步对话
     * @param memoryId 会话标识（用于会话记忆隔离）
     * @param message  用户消息
     * @return 完整的AI回复
     */
    @SystemMessage(fromResource = "prompts/system-prompt.txt")
    String chatSync(@MemoryId String memoryId, @UserMessage String message);
}