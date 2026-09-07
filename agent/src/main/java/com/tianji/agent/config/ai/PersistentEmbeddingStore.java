package com.tianji.agent.config.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 持久化向量存储（AI助手专用知识库）
 * <p>
 * 包装 InMemoryEmbeddingStore，在服务启动时从磁盘加载向量数据，
 * 在服务关闭时自动保存。避免每次重启都需要重新向量化所有文档。
 * </p>
 * <p>
 * 持久化文件路径：{storageDir}/ai-embedding-store.json
 * AI知识库文档目录：{storageDir}/ai-documents/
 * </p>
 */
@Slf4j
@Component
public class PersistentEmbeddingStore {

    private final InMemoryEmbeddingStore<TextSegment> store;

    private final Path persistenceFile;

    private final EmbeddingModel embeddingModel;

    private final String knowledgebaseStorageDir;

    private final String aiDocumentSubdir;

    private final int splitterMaxSegmentSize;

    private final int splitterMaxOverlapSize;

    public PersistentEmbeddingStore(
            EmbeddingModel embeddingModel,
            @Value("${agent.knowledgebase.storage-dir:data/knowledgebase}") String knowledgebaseStorageDir,
            @Value("${agent.knowledgebase.ai-document-subdir:ai-documents}") String aiDocumentSubdir,
            @Value("${agent.knowledgebase.splitter.max-segment-size:500}") int splitterMaxSegmentSize,
            @Value("${agent.knowledgebase.splitter.max-overlap-size:100}") int splitterMaxOverlapSize) {

        this.embeddingModel = embeddingModel;
        this.knowledgebaseStorageDir = knowledgebaseStorageDir;
        this.aiDocumentSubdir = aiDocumentSubdir;
        this.splitterMaxSegmentSize = splitterMaxSegmentSize;
        this.splitterMaxOverlapSize = splitterMaxOverlapSize;

        String projectRoot = System.getProperty("user.dir");
        this.persistenceFile = Paths.get(projectRoot, knowledgebaseStorageDir, "ai-embedding-store.json");

        this.store = loadOrCreateStore();
    }

    /**
     * 获取底层向量存储
     */
    public InMemoryEmbeddingStore<TextSegment> getStore() {
        return store;
    }

    /**
     * 服务关闭时自动保存向量数据到磁盘
     */
    @PreDestroy
    public void saveToDisk() {
        try {
            Files.createDirectories(persistenceFile.getParent());
            store.serializeToFile(persistenceFile.toString());
            log.info("向量库已持久化到磁盘: {}", persistenceFile);
        } catch (IOException e) {
            log.error("向量库持久化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从磁盘加载向量数据，如果文件不存在则重新构建
     */
    private InMemoryEmbeddingStore<TextSegment> loadOrCreateStore() {
        File file = persistenceFile.toFile();
        if (file.exists()) {
            try {
                InMemoryEmbeddingStore<TextSegment> loadedStore =
                        InMemoryEmbeddingStore.fromFile(persistenceFile.toString());
                log.info("从磁盘加载向量库成功，文件: {}", persistenceFile);
                return loadedStore;
            } catch (Exception e) {
                log.warn("从磁盘加载向量库失败，将重新构建: {}", e.getMessage());
            }
        }

        log.info("向量库文件不存在，开始重新构建向量库...");
        return buildStore();
    }

    /**
     * 重新构建向量库：加载文档 → 分割 → 嵌入 → 存储
     */
    private InMemoryEmbeddingStore<TextSegment> buildStore() {
        String projectRoot = System.getProperty("user.dir");
        String documentsPath = projectRoot + File.separator + knowledgebaseStorageDir + File.separator + aiDocumentSubdir;

        InMemoryEmbeddingStore<TextSegment> newStore = new InMemoryEmbeddingStore<>();

        File docDir = new File(documentsPath);
        if (!docDir.exists() || !docDir.isDirectory()) {
            log.warn("AI知识库文档目录不存在，向量库为空: {}", documentsPath);
            return newStore;
        }

        List<Document> documents = FileSystemDocumentLoader.loadDocuments(documentsPath);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(newStore)
                .documentSplitter(DocumentSplitters.recursive(splitterMaxSegmentSize, splitterMaxOverlapSize))
                .embeddingModel(embeddingModel)
                .build();
        ingestor.ingest(documents);

        log.info("AI向量库构建完成，文档目录: {}", documentsPath);
        return newStore;
    }
}