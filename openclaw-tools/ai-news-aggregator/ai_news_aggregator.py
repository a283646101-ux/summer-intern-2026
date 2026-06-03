#!/usr/bin/env python3
"""
AI 前沿资讯聚合 & 教程生成系统
==============================
多信源聚合 AI 技术资讯 → LLM 摘要去重 → 分层输出（速览/深度/教程）→ 邮箱推送

信源：
  - Arxiv Daily：AI/CV/NLP 最新论文
  - GitHub Trending：AI 相关热门仓库
  - Hacker News：AI 技术讨论

每 3 天运行一次，自动抓取、摘要、生成教程、归档推送。
"""

import os
import sys
import json
import re
import smtplib
import hashlib
import html
from datetime import datetime, timezone, timedelta
from email.mime.text import MIMEText
from email.header import Header
from pathlib import Path

try:
    import requests
except ImportError:
    print("❌ 缺少 requests 库，请安装：pip install requests")
    sys.exit(1)

# ── 尝试加载配置 ─────────────────────────────────────────
CONFIG_PATH = Path(__file__).parent / "config.py"
if CONFIG_PATH.exists():
    # 动态加载用户配置
    import importlib.util
    spec = importlib.util.spec_from_file_location("user_config", CONFIG_PATH)
    cfg = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(cfg)
    ZHIPU_API_KEY = getattr(cfg, "ZHIPU_API_KEY", "")
    SMTP_SENDER = getattr(cfg, "SMTP_SENDER", "2836461019@qq.com")
    SMTP_PASSWORD = getattr(cfg, "SMTP_PASSWORD", "")
    SMTP_RECIPIENT = getattr(cfg, "SMTP_RECIPIENT", "2836461019@qq.com")
    SMTP_ENABLED = getattr(cfg, "SMTP_ENABLED", False)
    ENABLE_ARXIV = getattr(cfg, "ENABLE_ARXIV", True)
    ENABLE_GITHUB_TRENDING = getattr(cfg, "ENABLE_GITHUB_TRENDING", True)
    ENABLE_HACKER_NEWS = getattr(cfg, "ENABLE_HACKER_NEWS", True)
else:
    ZHIPU_API_KEY = os.environ.get("ZHIPU_API_KEY", "")
    SMTP_SENDER = os.environ.get("SMTP_SENDER", "2836461019@qq.com")
    SMTP_PASSWORD = os.environ.get("SMTP_PASSWORD", "")
    SMTP_RECIPIENT = os.environ.get("SMTP_RECIPIENT", "2836461019@qq.com")
    SMTP_ENABLED = bool(SMTP_PASSWORD)
    ENABLE_ARXIV = True
    ENABLE_GITHUB_TRENDING = True
    ENABLE_HACKER_NEWS = True

# ── 路径 ──────────────────────────────────────────────────
DATA_DIR = Path(__file__).parent / "data"
DATA_DIR.mkdir(parents=True, exist_ok=True)
HISTORY_FILE = DATA_DIR / "history.json"
OUTPUT_DIR = DATA_DIR

# ── 常量 ──────────────────────────────────────────────────
TODAY = datetime.now(timezone(timedelta(hours=8))).strftime("%Y-%m-%d")
CHINA_TZ = timezone(timedelta(hours=8))

# =============================================================
# 第一部分：多信源聚合
# =============================================================

def fetch_arxiv_daily(max_results=5):
    """
    从 Arxiv API 获取最新 AI/CV/NLP/CL 论文。
    API 文档：https://info.arxiv.org/help/api/index.html
    """
    print("📄 [Arxiv] 正在抓取最新论文...")
    # 搜索近 3 天的 AI 相关论文
    categories = "cat:cs.AI+OR+cat:cs.CL+OR+cat:cs.LG+OR+cat:cs.CV"
    url = (
        f"http://export.arxiv.org/api/query?"
        f"search_query={categories}"
        f"&sortBy=submittedDate&sortOrder=descending"
        f"&max_results={max_results}"
    )

    try:
        resp = requests.get(url, timeout=30)
        resp.encoding = "utf-8"
        if resp.status_code != 200:
            print(f"⚠️  Arxiv API 返回 {resp.status_code}")
            return []

        # 解析 XML
        import xml.etree.ElementTree as ET
        root = ET.fromstring(resp.text)
        ns = {
            "atom": "http://www.w3.org/2005/Atom",
            "arxiv": "http://arxiv.org/schemas/atom",
        }

        papers = []
        for entry in root.findall("atom:entry", ns):
            title = entry.find("atom:title", ns)
            summary = entry.find("atom:summary", ns)
            published = entry.find("atom:published", ns)
            link = entry.find("atom:id", ns)
            authors = entry.findall("atom:author", ns)

            # 提取分类标签
            categories = entry.findall("atom:category", ns)
            tags = [c.get("term", "") for c in categories]

            paper = {
                "title": _clean_text(title.text) if title is not None else "",
                "summary": _clean_text(summary.text[:500]) if summary is not None else "",
                "url": link.text.strip() if link is not None else "",
                "published": published.text[:10] if published is not None else "",
                "authors": [a.find("atom:name", ns).text for a in authors[:3] if a.find("atom:name", ns) is not None],
                "tags": [t for t in tags if t.startswith("cs.")],
                "source": "arxiv",
            }
            papers.append(paper)

        print(f"   ✅ 抓取到 {len(papers)} 篇论文")
        return papers

    except Exception as e:
        print(f"   ❌ Arxiv 抓取出错：{e}")
        return []


def fetch_github_trending(max_results=5):
    """
    从 GitHub Trending 获取 AI 相关热门仓库。
    使用 unofficial API：https://github.com/trendinging
    """
    print("🐙 [GitHub] 正在抓取 Trending AI 仓库...")
    # 尝试用 github-trending-api
    url = "https://api.githunt.com/v1/trending?language=&since=weekly"
    repos = []

    try:
        resp = requests.get(url, timeout=15, headers={
            "User-Agent": "Mozilla/5.0 (compatible; AI-News-Aggregator/1.0)"
        })
        if resp.status_code == 200:
            data = resp.json()
            for item in data[:max_results]:
                name = item.get("full_name", item.get("name", ""))
                desc = item.get("description", "") or ""
                repo_url = f"https://github.com/{name}"
                stars = item.get("stars", item.get("stargazers_count", 0))
                lang = item.get("language", "")

                # 过滤：只看 AI 相关（关键词筛选）
                ai_keywords = ["ai", "llm", "gpt", "agent", "rag", "deep", "neural",
                               "transformer", "ml", "diffusion", "stable", "langchain"]
                name_lower = name.lower()
                desc_lower = desc.lower()
                if not any(kw in name_lower or kw in desc_lower for kw in ai_keywords):
                    continue

                repos.append({
                    "name": name,
                    "description": desc[:300] if desc else "",
                    "url": repo_url,
                    "stars": stars,
                    "language": lang,
                    "source": "github",
                })

            if repos:
                print(f"   ✅ 抓取到 {len(repos)} 个 AI 相关仓库")
                return repos[:max_results]
    except Exception as e:
        print(f"   ⚠️  GitHub Trending API 失败：{e}")

    # Fallback: 直接爬取 GitHub Trending 页面
    try:
        fallback_url = "https://github.com/trending?since=weekly"
        fallback_resp = requests.get(fallback_url, timeout=15, headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                          "AppleWebKit/537.36 (KHTML, like Gecko) "
                          "Chrome/120.0.0.0 Safari/537.36"
        })
        if fallback_resp.status_code == 200:
            from html.parser import HTMLParser

            class GHParser(HTMLParser):
                def __init__(self):
                    super().__init__()
                    self.repos = []
                    self._in_h2 = False
                    self._in_desc = False
                    self._current = {}

                def handle_starttag(self, tag, attrs):
                    attrs_dict = dict(attrs)
                    if tag == "h2" and "class" in attrs_dict:
                        self._in_h2 = True
                    if tag == "p" and "class" in attrs_dict:
                        cls = attrs_dict["class"]
                        if "col-9" in cls or "d-inline-block" in cls:
                            # Try matching desc paragraph
                            self._in_desc = True

                def handle_data(self, data):
                    if self._in_h2:
                        data = data.strip()
                        if "/" in data:
                            self._current["name"] = data
                            self._current["url"] = f"https://github.com/{data}"
                            self._current["source"] = "github"
                            self._in_h2 = False
                    elif self._in_desc:
                        data = data.strip()
                        if data:
                            self._current["description"] = data[:300]
                            self._in_desc = False

                def handle_endtag(self, tag):
                    if tag == "article":
                        if self._current.get("name"):
                            ai_kw = ["ai", "llm", "gpt", "agent", "rag",
                                      "deep", "neural", "transformer",
                                      "ml", "diffusion", "langchain"]
                            name_l = self._current.get("name", "").lower()
                            desc_l = self._current.get("description", "").lower()
                            if any(k in name_l or k in desc_l for k in ai_kw):
                                self.repos.append(dict(self._current))
                        self._current = {}
                    elif tag == "h2":
                        self._in_h2 = False
                    elif tag == "p":
                        self._in_desc = False

            parser = GHParser()
            parser.feed(fallback_resp.text)
            repos = parser.repos[:max_results]
            if repos:
                print(f"   ✅ [Fallback] 抓取到 {len(repos)} 个 AI 仓库")
                return repos
    except Exception as e:
        print(f"   ⚠️  GitHub Trending fallback 也失败了：{e}")

    print("   ⚠️  未抓取到 AI 相关仓库")
    return []


def fetch_hacker_news(max_results=5):
    """
    从 Hacker News API 获取 AI 相关热门讨论。
    API 文档：https://github.com/HackerNews/API
    """
    print("🗞️  [HN] 正在抓取 AI 相关讨论...")
    try:
        # 获取 Top Stories
        top_url = "https://hacker-news.firebaseio.com/v0/topstories.json"
        resp = requests.get(top_url, timeout=15)
        if resp.status_code != 200:
            print(f"   ⚠️  HN API 返回 {resp.status_code}")
            return []

        story_ids = resp.json()[:30]  # 取前 30 篇
        stories = []

        for sid in story_ids:
            item_url = f"https://hacker-news.firebaseio.com/v0/item/{sid}.json"
            item_resp = requests.get(item_url, timeout=10)
            if item_resp.status_code != 200:
                continue
            item = item_resp.json()
            if not item:
                continue

            title = item.get("title", "")
            url = item.get("url", f"https://news.ycombinator.com/item?id={sid}")
            score = item.get("score", 0)
            by = item.get("by", "")
            descendants = item.get("descendants", 0)

            # 过滤 AI 相关
            ai_keywords = ["ai", "artificial intelligence", "llm", "gpt", "chatgpt",
                           "deep learning", "machine learning", "neural network",
                           "transformer", "stable diffusion", "agent", "rag",
                           "openai", "anthropic", "google deepmind", "meta ai",
                           "claude", "gemini", "copilot", "mistral", "llama"]
            title_lower = title.lower()
            if not any(kw in title_lower for kw in ai_keywords):
                continue

            stories.append({
                "title": title,
                "url": url,
                "score": score,
                "by": by,
                "comments": descendants,
                "source": "hackernews",
            })

            if len(stories) >= max_results:
                break

        print(f"   ✅ 抓取到 {len(stories)} 篇 AI 讨论")
        return stories

    except Exception as e:
        print(f"   ❌ HN 抓取出错：{e}")
        return []


def _clean_text(text):
    """清理 XML 文本中的多余空白"""
    if not text:
        return ""
    text = re.sub(r'\s+', ' ', text).strip()
    return html.unescape(text)


# =============================================================
# 第二部分：LLM 内容处理
# =============================================================

def call_glm(prompt, temperature=0.5, max_tokens=2000):
    """调用智谱 GLM-4-Flash 进行文本生成"""
    if not ZHIPU_API_KEY:
        raise ValueError("缺少 ZHIPU_API_KEY，请配置 config.py 或设置环境变量")

    try:
        from zhipuai import ZhipuAI
        client = ZhipuAI(api_key=ZHIPU_API_KEY)
        resp = client.chat.completions.create(
            model="glm-4-flash",
            messages=[{"role": "user", "content": prompt}],
            temperature=temperature,
            max_tokens=max_tokens,
        )
        return resp.choices[0].message.content
    except ImportError:
        # Fallback: 使用 OpenAI 兼容接口
        from openai import OpenAI
        client = OpenAI(
            api_key=ZHIPU_API_KEY,
            base_url="https://open.bigmodel.cn/api/paas/v4",
        )
        resp = client.chat.completions.create(
            model="glm-4-flash",
            messages=[{"role": "user", "content": prompt}],
            temperature=temperature,
            max_tokens=max_tokens,
        )
        return resp.choices[0].message.content


def deduplicate_items(all_items):
    """基于 URL/title hash 去重，结合历史记录"""
    history = _load_history()

    new_items = []
    for item in all_items:
        key = item.get("url") or item.get("title", "")
        item_hash = hashlib.md5(key.encode()).hexdigest()
        if item_hash not in history["seen"]:
            history["seen"].add(item_hash)
            new_items.append(item)

    _save_history(history)
    return new_items


def _load_history():
    """加载已推送记录"""
    if HISTORY_FILE.exists():
        try:
            data = json.loads(HISTORY_FILE.read_text(encoding="utf-8"))
            return {"seen": set(data.get("seen", []))}
        except (json.JSONDecodeError, KeyError):
            pass
    return {"seen": set()}


def _save_history(history):
    """保存已推送记录（仅保留最近 30 天）"""
    HISTORY_FILE.write_text(
        json.dumps({"seen": list(history["seen"])[-500:]}, ensure_ascii=False),
        encoding="utf-8",
    )


def generate_digest(all_items):
    """
    用 LLM 生成结构化摘要文档（含三层次输出）
    """
    print("🤖 [LLM] 正在生成摘要和教程...")

    if not all_items:
        return "本期无新内容。所有资讯均已推送过。\n"

    # 组装原始内容
    raw = []
    for i, item in enumerate(all_items, 1):
        source_icon = {"arxiv": "📄", "github": "🐙", "hackernews": "🗞️"}.get(item["source"], "📌")
        raw.append(f"{source_icon} #{i}")
        raw.append(f"来源：{item['source']}")
        raw.append(f"标题：{item.get('title', item.get('name', '无标题'))}")
        raw.append(f"链接：{item.get('url', '')}")
        if item.get("summary") or item.get("description"):
            raw.append(f"摘要：{item.get('summary') or item.get('description', '')}")
        if item.get("stars"):
            raw.append(f"⭐ {item['stars']} stars")
        if item.get("score"):
            raw.append(f"🔥 {item['score']} points / 💬 {item.get('comments', 0)} comments")
        if item.get("tags"):
            raw.append(f"🏷️ {', '.join(item['tags'])}")
        raw.append("")

    raw_text = "\n".join(raw)

    prompt = f"""你是一位 AI 技术资讯编辑。请根据以下原始资讯，生成一份中文的 {TODAY} AI 前沿资讯摘要。

原始资讯：
{raw_text}

请按以下**结构化模版**输出（不要遗漏任何层级）：

---

## 🚀 极简速览（3 条以内）

每条一句话概括核心看点。

## 📖 深度阅读

对每条资讯做 100-200 字技术拆解：
- 解决了什么问题
- 技术/方法亮点
- 为什么值得关注

## 🛠️ 动手教程

选择其中 1-2 条最有实践价值的资讯，撰写简易动手教程。
要求：给出具体步骤、代码片段（如果有）、配置要点。
让读者看完能立刻上手尝试。
"""

    try:
        result = call_glm(prompt)
        return result
    except Exception as e:
        print(f"   ❌ LLM 调用失败：{e}")
        # Fallback：手动组装简单摘要
        fallback = [f"# 🗞️ AI 前沿资讯速览 - {TODAY}\n"]
        for item in all_items:
            title = item.get("title", item.get("name", "无标题"))
            url = item.get("url", "")
            fallback.append(f"- [{title}]({url})")
        return "\n".join(fallback)


def generate_wechat_email_body(digest_text):
    """用 LLM 把摘要改写成适合邮件阅读的简洁版本"""
    prompt = f"""请将以下 AI 资讯摘要改写为一封简洁的邮件正文，要求：

1. 保留核心信息，去掉冗余
2. 控制在 800 字以内
3. 开头用 "Hi 老板，这是 3 天来的 AI 前沿资讯精选 👇"
4. 保持友好的口语化风格
5. 末尾加上 "—— 你的赛博龙虾 🦞"

原始内容：
{digest_text[:3000]}
"""
    try:
        return call_glm(prompt, temperature=0.7, max_tokens=1500)
    except Exception:
        # Fallback：直接截取前 1000 字符
        return f"Hi 老板，这是 {TODAY} 的 AI 前沿资讯精选 👇\n\n{digest_text[:1000]}\n\n—— 你的赛博龙虾 🦞"


# =============================================================
# 第三部分：输出与推送
# =============================================================

def save_digest(digest_text):
    """保存完整摘要到 Markdown 文件"""
    filename = OUTPUT_DIR / f"digest_{TODAY}.md"
    content = f"""# 🗞️ AI 前沿资讯摘要 - {TODAY}

> 自动聚合 Arxiv / GitHub Trending / Hacker News
> 生成时间：{datetime.now(CHINA_TZ).strftime('%Y-%m-%d %H:%M:%S')}

{digest_text}

---
*本报告由 AI 资讯聚合系统自动生成，内容由 LLM 摘要整理，仅供参考。*
"""
    filename.write_text(content, encoding="utf-8")
    print(f"   💾 摘要已保存：{filename}")
    return filename


def send_email(subject, body):
    """发送邮件"""
    if not SMTP_ENABLED or not SMTP_PASSWORD:
        print("   💡 未启用邮件推送（配置 SMTP_PASSWORD 可开启）")
        return False

    # HTML 格式（把 markdown 链接转成 HTML）
    html_body = body.replace("\n", "<br>\n")
    html_body = re.sub(r'\[(.+?)\]\((.+?)\)', r'<a href="\2">\1</a>', html_body)

    msg = MIMEText(html_body, "html", "utf-8")
    msg["From"] = SMTP_SENDER
    msg["To"] = SMTP_RECIPIENT
    msg["Subject"] = Header(subject, "utf-8")

    try:
        with smtplib.SMTP_SSL("smtp.qq.com", 465, timeout=10) as server:
            server.login(SMTP_SENDER, SMTP_PASSWORD)
            server.sendmail(SMTP_SENDER, [SMTP_RECIPIENT], msg.as_string())
        print(f"   📧 邮件已推送到 {SMTP_RECIPIENT}")
        return True
    except Exception as e:
        print(f"   ❌ 邮件发送失败：{e}")
        return False


# =============================================================
# 第四部分：主流程
# =============================================================

def main():
    print(f"\n{'='*50}")
    print(f"  🦞 AI 前沿资讯聚合 & 教程生成系统")
    print(f"  日期：{TODAY}")
    print(f"{'='*50}\n")

    # 0. 检查 API Key
    if not ZHIPU_API_KEY:
        print("❌ 未设置 ZHIPU_API_KEY！")
        print("   请创建 config.py（参考 config.template.py）")
        print("   或设置环境变量：export ZHIPU_API_KEY='你的 Key'")
        sys.exit(1)

    # 1. 多信源聚合
    print("📡 [第1步] 多信源聚合\n" + "-" * 40)
    all_items = []

    if ENABLE_ARXIV:
        all_items.extend(fetch_arxiv_daily(5))
    if ENABLE_GITHUB_TRENDING:
        all_items.extend(fetch_github_trending(5))
    if ENABLE_HACKER_NEWS:
        all_items.extend(fetch_hacker_news(5))

    print(f"\n   原始抓取总计：{len(all_items)} 条")

    # 2. 去重
    print(f"\n🔄 [第2步] 去重（基于已推送历史）\n" + "-" * 40)
    new_items = deduplicate_items(all_items)
    print(f"   新内容：{len(new_items)} 条（已过滤 {len(all_items) - len(new_items)} 条重复）")

    if not new_items:
        print("\n✅ 本期无新内容，跳过生成。")
        save_digest("本期无新内容。所有资讯均已在此前推送过。\n")
        return

    # 3. LLM 生成摘要和教程
    print(f"\n🤖 [第3步] LLM 生成摘要 & 教程\n" + "-" * 40)
    digest = generate_digest(new_items)
    print(f"\n   摘要预览（前 100 字）：")
    print(f"   {digest[:100]}...")

    # 4. 保存文件
    print(f"\n💾 [第4步] 保存归档\n" + "-" * 40)
    filename = save_digest(digest)

    # 5. 邮件推送
    print(f"\n📧 [第5步] 邮件推送\n" + "-" * 40)
    email_body = generate_wechat_email_body(digest)
    subject = f"🦞 AI 前沿资讯精选 - {TODAY}"
    send_email(subject, email_body)

    # 6. 输出总结
    print(f"\n{'='*50}")
    print(f"  ✅ 完成！")
    print(f"  📄 摘要文件：{filename}")
    print(f"  🔢 本期资讯：{len(new_items)} 条")
    print(f"{'='*50}\n")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⏹️  用户中断")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ 程序异常：{e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
