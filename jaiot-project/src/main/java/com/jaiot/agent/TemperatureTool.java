package com.jaiot.agent;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.jaiot.entity.SensorData;
import com.jaiot.repository.SensorDataRepository;

/**
 * 温度查询工具
 *
 * 查询指定设备的最新温度数据。
 * @Tool 的描述文本对 LLM 决定是否调用此工具至关重要。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureTool {

    private final SensorDataRepository sensorRepository;

    @Tool("查询指定设备的当前温度(°C)，返回一个数值，例如 28.5")
    public double getTemperature(String deviceId) {
        SensorData data = sensorRepository
                .findFirstByDeviceIdOrderByCreateTimeDesc(deviceId)
                .orElseThrow(() -> new RuntimeException("未找到设备 " + deviceId + " 的传感器数据"));

        log.info("🌡️ 温度工具 [{}]: {}°C", deviceId, data.getTemperature());
        return data.getTemperature();
    }
}
