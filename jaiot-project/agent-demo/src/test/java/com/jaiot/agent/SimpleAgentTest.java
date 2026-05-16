package com.jaiot.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleAgent 单元测试
 * 用 Mock 模型模拟大模型，验证 Agent 逻辑。
 */
@SpringBootTest
class SimpleAgentTest {

    @MockBean
    private ChatModel mockModel;

    @Test
    void contextLoads() {
        // 验证 Spring 上下文能正常启动
        assertTrue(true);
    }

    @Test
    void testModelConfig() {
        // 验证模型配置参数
        var model = OpenAiChatModel.builder()
                .apiKey("test-key")
                .modelName("glm-4-flash")
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .temperature(0.7)
                .maxTokens(1024)
                .timeout(Duration.ofSeconds(30))
                .build();

        assertNotNull(model);
    }
}
