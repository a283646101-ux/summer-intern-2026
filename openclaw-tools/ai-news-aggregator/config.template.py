"""
AI 前沿资讯聚合 & 教程生成系统 — 配置模板
=========================================
复制为 config.py 并填入真实配置。
"""

# ── API 配置 ──────────────────────────────────────────────
# 智谱 GLM（必填，用于内容摘要和教程生成）
ZHIPU_API_KEY = "your_zhipu_api_key_here"

# ── 邮件配置（可选，不配则只保存本地文件）────────────────
SMTP_SENDER = "2836461019@qq.com"
SMTP_PASSWORD = ""  # QQ邮箱授权码，不是登录密码
SMTP_RECIPIENT = "2836461019@qq.com"
SMTP_ENABLED = False  # 设为 True 并填好密码才会发邮件

# ── 信源开关（按需开启/关闭）─────────────────────────────
ENABLE_ARXIV = True          # Arxiv 每日论文
ENABLE_GITHUB_TRENDING = True  # GitHub Trending AI 仓库
ENABLE_HACKER_NEWS = True    # Hacker News AI 讨论
