# 🦞 OpenClaw AI 工具集

基于 **OpenClaw Agent 编排框架 + 大模型 API** 封装的 AI 自动化工具集，解决实际工作流问题。

---

## 📦 工具列表

### 🦞 Daily Science Pusher — AI 每日科普推送
一条赛博龙虾每天为你打工：自动选题 → AI 生成科普 → 定时推送邮箱。全流程无人值守。

[→ 查看详情](daily-science-pusher/)

### 🤖 AI 前沿资讯聚合 & 教程生成系统
多信源聚合 AI 技术资讯 → LLM 摘要去重 → 分层输出（速览/深度/教程）→ 每 3 天推送邮件。

[→ 查看详情](ai-news-aggregator/)

---

## 🔧 通用环境要求

- Python 3.9+
- 依赖：`pip install requests zhipuai` 或 `pip install requests openai`
- 智谱 GLM API Key（环境变量 `ZHIPU_API_KEY`）
- QQ 邮箱 SMTP 授权码（可选，用于邮件推送）
