package com.jaiot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 决策响应 DTO
 *
 * 包含传感器数据、决策结果、中文理由、日语养护报告。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionResponse {

    /** 设备编号 */
    private String deviceId;

    /** 当前温度 (°C) */
    private Double temperature;

    /** 当前空气湿度 (%) */
    private Double humidity;

    /** 当前土壤湿度 (%) */
    private Double soilMoisture;

    /** 当前光照强度 (Lux) */
    private Integer light;

    /** 决策结果：water / shade / ventilate / noop */
    private String decision;

    /** 决策理由（中文） */
    private String reason;

    /** 日语养护报告（纯文本） */
    private String japaneseReport;

    /** 附加养护建议 */
    private String advice;
}
