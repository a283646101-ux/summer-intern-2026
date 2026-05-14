Spring Boot + AI 核心项目。
功能规划：
# 🌿 JAIoT 智能养护 Agent 系统

> 一个基于 Java + Spring Boot + AI Agent + IoT 的植物健康智能管理平台。
> 通过传感器采集环境数据，利用大模型 Agent 分析决策，自动给出养护建议（浇水/遮光/通风），并支持日语多语言报告。

## 1. 项目背景与目标
- **痛点**：家庭/办公植物常因忘记浇水或不了解植物习性而枯萎。
- **目标**：结合 IoT 传感器实时监测环境，用 AI Agent 代替人工判断，实现“无人养护”。
- **个人学习目标**：
  - 融合 Java 后端、AI Agent 开发、MQTT 物联网协议
  - 输出一个完整的软硬结合项目，用于杭州计算机实习求职
  - 在 Agent 决策链路中集成日语报告，同步练习日语

## 2. 系统架构图 (概念)
```text
+----------------+      MQTT       +-----------------+
| IoT 传感器设备 | ---------------> | Spring Boot 后端 |
| (温度/湿度/光照) |                  |   (数据接收)      |
+----------------+                  +--------+--------+
                                             |
                                    +--------v--------+
                                    |   MySQL 数据库   |
                                    +--------+--------+
                                             |
                                    +--------v--------+
                                    | AI Agent 模块    |
                                    | (LangChain4j)    |
                                    +--------+--------+
                                             |
                                    +--------v--------+
                                    | 决策结果 API     |
                                    | + 日语报告推送   |
                                    +-----------------+
3. 功能模块明细
3.1 IoT 数据采集与接收
通信协议：MQTT（使用 EMQX 公开 Broker 或本地部署）

模拟传感器：因目前无硬件，用脚本/Python/MQTT 客户端定时发布随机温湿度 JSON 数据，模拟真实设备

后端接收：Spring Boot 集成 MQTT 客户端 (Spring Integration 或 Eclipse Paho)，订阅主题 plant/sensor/+

数据持久化：接收到数据后解析并存入 MySQL 的 sensor_data 表

3.2 数据库设计（最小可用表）
表：sensor_data

字段	类型	说明
id	BIGINT (PK)	自增主键
device_id	VARCHAR(50)	设备标识
temperature	DOUBLE	温度 (℃)
humidity	DOUBLE	湿度 (%)
light	INT	光照强度 (Lux)
create_time	DATETIME	采集时间
表：decision_log

字段	类型	说明
id	BIGINT (PK)	主键
based_on_data_id	BIGINT	基于哪条传感器数据
agent_decision	VARCHAR(255)	Agent 决策结果（如 “浇水”）
reason	TEXT	决策理由（大模型生成）
japanese_report	TEXT	日语养护报告
create_time	DATETIME	决策时间
3.3 AI Agent 决策引擎
技术选型：LangChain4j (Java) + 通义千问 / OpenAI 兼容 API

Agent 定义：

大模型大脑：处理自然语言逻辑

工具 (Tools)：提供 get_latest_sensor_data(deviceId) 方法，让 Agent 可以自主查询最新数据

记忆 (Memory)：暂不引入长期记忆，每次决策只看当前数据

决策流程：

用户/定时任务调用 POST /api/agent/decide 接口
Agent 自动调用 get_latest_sensor_data 工具获取最新环境值
大模型根据预定义的养护规则（Prompt 中说明）给出决策和理由
同时要求大模型将决策结果翻译为日语，生成养护日文报告
结果存入 decision_log 表，返回 JSON
3.4 养护建议的 Prompt 规则示例（注入给 Agent）
你是一个专业的植物学家。请根据以下传感器数据决定养护动作：

温度高于 30℃ 建议“遮阳通风”

湿度低于 30% 建议“浇水”

光照低于 500 Lux 建议“补充光照”
如果数值正常请回复“无需操作”。请用中文给出理由，并用日语写一份简短的日式养护报告。

3.5 后端 REST API 设计
方法	路径	说明
POST	/api/sensor/data	接收传感器数据（也可由 MQTT 直接消费）
GET	/api/sensor/latest/{deviceId}	获取某设备最新一条数据
POST	/api/agent/decide	触发 Agent 决策，传入设备 ID，返回决策
GET	/api/agent/logs?deviceId=xxx	查看历史养护记录
3.6 日语报告输出示例
json
{
  "decision": "浇水",
  "reason": "土壤湿度仅为25%，植物出现轻微萎蔫迹象，建议立即浇透水。",
  "japanese_report": "現在の土壌湿度は25%です。植物が少し萎れていますので、すぐに水をたっぷり与えてください。"
}
4. 技术栈
后端框架：Spring Boot 3

数据库：MySQL 8.0

消息队列：MQTT (Eclipse Paho Java Client)

AI 框架：LangChain4j

大模型 API：通义千问 / 智谱 GLM（免费额度可用）

项目构建：Maven

版本控制：Git / GitHub

5. 里程碑 (v0.1 开发计划)
Week 1 (当前)：项目初始化、数据模型、MQTT 接收模拟数据、Agent 简单对话跑通

Week 2：实现 Agent 工具调用、决策接口、日语报告生成

Week 3：前端简单看板（可选）、异常处理、日志

Week 4：整体联调、文档完善、部署说明

6. 未来扩展方向
接入真实硬件（ESP32 + 土壤湿度传感器）

增加多植物管理、用户系统

Agent 长期记忆：学习植物生长趋势

多语言支持（英语/日语切换）

text

---

## 二、如何用这份规划让 AI 为你生成代码

你现在可以让 AI 助手指着这份 README 说：
> **“根据上面的功能规划，帮我生成一个 Spring Boot 项目的初始化代码，要求包含：**
>   1. Maven 的 pom.xml，引入 Spring Web、JPA、MySQL、MQTT (paho) 和 LangChain4j 依赖。
>   2. SensorData 实体类，DecisionLog 实体类，对应的 JPA Repository。
>   3. MQTT 消息接收的配置类（使用默认公共 Broker：`tcp://broker.emqx.io:1883`）。
>   4. 一个模拟传感器数据发送的 Python 脚本（放在 `/scripts/mock_sensor.py`）。
>   5. Agent 模块的 Service 层骨架，预留 Tool 和 LLM 调用位置。
>   6. 基础的 Controller 架子。
>
> 请给出所有的文件内容和存放路径，确保我复制到项目中可以直接运行。”

**如果你使用通义千问或 ChatGPT**，可以直接把上面这一整段连同 README 发过去，它会为你生成一个完整的项目压缩包结构。

---

## 三、你的下一步操作

1. **复制上面的功能规划** → 粘贴到 `jaiot-project/README.md` 文件中。
2. **提交到 Git**：
   ```bash
   git add README.md
   git commit -m "docs: add JAIoT project detailed feature planning"
   git push
接着用规划生成代码：打开你常用的 AI 工具，发送上述生成代码指令。

完成后继续执行今晚剩余任务（周三的 openclaw 科普推送器会用到类似的“需求→AI生成代码”模式，正好提前熟悉）。

这一步写得越扎实，后面写简历项目描述时就能直接提炼 bullet point，面试被问到也能条理清晰地讲清楚架构。有任何细节不确定或需要我帮你拟一段 prompt，随时叫我。

