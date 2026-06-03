/** 传感器数据 */
export interface SensorData {
  id: number;
  deviceId: string;
  temperature: number;
  humidity: number;
  soilMoisture: number;
  light: number;
  createTime: string;
}

/** Agent 决策响应 */
export interface DecisionResponse {
  deviceId: string;
  temperature: number;
  humidity: number;
  soilMoisture: number;
  light: number;
  decision: DecisionType;
  reason: string;
  japaneseReport: string;
  advice: string;
}

/** 决策类型 */
export type DecisionType =
  | 'water'
  | 'shade'
  | 'ventilate'
  | 'shade-ventilate'
  | 'noop'
  | 'error'
  | 'parse-error';

/** 决策日志（历史记录） */
export interface DecisionLog {
  id: number;
  deviceId: string;
  decision: DecisionType;
  reason: string;
  japaneseReport: string;
  advice: string;
  temperature: number;
  humidity: number;
  soilMoisture: number;
  light: number;
  rawResponse: string;
  createTime: string;
}

/** 分页响应 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

/** 统计 */
export interface DecisionStats {
  [key: string]: number;
}

/** RAG 文档 */
export interface RagDocument {
  id: number;
  fileName: string;
  summary: string;
  chunkCount: number;
  createTime: string;
}

/** SSE 事件 */
export interface SseToolCallEvent {
  type: 'tool';
  content: string;
}

export interface SseTokenEvent {
  type: 'token';
  content: string;
}

export interface SseCompleteEvent {
  type: 'complete';
  decision: string;
  temperature: number;
  humidity: number;
  soilMoisture: number;
  light: number;
  reason: string;
  japaneseReport: string;
  advice: string;
}

export type SseEvent = SseToolCallEvent | SseTokenEvent | SseCompleteEvent;

/** 聊天消息 */
export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  toolCalls?: string[];   // 工具调用过程记录
  decision?: DecisionResponse;  // 最终决策结果
  timestamp: number;
}
