# 🦞 Summer Intern 2026 · 暑期实习作品集

<!-- Shields -->
<p align="left">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=flat&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/LangChain4j-1.0-00A86B?style=flat" alt="LangChain4j"/>
  <img src="https://img.shields.io/badge/Vue.js-3-4FC08D?style=flat&logo=vuedotjs&logoColor=white" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Python-3-3776AB?style=flat&logo=python&logoColor=white" alt="Python"/>
  <img src="https://img.shields.io/badge/OpenClaw-Agent-6C47FF?style=flat" alt="OpenClaw"/>
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?style=flat&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=flat&logo=redis&logoColor=white" alt="Redis"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white" alt="Docker"/>
</p>

---

## 📋 项目总览

本仓库收录了我为 **2026 年暑期实习** 准备的三个方向作品，涵盖 **Java 后端开发、AI Agent 应用、全栈开发** 三大技能维度：

| # | 模块 | 关键词 | 技术栈 |
|---|------|--------|--------|
| 🏛️ | **[JAIoT 智能养护系统](#-jaiot-智能养护-agent-系统)** | 后端·AI Agent·IoT·REST API | Java 17, Spring Boot 3.3, LangChain4j, MySQL, Redis, MQTT |
| 🛠️ | **[OpenClaw 工具集](#-openclaw-ai-工具集)** | AI 编排·LLM API·自动化·定时推送 | Python, Zhipu GLM, OpenClaw Agent, SMTP, Cron |
| 📐 | **[Algs4 算法练习](#-algs4-算法练习)** | 数据结构·算法·笔试准备 | Java, 《算法·第四版》 |

---

## 🏛️ JAIoT 智能养护 Agent 系统

> **物联网 + AI Agent 驱动的植物养护决策系统**

一个从零构建的 Java 后端工程，融合 **Spring Boot、LangChain4j Agent、IoT 传感器数据采集、AI 驱动决策** 的完整全链路项目。

### ✨ 核心功能

| 模块 | 状态 | 说明 |
|------|------|------|
| 🎯 **Agent 灌溉决策 API** | ✅ 已完成 | Agent 查询传感器数据 → 自动判断是否需要浇水 → 返回决策 + 理由 |
| 🧰 **@Tool 传感器查询工具** | ✅ 已完成 | LangChain4j Tool 注解，Agent 可主动调用获取温湿度数据（Mock） |
| 🔌 **IoT 数据采集** | 📋 规划中 | MQTT 协议接收传感器数据，Spring Integration 集成 |
| 🌐 **REST API** | ✅ 已完成 | 通用对话 + Agent 决策双接口 |
| 🇯🇵 **日语报告** | 📋 规划中 | Agent 多语言输出，同步练习日语 |

### 🖥️ 代码亮点

**Agent 决策流程：**
```
POST /agent/decision
  → Agent 调用 queryLatestSensorData(sensorId)   ← @Tool 注解方法
  → 大模型分析温度/湿度数据
  → 按预设规则判断（温度>30°C/湿度<40% → 需浇水）
  → 返回结构化 JSON：{ decision, temperature, humidity, reason, advice }
```

**关键设计决策：**
- 使用 `AiServices.builder()` + `.chatLanguageModel()` + `.tools()` 构建 Agent
- 工具方法与业务逻辑解耦，`@Tool` 注解暴露给 LLM
- 决策结果 DTO 封装，Controller 层兜底 JSON 解析异常

### 🧪 快速启动

```bash
cd jaiot-project/agent-demo
mvn clean package -DskipTests
java -jar target/agent-demo-0.0.1-SNAPSHOT.jar

# 测试 Agent 决策
curl -X POST http://localhost:8081/api/agent/decision \
  -H "Content-Type: application/json" -d "{}"
```

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
| 🧍 日常身体 | 运动后为什么肌肉酸痛 | 无氧呼吸：C₆H₁₂O₆→2C₃H₆O₃ |

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
├── jaiot-project/              # 🏛️ Java + AI + IoT 核心项目
│   ├── agent-demo/             #    LangChain4j Agent 演示模块（带 Tool）
│   ├── pom.xml                 #    父 POM 配置
│   └── README.md               #    项目详细文档
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
- 熟练 **Spring Boot / MyBatis / Redis / MySQL** 后端技术栈
- 具备 **Vue 3 + 微信小程序** 前端实战经验
- 深度实践 **AI Agent 开发**：LangChain4j 集成、OpenClaw 编排、LLM API 调用
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
