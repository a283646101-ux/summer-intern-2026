# JAIoT Frontend — 智能养护 Agent 前端

## 启动

```bash
cd jaiot-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`，自动代理 `/api` 请求到后端 `http://localhost:8080`。

## 页面

| 路由 | 页面 | 功能 |
|------|------|------|
| `/dashboard` | 实时看板 | 传感器仪表盘 + 最新决策 + 触发决策按钮 |
| `/chat` | 养护对话 | 流式打字机 + 工具调用标签 + 日语报告折叠 |
| `/history` | 历史记录 | 决策列表 + 筛选 + 统计 + 导出 CSV |
| `/charts` | 数据图表 | 24h/7天折线图 + 多指标切换 |
