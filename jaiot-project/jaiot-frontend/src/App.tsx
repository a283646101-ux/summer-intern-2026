import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { DashboardPage } from './components/dashboard/DashboardPage';
import { ChatPage } from './components/chat/ChatPage';
import { HistoryPage } from './components/history/HistoryPage';
import { ChartsPage } from './components/charts/ChartsPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/chat" element={<ChatPage />} />
          <Route path="/history" element={<HistoryPage />} />
          <Route path="/charts" element={<ChartsPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
