import type { PageResponse, DecisionLog, DecisionStats } from '../types';

const BASE = '/api/agent/decisions';

export async function getDecisionHistory(
  deviceId = 'device-001',
  page = 0,
  size = 20,
): Promise<PageResponse<DecisionLog>> {
  const res = await fetch(`${BASE}?deviceId=${deviceId}&page=${page}&size=${size}`);
  if (!res.ok) throw new Error('获取决策历史失败');
  return res.json();
}

export async function getDecisionStats(
  deviceId?: string,
): Promise<DecisionStats> {
  const params = deviceId ? `?deviceId=${deviceId}` : '';
  const res = await fetch(`${BASE}/stats${params}`);
  if (!res.ok) throw new Error('获取决策统计失败');
  return res.json();
}

export async function getRecentDecisions(
  deviceId = 'device-001',
  limit = 10,
): Promise<DecisionLog[]> {
  const res = await fetch(`${BASE}/recent?deviceId=${deviceId}&limit=${limit}`);
  if (!res.ok) throw new Error('获取最近决策失败');
  return res.json();
}
