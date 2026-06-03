import { useState, useEffect, useCallback } from 'react';
import type { SensorData, DecisionLog } from '../types';
import { getLatestSensor } from '../api/sensor';
import { getRecentDecisions } from '../api/decision';

export function useSensorData(deviceId = 'device-001') {
  const [data, setData] = useState<SensorData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const d = await getLatestSensor(deviceId);
      setData(d);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [deviceId]);

  useEffect(() => { fetch(); }, [fetch]);

  return { data, loading, error, refresh: fetch };
}

export function useLatestDecision(deviceId = 'device-001') {
  const [decision, setDecision] = useState<DecisionLog | null>(null);
  const [loading, setLoading] = useState(true);

  const fetch = useCallback(async () => {
    try {
      const recent = await getRecentDecisions(deviceId, 1);
      setDecision(recent[0] || null);
    } catch { /* ignore */ }
    setLoading(false);
  }, [deviceId]);

  useEffect(() => { fetch(); }, [fetch]);

  return { decision, loading, refresh: fetch };
}
