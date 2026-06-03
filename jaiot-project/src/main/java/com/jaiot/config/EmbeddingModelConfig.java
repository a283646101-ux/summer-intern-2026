package com.jaiot.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌入模型配置（RAG 用）
 *
 * 将文本转为向量表示，用于知识库的相似度检索。
 * 使用 text-embedding-3-small，输出 512 维向量，兼顾性能和效率。
 */
@Slf4j
@Configuration
public class EmbeddingModelConfig {

    @Value("${zhipu.api-key}")
    private String apiKey;

    @Value("${zhipu.embedding-model:text-embedding-3-small}")
    private String modelName;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("📐 初始化 EmbeddingModel: model={}", modelName);
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .dimensions(512)
                .build();
    }
}
