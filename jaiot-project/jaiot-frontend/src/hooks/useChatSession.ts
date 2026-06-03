import { useCallback, useState, useRef } from 'react';
import type { ChatMessage, DecisionResponse } from '../types';
import { uid } from '../utils/format';

/**
 * 聊天状态管理 Hook
 *
 * 管理对话消息流、SSE 流式状态、工具调用过程。
 * 不直接管理 SSE 连接，由 ChatPage 按需创建/销毁 EventSource。
 */
export function useChatSession() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [streaming, setStreaming] = useState(false);
  const [currentTools, setCurrentTools] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const bufferRef = useRef('');
  const assistantMsgId = useRef(uid());

  /** 开始流式接收 — 先插入一条空的 assistant 消息占位 */
  const startStream = useCallback(() => {
    setStreaming(true);
    setError(null);
    setCurrentTools([]);
    bufferRef.current = '';
    assistantMsgId.current = uid();

    setMessages(prev => [...prev, {
      id: assistantMsgId.current,
      role: 'assistant',
      content: '',
      toolCalls: [],
      timestamp: Date.now(),
    }]);
  }, []);

  /** 添加用户消息 */
  const addUserMessage = useCallback((text: string) => {
    setMessages(prev => [...prev, {
      id: uid(),
      role: 'user',
      content: text,
      timestamp: Date.now(),
    }]);
  }, []);

  /** 工具调用过程推送 */
  const pushToolCall = useCallback((msg: string) => {
    setCurrentTools(prev => [...prev, msg]);
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? { ...m, toolCalls: [...(m.toolCalls || []), msg] }
          : m
      )
    );
  }, []);

  /** 流式 token 追加 */
  const appendToken = useCallback((token: string) => {
    bufferRef.current += token;
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? { ...m, content: bufferRef.current }
          : m
      )
    );
  }, []);

  /** 流式完成 — 附加决策结果 */
  const completeStream = useCallback((result?: DecisionResponse) => {
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? {
              ...m,
              decision: result || undefined,
              toolCalls: [...(m.toolCalls || []), '✅ 完成'],
            }
          : m
      )
    );
    setStreaming(false);
    setCurrentTools([]);
    bufferRef.current = '';
  }, []);

  /** 流式异常 — 显示错误消息 */
  const failStream = useCallback((errMsg: string) => {
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? { ...m, content: m.content || `⚠️ ${errMsg}` }
          : m
      )
    );
    setError(errMsg);
    setStreaming(false);
    setCurrentTools([]);
    bufferRef.current = '';
  }, []);

  /** 清除会话 */
  const clearMessages = useCallback(() => {
    setMessages([]);
    setCurrentTools([]);
    setError(null);
    bufferRef.current = '';
  }, []);

  return {
    messages,
    streaming,
    currentTools,
    error,
    startStream,
    addUserMessage,
    pushToolCall,
    appendToken,
    completeStream,
    failStream,
    clearMessages,
  };
}
