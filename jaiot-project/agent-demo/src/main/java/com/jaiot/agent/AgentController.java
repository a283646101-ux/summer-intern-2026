package com.jaiot.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Agent REST API
 * <p>
 * 提供聊天和灌溉决策接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final SimpleAgent simpleAgent;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/agent/chat
     * Body: { "message": "你好，你是谁？" }
     */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isBlank()) {
            return Map.of("error", "message 不能为空");
        }
        String reply = simpleAgent.say(message);
        return Map.of("reply", reply);
    }

    /**
     * GET /api/agent/chat?message=你好
     */
    @GetMapping("/chat")
    public Map<String, String> chatGet(@RequestParam String message) {
        String reply = simpleAgent.say(message);
        return Map.of("reply", reply);
    }

    /**
     * POST /agent/decision
     * <p>
     * Agent 查询传感器数据 → 自动判断是否需要浇水 → 返回决策结果 JSON。
     * <p>
     * Request body（可选）:
     *   { "sensorId": "sensor-001" }   // 默认 "sensor-001"
     * <p>
     * Response:
     *   {
     *     "sensorId": "sensor-001",
     *     "temperature": 28.5,
     *     "humidity": 45.0,
     *     "decision": "water",
     *     "reason": "...",
     *     "advice": "..."
     *   }
     */
    @PostMapping("/decision")
    public DecisionResponse decideIrrigation(@RequestBody(required = false) Map<String, String> request) {
        String sensorId = (request != null && request.containsKey("sensorId"))
                ? request.get("sensorId")
                : "sensor-001";

        log.info("🌱 POST /agent/decision - sensorId={}", sensorId);

        // Agent 调用大模型 + 传感器工具，返回 JSON 字符串
        String agentJson = simpleAgent.decideIrrigation(sensorId);

        // 尝试解析为结构化对象
        try {
            return objectMapper.readValue(agentJson, DecisionResponse.class);
        } catch (Exception e) {
            log.warn("⚠️ Agent 返回非标准 JSON，尝试兜底解析: {}", agentJson);
            // 兜底：将原始文本放入 reason 字段
            return new DecisionResponse(
                    sensorId, 0, 0,
                    "unknown", agentJson, "Agent 返回格式异常，请查看 reason 字段的原始内容"
            );
        }
    }
}
