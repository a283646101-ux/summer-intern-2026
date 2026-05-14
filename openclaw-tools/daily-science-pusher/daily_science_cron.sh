#!/bin/bash
# daily_science_cron.sh — 由 cron 调用的包装脚本
# 加载环境变量并执行 daily_science.py

WORKSPACE="/home/a2836461019/.openclaw/workspace"
ENV_FILE="$WORKSPACE/.env.daily_science"
SCRIPT="$WORKSPACE/daily_science.py"

cd "$WORKSPACE" || { echo "❌ 无法进入工作目录"; exit 1; }

# 加载环境变量（如果存在）
if [ -f "$ENV_FILE" ]; then
    set -a
    source "$ENV_FILE"
    set +a
fi

source "$WORKSPACE/.venv/bin/activate"
exec python3 "$SCRIPT"
