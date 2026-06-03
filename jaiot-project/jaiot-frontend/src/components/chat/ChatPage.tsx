import { useState, useRef, useEffect, useCallback } from 'react';
import {
  Send, Trash2, Sparkles, Droplets, Sun, Wind, BookOpen, Loader2,
} from 'lucide-react';
import { useChatSession } from '../../hooks/useChatSession';
import { MessageBubble } from './MessageBubble';
import { getStreamDecisionUrl } from '../../api/agent';
import { sendChat } from '../../api/agent';
import { triggerDecision } from '../../api/agent';
import type { DecisionResponse } from '../../types';

const quickActions = [
  { label: '今天需要浇水吗？', icon: Droplets },
  { label: '光照是否充足？', icon: Sun },
  { label: '需要通风吗？', icon: Wind },
  { label: '本周养护建议', icon: Sparkles },
  { label: '查询养护知识', icon: BookOpen },
];

const GREETING = `🦞 你好！我是虾哥，你的智能养护助手。

我可以根据实时传感器数据分析植物状态，给出浇水、遮阳、通风等养护建议。

**你可以试试：**
• 点击下方的快捷提问
• 直接输入养护问题（如"叶子发黄怎么办"）
• 输入"决策"或"养护"等关键字 → 自动触发传感器分析 + 流式输出`;

export function ChatPage() {
  const {
    messages, streaming, currentTools, error,
    startStream, addUserMessage, pushToolCall,
    appendToken, completeStream, failStream, clearMessages,
  } = useChatSession();

  const [input, setInput] = useState('');
  const endRef = useRef<HTMLDivElement>(null);
  const sourceRef = useRef<EventSource | null>(null);

  // 自动滚动到底部
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, currentTools]);

  // 组件卸载时清理 SSE
  useEffect(() => () => sourceRef.current?.close(), []);

  /** 关闭旧 SSE */
  const closeSSE = useCallback(() => {
    sourceRef.current?.close();
    sourceRef.current = null;
  }, []);

  /** 创建 SSE 连接并绑定事件 */
  const connectSSE = useCallback((url: string) => {
    closeSSE();
    const source = new EventSource(url);
    sourceRef.current = source;

    source.addEventListener('tool_call', (e: MessageEvent) => {
      try {
        const data = JSON.parse(e.data);
        pushToolCall(data.content || '');
      } catch { /* ignore malformed event */ }
    });

    source.addEventListener('token', (e: MessageEvent) => {
      try {
        const data = JSON.parse(e.data);
        appendToken(data.content || '');
      } catch { /* ignore */ }
    });

    source.addEventListener('complete', (e: MessageEvent) => {
      try {
        const data = JSON.parse(e.data);
        completeStream(data as DecisionResponse);
      } catch {
        completeStream();
      }
      source.close();
      sourceRef.current = null;
    });

    source.addEventListener('error', () => {
      // EventSource 在连接失败时也会触发 error
      // 如果有 onopen 回调且未触发，说明连接失败
      if (source.readyState === EventSource.CLOSED) {
        failStream('后端连接失败，请确认后端已启动');
        source.close();
        sourceRef.current = null;
      }
    });

    // 5 秒超时保护
    const timeout = setTimeout(() => {
      if (source.readyState !== EventSource.CLOSED) {
        failStream('后端响应超时，请稍后重试');
        source.close();
        sourceRef.current = null;
      }
    }, 30_000);

    source.addEventListener('complete', () => clearTimeout(timeout), { once: true });

    return source;
  }, [pushToolCall, appendToken, completeStream, failStream]);

  /** 发送消息 */
  const handleSend = useCallback(async () => {
    const text = input.trim();
    if (!text || streaming) return;
    setInput('');

    addUserMessage(text);
    startStream();

    try {
      if (isQueryAboutDecision(text)) {
        // ----- 走 SSE 流式决策 -----
        connectSSE(getStreamDecisionUrl('device-001'));
      } else {
        // ----- 走 REST API 普通对话 -----
        const reply = await sendChat(text);

        // 模拟逐字输出 （打字机效果）
        for (let i = 0; i < reply.length; i++) {
          appendToken(reply[i]);
          // 随机间隔 10~30ms 模拟真实打字机
          await new Promise(r => setTimeout(r, 10 + Math.random() * 20));
        }
        completeStream();
      }
    } catch (e: any) {
      failStream(e.message || '请求失败，请重试');
    }
  }, [input, streaming, addUserMessage, startStream, connectSSE, appendToken, completeStream, failStream]);

  return (
    <div className="flex flex-col h-[calc(100vh-5rem)] max-w-4xl mx-auto animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">养护对话</h1>
          <p className="text-sm text-slate-500 mt-1">向虾哥提问，获取智能养护建议</p>
        </div>
        {messages.length > 0 && (
          <button
            onClick={clearMessages}
            className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-slate-500 bg-slate-800/50 border border-slate-700/50 hover:text-red-400 hover:border-red-500/30 transition-all"
          >
            <Trash2 size={14} />
            清除会话
          </button>
        )}
      </div>

      {/* Error banner */}
      {error && (
        <div className="mb-3 px-4 py-2 rounded-xl bg-red-500/10 border border-red-500/20 text-xs text-red-400 flex items-center gap-2">
          <span>⚠️ {error}</span>
          <button
            onClick={() => { /* error 会在下一次用户发送后自动清除 */ }}
            className="ml-auto text-red-500 hover:text-red-400"
          >
            ✕
          </button>
        </div>
      )}

      {/* Message list */}
      <div className="flex-1 overflow-y-auto space-y-3 pr-2">
        {/* 首次进入的问候语 */}
        {messages.length === 0 && (
          <div className="glass-card p-5 text-sm text-slate-300 leading-relaxed whitespace-pre-line">
            {GREETING}
          </div>
        )}

        {messages.map((msg) => (
          <MessageBubble key={msg.id} message={msg} />
        ))}

        {/* 工具调用过程提示条 */}
        {streaming && currentTools.length > 0 && (
          <div className="flex items-center gap-2 ml-12 text-xs text-forest-400 mb-2">
            <Loader2 size={12} className="animate-spin" />
            <span>虾哥正在：</span>
            <div className="flex flex-wrap gap-1">
              {currentTools.map((t, i) => (
                <span
                  key={i}
                  className="px-2 py-0.5 rounded-full bg-forest-500/10 text-[10px] border border-forest-500/20"
                >
                  {t}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* 打字机光标 */}
        {streaming && (
          <div className="flex items-center gap-2 ml-12 text-sm text-forest-400">
            <span className="typing-cursor" />
          </div>
        )}

        <div ref={endRef} />
      </div>

      {/* 快捷提问（仅首次显示） */}
      {messages.length === 0 && (
        <div className="flex gap-2 mt-4 overflow-x-auto pb-2">
          {quickActions.map(({ label, icon: Icon }) => (
            <button
              key={label}
              onClick={() => setInput(label)}
              className="flex items-center gap-1.5 shrink-0 px-3 py-2 rounded-xl text-xs text-slate-400 bg-slate-800/50 border border-slate-700/50 hover:border-forest-500/30 hover:text-forest-400 transition-all"
            >
              <Icon size={12} />
              {label}
            </button>
          ))}
        </div>
      )}

      {/* 输入框 */}
      <div className="mt-4 flex gap-2">
        <input
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault();
              handleSend();
            }
          }}
          placeholder="输入养护问题，回车发送..."
          disabled={streaming}
          className="flex-1 bg-slate-900/70 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-forest-500/40 focus:border-forest-500/40 transition-all disabled:opacity-50"
        />
        <button
          onClick={handleSend}
          disabled={!input.trim() || streaming}
          className="flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-r from-forest-600 to-emerald-600 text-white disabled:opacity-40 hover:from-forest-500 hover:to-emerald-500 transition-all shadow-lg shadow-forest-500/20"
        >
          {streaming ? (
            <Loader2 size={18} className="animate-spin" />
          ) : (
            <Send size={18} />
          )}
        </button>
      </div>
    </div>
  );
}

/** 判断查询是否需要触发传感器决策（走 SSE） vs 普通对话（走 REST） */
function isQueryAboutDecision(text: string): boolean {
  const decisionKeywords = [
    '浇水', '遮阳', '通风', '养护', '决策',
    '传感器', '数据', '状态', '需要', '健康',
  ];
  return decisionKeywords.some(k => text.includes(k));
}
