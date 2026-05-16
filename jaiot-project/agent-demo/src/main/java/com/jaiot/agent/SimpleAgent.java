package com.jaiot.agent;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LangChain4j Agent —— 虾哥 🦞
 * <p>
 * 注册了 {@link SensorTool}，Agent 会在需要时主动调用工具
 * 获取传感器数据，然后根据温湿度做出灌溉决策。
 */
@Slf4j
@Component
public class SimpleAgent {

    /**
     * Agent 行为接口 —— LangChain4j 自动生成实现
     */
    public interface Assistant {

        /**
         * 通用对话
         */
        @SystemMessage("""
                你是一个有用的智能助手，名叫「虾哥」。
                你是蓝色的科技龙虾，戴着墨镜，擅长用幽默的方式回答问题。
                用中文回答，保持简短有趣。
                """)
        String chat(String userMessage);

        /**
         * 灌溉决策 —— Agent 会调用 SensorTool 查询数据后自动判断
         */
        @SystemMessage("""
                你叫「虾哥」，是一个农业物联网 AI 助手。
                你现在有一个工具可用：queryLatestSensorData(sensorId)，可以查询指定传感器的实时温湿度数据。

                你的任务：
                1. 先调用工具获取指定传感器的温湿度数据
                2. 根据以下规则做出灌溉决策：
                   - 如果温度 > 30°C 或 湿度 < 40% → 建议立即浇水，决策为 "water"
                   - 如果温度在 20°C~30°C 且 湿度在 40%~70% → 不需要浇水，决策为 "no-water"
                   - 如果湿度 > 70% → 土壤过湿，不需要浇水，决策为 "no-water"，并提示注意排水
                3. 以 JSON 格式输出决策结果（不要加 markdown 代码块标记），包含以下字段：
                   sensorId, temperature, humidity, decision, reason, advice

                注意：直接输出纯 JSON，不要包含 ```json ``` 标记。
                """)
        String decideIrrigation(String sensorId);
    }

    private final Assistant assistant;

    public SimpleAgent(ChatModel model, SensorTool sensorTool) {
        this.assistant = AiServices.<Assistant>builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(sensorTool)  // 注册传感器工具
                .build();
        log.info("🦞 虾哥 Agent 已就绪（带传感器工具）！");
    }

    /**
     * 通用对话
     */
    public String say(String userMessage) {
        log.debug("💬 用户输入: {}", userMessage);
        String reply = assistant.chat(userMessage);
        log.debug("🦞 Agent 回复: {}", reply);
        return reply;
    }

    /**
     * 灌溉决策 —— Agent 自动调用传感器工具 + 大模型分析
     *
     * @param sensorId 传感器编号
     * @return Agent 返回的 JSON 字符串（含 decision / reason / advice）
     */
    public String decideIrrigation(String sensorId) {
        log.info("🌱 触发灌溉决策，传感器: {}", sensorId);
        long start = System.currentTimeMillis();
        String result = assistant.decideIrrigation(sensorId);
        long elapsed = System.currentTimeMillis() - start;
        log.info("✅ 决策完成 ({}ms), 结果: {}", elapsed, result);
        return result;
    }
}
