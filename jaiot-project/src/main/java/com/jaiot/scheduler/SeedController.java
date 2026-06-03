package com.jaiot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据种子手动触发 API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SeedController {

    private final SensorDataSeeder seeder;

    @PostMapping("/reseed")
    public ResponseEntity<Map<String, Object>> reseed() {
        try {
            seeder.seedIfEmpty();
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "message", "种子数据已检查/生成"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
