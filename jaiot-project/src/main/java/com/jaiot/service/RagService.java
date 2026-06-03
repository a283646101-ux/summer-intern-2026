package com.jaiot.service;

import com.jaiot.entity.RagDocument;
import com.jaiot.repository.RagDocumentRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG 检索服务
 *
 * 加载养护知识文档 → 切分 → 向量化 → 存储 → 检索。
 * 支持在运行时通过 API 查询知识库。
 */
@Slf4j
@Service
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final RagDocumentRepository ragDocumentRepository;
    private final ResourceLoader resourceLoader;

    @Value("${rag.max-results:3}")
    private int maxResults;

    @Value("${rag.min-score:0.6}")
    private double minScore;

    /** 内存向量存储（生产环境应替换为 Chroma / PGVector） */
    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    public RagService(EmbeddingModel embeddingModel,
            RagDocumentRepository ragDocumentRepository,
            ResourceLoader resourceLoader) {
        this.embeddingModel = embeddingModel;
        this.ragDocumentRepository = ragDocumentRepository;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 启动时自动加载内置知识文档
     */
    @PostConstruct
    public void init() {
        try {
            loadBuiltinDocument("classpath:rag/plant-care-knowledge.txt", "内置植物养护知识库");
            log.info("📚 RAG 知识库初始化完成");
        } catch (Exception e) {
            log.warn("⚠️ RAG 知识库初始化跳过: {}", e.getMessage());
        }
    }

    /**
     * 加载并向量化一篇文档
     */
    public void loadBuiltinDocument(String resourcePath, String displayName) {
        try {
            Resource resource = resourceLoader.getResource(resourcePath);
            if (!resource.exists()) {
                log.warn("RAG 文档不存在: {}", resourcePath);
                return;
            }

            // 读取文档内容
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            String text = content.toString();
            if (text.isBlank()) {
                log.warn("RAG 文档内容为空: {}", resourcePath);
                return;
            }

            // 切分文档为块
            Document document = Document.from(text);
            DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);
            List<TextSegment> segments = splitter.split(document);

            // 向量化并存储
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            embeddingStore.addAll(embeddings, segments);

            // 记录文档元信息
            RagDocument doc = RagDocument.builder()
                    .fileName(displayName)
                    .summary(text.length() > 200 ? text.substring(0, 200) + "..." : text)
                    .chunkCount(segments.size())
                    .createTime(LocalDateTime.now())
                    .build();
            ragDocumentRepository.save(doc);

            log.info("✅ RAG 文档已加载: {} ({} 个文档块, {} 字符)",
                    displayName, segments.size(), text.length());

        } catch (Exception e) {
            log.error("RAG 文档加载失败: {}", e.getMessage());
        }
    }

    /**
     * 检索知识库，返回相关文档块
     */
    public String search(String query) {
        // 将问题向量化
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 向量相似度检索
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        if (matches.isEmpty()) {
            return "未找到相关知识。";
        }

        // 组装检索结果
        StringBuilder result = new StringBuilder();
        result.append("根据知识库，找到以下相关信息：\n\n");
        for (int i = 0; i < matches.size(); i++) {
            EmbeddingMatch<TextSegment> match = matches.get(i);
            result.append("---\n");
            result.append("【相关段落 ").append(i + 1).append("】\n");
            result.append(match.embedded().text()).append("\n");
        }

        log.info("🔍 RAG 检索: query={}, results={}", query, matches.size());
        return result.toString();
    }

    /**
     * 使用 RAG 增强回答问题（结合 LLM 生成）
     */
    public String ask(String query) {
        String knowledge = search(query);
        // 将检索结果和问题一起返回给上层处理
        return knowledge;
    }

    /**
     * 获取已导入的文档列表
     */
    public List<RagDocument> listDocuments() {
        return ragDocumentRepository.findAll();
    }
}
