package com.jaiot.service;

import com.jaiot.entity.SensorData;
import com.jaiot.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 传感器数据服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorDataRepository sensorRepository;

    /**
     * 获取某设备最新传感器数据
     */
    public Optional<SensorData> getLatest(String deviceId) {
        return sensorRepository.findFirstByDeviceIdOrderByCreateTimeDesc(deviceId);
    }

    /**
     * 获取某设备历史数据（分页）
     */
    public Page<SensorData> getHistory(String deviceId, int page, int size) {
        return sensorRepository.findByDeviceIdOrderByCreateTimeDesc(
                deviceId, PageRequest.of(page, size));
    }

    /**
     * 按时间范围获取历史数据（分页）
     */
    public Page<SensorData> getHistoryByTimeRange(
            String deviceId, LocalDateTime start, LocalDateTime end, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (deviceId != null && !deviceId.isBlank()) {
            return sensorRepository.findByDeviceIdAndCreateTimeBetweenOrderByCreateTimeDesc(
                    deviceId, start, end, pageable);
        } else {
            return sensorRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(
                    start, end, pageable);
        }
    }

    /**
     * 保存传感器数据
     */
    public SensorData save(SensorData data) {
        log.debug("💾 保存传感器数据: deviceId={}, temp={}, humid={}, soil={}, light={}",
                data.getDeviceId(), data.getTemperature(), data.getHumidity(),
                data.getSoilMoisture(), data.getLight());
        return sensorRepository.save(data);
    }
}
