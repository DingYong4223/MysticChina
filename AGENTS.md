# MysticChina Agent 开发规范

## KuiklyUI 开发参考文档

**涉及 KuiklyUI 的开发工作时，先查以下文档，再写代码：**

| 文档 | 用途 |
|-----|-----|
| `docs/KUIKLY_GUIDE.md` | 开发指南：编程模型、响应式系统、常见陷阱、最佳实践 |
| `docs/KUIKLY_API.md` | API 速查：所有视图/属性/模块/动画/指令的完整方法表 |

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

This project is indexed by GitNexus as **ExploringChina** (1839 symbols, 3557 relationships, 104 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/ExploringChina/context` | Codebase overview, check index freshness |
| `gitnexus://repo/ExploringChina/clusters` | All functional areas |
| `gitnexus://repo/ExploringChina/processes` | All execution flows |
| `gitnexus://repo/ExploringChina/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
|------|---------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
