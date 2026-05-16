package com.jaiot.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class AgentApplication {

    /**
     * 智谱 GLM API Key
     * 优先从环境变量读取，方便 CI / 生产部署。
     * 开发环境使用硬编码默认值。
     */
    private static final String ZHIPU_API_KEY = System.getenv().getOrDefault(
            "ZHIPU_API_KEY",
            "18331eaf6401403f814843e0879181ae.ZXZEJl91b6BMAroU"
    );

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
                .apiKey(ZHIPU_API_KEY)
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
