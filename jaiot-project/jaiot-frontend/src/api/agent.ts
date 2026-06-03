import type { DecisionResponse } from '../types';

const BASE = '/api/agent';

export async function sendChat(message: string): Promise<string> {
  const res = await fetch(`${BASE}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message }),
  });
  const data = await res.json();
  if (data.error) throw new Error(data.error);
  return data.reply;
}

export async function triggerDecision(deviceId = 'device-001'): Promise<DecisionResponse> {
  const res = await fetch(`${BASE}/decide`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ deviceId }),
  });
  if (!res.ok) throw new Error('决策请求失败');
  return res.json();
}

/** 流式决策 — 返回 EventSource URL */
export function getStreamDecisionUrl(deviceId = 'device-001'): string {
  return `${BASE}/stream-decision?deviceId=${deviceId}`;
}
