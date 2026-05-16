package com.jaiot.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 灌溉决策结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResponse {
    /** 传感器 ID */
    private String sensorId;
    /** 当前温度 (°C) */
    private double temperature;
    /** 当前湿度 (%) */
    private double humidity;
    /** Agent 决策：water / no-water */
    private String decision;
    /** 决策理由 / 分析说明 */
    private String reason;
    /** 附加建议 */
    private String advice;
}
