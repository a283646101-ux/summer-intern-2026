const API_BASE = '/api';

export interface ChatMessageDTO {
  id?: number;
  sessionId: string;
  role: 'user' | 'assistant';
  content: string;
  createTime?: string;
}

/**
 * 获取指定会话的聊天历史
 */
export async function fetchChatHistory(sessionId: string = 'device-001'): Promise<ChatMessageDTO[]> {
  const res = await fetch(`${API_BASE}/chat/history?sessionId=${encodeURIComponent(sessionId)}`);
  if (!res.ok) throw new Error('获取聊天历史失败');
  return res.json();
}

/**
 * 持久化单条聊天消息
 */
export async function saveChatMessage(
  sessionId: string,
  role: 'user' | 'assistant',
  content: string
): Promise<void> {
  await fetch(`${API_BASE}/chat/save`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sessionId, role, content }),
  });
}

/**
 * 发送聊天消息到 AI（后端会自动持久化）
 */
export async function sendChatMessage(
  message: string,
  sessionId: string = 'device-001'
): Promise<{ reply: string; sessionId: string }> {
  const res = await fetch(`${API_BASE}/agent/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, sessionId }),
  });
  if (!res.ok) throw new Error('发送消息失败');
  return res.json();
}
