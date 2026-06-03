package com.jaiot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 传感器数据实体
 *
 * 对应数据库 sensor_data 表，存储 IoT 设备采集的环境数据。
 * 包含温度、湿度、土壤湿度、光照强度四个核心指标。
 */
@Entity
@Table(name = "sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 设备编号 */
    @Column(nullable = false, length = 50)
    private String deviceId;

    /** 温度 (°C) */
    @Column(nullable = false)
    private Double temperature;

    /** 空气湿度 (%) */
    @Column(nullable = false)
    private Double humidity;

    /** 土壤湿度 (%) */
    @Column(name = "soil_moisture", nullable = false)
    private Double soilMoisture;

    /** 光照强度 (Lux) */
    @Column(nullable = false)
    private Integer light;

    /** 数据采集时间 */
    @Column(nullable = false)
    private LocalDateTime createTime;
}
