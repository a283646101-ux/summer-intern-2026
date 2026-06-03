# 🦞 Summer Intern 2026 · 暑期实习作品集

<!-- Shields -->
<p align="left">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=flat&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/LangChain4j-1.0-00A86B?style=flat" alt="LangChain4j"/>
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=flat&logo=react&logoColor=white" alt="React 18"/>
  <img src="https://img.shields.io/badge/Vite-6-646CFF?style=flat&logo=vite&logoColor=white" alt="Vite 6"/>
  <img src="https://img.shields.io/badge/Tailwind-3-06B6D4?style=flat&logo=tailwindcss&logoColor=white" alt="Tailwind CSS"/>
  <img src="https://img.shields.io/badge/Supabase-PostgreSQL-3FCF8E?style=flat&logo=supabase&logoColor=white" alt="Supabase"/>
  <img src="https://img.shields.io/badge/Python-3-3776AB?style=flat&logo=python&logoColor=white" alt="Python"/>
  <img src="https://img.shields.io/badge/OpenClaw-Agent-6C47FF?style=flat" alt="OpenClaw"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white" alt="Docker"/>
</p>

---

## 📋 项目总览

本仓库收录了我为 **2026 年暑期实习** 准备的三个方向作品，涵盖 **Java 后端开发、AI Agent 应用、全栈开发** 三大技能维度：

| # | 模块 | 关键词 | 技术栈 |
|---|------|--------|--------|
| 🏛️ | **[JAIoT 智能养护系统](#-jaiot-智能养护全栈系统)** | 全栈·AI Agent·RAG·IoT·Supabase | Java 17, Spring Boot 3.3, LangChain4j, React, Tailwind, Supabase |
| 🛠️ | **[OpenClaw 工具集](#-openclaw-ai-工具集)** | AI 编排·LLM API·自动化·定时推送 | Python, Zhipu GLM, OpenClaw Agent, SMTP |
| 📐 | **[Algs4 算法练习](#-algs4-算法练习)** | 数据结构·算法·笔试准备 | Java, 《算法·第四版》 |

---

## 🏛️ JAIoT 智能养护全栈系统

> **Java AI + IoT = JAIoT 🦞 — 从零构建的 AI Agent 全栈平台**

基于 **Spring Boot 3.3 + LangChain4j 1.0** 构建的植物健康智能管理平台，是一个覆盖**后端、AI Agent、RAG、前端可视化、云数据库**的完整全栈项目。

### 🚀 项目演进

本项目经历了两个阶段的重构演进：

| 阶段 | 版本 | 说明 |
|------|------|------|
| 🐣 **v1 — Agent Demo** | 初始 | 单体 Agent 演示，LangChain4j + 单工具灌溉决策 |
| 🚀 **v2 — 全栈平台** | 当前 | 完整后端重构 + React 前端 + Supabase + RAG + 多工具 Agent |

### ✨ 六大核心功能

| # | 功能 | 说明 | 技术亮点 |
|---|------|------|----------|
| 🌡️ | **传感器数据看板** | 实时/历史温湿度、土壤湿度、光照数据展示 | Recharts 图表 + 定时轮询 |
| 🤖 | **Agent 多工具决策** | Agent 同时查询温/湿/光/土壤 4 个传感器，综合判断养护动作 | 多 `@Tool` 协作 + Function Calling |
| 💬 | **流式输出（打字机）** | Agent 回复逐字推送到前端，实时展示思考过程 | SSE + WebFlux + 流式 ChatModel |
| 🇯🇵 | **日语养护报告** | Agent 自动生成日语版养护建议，多语言展示 | 多语言 System Prompt |
| 📚 | **RAG 知识库查询** | 基于植物养护文档的智能问答 | Embedding + Vector Store + 检索增强生成 |
| 📝 | **历史决策记录** | 所有 Agent 决策持久化，支持按设备/时间查询 | JPA + Supabase (PostgreSQL) |

### 🖥️ 系统架构

```
┌──────────────────────────────────────────────────────────┐
│                     React 前端 (Vite)                     │
│  Dashboard · 传感器图表 · Chat 界面 · RAG 问答 · 历史查询 │
└────────────────────┬─────────────────────────────────────┘
                     │ REST / SSE API
┌────────────────────▼─────────────────────────────────────┐
│              Spring Boot 3.3 后端                         │
│  ┌──────┬──────┬──────┬──────┬──────┬──────┐            │
│  │Agent │Sensor│Decision│Chat│RAG │Health│            │
│  │Ctrl  │Ctrl  │Ctrl   │Ctrl│Ctrl │Ctrl  │            │
│  └──┬───┴──┬───┴───┬──┴──┬──┴──┬──┴──┬───┘            │
│     │      │       │     │     │      │                  │
│  ┌──▼──┐┌──▼──┐┌──▼──┐┌──▼──┐┌──▼──┐                    │
│  │Agent││Sensor││Decis││Chat ││RAG  │                    │
│  │Svc  ││Svc  ││Svc  ││Svc  ││Svc  │                    │
│  └──┬──┘└─────┘└─────┘└─────┘└──┬──┘                    │
│     │                            │                        │
│  ┌──▼─────────────────────────┐  │                        │
│  │  LangChain4j Agent Core    │  │                        │
│  │  AiServices + 4×@Tool      │  │                        │
│  └──────┬─────────────────────┘  │                        │
│         │                        │                        │
│  ┌──────▼──────┐    ┌────────────▼──────────┐             │
│  │ ChatModel   │    │ EmbeddingModel        │             │
│  │ (智谱GLM-4) │    │ (智谱Embedding-2)      │             │
│  └─────────────┘    └───────────┬───────────┘             │
│                                 │                         │
└─────────────────────────────────┼─────────────────────────┘
                                  │
    ┌─────────────────────────────┼──────────────────────┐
    │          Supabase (PostgreSQL)                     │
    │  sensor_data · decision_log · chat_messages · rag  │
    └────────────────────────────────────────────────────┘
```

### 🧰 Agent 多工具决策流程

```
POST /api/agent/decision?sensorId=device-001
  → Agent 调用 queryTemperature(sensorId)    ← @Tool
  → Agent 调用 queryHumidity(sensorId)       ← @Tool
  → Agent 调用 querySoilMoisture(sensorId)   ← @Tool
  → Agent 调用 queryLightIntensity(sensorId) ← @Tool
  → LLM 综合分析 4 路传感器数据
  → 输出 JSON：{ decision, reason, advice, japaneseReport }
```

### 🧪 快速启动

```bash
# 后端
cd jaiot-project
export ZHIPU_API_KEY="你的智谱API密钥"
mvn spring-boot:run -pl agent-demo

# 前端
cd jaiot-project/jaiot-frontend
npm install
npm run dev
```

### 🗄️ 数据库

- **开发环境**: H2 (无需配置)
- **生产环境**: Supabase PostgreSQL（`supabase-schema.sql` + `application-supabase.yml`）
- 含 50+ 条真实场景模拟种子数据（3 设备 × 不同时段 × 不同环境条件）

---

## 🛠️ OpenClaw AI 工具集

> **基于 OpenClaw Agent 编排框架 + 大模型 API 的自动化工具**

### 🦞 Daily Science Pusher — AI 每日科普推送

一条赛博龙虾每天为你打工：**自动选题 → AI 生成科普文章 → 格式化输出 → 定时推送邮箱**，全流程无人值守。

#### ✨ 特性

- 🤖 **AI 全自动生成** — 利用智谱 GLM-4-Flash 生成高质量科普内容
- 🧠 **结构化科普模板** — 现象描述 → 原理解析（含公式）→ 生活建议 → 冷知识
- 🏥 **25 道精选题库** — 覆盖人体疾病、健康饮食、日常身体、物理原理、生活常识五大类
- 🧮 **带公式的硬核科普** — 每个科普至少引入 1~2 个核心公式并逐符号解释
- 📬 **邮件自动推送** — 每天早 8:00 通过 QQ 邮箱 SMTP 准时送达
- ⏰ **双重定时保障** — OpenClaw Cron + 系统 Crontab 双保险

#### 🖥️ 选题示例

| 类别 | 选题 | 涉及公式 |
|------|------|----------|
| 🏥 人体疾病 | 高血压为什么是"沉默杀手" | 伯努利原理、泊肃叶定律 |
| 🥗 健康饮食 | 为什么空腹喝咖啡伤胃 | 胃液 pH 值、质子泵机制 |
| 🔬 生活常识 | 冰箱为什么不能给房间降温 | 卡诺热机效率 η=1-Tc/Th |
| 🏠 生活常识 | 微波炉加热的原理 | E=hν, 介电加热 |
| 🔬 生活常识 | 为什么油锅着火不能用水浇 | 水汽化膨胀约 1700 倍 |

#### 🧪 快速启动

```bash
cd openclaw-tools/daily-science-pusher
pip install zhipuai
export ZHIPU_API_KEY="你的智谱 API Key"
python3 daily_science.py
```

---

## 📐 Algs4 算法练习

> **系统化算法训练 · 稳扎数据结构与算法基础**

正在系统跟学 **《算法，第四版》**（Sedgewick & Wayne），按照章节逐题实践，已完成内容：

| 章节 | 主题 |
|------|------|
| Chapter 1 | 基础编程模型、数据抽象、背包/队列/栈、Union-Find |
| Chapter 2 | 排序（选择/插入/希尔/归并/快排/堆排） |
| Chapter 3 | 查找（BST、红黑树、散列表） |
| Chapter 4 | 图（DFS/BFS、最短路径、最小生成树） |

> *正在持续更新中...*

---

## 📦 仓库结构

```
summer-intern-2026/
├── jaiot-project/              # 🏛️ JAIoT 全栈智能养护系统
│   ├── agent-demo/             #    Agent 决策模块（LangChain4j）
│   ├── jaiot-frontend/         #    React 前端（Vite + Tailwind）
│   ├── src/                    #    重构后端
│   │   └── main/java/com/jaiot/
│   │       ├── agent/          #      Agent 多工具 + 接口定义
│   │       ├── config/         #      ChatModel / Embedding / Web 配置
│   │       ├── controller/     #      REST 控制器 × 6
│   │       ├── dto/            #      请求/响应 DTO
│   │       ├── entity/         #      JPA 实体 × 4
│   │       ├── repository/     #      Spring Data JPA
│   │       ├── scheduler/      #      定时模拟传感器数据
│   │       └── service/        #      业务逻辑层
│   ├── supabase-schema.sql     #    PostgreSQL 建表 + 种子数据
│   └── README.md               #    项目详细技术文档
│
├── openclaw-tools/             # 🛠️ OpenClaw AI 工具集
│   └── daily-science-pusher/   #    AI 每日科普推送（Python + Zhipu GLM）
│
├── algs4-practice/             # 📐 算法练习（《算法·第四版》）
│   └── README.md               #     练习记录
│
├── README.md                   # 📄 本文件（作品集总览）
└── .gitignore
```

---

## 🧑‍💻 关于我

- **准大三学生**，方向：Java 后端开发 / AI Agent 应用研发
- 熟练 **Spring Boot / MyBatis / Redis / MySQL / PostgreSQL** 后端技术栈
- 具备 **React + Vue 3 + 微信小程序** 前端实战经验
- 深度实践 **AI Agent 开发**：LangChain4j Agent + Tool + RAG 完整链路
- 独立完成 **全栈项目架构**：从后端 API 设计 → 前端可视化 → 云数据库部署
- **Supabase** 云数据库实战部署经验
- **Google Developer Program** 成员，持续关注前沿技术

---

## 📬 联系我

- **邮箱**：2836461019@qq.com
- **GitHub**：[github.com/a283646101-ux](https://github.com/a283646101-ux)
- **求职方向**：2026 年暑期实习 — Java 后端 / 全栈开发 / AI Agent 应用研发
- **期望城市**：杭州

---

<p align="center">
  🦞 <i>Built with curiosity, caffeine, and crustacean energy.</i>
</p>
