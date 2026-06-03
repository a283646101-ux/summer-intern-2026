package com.jaiot.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    /**
     * 配置大模型（与智谱 GLM 兼容的 OpenAI API）
     * 也支持 DeepSeek / 通义千问等任意 OpenAI 兼容接口
     */
    @Bean
    public ChatModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("ZHIPU_API_KEY"))
                .modelName("glm-4-flash")
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .temperature(0.7)
                .maxTokens(1024)
                .timeout(Duration.ofSeconds(30))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
