import { useState, useCallback } from 'react';
import type { PageResponse, DecisionLog, DecisionStats } from '../types';
import { getDecisionHistory, getDecisionStats } from '../api/decision';

export function useDecisionHistory(deviceId = 'device-001') {
  const [page, setPage] = useState<PageResponse<DecisionLog> | null>(null);
  const [stats, setStats] = useState<DecisionStats | null>(null);
  const [loading, setLoading] = useState(false);

  const fetch = useCallback(async (p = 0, size = 20) => {
    setLoading(true);
    try {
      const [d, s] = await Promise.all([
        getDecisionHistory(deviceId, p, size),
        getDecisionStats(deviceId),
      ]);
      setPage(d);
      setStats(s);
    } catch { /* ignore */ }
    setLoading(false);
  }, [deviceId]);

  return { page, stats, loading, fetch };
}
