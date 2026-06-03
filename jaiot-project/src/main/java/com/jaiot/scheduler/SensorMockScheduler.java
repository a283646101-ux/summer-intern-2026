package com.jaiot.scheduler;

import com.jaiot.entity.SensorData;
import com.jaiot.repository.SensorDataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 传感器数据模拟器
 *
 * 在没有真实 IoT 硬件时，通过定时任务模拟传感器数据上报。
 * 每 30 秒生成一条随机数据，模拟不同环境场景。
 * 数据范围覆盖：正常、高温、干燥、强光、过湿等场景，
 * 方便测试 Agent 的各种决策逻辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensorMockScheduler {

    private final SensorDataRepository sensorRepository;
    private final Random random = new Random();

    /**
     * 启动时先写入一条初始数据，避免查询为空
     */
    @PostConstruct
    public void initFirstData() {
        SensorData data = generateRandomData("device-001");
        sensorRepository.save(data);
        log.info("📡 初始传感器数据已写入: {}°C / {}% / {}% / {} Lux",
                data.getTemperature(), data.getHumidity(),
                data.getSoilMoisture(), data.getLight());
    }

    /**
     * 每 30 秒生成一条模拟传感器数据
     *
     * 数据范围设计：
     *   温度：15°C ~ 38°C（覆盖正常/高温）
     *   空气湿度：20% ~ 90%（覆盖干燥/正常/潮湿）
     *   土壤湿度：10% ~ 80%（覆盖干旱/正常/过湿）
     *   光照：200 ~ 3500 Lux（覆盖弱光/正常/强光）
     */
    @Scheduled(fixedRate = 30_000)
    public void generateMockData() {
        // 随机切换场景模式，让数据更有变化
        int mode = random.nextInt(5);
        SensorData data;

        switch (mode) {
            case 0: // 正常天气
                data = SensorData.builder()
                        .deviceId("device-001")
                        .temperature(round(22 + random.nextDouble() * 6))    // 22~28°C
                        .humidity(round(45 + random.nextDouble() * 25))       // 45~70%
                        .soilMoisture(round(35 + random.nextDouble() * 30))   // 35~65%
                        .light(800 + random.nextInt(1200))                     // 800~2000 Lux
                        .createTime(LocalDateTime.now())
                        .build();
                break;
            case 1: // 高温干燥（需要浇水 + 遮阳）
                data = SensorData.builder()
                        .deviceId("device-001")
                        .temperature(round(32 + random.nextDouble() * 6))    // 32~38°C
                        .humidity(round(20 + random.nextDouble() * 15))       // 20~35%
                        .soilMoisture(round(10 + random.nextDouble() * 15))   // 10~25%
                        .light(2500 + random.nextInt(1000))                   // 2500~3500 Lux
                        .createTime(LocalDateTime.now())
                        .build();
                break;
            case 2: // 高温高湿（需要通风）
                data = SensorData.builder()
                        .deviceId("device-001")
                        .temperature(round(30 + random.nextDouble() * 5))    // 30~35°C
                        .humidity(round(75 + random.nextDouble() * 15))       // 75~90%
                        .soilMoisture(round(50 + random.nextDouble() * 20))   // 50~70%
                        .light(1500 + random.nextInt(1000))                   // 1500~2500 Lux
                        .createTime(LocalDateTime.now())
                        .build();
                break;
            case 3: // 弱光低温（无需操作或注意补光）
                data = SensorData.builder()
                        .deviceId("device-001")
                        .temperature(round(15 + random.nextDouble() * 5))    // 15~20°C
                        .humidity(round(50 + random.nextDouble() * 20))       // 50~70%
                        .soilMoisture(round(40 + random.nextDouble() * 20))   // 40~60%
                        .light(200 + random.nextInt(400))                     // 200~600 Lux
                        .createTime(LocalDateTime.now())
                        .build();
                break;
            case 4: // 土壤过湿（注意排水）
                data = SensorData.builder()
                        .deviceId("device-001")
                        .temperature(round(24 + random.nextDouble() * 4))    // 24~28°C
                        .humidity(round(60 + random.nextDouble() * 15))       // 60~75%
                        .soilMoisture(round(70 + random.nextDouble() * 10))   // 70~80%
                        .light(1000 + random.nextInt(800))                    // 1000~1800 Lux
                        .createTime(LocalDateTime.now())
                        .build();
                break;
            default:
                data = generateRandomData("device-001");
        }

        sensorRepository.save(data);
        log.debug("📡 [{}] {}°C / {}% / {}% / {} Lux — 模式:{}",
                data.getDeviceId(), data.getTemperature(), data.getHumidity(),
                data.getSoilMoisture(), data.getLight(), modeName(mode));
    }

    private SensorData generateRandomData(String deviceId) {
        return SensorData.builder()
                .deviceId(deviceId)
                .temperature(round(20 + random.nextDouble() * 18))    // 20~38°C
                .humidity(round(25 + random.nextDouble() * 60))       // 25~85%
                .soilMoisture(round(15 + random.nextDouble() * 60))   // 15~75%
                .light(300 + random.nextInt(3000))                    // 300~3300 Lux
                .createTime(LocalDateTime.now())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String modeName(int mode) {
        return switch (mode) {
            case 0 -> "🌤️ 正常";
            case 1 -> "🔥 高温干燥";
            case 2 -> "💨 高温高湿";
            case 3 -> "🌥️ 弱光低温";
            case 4 -> "💧 土壤过湿";
            default -> "❓ 随机";
        };
    }
}
