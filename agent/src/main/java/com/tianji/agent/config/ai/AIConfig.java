package com.tianji.agent.config.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 * 配置向量数据库(知识库)、检索器、会话记忆提供者
 * <p>
 * 向量数据库使用 PersistentEmbeddingStore 实现文件持久化，服务重启后无需重新向量化。
 * </p>
 */
@Configuration
public class AIConfig {

    /** 检索最小相似度阈值（0.0~1.0） */
    @Value("${agent.knowledgebase.retriever.min-score:0.5}")
    private double retrieverMinScore;

    /** 检索最大返回结果数 */
    @Value("${agent.knowledgebase.retriever.max-results:3}")
    private int retrieverMaxResults;

    /** 会话记忆最大保留消息数 */
    @Value("${agent.knowledgebase.memory.max-messages:20}")
    private int memoryMaxMessages;

    /** 向量嵌入模型（将文本转为向量） */
    @Autowired
    private EmbeddingModel embeddingModel;

    /** 持久化向量存储（服务重启后自动从磁盘恢复） */
    @Autowired
    private PersistentEmbeddingStore persistentEmbeddingStore;

    /** Redis 会话记忆存储（持久化对话历史） */
    @Autowired
    private ChatMemoryStore redisChatMemoryStore;

    /**
     * 向量数据库操作对象 -- 从 PersistentEmbeddingStore 获取持久化的向量存储
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return persistentEmbeddingStore.getStore();
    }

    /**
     * 构建向量数据库检索对象 -- 从数据库检索相似文档
     */
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .minScore(retrieverMinScore)
                .maxResults(retrieverMaxResults)
                .embeddingModel(embeddingModel)
                .build();
    }

    /**
     * 会话记忆提供者 -- 使用Redis持久化会话记忆
     */
    @Bean
    public dev.langchain4j.memory.chat.ChatMemoryProvider chatMemoryProvider() {
        return new dev.langchain4j.memory.chat.ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(memoryMaxMessages)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
    }
}