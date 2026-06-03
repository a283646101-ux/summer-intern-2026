package com.jaiot.controller;

import com.jaiot.entity.DecisionLog;
import com.jaiot.service.DecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 历史决策记录 API
 *
 * 支持：
 * - 查询某设备的决策历史
 * - 按时间范围筛选
 * - 决策统计
 * - 最新 N 条记录
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    /**
     * GET /api/agent/decisions?deviceId=device-001&page=0&size=20
     * 查询历史决策记录
     */
    @GetMapping
    public ResponseEntity<Page<DecisionLog>> getHistory(
            @RequestParam(defaultValue = "device-001") String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(decisionService.getHistory(deviceId, page, size));
    }

    /**
     * GET /api/agent/decisions/range
     * 按时间范围查询
     */
    @GetMapping("/range")
    public ResponseEntity<Page<DecisionLog>> getHistoryByTimeRange(
            @RequestParam(defaultValue = "device-001") String deviceId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (start == null) start = LocalDateTime.now().minusDays(30);
        if (end == null) end = LocalDateTime.now();

        return ResponseEntity.ok(
                decisionService.getHistoryByTimeRange(deviceId, start, end, page, size));
    }

    /**
     * GET /api/agent/decisions/stats?deviceId=device-001
     * 决策统计（各类型次数）
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(
            @RequestParam(required = false) String deviceId) {

        return ResponseEntity.ok(decisionService.getStats(deviceId));
    }

    /**
     * GET /api/agent/decisions/recent?deviceId=device-001&limit=5
     * 获取最近 N 条决策记录
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DecisionLog>> getRecent(
            @RequestParam(defaultValue = "device-001") String deviceId,
            @RequestParam(defaultValue = "5") int limit) {

        return ResponseEntity.ok(decisionService.getRecent(deviceId, limit));
    }
}
