import { useEffect, useState } from 'react';
import { Search, Filter, ChevronDown, ChevronUp, Download } from 'lucide-react';
import type { DecisionLog, DecisionStats } from '../../types';
import { getDecisionHistory, getDecisionStats } from '../../api/decision';
import {
  formatTime,
  decisionLabel,
  decisionColor,
  decisionBg,
} from '../../utils/format';
import { clsx } from 'clsx';

const decisionTypes = ['', 'water', 'shade', 'ventilate', 'shade-ventilate', 'noop'];

export function HistoryPage() {
  const [logs, setLogs] = useState<DecisionLog[]>([]);
  const [stats, setStats] = useState<DecisionStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filterType, setFilterType] = useState('');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [dateFilter, setDateFilter] = useState('');

  const fetch = async (p = 0) => {
    setLoading(true);
    try {
      const [d, s] = await Promise.all([
        getDecisionHistory('device-001', p, 20),
        getDecisionStats('device-001'),
      ]);
      setLogs(d.content);
      setTotalPages(d.totalPages);
      setPage(d.number);
      setStats(s);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetch(0); }, []);

  // Filter locally
  const filtered = logs.filter(l => {
    if (filterType && l.decision !== filterType) return false;
    if (dateFilter) {
      const day = l.createTime.slice(0, 10);
      if (day !== dateFilter) return false;
    }
    return true;
  });

  const handleExport = () => {
    const headers = '时间,决策,温度,湿度,土壤湿度,光照,理由,日语报告\n';
    const rows = logs.map(l =>
      `"${l.createTime}","${l.decision}","${l.temperature}","${l.humidity}","${l.soilMoisture}","${l.light}","${(l.reason || '').replace(/"/g, '""')}","${(l.japaneseReport || '').replace(/"/g, '""')}"`
    ).join('\n');
    const blob = new Blob([headers + rows], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `jaiot-decisions-${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">历史决策记录</h1>
          <p className="text-sm text-slate-500 mt-1">查看 Agent 的所有养护决策</p>
        </div>
        <button
          onClick={handleExport}
          className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-slate-400 bg-slate-800/50 border border-slate-700/50 hover:text-forest-400 hover:border-forest-500/30 transition-all"
        >
          <Download size={14} />
          导出 CSV
        </button>
      </div>

      {/* Stats bar */}
      {stats && (
        <div className="flex gap-2 flex-wrap">
          {Object.entries(stats).map(([key, count]) => (
            <span
              key={key}
              className={clsx(
                'px-3 py-1.5 rounded-full text-xs font-medium border',
                decisionBg(key),
                decisionColor(key)
              )}
            >
              {decisionLabel(key)} × {count}
            </span>
          ))}
        </div>
      )}

      {/* Filters */}
      <div className="flex gap-3">
        <div className="relative">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="date"
            value={dateFilter}
            onChange={e => setDateFilter(e.target.value)}
            className="bg-slate-900/70 border border-slate-700/60 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-300 focus:outline-none focus:ring-2 focus:ring-forest-500/40"
          />
        </div>
        <div className="flex gap-1">
          {decisionTypes.map(t => (
            <button
              key={t}
              onClick={() => setFilterType(t)}
              className={clsx(
                'px-3 py-1.5 rounded-xl text-xs font-medium border transition-all',
                filterType === t
                  ? 'bg-forest-500/10 text-forest-400 border-forest-500/30'
                  : 'text-slate-500 border-slate-700/50 hover:text-slate-300 hover:border-slate-600'
              )}
            >
              {t ? decisionLabel(t) : '全部'}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="space-y-2">
        {loading && (
          <div className="text-center py-12 text-sm text-slate-500">加载中...</div>
        )}

        {!loading && filtered.length === 0 && (
          <div className="text-center py-12 text-sm text-slate-600">暂无决策记录</div>
        )}

        {filtered.map((log) => (
          <div key={log.id} className="glass-card overflow-hidden">
            {/* Summary row */}
            <div
              onClick={() => setExpandedId(expandedId === log.id ? null : log.id)}
              className="flex items-center gap-4 px-5 py-3 cursor-pointer hover:bg-slate-800/30 transition-colors"
            >
              <span className={clsx(
                'text-xs font-semibold shrink-0',
                decisionColor(log.decision)
              )}>
                {decisionLabel(log.decision)}
              </span>
              <div className="flex gap-3 text-xs text-slate-500 font-mono flex-1">
                <span>🌡️ {log.temperature}°C</span>
                <span>💧 {log.humidity}%</span>
                <span>🪴 {log.soilMoisture}%</span>
                <span>☀️ {log.light}Lux</span>
              </div>
              <span className="text-[10px] text-slate-600 shrink-0">
                {formatTime(log.createTime)}
              </span>
              {expandedId === log.id
                ? <ChevronUp size={14} className="text-slate-500 shrink-0" />
                : <ChevronDown size={14} className="text-slate-500 shrink-0" />
              }
            </div>

            {/* Expanded detail */}
            {expandedId === log.id && (
              <div className="px-5 pb-4 pt-1 border-t border-slate-800/60 animate-slide-up space-y-3">
                {log.reason && (
                  <div>
                    <p className="text-[10px] text-slate-500 font-semibold uppercase mb-1">📋 理由</p>
                    <p className="text-sm text-slate-300">{log.reason}</p>
                  </div>
                )}
                {log.advice && (
                  <div>
                    <p className="text-[10px] text-slate-500 font-semibold uppercase mb-1">💡 建议</p>
                    <p className="text-sm text-amber-300">{log.advice}</p>
                  </div>
                )}
                {log.japaneseReport && (
                  <details className="group">
                    <summary className="cursor-pointer text-[10px] text-forest-400 font-semibold hover:text-forest-300">
                      🇯🇵 日语养护报告
                    </summary>
                    <p className="mt-2 text-sm text-slate-400 leading-relaxed whitespace-pre-wrap">
                      {log.japaneseReport}
                    </p>
                  </details>
                )}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-center gap-2">
        {Array.from({ length: Math.min(totalPages, 10) }, (_, i) => (
          <button
            key={i}
            onClick={() => fetch(i)}
            className={clsx(
              'w-8 h-8 rounded-lg text-xs font-medium border transition-all',
              page === i
                ? 'bg-forest-500/10 text-forest-400 border-forest-500/30'
                : 'text-slate-500 border-slate-700/50 hover:text-slate-300'
            )}
          >
            {i + 1}
          </button>
        ))}
      </div>
    </div>
  );
}
