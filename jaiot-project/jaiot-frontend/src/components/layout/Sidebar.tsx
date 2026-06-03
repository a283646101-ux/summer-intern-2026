import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  MessageSquare,
  History,
  BarChart3,
  Sprout,
} from 'lucide-react';

const navItems = [
  { path: '/dashboard', label: '实时看板', icon: LayoutDashboard },
  { path: '/chat',      label: '养护对话', icon: MessageSquare },
  { path: '/history',   label: '历史记录', icon: History },
  { path: '/charts',    label: '数据图表', icon: BarChart3 },
];

export function Sidebar() {
  return (
    <aside className="flex flex-col w-60 bg-slate-900/50 border-r border-slate-800/60 shrink-0">
      {/* Logo */}
      <div className="flex items-center gap-3 px-6 py-5 border-b border-slate-800/60">
        <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-forest-500 to-emerald-600 flex items-center justify-center text-lg">
          🦞
        </div>
        <div>
          <h1 className="text-sm font-bold text-slate-100 leading-tight">智能养护</h1>
          <p className="text-[10px] text-slate-500 font-mono">JAIoT Agent</p>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map(({ path, label, icon: Icon }) => (
          <NavLink
            key={path}
            to={path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-forest-500/10 text-forest-400 border border-forest-500/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50 border border-transparent'
              }`
            }
          >
            <Icon size={18} strokeWidth={1.5} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="px-5 py-4 border-t border-slate-800/60">
        <div className="flex items-center gap-2 text-xs text-slate-600">
          <Sprout size={14} className="text-forest-600" />
          <span>JAIoT v0.1</span>
        </div>
      </div>
    </aside>
  );
}
