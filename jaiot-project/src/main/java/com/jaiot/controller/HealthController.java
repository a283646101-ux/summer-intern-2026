package com.jaiot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK 🦞";
    }

    @GetMapping("/")
    public String index() {
        return """
                🦞 JAIoT 智能养护 Agent 系统 🪴
                
                API 端点：
                GET  /health                          — 健康检查
                GET  /api/sensor/latest/{deviceId}    — 最新传感器数据
                GET  /api/sensor/history              — 历史数据
                POST /api/sensor/data                 — 接收传感器数据
                POST /api/agent/chat                  — 通用对话
                POST /api/agent/decide                — 养护决策
                GET  /api/agent/stream-decision       — 流式决策 (SSE)
                GET  /api/agent/decisions             — 历史决策
                GET  /api/agent/decisions/stats       — 决策统计
                POST /api/rag/ask                     — RAG 知识查询
                GET  /api/rag/documents               — 知识文档列表
                """;
    }
}
