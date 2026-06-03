import { useEffect, useRef, useCallback, useState } from 'react';
import type { SseEvent, ChatMessage, DecisionResponse } from '../types';
import { uid } from '../utils/format';

type SseHandler = {
  onToolCall?: (msg: string) => void;
  onToken?: (token: string) => void;
  onComplete?: (result: DecisionResponse) => void;
  onError?: (err: string) => void;
};

export function useSSE(url: string, handlers: SseHandler) {
  const sourceRef = useRef<EventSource | null>(null);
  const [connected, setConnected] = useState(false);

  const connect = useCallback(() => {
    disconnect();
    const source = new EventSource(url);
    sourceRef.current = source;

    source.addEventListener('tool_call', (e) => {
      try {
        const data = JSON.parse(e.data);
        handlers.onToolCall?.(data.content || '');
      } catch { /* ignore */ }
    });

    source.addEventListener('token', (e) => {
      try {
        const data = JSON.parse(e.data);
        handlers.onToken?.(data.content || '');
      } catch { /* ignore */ }
    });

    source.addEventListener('complete', (e) => {
      try {
        const data = JSON.parse(e.data);
        handlers.onComplete?.(data as unknown as DecisionResponse);
      } catch { /* ignore */ }
      source.close();
      setConnected(false);
    });

    source.addEventListener('error', (e) => {
      handlers.onError?.('SSE 连接异常');
      source.close();
      setConnected(false);
    });

    source.onopen = () => setConnected(true);
  }, [url]);

  const disconnect = useCallback(() => {
    sourceRef.current?.close();
    sourceRef.current = null;
    setConnected(false);
  }, []);

  useEffect(() => {
    return () => disconnect();
  }, [disconnect]);

  return { connect, disconnect, connected };
}

/** 在聊天场景中使用 SSE + 历史记录 */
export function useChatSSE(sessionId: string = 'device-001') {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [streaming, setStreaming] = useState(false);
  const [currentTools, setCurrentTools] = useState<string[]>([]);
  const bufferRef = useRef('');
  const sessionIdRef = useRef(sessionId);

  const assistantMsgId = useRef(uid());

  // 从后端加载历史
  const loadHistory = useCallback(async (sid?: string) => {
    const s = sid || sessionIdRef.current;
    sessionIdRef.current = s;
    try {
      const { fetchChatHistory } = await import('../api/chat');
      const history = await fetchChatHistory(s);
      const chatMessages: ChatMessage[] = history.map((m) => ({
        id: uid(),
        role: m.role as 'user' | 'assistant',
        content: m.content,
        timestamp: Date.now(),
      }));
      setMessages(chatMessages);
    } catch {
      // 加载失败不影响使用
    }
  }, []);

  const startStream = useCallback((deviceId: string) => {
    setStreaming(true);
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

  const handleToolCall = useCallback((msg: string) => {
    setCurrentTools(prev => [...prev, msg]);
    // 更新消息中的 toolCalls
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? { ...m, toolCalls: [...(m.toolCalls || []), msg] }
          : m
      )
    );
  }, []);

  const handleToken = useCallback((token: string) => {
    bufferRef.current += token;
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? { ...m, content: bufferRef.current }
          : m
      )
    );
  }, []);

  const handleComplete = useCallback((result: DecisionResponse) => {
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? { ...m, decision: result, toolCalls: [...(m.toolCalls || []), '✅ 决策完成'] }
          : m
      )
    );
    setStreaming(false);
    setCurrentTools([]);
    bufferRef.current = '';
  }, []);

  const handleError = useCallback((err: string) => {
    setMessages(prev =>
      prev.map(m =>
        m.id === assistantMsgId.current
          ? { ...m, content: m.content || '⚠️ 连接异常: ' + err }
          : m
      )
    );
    setStreaming(false);
    setCurrentTools([]);
  }, []);

  const addUserMessage = useCallback((text: string) => {
    setMessages(prev => [...prev, {
      id: uid(),
      role: 'user',
      content: text,
      timestamp: Date.now(),
    }]);
  }, []);

  const clearMessages = useCallback(() => {
    setMessages([]);
    setCurrentTools([]);
    bufferRef.current = '';
  }, []);

  return {
    messages,
    streaming,
    currentTools,
    startStream,
    loadHistory,
    addUserMessage,
    handleToolCall,
    handleToken,
    handleComplete,
    handleError,
    clearMessages,
  };
}
