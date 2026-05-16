package com.jaiot.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传感器数据模型 —— 温度 & 湿度
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {
    /** 传感器 ID */
    private String sensorId;
    /** 温度（摄氏度） */
    private double temperature;
    /** 湿度（百分比） */
    private double humidity;
    /** 数据采集时间（ISO-8601） */
    private String timestamp;
}
