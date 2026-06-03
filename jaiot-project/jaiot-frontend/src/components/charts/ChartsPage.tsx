import { useEffect, useState, useMemo } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { CalendarDays, Clock, TrendingUp } from 'lucide-react';
import type { SensorData } from '../../types';
import { getSensorHistoryByRange } from '../../api/sensor';
import { getRecentDecisions } from '../../api/decision';
import { format } from 'date-fns';
import { clsx } from 'clsx';

type Range = '24h' | '7d';

export function ChartsPage() {
  const [range, setRange] = useState<Range>('24h');
  const [sensorData, setSensorData] = useState<SensorData[]>([]);
  const [decisionPoints, setDecisionPoints] = useState<{ time: number; label: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeMetric, setActiveMetric] = useState<string>('temperature');

  const fetchData = async () => {
    setLoading(true);
    try {
      const now = new Date();
      const start = range === '24h'
        ? new Date(now.getTime() - 24 * 60 * 60 * 1000)
        : new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

      const [sensorRes, decisions] = await Promise.all([
        getSensorHistoryByRange(
          'device-001',
          start,
          now,
          0,
          range === '24h' ? 100 : 300
        ),
        getRecentDecisions('device-001', 20),
      ]);

      setSensorData(sensorRes.content);

      // Map decision points
      const points = decisions
        .filter(d => d.decision && d.decision !== 'noop')
        .map(d => ({
          time: new Date(d.createTime).getTime(),
          label: d.decision,
        }));
      setDecisionPoints(points);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, [range]);

  // Chart data
  const chartData = useMemo(() => {
    return sensorData.map(d => ({
      time: new Date(d.createTime).getTime(),
      temperature: d.temperature,
      humidity: d.humidity,
      soilMoisture: d.soilMoisture,
      light: d.light,
    }));
  }, [sensorData]);

  const metrics = [
    { key: 'temperature', label: '温度', color: '#ef4444', unit: '°C' },
    { key: 'humidity', label: '湿度', color: '#3b82f6', unit: '%' },
    { key: 'soilMoisture', label: '土壤湿度', color: '#10b981', unit: '%' },
    { key: 'light', label: '光照', color: '#f59e0b', unit: 'Lux' },
  ];

  const currentMetric = metrics.find(m => m.key === activeMetric)!;

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">数据趋势图</h1>
          <p className="text-sm text-slate-500 mt-1">传感器数据历史变化趋势</p>
        </div>
        <div className="flex gap-2">
          {(['24h', '7d'] as Range[]).map(r => (
            <button
              key={r}
              onClick={() => setRange(r)}
              className={clsx(
                'flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-medium border transition-all',
                range === r
                  ? 'bg-forest-500/10 text-forest-400 border-forest-500/30'
                  : 'text-slate-500 border-slate-700/50 hover:text-slate-300'
              )}
            >
              {r === '24h' ? <Clock size={12} /> : <CalendarDays size={12} />}
              {r === '24h' ? '24 小时' : '7 天'}
            </button>
          ))}
        </div>
      </div>

      {/* Metric selector */}
      <div className="flex gap-2">
        {metrics.map(m => (
          <button
            key={m.key}
            onClick={() => setActiveMetric(m.key)}
            className={clsx(
              'flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-medium border transition-all',
              activeMetric === m.key
                ? 'bg-slate-800 text-slate-200 border-slate-600'
                : 'text-slate-500 border-slate-700/50 hover:text-slate-300'
            )}
          >
            <TrendingUp size={12} style={{ color: m.color }} />
            {m.label}
          </button>
        ))}
      </div>

      {/* Main chart */}
      <div className="glass-card p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-slate-200">
            {currentMetric.label}变化趋势
          </h3>
          <span className="text-xs text-slate-500 font-mono">
            {chartData.length} 个采样点
          </span>
        </div>

        {loading ? (
          <div className="h-[400px] flex items-center justify-center text-sm text-slate-500">
            加载数据中...
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={400}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
              <XAxis
                dataKey="time"
                tick={{ fill: '#64748b', fontSize: 11 }}
                tickLine={{ stroke: '#334155' }}
                axisLine={{ stroke: '#334155' }}
                tickFormatter={(v) => {
                  const fmt = range === '24h' ? 'HH:mm' : 'MM/dd';
                  return format(new Date(v), fmt);
                }}
              />
              <YAxis
                tick={{ fill: '#64748b', fontSize: 11 }}
                tickLine={{ stroke: '#334155' }}
                axisLine={{ stroke: '#334155' }}
                width={50}
              />
              <Tooltip
                contentStyle={{
                  background: '#0f172a',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  fontSize: '12px',
                }}
                labelFormatter={(v) => format(new Date(v), 'yyyy/MM/dd HH:mm')}
                formatter={(value: number) => [`${value}${currentMetric.unit}`, currentMetric.label]}
              />
              <Line
                type="monotone"
                dataKey={activeMetric}
                stroke={currentMetric.color}
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
              />
              {/* Decision markers */}
              {decisionPoints.map((dp, i) => {
                const closest = chartData.find(d =>
                  Math.abs(d.time - dp.time) < 60000 // within 1 min
                );
                if (!closest) return null;
                return (
                  <Line
                    key={i}
                    dataKey="temperature"
                    stroke="transparent"
                    dot={{ r: 6, fill: '#f59e0b', stroke: '#f59e0b', strokeWidth: 0 }}
                  />
                );
              })}
              <Legend />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Mini charts for all metrics */}
      <div className="grid grid-cols-2 gap-4">
        {metrics.map(m => (
          <div key={m.key} className="glass-card p-4">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-slate-400">{m.label}</span>
              <span className="text-[10px] text-slate-600 font-mono">{m.unit}</span>
            </div>
            <ResponsiveContainer width="100%" height={120}>
              <LineChart data={chartData.slice(-50)}>
                <Line
                  type="monotone"
                  dataKey={m.key}
                  stroke={m.color}
                  strokeWidth={1.5}
                  dot={false}
                />
                <XAxis
                  dataKey="time"
                  hide
                />
                <YAxis hide />
              </LineChart>
            </ResponsiveContainer>
          </div>
        ))}
      </div>
    </div>
  );
}
