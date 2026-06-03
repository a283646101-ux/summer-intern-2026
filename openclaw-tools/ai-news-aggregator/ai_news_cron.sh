#!/bin/bash
# ai_news_cron.sh — 由 cron 调用的包装脚本
# 作用：加载环境变量 → 切换到正确目录 → 执行 ai_news_aggregator.py

WORKSPACE="/home/a2836461019/.openclaw/workspace"
PROJECT_DIR="$WORKSPACE/openclaw-tools/ai-news-aggregator"
SCRIPT="$PROJECT_DIR/ai_news_aggregator.py"
ENV_FILE="$WORKSPACE/.env.ai_news"

cd "$PROJECT_DIR" || { echo "❌ 无法进入项目目录"; exit 1; }

# 加载环境变量（如果存在）
if [ -f "$ENV_FILE" ]; then
    set -a
    source "$ENV_FILE"
    set +a
fi

# 激活虚拟环境（如果有）
if [ -f "$WORKSPACE/.venv/bin/activate" ]; then
    source "$WORKSPACE/.venv/bin/activate"
fi

exec python3 "$SCRIPT"
