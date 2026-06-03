import { clsx } from 'clsx';
import { Bot, User } from 'lucide-react';
import type { ChatMessage } from '../../types';
import { decisionLabel, decisionColor } from '../../utils/format';

interface Props {
  message: ChatMessage;
}

export function MessageBubble({ message }: Props) {
  const isUser = message.role === 'user';
  const isStreaming = message.role === 'assistant' && message.content === '';

  return (
    <div className={clsx(
      'flex gap-3 animate-slide-up',
      isUser ? 'flex-row-reverse' : ''
    )}>
      {/* Avatar */}
      <div className={clsx(
        'w-8 h-8 rounded-full flex items-center justify-center shrink-0',
        isUser
          ? 'bg-sky-500/20 text-sky-400'
          : 'bg-forest-500/20 text-forest-400'
      )}>
        {isUser ? <User size={16} /> : <Bot size={16} />}
      </div>

      {/* Bubble */}
      <div className={clsx(
        'max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed',
        isUser
          ? 'bg-sky-500/10 text-sky-100 border border-sky-500/20'
          : 'glass-card text-slate-200'
      )}>
        {/* Tool call tags */}
        {message.toolCalls && message.toolCalls.length > 0 && !isStreaming && (
          <div className="flex flex-wrap gap-1.5 mb-2">
            {message.toolCalls.map((t, i) => (
              <span
                key={i}
                className={clsx(
                  'px-2 py-0.5 rounded-full text-[10px] font-medium border',
                  t.includes('✅')
                    ? 'text-forest-400 bg-forest-500/10 border-forest-500/20'
                    : 'text-amber-400 bg-amber-500/10 border-amber-500/20'
                )}
              >
                {t}
              </span>
            ))}
          </div>
        )}

        {/* Decision badge */}
        {message.decision && (
          <div className="mb-2">
            <span className={clsx(
              'inline-block px-2 py-0.5 rounded-full text-[10px] font-semibold',
              decisionColor(message.decision.decision)
            )}>
              🏷️ {decisionLabel(message.decision.decision)}
            </span>
          </div>
        )}

        {/* Content */}
        <div className="whitespace-pre-wrap break-words">
          {message.content}
          {isStreaming && <span className="typing-cursor" />}
        </div>

        {/* Decision details */}
        {message.decision && !isStreaming && (
          <div className="mt-3 pt-3 border-t border-slate-700/50 space-y-2 text-xs text-slate-400">
            <div className="flex gap-3 font-mono">
              <span>🌡️ {message.decision.temperature}°C</span>
              <span>💧 {message.decision.humidity}%</span>
              <span>🪴 {message.decision.soilMoisture}%</span>
              <span>☀️ {message.decision.light} Lux</span>
            </div>

            {message.decision.reason && (
              <p className="text-slate-300">{message.decision.reason}</p>
            )}

            {message.decision.japaneseReport && (
              <details className="group">
                <summary className="cursor-pointer text-forest-400 text-[10px] hover:text-forest-300">
                  🇯🇵 查看日语报告
                </summary>
                <p className="mt-2 text-slate-400 leading-relaxed whitespace-pre-wrap">
                  {message.decision.japaneseReport}
                </p>
              </details>
            )}

            {message.decision.advice && (
              <div className="mt-2 p-2 rounded-lg bg-amber-500/5 border border-amber-500/10">
                <p className="text-amber-300 text-[10px] font-semibold mb-1">💡 养护建议</p>
                <p className="text-slate-300">{message.decision.advice}</p>
              </div>
            )}
          </div>
        )}

        {/* Timestamp */}
        {!isStreaming && (
          <div className={clsx(
            'mt-1 text-[10px]',
            isUser ? 'text-sky-600 text-right' : 'text-slate-600'
          )}>
            {new Date(message.timestamp).toLocaleTimeString('zh-CN')}
          </div>
        )}
      </div>
    </div>
  );
}
