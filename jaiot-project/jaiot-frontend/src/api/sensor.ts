import type { SensorData, PageResponse } from '../types';
import { format } from 'date-fns';

const BASE = '/api/sensor';

export async function getLatestSensor(deviceId = 'device-001'): Promise<SensorData> {
  const res = await fetch(`${BASE}/latest/${deviceId}`);
  if (!res.ok) throw new Error('获取传感器数据失败');
  return res.json();
}

/** 把 Date 转成后端需要的 yyyy-MM-dd HH:mm:ss 格式 */
function toBackendDate(d: Date): string {
  return format(d, "yyyy-MM-dd HH:mm:ss");
}

export async function getSensorHistoryByRange(
  deviceId: string,
  start: Date,
  end: Date,
  page = 0,
  size = 200,
): Promise<PageResponse<SensorData>> {
  const params = new URLSearchParams({
    deviceId,
    start: toBackendDate(start),
    end: toBackendDate(end),
    page: String(page),
    size: String(size),
  });
  const res = await fetch(`${BASE}/history?${params}`);
  if (!res.ok) throw new Error('获取传感器历史失败');
  return res.json();
}
