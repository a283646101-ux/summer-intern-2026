import { useState, useEffect } from 'react';
import { RefreshCw, Play, Thermometer, Droplets, Sun, Sprout } from 'lucide-react';
import { useSensorData, useLatestDecision } from '../../hooks/useSensorData';
import { SensorGauge } from './SensorGauge';
import { LatestDecisionCard } from './LatestDecisionCard';
import { isTempWarning, isHumidityWarning, isSoilWarning, isLightWarning, timeAgo } from '../../utils/format';
import { triggerDecision } from '../../api/agent';
import { useNavigate } from 'react-router-dom';
import type { DecisionResponse } from '../../types';

export function DashboardPage() {
  const { data: sensor, loading: sensorLoading, refresh: refreshSensor } = useSensorData();
  const { decision, loading: decLoading, refresh: refreshDecision } = useLatestDecision();
  const navigate = useNavigate();

  const [autoRefresh, setAutoRefresh] = useState(true);
  const [deciding, setDeciding] = useState(false);
  const [lastDecision, setLastDecision] = useState<DecisionResponse | null>(null);

  // 自动刷新
  useEffect(() => {
    if (!autoRefresh) return;
    const timer = setInterval(() => {
      refreshSensor();
      refreshDecision();
    }, 15000);
    return () => clearInterval(timer);
  }, [autoRefresh, refreshSensor, refreshDecision]);

  const handleManualDecision = async () => {
    setDeciding(true);
    try {
      const result = await triggerDecision();
      setLastDecision(result);
      refreshDecision();
    } catch { /* ignore */ }
    setDeciding(false);
  };

  const gauges = sensor
    ? [
        {
          label: '温度',
          value: sensor.temperature,
          unit: '°C',
          icon: Thermometer,
          min: 0, max: 45,
          warning: isTempWarning(sensor.temperature),
          normalRange: '10–30°C',
        },
        {
          label: '空气湿度',
          value: sensor.humidity,
          unit: '%',
          icon: Droplets,
          min: 0, max: 100,
          warning: isHumidityWarning(sensor.humidity),
          normalRange: '30–80%',
        },
        {
          label: '土壤湿度',
          value: sensor.soilMoisture,
          unit: '%',
          icon: Sprout,
          min: 0, max: 100,
          warning: isSoilWarning(sensor.soilMoisture),
          normalRange: '30–70%',
        },
        {
          label: '光照强度',
          value: sensor.light,
          unit: 'Lux',
          icon: Sun,
          min: 0, max: 4000,
          warning: isLightWarning(sensor.light),
          normalRange: '500–2500 Lux',
        },
      ]
    : [];

  return (
    <div className="max-w-7xl mx-auto space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">实时环境看板</h1>
          <p className="text-sm text-slate-500 mt-1">
            {sensor ? `最后更新 ${timeAgo(sensor.createTime)}` : '等待传感器数据...'}
          </p>
        </div>
        <div className="flex items-center gap-3">
          {/* Auto-refresh toggle */}
          <button
            onClick={() => setAutoRefresh(!autoRefresh)}
            className={`flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-medium transition-all ${
              autoRefresh
                ? 'bg-forest-500/10 text-forest-400 border border-forest-500/20'
                : 'bg-slate-800/50 text-slate-500 border border-slate-700/50'
            }`}
          >
            <RefreshCw size={14} className={autoRefresh ? 'animate-spin-slow' : ''} />
            {autoRefresh ? '自动刷新中' : '自动刷新关闭'}
          </button>

          {/* Manual refresh */}
          <button
            onClick={refreshSensor}
            disabled={sensorLoading}
            className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-medium bg-slate-800/50 text-slate-400 border border-slate-700/50 hover:bg-slate-700/50 transition-colors"
          >
            <RefreshCw size={14} className={sensorLoading ? 'animate-spin' : ''} />
            刷新
          </button>

          {/* Trigger decision */}
          <button
            onClick={handleManualDecision}
            disabled={deciding}
            className="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold bg-gradient-to-r from-forest-600 to-emerald-600 text-white hover:from-forest-500 hover:to-emerald-500 transition-all shadow-lg shadow-forest-500/20"
          >
            <Play size={14} className={deciding ? 'animate-pulse' : ''} />
            {deciding ? '分析中...' : '触发养护决策'}
          </button>
        </div>
      </div>

      {/* Sensor gauges */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {gauges.map((g) => (
          <SensorGauge key={g.label} {...g} />
        ))}
      </div>

      {/* Latest decision + last manual decision */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {decision && (
          <LatestDecisionCard
            decision={decision}
            title="最近自动决策"
            onClick={() => navigate('/history')}
          />
        )}
        {lastDecision && (
          <LatestDecisionCard
            decision={{
              ...lastDecision,
              id: 0,
              deviceId: lastDecision.deviceId,
              rawResponse: '',
              createTime: new Date().toISOString(),
            }}
            title="手动触发决策"
            isManual
          />
        )}
      </div>

      {/* Quick links */}
      <div className="flex gap-3">
        <button
          onClick={() => navigate('/chat')}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm bg-slate-800/50 text-slate-300 border border-slate-700/50 hover:bg-slate-700/50 transition-all"
        >
          💬 去对话面板提问
        </button>
        <button
          onClick={() => navigate('/charts')}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm bg-slate-800/50 text-slate-300 border border-slate-700/50 hover:bg-slate-700/50 transition-all"
        >
          📊 查看历史数据趋势
        </button>
      </div>
    </div>
  );
}
