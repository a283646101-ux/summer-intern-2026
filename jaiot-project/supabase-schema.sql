-- =============================================================
-- JAIoT 智能养护系统 - Supabase/PostgreSQL Schema + 种子数据
-- 用法: 在 Supabase SQL Editor 中执行
-- =============================================================

-- 1. 传感器数据表
CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL,
    temperature DOUBLE PRECISION NOT NULL,
    humidity DOUBLE PRECISION NOT NULL,
    soil_moisture DOUBLE PRECISION NOT NULL,
    light INTEGER NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sensor_data_device_id ON sensor_data(device_id);
CREATE INDEX IF NOT EXISTS idx_sensor_data_create_time ON sensor_data(create_time DESC);
CREATE INDEX IF NOT EXISTS idx_sensor_data_device_time ON sensor_data(device_id, create_time DESC);

-- 2. 决策日志表
CREATE TABLE IF NOT EXISTS decision_log (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL,
    decision VARCHAR(50) NOT NULL,
    reason TEXT,
    japanese_report TEXT,
    advice TEXT,
    temperature DOUBLE PRECISION,
    humidity DOUBLE PRECISION,
    soil_moisture DOUBLE PRECISION,
    light INTEGER,
    raw_response TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_decision_log_device_id ON decision_log(device_id);
CREATE INDEX IF NOT EXISTS idx_decision_log_create_time ON decision_log(create_time DESC);

-- 3. 聊天消息表（对话记忆持久化）
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages(session_id, create_time ASC);

-- =============================================================
-- 传感器种子数据（50+ 条真实场景数据）
-- 模拟 3 个设备、不同时段、不同环境条件
-- =============================================================

-- device-001: 阳台植物区（光照变化大、温度偏高）
INSERT INTO sensor_data (device_id, temperature, humidity, soil_moisture, light, create_time) VALUES
('device-001', 28.5, 65.0, 45.2, 1800, '2026-05-20 08:00:00'),
('device-001', 30.2, 60.5, 40.1, 2200, '2026-05-20 10:00:00'),
('device-001', 33.8, 55.0, 32.5, 2800, '2026-05-20 12:00:00'),
('device-001', 34.5, 52.0, 28.0, 3100, '2026-05-20 14:00:00'),
('device-001', 32.0, 58.0, 30.5, 2500, '2026-05-20 16:00:00'),
('device-001', 28.0, 68.0, 38.0, 800,  '2026-05-20 18:00:00'),
('device-001', 25.0, 75.0, 42.0, 200,  '2026-05-20 20:00:00'),
('device-001', 27.5, 66.0, 44.0, 1600, '2026-05-21 08:00:00'),
('device-001', 31.0, 59.0, 38.0, 2400, '2026-05-21 10:00:00'),
('device-001', 35.2, 48.0, 22.0, 3500, '2026-05-21 12:00:00'),
('device-001', 36.0, 45.0, 18.0, 3800, '2026-05-21 14:00:00'),
('device-001', 33.5, 52.0, 25.0, 2700, '2026-05-21 16:00:00'),
('device-001', 29.0, 65.0, 35.0, 900,  '2026-05-21 18:00:00'),
('device-001', 24.0, 78.0, 48.0, 150,  '2026-05-21 20:00:00'),
('device-001', 28.0, 64.0, 42.0, 1700, '2026-05-22 08:00:00'),
('device-001', 32.5, 56.0, 33.0, 2600, '2026-05-22 10:00:00'),
('device-001', 34.0, 50.0, 26.0, 3200, '2026-05-22 12:00:00'),
('device-001', 33.0, 54.0, 29.0, 2900, '2026-05-22 14:00:00'),
('device-001', 30.5, 62.0, 36.0, 2100, '2026-05-22 16:00:00'),
('device-001', 26.5, 72.0, 44.0, 600,  '2026-05-22 18:00:00');

-- device-002: 室内绿植区（恒温、湿度较高、光照稳定）
INSERT INTO sensor_data (device_id, temperature, humidity, soil_moisture, light, create_time) VALUES
('device-002', 24.0, 72.0, 55.0, 1200, '2026-05-20 08:00:00'),
('device-002', 25.5, 70.0, 50.0, 1400, '2026-05-20 10:00:00'),
('device-002', 26.5, 68.0, 48.0, 1500, '2026-05-20 12:00:00'),
('device-002', 27.0, 66.0, 45.0, 1600, '2026-05-20 14:00:00'),
('device-002', 26.0, 70.0, 47.0, 1300, '2026-05-20 16:00:00'),
('device-002', 24.5, 74.0, 52.0, 800,  '2026-05-20 18:00:00'),
('device-002', 23.0, 78.0, 56.0, 300,  '2026-05-20 20:00:00'),
('device-002', 24.5, 71.0, 54.0, 1100, '2026-05-21 08:00:00'),
('device-002', 26.0, 68.0, 49.0, 1450, '2026-05-21 10:00:00'),
('device-002', 27.5, 65.0, 44.0, 1600, '2026-05-21 12:00:00'),
('device-002', 28.0, 64.0, 42.0, 1700, '2026-05-21 14:00:00'),
('device-002', 27.0, 67.0, 45.0, 1400, '2026-05-21 16:00:00'),
('device-002', 25.0, 73.0, 50.0, 700,  '2026-05-21 18:00:00'),
('device-002', 22.5, 80.0, 58.0, 200,  '2026-05-21 20:00:00'),
('device-002', 24.0, 72.0, 55.0, 1000, '2026-05-22 08:00:00'),
('device-002', 26.5, 68.0, 48.0, 1500, '2026-05-22 10:00:00'),
('device-002', 27.0, 66.0, 46.0, 1550, '2026-05-22 12:00:00'),
('device-002', 27.5, 65.0, 43.0, 1650, '2026-05-22 14:00:00'),
('device-002', 26.0, 69.0, 47.0, 1350, '2026-05-22 16:00:00'),
('device-002', 24.0, 75.0, 53.0, 600,  '2026-05-22 18:00:00');

-- device-003: 室外花园（温湿度波动大、光照极强、土壤易干）
INSERT INTO sensor_data (device_id, temperature, humidity, soil_moisture, light, create_time) VALUES
('device-003', 26.0, 58.0, 35.0, 2500, '2026-05-20 08:00:00'),
('device-003', 31.0, 45.0, 25.0, 4000, '2026-05-20 10:00:00'),
('device-003', 36.5, 35.0, 15.0, 5500, '2026-05-20 12:00:00'),
('device-003', 38.0, 30.0, 10.0, 6000, '2026-05-20 14:00:00'),
('device-003', 35.0, 38.0, 18.0, 4800, '2026-05-20 16:00:00'),
('device-003', 30.0, 50.0, 28.0, 2000, '2026-05-20 18:00:00'),
('device-003', 25.0, 62.0, 38.0, 500,  '2026-05-20 20:00:00'),
('device-003', 27.5, 55.0, 32.0, 2600, '2026-05-21 08:00:00'),
('device-003', 33.0, 40.0, 20.0, 4200, '2026-05-21 10:00:00'),
('device-003', 37.0, 32.0, 12.0, 5800, '2026-05-21 12:00:00'),
('device-003', 39.0, 28.0, 8.0,  6500, '2026-05-21 14:00:00'),
('device-003', 36.0, 35.0, 14.0, 5000, '2026-05-21 16:00:00'),
('device-003', 31.0, 48.0, 25.0, 2200, '2026-05-21 18:00:00'),
('device-003', 24.0, 65.0, 40.0, 400,  '2026-05-21 20:00:00'),
('device-003', 28.0, 54.0, 33.0, 2700, '2026-05-22 08:00:00'),
('device-003', 34.5, 38.0, 18.0, 4500, '2026-05-22 10:00:00'),
('device-003', 38.5, 30.0, 10.0, 6000, '2026-05-22 12:00:00'),
('device-003', 37.0, 33.0, 13.0, 5500, '2026-05-22 14:00:00'),
('device-003', 34.0, 40.0, 20.0, 4200, '2026-05-22 16:00:00'),
('device-003', 29.0, 52.0, 30.0, 1800, '2026-05-22 18:00:00');
