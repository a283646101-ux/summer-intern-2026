package com.jaiot.agent;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.jaiot.entity.SensorData;
import com.jaiot.repository.SensorDataRepository;

/**
 * 空气湿度查询工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HumidityTool {

    private final SensorDataRepository sensorRepository;

    @Tool("查询指定设备的当前空气湿度(%)，返回一个数值，例如 45.0")
    public double getHumidity(String deviceId) {
        SensorData data = sensorRepository
                .findFirstByDeviceIdOrderByCreateTimeDesc(deviceId)
                .orElseThrow(() -> new RuntimeException("未找到设备 " + deviceId + " 的传感器数据"));

        log.info("💧 湿度工具 [{}]: {}%", deviceId, data.getHumidity());
        return data.getHumidity();
    }
}
