package com.jaiot.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 大模型配置
 *
 * 配置 ChatModel（普通会话）和 StreamingChatModel（流式输出）。
 * 兼容智谱 GLM / DeepSeek / 通义千问等任意 OpenAI 兼容 API。
 */
@Slf4j
@Configuration
public class ChatModelConfig {

    @Value("${zhipu.api-key}")
    private String apiKey;

    @Value("${zhipu.chat-model:glm-4-flash}")
    private String chatModel;

    @Value("${zhipu.temperature:0.7}")
    private Double temperature;

    @Value("${zhipu.max-tokens:2048}")
    private Integer maxTokens;

    /**
     * 普通对话模型（非流式）
     */
    @Bean
    public ChatModel chatLanguageModel() {
        log.info("🤖 初始化 ChatModel: model={}, temperature={}", chatModel, temperature);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModel)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 流式模型（SSE 打字机效果）
     */
    @Bean
    public StreamingChatModel streamingChatLanguageModel() {
        log.info("⚡ 初始化 StreamingChatModel: model={}", chatModel);
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModel)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
