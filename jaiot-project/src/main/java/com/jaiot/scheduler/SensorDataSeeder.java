package com.jaiot.scheduler;

import com.jaiot.entity.SensorData;
import com.jaiot.repository.SensorDataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 传感器历史数据种子
 *
 * 启动时生成大量历史数据，用于前端图表展示和测试。
 * 仅当 sensor_data 表为空时执行，不会重复插入。
 *
 * 生成策略：
 * - 过去 7 天，每 10 分钟一条
 * - 每天模拟不同的环境模式（正常/高温/阴雨/干燥等）
 * - 数据平滑过渡，不跳变
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataSeeder {

    private final SensorDataRepository repository;

    private final Random random = new Random(42); // 固定种子，结果可复现

    @PostConstruct
    public void seedIfEmpty() {
        if (repository.count() > 0) {
            log.info("📊 传感器数据表已有数据，跳过种子初始化 (count={})", repository.count());
            return;
        }

        log.info("📊 开始生成传感器历史种子数据...");
        long start = System.currentTimeMillis();

        List<SensorData> batch = new ArrayList<>();
        int totalPoints = 7 * 24 * 6; // 7天 × 24小时 × 每10分钟6条 = 1008条
        LocalDateTime now = LocalDateTime.now();

        // 每日温度基准（有渐变）
        double[] dailyBaseTemp = {22, 28, 33, 25, 20, 30, 26};  // 周一~周日
        double[] dailyBaseHumid = {60, 45, 35, 70, 55, 40, 50};
        double[] dailyBaseSoil = {50, 35, 20, 65, 45, 30, 40};
        int[] dailyBaseLight = {1200, 1800, 2800, 800, 1500, 2200, 1600};

        double prevTemp = 25, prevHumid = 55, prevSoil = 45, prevLight = 1500;

        for (int day = 0; day < 7; day++) {
            for (int interval = 0; interval < 144; interval++) { // 144 = 24h × 6条/h
                // 时间 = 当前时间往前推（day 0=6天前, day 6=现在）
                int minutesAgo = ((6 - day) * 24 * 60) + (interval * 10);
                LocalDateTime time = now.minusMinutes(minutesAgo);

                // 一天内的正弦波动（白天高，晚上低）
                double hourOfDay = time.getHour() + time.getMinute() / 60.0;
                double dayFactor = Math.sin(Math.PI * (hourOfDay - 5) / 14); // 5点最低，19点最高
                dayFactor = Math.max(-1, Math.min(1, dayFactor));

                // 随机扰动（平滑变化 ≈ 前值 ± 小随机量）
                double delta = 0.15; // 每次最多变化 15%
                prevTemp = smooth(prevTemp, dailyBaseTemp[day] + dayFactor * 6, delta);
                prevHumid = smooth(prevHumid, dailyBaseHumid[day] - dayFactor * 10, delta);
                prevSoil = smooth(prevSoil, dailyBaseSoil[day] + (random.nextDouble() - 0.5) * 8, delta);
                prevLight = smooth(prevLight, dailyBaseLight[day] * Math.max(0, dayFactor), delta);

                // 添加随机尖峰（模拟异常）
                double spike = random.nextDouble();
                if (spike < 0.03) { // 3% 概率出现尖峰
                    prevTemp += (random.nextDouble() - 0.5) * 8;
                }
                if (spike > 0.97) {
                    prevHumid += (random.nextDouble() - 0.5) * 20;
                }

                SensorData data = SensorData.builder()
                        .deviceId("device-001")
                        .temperature(Math.round(prevTemp * 10.0) / 10.0)
                        .humidity(Math.round(Math.max(0, Math.min(100, prevHumid)) * 10.0) / 10.0)
                        .soilMoisture(Math.round(Math.max(0, Math.min(100, prevSoil)) * 10.0) / 10.0)
                        .light(Math.max(0, (int) Math.round(prevLight)))
                        .createTime(time)
                        .build();

                batch.add(data);

                // 每 100 条批量写入一次
                if (batch.size() >= 100) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }
        }

        // 写入剩余批次
        if (!batch.isEmpty()) {
            repository.saveAll(batch);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("✅ 传感器种子数据生成完成：{} 条，耗时 {}ms", totalPoints, elapsed);
    }

    /** 平滑过渡：当前值向目标值靠近，加随机扰动 */
    private double smooth(double current, double target, double maxDelta) {
        double step = (target - current) * 0.3; // 每次向目标靠近 30%
        double noise = (random.nextDouble() - 0.5) * maxDelta * 2;
        return current + step + noise;
    }
}
