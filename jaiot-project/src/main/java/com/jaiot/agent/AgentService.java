package com.jaiot.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaiot.dto.DecisionResponse;
import com.jaiot.entity.ChatMessage;
import com.jaiot.entity.DecisionLog;
import com.jaiot.repository.ChatMessageRepository;
import com.jaiot.repository.DecisionLogRepository;
import com.jaiot.repository.SensorDataRepository;
import com.jaiot.service.RagService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Agent 核心服务
 *
 * 提供三种模式：
 * 1. chat() — 通用对话（带记忆 + RAG 增强）
 * 2. decide() — 完整决策（非流式）
 * 3. streamDecide() — 流式决策（SSE）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final TemperatureTool temperatureTool;
    private final HumidityTool humidityTool;
    private final LightTool lightTool;
    private final SoilMoistureTool soilMoistureTool;
    private final ChatMessageRepository chatMessageRepository;
    private final DecisionLogRepository decisionLogRepository;
    private final RagService ragService;
    private final ObjectMapper objectMapper;

    private AgentAssistant assistant;

    @PostConstruct
    public void init() {
        this.assistant = AiServices.builder(AgentAssistant.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .tools(temperatureTool, humidityTool, lightTool, soilMoistureTool)
                .build();
        log.info("Agent ready: 4 tools + streaming + memory + RAG");
    }

    // ==================== 通用对话（带记忆 + RAG） ====================

    /**
     * 通用对话：加载历史记忆 + RAG 知识检索 → 发给 LLM → 保存
     */
    public String chat(String message) {
        return chat(message, "default-session");
    }

    public String chat(String message, String sessionId) {
        log.info("Chat [{}]: {}", sessionId, message);

        // 保存用户消息
        saveMsg(sessionId, "user", message);

        // 1. 加载历史（最近 10 条）
        List<ChatMessage> history = chatMessageRepository
                .findBySessionIdOrderByCreateTimeAsc(sessionId);
        List<ChatMessage> recent = history.size() > 10
                ? history.subList(history.size() - 10, history.size())
                : history;

        // 2. 检索 RAG 知识
        String ragKnowledge = "";
        try {
            String searchResult = ragService.search(message);
            if (searchResult != null && !searchResult.contains("未找到相关知识")) {
                ragKnowledge = "\n\n参考知识库信息：\n" + searchResult;
            }
        } catch (Exception e) {
            log.warn("RAG search failed: {}", e.getMessage());
        }

        // 3. 构建对话上下文
        StringBuilder context = new StringBuilder();
        context.append("以下是之前的对话记录：\n");
        for (ChatMessage msg : recent) {
            String role = "user".equals(msg.getRole()) ? "用户" : "虾哥";
            context.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        context.append("\n---\n");
        context.append("用户的新问题：").append(message);
        context.append(ragKnowledge);

        // 4. 调用 LLM（使用系统提示 + 上下文）
        String reply = assistant.chat(context.toString());

        // 5. 保存 Assistant 回复
        saveMsg(sessionId, "assistant", reply);

        return reply;
    }

    // ==================== 完整决策 ====================

    public DecisionResponse decide(String deviceId) {
        log.info("Decide: device={}", deviceId);
        long start = System.currentTimeMillis();

        try {
            String rawJson = assistant.decide(buildDecisionInput(deviceId));
            long elapsed = System.currentTimeMillis() - start;
            log.info("Decision done ({}ms)", elapsed);

            DecisionResponse response = parseDecision(rawJson, deviceId);
            saveDecisionLog(deviceId, rawJson, response);
            return response;
        } catch (Exception e) {
            log.error("Decision failed: {}", e.getMessage());
            DecisionResponse fb = DecisionResponse.builder()
                    .deviceId(deviceId).decision("error")
                    .reason("AI unavailable: " + e.getMessage()).build();
            saveDecisionLog(deviceId, "ERROR", fb);
            return fb;
        }
    }

    // ==================== 流式决策（SSE） ====================

    public void streamDecide(String deviceId,
                             Consumer<String> onToolResult,
                             Consumer<String> onToken,
                             Consumer<DecisionResponse> onComplete,
                             Consumer<Throwable> onError) {
        try {
            // Phase 1: 获取传感器数据
            onToolResult.accept("Querying temperature...");
            double temp = temperatureTool.getTemperature(deviceId);

            onToolResult.accept("Querying humidity...");
            double hum = humidityTool.getHumidity(deviceId);

            onToolResult.accept("Querying soil moisture...");
            double soil = soilMoistureTool.getSoilMoisture(deviceId);

            onToolResult.accept("Querying light...");
            int lux = lightTool.getLight(deviceId);

            onToolResult.accept(String.format(
                    "Data: T=%.1fC H=%.1f%% S=%.1f%% L=%d Lux",
                    temp, hum, soil, lux));

            log.info("Sensor data: {}C {}% {}% {} Lux", temp, hum, soil, lux);

            // Phase 2: 流式生成决策
            StringBuilder fullResp = new StringBuilder();

            streamingChatModel.chat(
                    List.of(
                            SystemMessage.from(getSystemPrompt()),
                            UserMessage.from(buildDecisionInput(deviceId))
                    ),
                    new StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String partial) {
                            fullResp.append(partial);
                            onToken.accept(partial);
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse response) {
                            try {
                                String text = fullResp.toString().trim();
                                DecisionResponse result = parseDecision(text, deviceId);
                                if (result.getTemperature() == null || result.getTemperature() == 0)
                                    result.setTemperature(temp);
                                if (result.getHumidity() == null || result.getHumidity() == 0)
                                    result.setHumidity(hum);
                                if (result.getSoilMoisture() == null || result.getSoilMoisture() == 0)
                                    result.setSoilMoisture(soil);
                                if (result.getLight() == null || result.getLight() == 0)
                                    result.setLight(lux);
                                saveDecisionLog(deviceId, text, result);
                                onComplete.accept(result);
                            } catch (Exception e) {
                                log.error("Parse error: {}", e.getMessage());
                                DecisionResponse fb = DecisionResponse.builder()
                                        .deviceId(deviceId).temperature(temp).humidity(hum)
                                        .soilMoisture(soil).light(lux)
                                        .decision("parse-error")
                                        .reason("Parse error: " + e.getMessage())
                                        .build();
                                onComplete.accept(fb);
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            onError.accept(error);
                        }
                    }
            );

        } catch (Exception e) {
            log.error("Stream start failed: {}", e.getMessage());
            onError.accept(e);
        }
    }

    // ==================== 工具方法 ====================

    private String buildDecisionInput(String deviceId) {
        return "Please analyze device " + deviceId + " and make a care decision.";
    }

    private String getSystemPrompt() {
        return """
                You are "XiaGe", a professional plant care AI assistant.
                You have the sensor data already. Follow these rules:

                RULES:
                - soilMoisture < 30% -> decision: "water"
                - soilMoisture > 70% -> decision: "noop"
                - light > 2500 Lux -> decision: "shade"
                - temperature > 32C -> decision: "ventilate"
                - high temp + strong light -> decision: "shade-ventilate"
                - all normal -> decision: "noop"

                Output ONLY pure JSON, no markdown markers.
                Fields: decision (one of "water","shade","ventilate","shade-ventilate","noop"),
                temperature, humidity, soilMoisture, light (numbers),
                reason (Chinese explanation),
                japaneseReport (Japanese text ~250 chars),
                advice (Chinese advice)
                """;
    }

    private DecisionResponse parseDecision(String rawJson, String deviceId) {
        try {
            String cleaned = rawJson
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();
            int s = cleaned.indexOf('{');
            int e = cleaned.lastIndexOf('}');
            if (s >= 0 && e > s) cleaned = cleaned.substring(s, e + 1);
            DecisionResponse r = objectMapper.readValue(cleaned, DecisionResponse.class);
            if (r.getDeviceId() == null) r.setDeviceId(deviceId);
            return r;
        } catch (Exception ex) {
            log.warn("JSON parse error: {}", ex.getMessage());
            return DecisionResponse.builder()
                    .deviceId(deviceId).decision("parse-error")
                    .reason("Parse error: " + ex.getMessage())
                    .build();
        }
    }

    private void saveDecisionLog(String deviceId, String rawJson, DecisionResponse r) {
        try {
            DecisionLog dl = DecisionLog.builder()
                    .deviceId(deviceId)
                    .decision(r.getDecision() != null ? r.getDecision() : "unknown")
                    .reason(r.getReason()).japaneseReport(r.getJapaneseReport())
                    .advice(r.getAdvice())
                    .temperature(r.getTemperature()).humidity(r.getHumidity())
                    .soilMoisture(r.getSoilMoisture()).light(r.getLight())
                    .rawResponse(rawJson).createTime(LocalDateTime.now())
                    .build();
            decisionLogRepository.save(dl);
        } catch (Exception e) {
            log.error("Save decision log failed: {}", e.getMessage());
        }
    }

    private void saveMsg(String sessionId, String role, String content) {
        try {
            ChatMessage msg = ChatMessage.builder()
                    .sessionId(sessionId).role(role).content(content)
                    .createTime(LocalDateTime.now()).build();
            chatMessageRepository.save(msg);
        } catch (Exception e) {
            log.error("Save chat msg failed: {}", e.getMessage());
        }
    }
}
