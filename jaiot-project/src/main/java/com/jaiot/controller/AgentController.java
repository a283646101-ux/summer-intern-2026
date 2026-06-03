package com.jaiot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaiot.agent.AgentService;
import com.jaiot.dto.DecisionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import org.springframework.core.task.TaskExecutor;

/**
 * Agent 决策 API
 *
 * 支持：
 * - 完整决策（POST /api/agent/decide）
 * - 流式决策 SSE（GET /api/agent/stream-decision）
 * - 通用对话（POST /api/agent/chat）
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final ObjectMapper objectMapper;
    private final TaskExecutor taskExecutor;

    /**
     * POST /api/agent/chat
     * 通用对话（带会话记忆持久化）
     *
     * Body: { "message": "...", "sessionId": "device-001" }
     */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isBlank()) {
            return Map.of("error", "message 不能为空");
        }
        try {
            String reply = agentService.chat(message);
            return Map.of("reply", reply);
        } catch (Exception e) {
            log.error("chat 接口异常: {}", e.getMessage());
            return Map.of("error", "AI 服务暂时不可用，请稍后重试");
        }
    }

    /**
     * POST /api/agent/decide
     * 完整决策（非流式）
     *
     * Body: { "deviceId": "device-001" }
     */
    @PostMapping("/decide")
    public ResponseEntity<DecisionResponse> decide(@RequestBody Map<String, String> request) {
        String deviceId = request != null && request.containsKey("deviceId")
                ? request.get("deviceId")
                : "device-001";

        log.info("🌱 POST /api/agent/decide - deviceId={}", deviceId);

        try {
            DecisionResponse response = agentService.decide(deviceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("decide 接口异常: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(DecisionResponse.builder()
                            .deviceId(deviceId)
                            .decision("error")
                            .reason("AI 服务暂时不可用: " + e.getMessage())
                            .build());
        }
    }

    /**
     * GET /api/agent/stream-decision?deviceId=device-001
     * 流式决策（SSE 打字机效果）
     *
     * 事件流说明：
     * event: tool_call
     * data: {"type":"tool","content":"🌡️ 正在查询温度数据..."}
     *
     * event: token
     * data: {"type":"token","content":"{"}
     *
     * event: complete
     * data: {"type":"complete","decision":"water","reason":"..."}
     */
    @GetMapping(value = "/stream-decision", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamDecision(@RequestParam(defaultValue = "device-001") String deviceId) {
        // 60 秒超时
        SseEmitter emitter = new SseEmitter(60_000L);

        taskExecutor.execute(() -> {
            try {
                agentService.streamDecide(
                        deviceId,
                        // onToolResult — 工具调用中间结果，以 tool_call 事件推送
                        toolResult -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("tool_call")
                                        .data(Map.of("type", "tool", "content", toolResult)));
                            } catch (IOException e) {
                                throw new RuntimeException("SSE 发送 tool_call 失败", e);
                            }
                        },
                        // onToken — 每个生成 token，以 token 事件推送
                        token -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("token")
                                        .data(Map.of("type", "token", "content", token)));
                            } catch (IOException e) {
                                throw new RuntimeException("SSE 发送 token 失败", e);
                            }
                        },
                        // onComplete — 决策完成，推送完整结果
                        response -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("complete")
                                        .data(Map.of(
                                                "type", "complete",
                                                "decision", response.getDecision(),
                                                "temperature", response.getTemperature(),
                                                "humidity", response.getHumidity(),
                                                "soilMoisture", response.getSoilMoisture(),
                                                "light", response.getLight(),
                                                "reason", response.getReason(),
                                                "japaneseReport", response.getJapaneseReport(),
                                                "advice", response.getAdvice())));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("SSE 发送 complete 事件失败: {}", e.getMessage());
                                emitter.completeWithError(e);
                            }
                        },
                        // onError — 异常处理
                        error -> {
                            log.error("SSE 流式决策异常: {}", error.getMessage());
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(Map.of("type", "error", "message", error.getMessage())));
                            } catch (IOException e) {
                                // ignore
                            }
                            emitter.completeWithError(error);
                        });
            } catch (Exception e) {
                log.error("SSE 流式决策处理异常: {}", e.getMessage());
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("type", "error", "message", e.getMessage())));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
