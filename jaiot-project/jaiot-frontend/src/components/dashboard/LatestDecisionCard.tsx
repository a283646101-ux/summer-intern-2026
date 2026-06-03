import { clsx } from 'clsx';
import { ChevronRight } from 'lucide-react';
import type { DecisionLog } from '../../types';
import { decisionLabel, decisionColor, decisionBg, formatTime, truncate } from '../../utils/format';

interface Props {
  decision: DecisionLog;
  title: string;
  isManual?: boolean;
  onClick?: () => void;
}

export function LatestDecisionCard({ decision, title, isManual, onClick }: Props) {
  return (
    <div
      onClick={onClick}
      className={clsx(
        'glass-card-hover p-5',
        onClick && 'cursor-pointer',
        isManual && 'ring-1 ring-amber-500/20'
      )}
    >
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
          {title}
        </h3>
        {isManual && (
          <span className="text-[10px] text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded-full">
            手动
          </span>
        )}
        {onClick && <ChevronRight size={14} className="text-slate-600" />}
      </div>

      {decision ? (
        <>
          {/* Decision badge */}
          <span className={clsx(
            'inline-block px-3 py-1 rounded-full text-xs font-semibold border',
            decisionBg(decision.decision),
            decisionColor(decision.decision)
          )}>
            {decisionLabel(decision.decision)}
          </span>

          {/* Sensor data summary */}
          <div className="flex gap-4 mt-3 text-xs text-slate-500 font-mono">
            <span>🌡️ {decision.temperature}°C</span>
            <span>💧 {decision.humidity}%</span>
            <span>🪴 {decision.soilMoisture}%</span>
            <span>☀️ {decision.light}Lux</span>
          </div>

          {/* Reason */}
          <p className="mt-2 text-sm text-slate-300 leading-relaxed">
            {truncate(decision.reason || '暂无理由', 120)}
          </p>

          {/* Time */}
          <p className="mt-2 text-[10px] text-slate-600">
            {formatTime(decision.createTime)}
          </p>
        </>
      ) : (
        <p className="text-sm text-slate-500">暂无决策记录</p>
      )}
    </div>
  );
}
