package com.jaiot.service;

import com.jaiot.entity.DecisionLog;
import com.jaiot.repository.DecisionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 决策记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionService {

    private final DecisionLogRepository decisionLogRepository;

    /**
     * 查询某设备的历史决策记录（分页）
     */
    public Page<DecisionLog> getHistory(String deviceId, int page, int size) {
        return decisionLogRepository.findByDeviceIdOrderByCreateTimeDesc(
                deviceId, PageRequest.of(page, size));
    }

    /**
     * 按时间范围查询历史决策
     */
    public Page<DecisionLog> getHistoryByTimeRange(
            String deviceId, LocalDateTime start, LocalDateTime end, int page, int size) {
        return decisionLogRepository.findByDeviceIdAndTimeRange(
                deviceId, start, end, PageRequest.of(page, size));
    }

    /**
     * 获取决策统计（各类型次数）
     */
    public Map<String, Long> getStats(String deviceId) {
        List<Object[]> results;
        if (deviceId != null && !deviceId.isBlank()) {
            results = decisionLogRepository.countByDecisionAndDeviceId(deviceId);
        } else {
            results = decisionLogRepository.countByDecision();
        }

        Map<String, Long> stats = new LinkedHashMap<>();
        for (Object[] row : results) {
            stats.put((String) row[0], (Long) row[1]);
        }
        return stats;
    }

    /**
     * 获取最新 N 条决策记录
     */
    public List<DecisionLog> getRecent(String deviceId, int limit) {
        Page<DecisionLog> page = decisionLogRepository.findByDeviceIdOrderByCreateTimeDesc(
                deviceId, PageRequest.of(0, limit));
        return page.getContent();
    }
}
