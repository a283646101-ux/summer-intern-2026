import { format, formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';

/** 格式化时间 */
export function formatTime(iso: string): string {
  return format(new Date(iso), 'MM/dd HH:mm:ss');
}

/** 格式化日期 */
export function formatDate(iso: string): string {
  return format(new Date(iso), 'yyyy/MM/dd');
}

/** 相对时间 */
export function timeAgo(iso: string): string {
  return formatDistanceToNow(new Date(iso), { addSuffix: true, locale: zhCN });
}

/** 决策类型 → 中文标签 */
export function decisionLabel(type: string): string {
  const map: Record<string, string> = {
    water: '💧 浇水',
    shade: '🌥️ 遮阳',
    ventilate: '💨 通风',
    'shade-ventilate': '🌥️ 遮阳 + 通风',
    noop: '✅ 无需操作',
    error: '❌ 错误',
    'parse-error': '⚠️ 解析失败',
  };
  return map[type] || type;
}

/** 决策类型 → 颜色类 */
export function decisionColor(type: string): string {
  const map: Record<string, string> = {
    water: 'text-sky-400',
    shade: 'text-amber-400',
    ventilate: 'text-cyan-400',
    'shade-ventilate': 'text-orange-400',
    noop: 'text-forest-400',
    error: 'text-red-400',
    'parse-error': 'text-yellow-400',
  };
  return map[type] || 'text-slate-400';
}

/** 决策类型 → 背景色 */
export function decisionBg(type: string): string {
  const map: Record<string, string> = {
    water: 'bg-sky-500/10 border-sky-500/30',
    shade: 'bg-amber-500/10 border-amber-500/30',
    ventilate: 'bg-cyan-500/10 border-cyan-500/30',
    'shade-ventilate': 'bg-orange-500/10 border-orange-500/30',
    noop: 'bg-forest-500/10 border-forest-500/30',
    error: 'bg-red-500/10 border-red-500/30',
    'parse-error': 'bg-yellow-500/10 border-yellow-500/30',
  };
  return map[type] || 'bg-slate-800 border-slate-700';
}

/** 温度是否异常 */
export function isTempWarning(t: number): boolean {
  return t > 30 || t < 10;
}

/** 湿度是否异常 */
export function isHumidityWarning(h: number): boolean {
  return h < 30 || h > 80;
}

/** 土壤湿度是否异常 */
export function isSoilWarning(s: number): boolean {
  return s < 30 || s > 70;
}

/** 光照是否异常 */
export function isLightWarning(l: number): boolean {
  return l > 2500 || l < 500;
}

/** 生成唯一 ID */
export function uid(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
}

/** 截断文本 */
export function truncate(text: string, max: number): string {
  if (text.length <= max) return text;
  return text.slice(0, max) + '...';
}
