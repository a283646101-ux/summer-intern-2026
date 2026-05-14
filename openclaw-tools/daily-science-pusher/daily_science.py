#!/usr/bin/env python3
"""
日常科普知识生成脚本
=====================
调用大模型 API 生成一条通俗易懂的生活科普知识，
带上当天日期，追加保存到 daily_science.txt，
并发送到指定邮箱。

依赖安装（任选其一）：
  pip install openai           # 通义千问（兼容 OpenAI SDK）
  pip install zhipuai          # 智谱 GLM（官方 SDK）
"""

import os
import sys
import smtplib
import json
from email.mime.text import MIMEText
from email.header import Header
from datetime import date

# ── 配置区域 ─────────────────────────────────────────────
# 方式一：智谱 GLM（推荐）
GLM_API_KEY = os.environ.get("ZHIPU_API_KEY", "")

# 方式二：通义千问
QWEN_API_KEY = os.environ.get("DASHSCOPE_API_KEY", "")

# 选择调用哪个模型："glm" 或 "qwen"
PROVIDER = os.environ.get("SCIENCE_PROVIDER", "glm").lower()

# ── 邮件配置 ─────────────────────────────────────────────
# QQ邮箱 SMTP 配置（需要开启 SMTP 服务并获取授权码）
# 授权码获取：QQ邮箱 → 设置 → 账户 → POP3/IMAP/SMTP 服务 → 生成授权码
SMTP_HOST = "smtp.qq.com"
SMTP_PORT = 465  # SSL
SMTP_SENDER = os.environ.get("SMTP_SENDER", "2836461019@qq.com")
SMTP_PASSWORD = os.environ.get("SMTP_PASSWORD", "")  # QQ邮箱授权码，不是登录密码
SMTP_RECIPIENT = os.environ.get("SMTP_RECIPIENT", "2836461019@qq.com")
SMTP_ENABLED = bool(SMTP_PASSWORD)  # 有密码才发邮件
# ────────────────────────────────────────────────────────

PROMPT = "请生成一条生活中的物理/化学/生物科普知识，要求通俗易懂，带现象解释，字数 300 以内。"
OUTPUT_FILE = "daily_science.txt"


def call_glm(prompt: str) -> str:
    from zhipuai import ZhipuAI
    if not GLM_API_KEY:
        raise ValueError("缺少 ZHIPU_API_KEY 环境变量")
    client = ZhipuAI(api_key=GLM_API_KEY)
    resp = client.chat.completions.create(
        model="glm-4-flash",
        messages=[{"role": "user", "content": prompt}],
        temperature=0.7,
        max_tokens=600,
    )
    return resp.choices[0].message.content


def call_qwen(prompt: str) -> str:
    from openai import OpenAI
    if not QWEN_API_KEY:
        raise ValueError("缺少 DASHSCOPE_API_KEY 环境变量")
    client = OpenAI(
        api_key=QWEN_API_KEY,
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    )
    resp = client.chat.completions.create(
        model="qwen-plus",
        messages=[{"role": "user", "content": prompt}],
        temperature=0.7,
        max_tokens=600,
    )
    return resp.choices[0].message.content


def send_email(subject: str, body: str) -> bool:
    """通过 QQ邮箱 SMTP 发送邮件。"""
    if not SMTP_PASSWORD:
        print("⚠️  未配置 SMTP_PASSWORD，跳过邮件发送")
        return False

    msg = MIMEText(body, "plain", "utf-8")
    msg["From"] = SMTP_SENDER
    msg["To"] = SMTP_RECIPIENT
    msg["Subject"] = Header(subject, "utf-8")

    try:
        with smtplib.SMTP_SSL(SMTP_HOST, SMTP_PORT, timeout=10) as server:
            server.login(SMTP_SENDER, SMTP_PASSWORD)
            server.sendmail(SMTP_SENDER, [SMTP_RECIPIENT], msg.as_string())
        print(f"📧 邮件已发送到 {SMTP_RECIPIENT}")
        return True
    except Exception as e:
        print(f"❌ 邮件发送失败：{e}")
        return False


def main():
    # 1. 检查环境
    if PROVIDER == "glm":
        if not GLM_API_KEY:
            print("❌ 未设置 ZHIPU_API_KEY 环境变量。")
            print("   请先设置：export ZHIPU_API_KEY='你的智谱 API Key'")
            sys.exit(1)
        print("🔄 正在调用智谱 GLM...")
        result = call_glm(PROMPT)
    elif PROVIDER == "qwen":
        if not QWEN_API_KEY:
            print("❌ 未设置 DASHSCOPE_API_KEY 环境变量。")
            print("   请先设置：export DASHSCOPE_API_KEY='你的通义千问 API Key'")
            sys.exit(1)
        print("🔄 正在调用通义千问...")
        result = call_qwen(PROMPT)
    else:
        print(f"❌ 未知 PROVIDER: {PROVIDER}，请设为 glm 或 qwen")
        sys.exit(1)

    # 2. 组装内容
    today = date.today().isoformat()
    entry = f"\n{'=' * 50}\n日期：{today}\n{'=' * 50}\n{result}\n"

    # 3. 追加写入文件
    os.makedirs(os.path.dirname(OUTPUT_FILE) or ".", exist_ok=True)
    with open(OUTPUT_FILE, "a", encoding="utf-8") as f:
        f.write(entry)

    print(f"✅ 科普知识已追加保存到 {OUTPUT_FILE}")
    print(f"   日期：{today}")
    print(f"   内容预览：{result[:80]}...")

    # 4. 发送邮件（如果配置了 SMTP）
    if SMTP_ENABLED:
        subject = f"XiaGe Science Daily - {today}"
        email_body = f"""Hi 老板，早上好！

今天的科普知识已为您准备好：

{result}

---
来自：虾哥科技龙虾
        """
        send_email(subject, email_body)
    else:
        print("💡 提示：如需邮件推送，请设置 SMTP_PASSWORD 环境变量")
        print("   获取方式：QQ邮箱 → 设置 → 账户 → 生成授权码")
        print("   export SMTP_PASSWORD='你的16位授权码'")


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"❌ 出错了：{e}")
        sys.exit(1)
