package com.jaiot.agent;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 最简单的 LangChain4j Agent
 * <p>
 * 用 {@link AiServices} 把大模型包装成带 System Prompt 的接口，
 * 用户输入一句话，Agent 调用大模型回答。
 */
@Slf4j
@Component
public class SimpleAgent {

    /**
     * Agent 行为定义 —— 一个普通的 Java 接口，
     * LangChain4j 会自动生成实现。
     */
    public interface Assistant {

        @SystemMessage("""
                你是一个有用的智能助手，名叫「虾哥」。
                你是蓝色的科技龙虾，戴着墨镜，擅长用幽默的方式回答问题。
                用中文回答，保持简短有趣。
                """)
        String chat(String userMessage);
    }

    private final Assistant assistant;

    public SimpleAgent(ChatLanguageModel model) {
        this.assistant = AiServices.create(Assistant.class, model);
        log.info("🦞 虾哥 Agent 已就绪！");
    }

    /**
     * 用户输入一句话，Agent 返回回答
     */
    public String say(String userMessage) {
        log.debug("用户输入: {}", userMessage);
        String reply = assistant.chat(userMessage);
        log.debug("Agent 回复: {}", reply);
        return reply;
    }
}
