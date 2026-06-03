package com.jaiot.repository;

import com.jaiot.entity.DecisionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 决策日志 Repository
 */
@Repository
public interface DecisionLogRepository extends JpaRepository<DecisionLog, Long> {

    /** 按设备查询，时间倒序 */
    Page<DecisionLog> findByDeviceIdOrderByCreateTimeDesc(String deviceId, Pageable pageable);

    /** 按设备 + 时间范围查询 */
    @Query("SELECT d FROM DecisionLog d WHERE d.deviceId = :deviceId " +
           "AND d.createTime BETWEEN :start AND :end ORDER BY d.createTime DESC")
    Page<DecisionLog> findByDeviceIdAndTimeRange(
            @Param("deviceId") String deviceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    /** 统计各决策类型出现的次数 */
    @Query("SELECT d.decision, COUNT(d) FROM DecisionLog d GROUP BY d.decision")
    List<Object[]> countByDecision();

    /** 按设备统计决策类型分布 */
    @Query("SELECT d.decision, COUNT(d) FROM DecisionLog d " +
           "WHERE d.deviceId = :deviceId GROUP BY d.decision")
    List<Object[]> countByDecisionAndDeviceId(@Param("deviceId") String deviceId);
}
