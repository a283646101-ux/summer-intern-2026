import type { RagDocument } from '../types';

const BASE = '/api/rag';

export async function askKnowledge(query: string): Promise<string> {
  const res = await fetch(`${BASE}/ask`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query }),
  });
  const data = await res.json();
  if (data.error) throw new Error(data.error);
  return data.knowledge;
}

export async function listDocuments(): Promise<RagDocument[]> {
  const res = await fetch(`${BASE}/documents`);
  if (!res.ok) throw new Error('获取知识文档列表失败');
  return res.json();
}
