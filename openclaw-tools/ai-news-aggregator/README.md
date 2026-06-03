# 🦞 AI 前沿资讯聚合 & 教程生成系统

> **多信源聚合 → LLM 摘要去重 → 分层输出 → 定期推送**
> 每 3 天自动运行一次，追踪 AI 前沿动态。

---

## ✨ 功能特性

| 特性 | 说明 |
|------|------|
| 📡 **三信道聚合** | Arxiv 最新论文 / GitHub Trending AI 仓库 / Hacker News AI 讨论 |
| 🤖 **LLM 智能处理** | 智谱 GLM-4-Flash 自动摘要、去重、生成教程 |
| 📊 **三层输出** | 🚀 极简速览 → 📖 深度阅读 → 🛠️ 动手教程 |
| 🔄 **智能去重** | 基于 URL/title 哈希，避免重复推送 |
| 📬 **邮件推送** | 支持 QQ 邮箱 SMTP 推送 |
| 💾 **本地归档** | 每次生成 Markdown 摘要文件，可回溯查阅 |

## 📡 信源说明

| 信源 | 获取方式 | 内容 |
|------|----------|------|
| **Arxiv** | 官方 API | cs.AI / cs.CL / cs.LG / cs.CV 最新论文 |
| **GitHub Trending** | 页面爬取 | AI/LLM/Agent 相关热门仓库（按关键词筛选） |
| **Hacker News** | Firebase API | AI 相关热门技术讨论（Top Stories 中筛选） |

## 🖥️ 三层内容结构

```
# 🚀 极简速览
每条资讯 1 句话，30 秒看完本期看点

# 📖 深度阅读
每条资讯 100-200 字技术拆解：
- 解决了什么问题
- 技术/方法亮点
- 为什么值得关注

# 🛠️ 动手教程
选 1-2 条最有实践价值的资讯 → 完整教程：
- 环境准备、配置步骤
- 关键代码片段
- 验证方式
```

## 🧪 快速启动

```bash
# 1. 配置
cd openclaw-tools/ai-news-aggregator
cp config.template.py config.py
# 编辑 config.py 填入 ZHIPU_API_KEY

# 2. 安装依赖
pip install requests zhipuai  # 或 pip install requests openai

# 3. 手动运行
python3 ai_news_aggregator.py

# 4. 查看输出
cat data/digest_$(date +%Y-%m-%d).md
```

## ⏰ 定时设置

### 方式一：系统 Crontab（推荐）

```bash
# 每 3 天早 8:00 运行
crontab -e
# 添加一行：
0 8 */3 * * /path/to/ai-news-aggregator/ai_news_cron.sh
```

### 方式二：OpenClaw Cron

```yaml
# 在 OpenClaw 中设置
schedule:
  kind: cron
  expr: "0 8 */3 * *"
  tz: "Asia/Shanghai"
payload:
  kind: agentTurn
  message: 运行 AI 资讯聚合系统
```

## 📁 项目结构

```
ai-news-aggregator/
├── ai_news_aggregator.py    # 🧠 主脚本
├── ai_news_cron.sh          # ⏰ cron 包装脚本
├── config.template.py       # 📝 配置模板
├── README.md                # 📄 本文件
└── data/                    # 📂 输出归档
    ├── digest_YYYY-MM-DD.md #   Markdown 摘要文件
    └── history.json         #   去重记录
```
