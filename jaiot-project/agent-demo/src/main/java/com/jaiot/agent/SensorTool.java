package com.jaiot.agent;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 传感器查询工具 —— 虾哥 Agent 的「眼睛」
 * <p>
 * 通过 @Tool 注解暴露给大模型，Agent 在需要时可以调用该方法
 * 获取传感器实时数据（当前为 mock 实现）。
 */
@Slf4j
@Component
public class SensorTool {

    private final Random random = new Random();

    /**
     * 查询指定传感器的最新温湿度数据
     *
     * @param sensorId 传感器编号，例如 "sensor-001"
     * @return 传感器数据（温度 °C / 湿度 %）
     */
    @Tool("查询指定传感器的当前温湿度数据，返回温度(°C)和湿度(%)")
    public SensorData queryLatestSensorData(String sensorId) {
        // ── Mock 数据生成 ──
        // 温度范围: 20°C ~ 35°C，模拟不同天气情况
        double temperature = Math.round((25 + (random.nextDouble() * 10 - 5)) * 10.0) / 10.0;
        // 湿度范围: 30% ~ 85%，模拟干湿差异
        double humidity = Math.round((60 + (random.nextDouble() * 30 - 15)) * 10.0) / 10.0;

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        log.info("🔍 传感器 [{}] 数据: 温度={}°C, 湿度={}%, 时间={}", sensorId, temperature, humidity, now);

        return new SensorData(sensorId, temperature, humidity, now);
    }
}
