import { clsx } from 'clsx';
import type { LucideIcon } from 'lucide-react';

interface Props {
  label: string;
  value: number;
  unit: string;
  icon: LucideIcon;
  min: number;
  max: number;
  warning: boolean;
  normalRange: string;
}

export function SensorGauge({ label, value, unit, icon: Icon, min, max, warning, normalRange }: Props) {
  const pct = Math.min(100, Math.max(0, ((value - min) / (max - min)) * 100));

  return (
    <div className={clsx(
      'glass-card-hover p-5 flex flex-col',
      warning ? 'ring-1 ring-red-500/20' : ''
    )}>
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Icon size={18} className={warning ? 'text-red-400' : 'text-forest-400'} />
          <span className="text-xs font-medium text-slate-400">{label}</span>
        </div>
        <span className="text-[10px] text-slate-600 font-mono">{normalRange}</span>
      </div>

      {/* Value */}
      <div className="flex items-baseline gap-1 mb-4">
        <span className={clsx(
          'text-3xl font-bold tabular-nums',
          warning ? 'text-red-400' : 'text-slate-100'
        )}>
          {value}
        </span>
        <span className="text-sm text-slate-500 font-mono">{unit}</span>
      </div>

      {/* Progress bar */}
      <div className="relative h-2 bg-slate-800 rounded-full overflow-hidden">
        <div
          className={clsx(
            'h-full rounded-full transition-all duration-500 ease-out',
            warning ? 'bg-gradient-to-r from-red-500 to-orange-400' : 'bg-gradient-to-r from-forest-500 to-emerald-400'
          )}
          style={{ width: `${pct}%` }}
        />
      </div>

      {/* Status */}
      <div className="mt-2 text-[10px] font-medium">
        {warning ? (
          <span className="text-red-400">⚠️ 数值异常</span>
        ) : (
          <span className="text-forest-400">✓ 正常</span>
        )}
      </div>
    </div>
  );
}
