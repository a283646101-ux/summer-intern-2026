package com.jaiot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 决策日志实体
 *
 * 对应数据库 decision_log 表，记录每次 Agent 的完整决策过程。
 * 包括决策时的环境数据、决策结果、中文理由、日语报告。
 */
@Entity
@Table(name = "decision_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 设备编号 */
    @Column(nullable = false, length = 50)
    private String deviceId;

    /** 决策结果：water / shade / ventilate / noop */
    @Column(nullable = false, length = 20)
    private String decision;

    /** 决策理由（中文） */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /** 日语养护报告（纯文本） */
    @Column(name = "japanese_report", columnDefinition = "TEXT")
    private String japaneseReport;

    /** 附加建议 */
    @Column(columnDefinition = "TEXT")
    private String advice;

    /** 决策时的温度 */
    private Double temperature;

    /** 决策时的空气湿度 */
    private Double humidity;

    /** 决策时的土壤湿度 */
    @Column(name = "soil_moisture")
    private Double soilMoisture;

    /** 决策时的光照强度 */
    private Integer light;

    /** Agent 原始响应（完整 JSON） */
    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    /** 决策时间 */
    @Column(nullable = false)
    private LocalDateTime createTime;
}
