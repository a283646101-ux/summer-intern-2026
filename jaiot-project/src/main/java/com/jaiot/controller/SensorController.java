package com.jaiot.controller;

import com.jaiot.entity.SensorData;
import com.jaiot.service.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 传感器数据 API
 *
 * 支持：
 * - 查询最新数据
 * - 查询历史数据（分页 + 时间范围）
 * - 接收传感器上报（HTTP 方式）
 */
@Slf4j
@RestController
@RequestMapping("/api/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    /**
     * GET /api/sensor/latest/{deviceId}
     * 获取某设备最新一条传感器数据
     */
    @GetMapping("/latest/{deviceId}")
    public ResponseEntity<?> getLatest(@PathVariable String deviceId) {
        Optional<SensorData> data = sensorService.getLatest(deviceId);
        if (data.isPresent()) {
            return ResponseEntity.ok(data.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/sensor/history
     * 获取历史数据（支持分页和时间范围）
     *
     * 参数：
     *   deviceId  - 设备编号（可选）
     *   start     - 开始时间 (yyyy-MM-dd HH:mm:ss, 可选)
     *   end       - 结束时间 (yyyy-MM-dd HH:mm:ss, 可选)
     *   page      - 页码（默认 0）
     *   size      - 每页条数（默认 20）
     */
    @GetMapping("/history")
    public ResponseEntity<Page<SensorData>> getHistory(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (start == null) start = LocalDateTime.now().minusDays(7);
        if (end == null) end = LocalDateTime.now();

        Page<SensorData> result = sensorService.getHistoryByTimeRange(deviceId, start, end, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/sensor/data
     * 接收传感器上报数据
     *
     * Body:
     * {
     *   "deviceId": "device-001",
     *   "temperature": 28.5,
     *   "humidity": 45.0,
     *   "soilMoisture": 35.0,
     *   "light": 1200
     * }
     */
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> receiveData(@RequestBody SensorData data) {
        if (data.getCreateTime() == null) {
            data.setCreateTime(LocalDateTime.now());
        }
        SensorData saved = sensorService.save(data);
        log.info("📡 接收传感器数据: deviceId={}, temp={}°C, humid={}%, soil={}%, light={}Lux",
                saved.getDeviceId(), saved.getTemperature(), saved.getHumidity(),
                saved.getSoilMoisture(), saved.getLight());
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "id", saved.getId()
        ));
    }
}
