package com.jaiot.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Agent REST API
 * 用户发送 POST 请求，Agent 调用大模型回答。
 */
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final SimpleAgent simpleAgent;

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
     * 查询参数方式，方便浏览器测试
     */
    @GetMapping("/chat")
    public Map<String, String> chatGet(@RequestParam String message) {
        String reply = simpleAgent.say(message);
        return Map.of("reply", reply);
    }
}
