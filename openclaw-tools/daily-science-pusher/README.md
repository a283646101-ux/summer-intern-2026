# 🦞 Daily Science Pusher

> **每天早晨 8:00，一条有趣的生活科普知识，自动推送到你的邮箱。**

一条赛博龙虾为你打工，每天用大模型生成通俗易懂的科学小知识，保存到本地，同时通过邮件送达。适合那些想在早餐时间随手学点东西的人。

---

## ✨ 功能

| 功能 | 说明 |
|------|------|
| 🤖 AI 生成 | 调用智谱 GLM-4-Flash（免费模型）生成生活科普知识（物理/化学/生物） |
| 📄 本地归档 | 自动追加到 `daily_science.txt`，日积月累形成知识库 |
| 📬 邮件推送 | 通过 QQ邮箱 SMTP，每天定时发送到指定邮箱 |
| 🕐 双定时保险 | OpenClaw Cron + 系统 Crontab 双重保障 |

---

## 🛠 技术栈

- **Python 3** — 主脚本语言
- **智谱 GLM-4-Flash** — AI 内容生成（免费模型）
- **smtplib** — Python 标准库邮件发送
- **Crontab / OpenClaw Cron** — 定时任务调度
- **Linux (WSL2)** — 运行环境

---

## 📦 文件结构

```
daily-science-pusher/
├── daily_science.py          # 主脚本：AI 生成 + 保存 + 发邮件
├── daily_science_cron.sh     # Crontab 包装脚本（加载环境变量）
├── .env.daily_science        # 🚫 环境变量配置（含 API Key，不提交）
├── .env.daily_science.template  # 环境变量模板
├── daily_science.txt         # 📄 生成的科普内容（运行时自动创建）
└── README.md                 # 本文件
```

---

## 🚀 使用方法

### 1. 安装依赖

```bash
cd daily-science-pusher
python3 -m venv .venv
source .venv/bin/activate
pip install zhipuai httpx sniffio
```

### 2. 配置环境变量

```bash
cp .env.daily_science.template .env.daily_science
```

编辑 `.env.daily_science`，填入你的密钥：

```bash
# AI 模型（智谱 GLM）
export ZHIPU_API_KEY="你的智谱 API Key"    # 获取：https://open.bigmodel.cn/

# QQ邮箱 SMTP（用于邮件推送）
export SMTP_SENDER="your_email@qq.com"
export SMTP_PASSWORD="你的16位授权码"        # 获取：QQ邮箱 → 设置 → 账户 → 生成授权码
export SMTP_RECIPIENT="your_email@qq.com"
```

### 3. 运行测试

```bash
source .env.daily_science
source .venv/bin/activate
python3 daily_science.py
```

### 4. 设置定时推送（每天早上 8:00）

```bash
crontab -e
# 添加一行：
0 8 * * * cd /path/to/daily-science-pusher && bash daily_science_cron.sh >> daily_science_cron.log 2>&1
```

---

## 📸 效果预览

### 终端输出

```
🔄 正在调用智谱 GLM...
✅ 科普知识已追加保存到 daily_science.txt
   日期：2026-05-14
   内容预览：生活中的化学小常识：为什么洗衣服时加入洗衣粉会有泡沫？
              洗衣粉中含有表面活性剂，这种物质能降低水的表面张力...
📧 邮件已发送到 2836461019@qq.com
```

### 本地归档文件

```
==================================================
日期：2026-05-14
==================================================
生活中的化学小常识：为什么洗衣服时加入洗衣粉会有泡沫？

解释：洗衣粉中含有表面活性剂，这种物质能降低水的表面张力，
使水更容易渗透衣物纤维。表面活性剂还能使衣物纤维上的油污、
污渍更容易被水冲刷掉。在洗衣过程中，表面活性剂与水中的钙、
镁离子反应，形成泡沫，泡沫能更好地包裹污渍，帮助其脱离衣物
纤维。所以，加入洗衣粉会产生泡沫，这是清洁衣物的重要过程。
```

### 邮件送达效果

每日 8:00 自动发送到 QQ邮箱，标题 `XiaGe Science Daily - YYYY-MM-DD`，正文包含完整的科普内容。

---

## ⚙️ 自定义配置

### 切换 AI 模型

脚本支持通义千问作为备选模型：

```bash
export DASHSCOPE_API_KEY="你的DashScope Key"    # 获取：https://dashscope.aliyun.com/
export SCIENCE_PROVIDER="qwen"                  # 设为 qwen 即可切换
```

### 修改推送时间

编辑 crontab，修改 `0 8 * * *` 中的小时和分钟即可。

---

## 📝 License

MIT
