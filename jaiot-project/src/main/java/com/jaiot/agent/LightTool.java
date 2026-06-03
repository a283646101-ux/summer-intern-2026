package com.jaiot.agent;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.jaiot.entity.SensorData;
import com.jaiot.repository.SensorDataRepository;

/**
 * 光照强度查询工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LightTool {

    private final SensorDataRepository sensorRepository;

    @Tool("查询指定设备的当前光照强度(Lux)，返回一个整数，例如 1200")
    public int getLight(String deviceId) {
        SensorData data = sensorRepository
                .findFirstByDeviceIdOrderByCreateTimeDesc(deviceId)
                .orElseThrow(() -> new RuntimeException("未找到设备 " + deviceId + " 的传感器数据"));

        log.info("☀️ 光照工具 [{}]: {} Lux", deviceId, data.getLight());
        return data.getLight();
    }
}
