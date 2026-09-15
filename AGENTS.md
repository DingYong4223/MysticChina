# MysticChina Agent 开发规范

## KuiklyUI 开发参考文档

**涉及 KuiklyUI 的开发工作时，先查以下文档，再写代码：**

| 文档 | 用途 |
|-----|-----|
| `../aidoc/KUIKLY_GUIDE.md` | 开发指南：编程模型、响应式系统、常见陷阱、最佳实践 |
| `../aidoc/KUIKLY_API.md` | API 速查：所有视图/属性/模块/动画/指令的完整方法表 |
| `../aidoc/local.md` | **kuiklyui_wp** — KuiklyUI 源码本地路径，aidoc 无解时直接读源码 |

> **查询优先级：** aidoc 文档（GUIDE + API）优先 → 仍无解时，读 `kuiklyui_wp` 指向的 KuiklyUI 源码查找接口定义和实现细节。

### 关键开发规则

1. **`val ctx = this`**：`body()` 返回的 lambda 内，`this` 是 `ViewContainer`，必须在 lambda 外先 `val ctx = this`
2. **状态声明位置**：所有 `observable` 属性必须在类属性级声明，不能在 `body()` 内
3. **横向滚动末尾**：`paddingRight` 对滚动容器无效，末尾留白必须加空 `View { attr { width(16f) } }`
4. **定时器**：重复执行用 `Timer().schedule(delay, period) { }`，不用 `setInterval`（不存在）
5. **`on()` 不存在**：自定义事件用 `registerEvent("name", handler)`
6. **`Animation.spring()`** 不存在，用 `Animation.springEaseOut(duration, damping, velocity)`

---

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **ExploringChina** (2217 symbols, 4065 relationships, 112 execution flows). Use GitNexus graph tools to understand code, assess impact, and navigate safely.

> Index stale? Run `node .gitnexus/run.cjs analyze` from the project root — it auto-selects an available runner. No `.gitnexus/run.cjs` yet? `npx gitnexus analyze` (npm 11 crash → `npm i -g gitnexus`; #1939).

## Always Do

- **MUST run impact analysis before editing.** Use `impact({target: "symbolName", direction: "upstream"})` (MCP) or `node .gitnexus/run.cjs impact "symbolName" --direction upstream --repo .` (CLI fallback); report callers, processes, and risk. Never substitute grep for graph analysis.
- **MUST analyze graph changes before committing.** Use `detect_changes({scope: "all"})` (MCP) or `node .gitnexus/run.cjs detect-changes --scope all --repo .` (CLI fallback). For regression review: `detect_changes({scope: "compare", base_ref: "main"})` or `node .gitnexus/run.cjs detect-changes --scope compare --base-ref "main" --repo .`.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `query({search_query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `context({name: "symbolName"})`.
- For security review, `explain({target: "fileOrSymbol"})` lists taint findings (source→sink flows; needs `analyze --pdg`).

## Never Do

- NEVER edit a function, class, or method before MCP/CLI impact analysis.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `rename` which understands the call graph.
- NEVER commit before MCP/CLI graph change analysis.

## Resources

| Resource | Use for |
| --- | --- |
| `gitnexus://repo/ExploringChina/context` | Codebase overview, check index freshness |
| `gitnexus://repo/ExploringChina/clusters` | All functional areas |
| `gitnexus://repo/ExploringChina/processes` | All execution flows |
| `gitnexus://repo/ExploringChina/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
| --- | --- |
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
