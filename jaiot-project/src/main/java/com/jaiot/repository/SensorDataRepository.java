package com.jaiot.repository;

import com.jaiot.entity.SensorData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 传感器数据 Repository
 */
@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    /** 查询某设备最新一条数据 */
    Optional<SensorData> findFirstByDeviceIdOrderByCreateTimeDesc(String deviceId);

    /** 按设备查询，时间倒序 */
    Page<SensorData> findByDeviceIdOrderByCreateTimeDesc(String deviceId, Pageable pageable);

    /** 按设备 + 时间范围查询 */
    Page<SensorData> findByDeviceIdAndCreateTimeBetweenOrderByCreateTimeDesc(
            String deviceId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    /** 按时间范围查询（所有设备） */
    Page<SensorData> findByCreateTimeBetweenOrderByCreateTimeDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);
}
