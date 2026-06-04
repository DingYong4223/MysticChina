# 草稿选中模式实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 HomePage 剪辑 Tab 的草稿箱启用长按多选、3 列布局、底部操作栏批量删除

**Architecture:** DraftManager 模块封装选中状态 + 持久化，DraftActionBar 组件负责底部操作栏 UI，HomePage 仅做组装和路由拦截。持久化通过 SharedPreferences + 手动 JSON 序列化实现。

**Tech Stack:** KuiklyUI DSL, SharedPreferencesModule, observableList/observable

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `manager/DraftManager.kt` | 新增 | 草稿列表状态、选中状态、SP 持久化 |
| `components/DraftActionBar.kt` | 新增 | 底部操作栏 UI（全选/删除） |
| `pages/HomePage.kt` | 修改 | 集成 DraftManager，3 列布局，长按选中，返回键拦截 |

---

### Task 1: 创建 DraftManager 模块

**Files:**
- Create: `shared/src/commonMain/kotlin/com/yijian/manager/DraftManager.kt`

- [ ] **Step 1: 创建 DraftManager 类骨架**

```kotlin
package com.yijian.manager

import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.yijian.model.VideoInfo

class DraftManager(private val sp: SharedPreferencesModule) {

    companion object {
        private const val SP_KEY_DRAFTS = "yijian_drafts"
    }

    var draftList by observableList<VideoInfo>()
    var selectedIds by observableList<String>()
    var isEditing by observable(false)

    val selectedCount: Int get() = selectedIds.size
    val isAllSelected: Boolean get() = draftList.isNotEmpty() && selectedIds.size == draftList.size

    fun load() {
        val json = sp.getString(SP_KEY_DRAFTS) ?: return
        draftList.clear()
        draftList.addAll(parseVideoInfoList(json))
    }

    fun add(info: VideoInfo) {
        draftList.add(0, info)
        save()
    }

    fun remove(ids: List<String>) {
        val idSet = ids.toSet()
        draftList.removeAll { it.id in idSet }
        selectedIds.removeAll { it in idSet }
        if (draftList.isEmpty()) exitEditing()
        save()
    }

    fun enterSelection(id: String) {
        isEditing = true
        if (!selectedIds.contains(id)) selectedIds.add(id)
    }

    fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id)
        else selectedIds.add(id)
        if (selectedIds.isEmpty()) exitEditing()
    }

    fun selectAll() {
        selectedIds.clear()
        selectedIds.addAll(draftList.map { it.id })
    }

    fun deselectAll() {
        selectedIds.clear()
    }

    fun exitEditing() {
        isEditing = false
        selectedIds.clear()
    }

    // ─── 持久化 ───

    private fun save() {
        sp.setString(SP_KEY_DRAFTS, serializeVideoInfoList(draftList))
    }

    private fun serializeVideoInfoList(list: List<VideoInfo>): String {
        val sb = StringBuilder("[")
        for ((i, v) in list.withIndex()) {
            if (i > 0) sb.append(',')
            sb.append("""{"id":"${escape(v.id)}","title":"${escape(v.title)}","path":"${escape(v.path)}",""")
            sb.append("""duration":${v.duration},"fileSize":${v.fileSize},"createTime":${v.createTime}}""")
        }
        sb.append(']')
        return sb.toString()
    }

    private fun parseVideoInfoList(json: String): List<VideoInfo> {
        if (!json.startsWith('[') || !json.endsWith(']')) return emptyList()
        val trimmed = json.substring(1, json.length - 1).trim()
        if (trimmed.isEmpty()) return emptyList()
        val result = mutableListOf<VideoInfo>()
        var i = 0
        while (i < trimmed.length) {
            val objStart = trimmed.indexOf('{', i)
            if (objStart < 0) break
            val objEnd = trimmed.indexOf('}', objStart)
            if (objEnd < 0) break
            val obj = trimmed.substring(objStart, objEnd + 1)
            result.add(parseVideoInfo(obj) ?: continue)
            i = objEnd + 1
        }
        return result
    }

    private fun parseVideoInfo(obj: String): VideoInfo? {
        try {
            val id = extractStr(obj, "id") ?: return null
            val title = extractStr(obj, "title") ?: ""
            val path = extractStr(obj, "path") ?: ""
            val duration = extractLong(obj, "duration")
            val fileSize = extractLong(obj, "fileSize")
            val createTime = extractLong(obj, "createTime")
            return VideoInfo(
                id = id, title = title, path = path,
                duration = duration, fileSize = fileSize,
                createTime = createTime,
            )
        } catch (_: Exception) { return null }
    }

    private fun extractStr(json: String, key: String): String? {
        val idx = json.indexOf("\"$key\"")
        if (idx < 0) return null
        val colon = json.indexOf(':', idx)
        if (colon < 0) return null
        var start = colon + 1
        while (start < json.length && json[start] == ' ') start++
        if (start >= json.length || json[start] != '"') return null
        start++
        val sb = StringBuilder()
        var pos = start
        while (pos < json.length) {
            val c = json[pos]
            if (c == '\\') { sb.append(json.getOrElse(pos + 1) { '?' }); pos += 2 }
            else if (c == '"') break
            else { sb.append(c); pos++ }
        }
        return sb.toString()
    }

    private fun extractLong(json: String, key: String): Long {
        val idx = json.indexOf("\"$key\"")
        if (idx < 0) return 0L
        val colon = json.indexOf(':', idx)
        if (colon < 0) return 0L
        var start = colon + 1
        while (start < json.length && !json[start].isDigit() && json[start] != '-') start++
        var end = start
        while (end < json.length && (json[end].isDigit() || json[end] == '-')) end++
        return json.substring(start, end).trim().toLongOrNull() ?: 0L
    }

    private fun escape(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
```

- [ ] **Step 2: 验证编译**

Run: `./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add shared/src/commonMain/kotlin/com/yijian/manager/DraftManager.kt
git commit -m "feat: add DraftManager with selection state and SP persistence"
```

---

### Task 2: 创建 DraftActionBar 组件

**Files:**
- Create: `shared/src/commonMain/kotlin/com/yijian/components/DraftActionBar.kt`

- [ ] **Step 1: 创建 DraftActionBar**

```kotlin
package com.yijian.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.yijian.theme.YijianColors

/**
 * 底部操作栏 — 选中模式时覆盖 Tab Bar
 *
 * ┌─────────────────────────────────────┐
 * │         │                            │
 * │  全选    │         删除(N)            │
 * │         │                            │
 * └─────────────────────────────────────┘
 */
class DraftActionBarConfig(
    val isAllSelected: Boolean,
    val selectedCount: Int,
    val onToggleSelectAll: () -> Unit,
    val onDelete: () -> Unit,
)

internal fun ViewContainer<*, *>.DraftActionBar(
    config: DraftActionBarConfig,
    safeAreaBottom: Float,
) {
    val selectAllLabel = if (config.isAllSelected) "反选" else "全选"
    val canDelete = config.selectedCount > 0

    View {
        attr {
            height(56f + safeAreaBottom)
            backgroundColor(Color(0xFF1E1E1E))
            flexDirectionRow()
            alignItemsCenter()
            paddingBottom(safeAreaBottom)
        }

        // 全选/反选
        View {
            attr { flex(1f); allCenter() }
            event { click { config.onToggleSelectAll.invoke() } }
            Text {
                attr {
                    text(selectAllLabel)
                    fontSize(15f); color(YijianColors.textPrimary)
                }
            }
        }

        // 分割线
        View { attr { width(1f); height(30f); backgroundColor(YijianColors.surfaceLight) } }

        // 删除
        View {
            attr { flex(1f); allCenter() }
            event { click { if (canDelete) config.onDelete.invoke() } }
            Text {
                attr {
                    text(if (config.selectedCount > 0) "删除(${config.selectedCount})" else "删除")
                    fontSize(15f)
                    color(if (canDelete) Color(0xFFFF3B30) else Color(0xFF666666))
                }
            }
        }
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add shared/src/commonMain/kotlin/com/yijian/components/DraftActionBar.kt
git commit -m "feat: add DraftActionBar component for batch operations"
```

---

### Task 3: 修改 HomePage — 集成 DraftManager + 3 列布局

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/yijian/pages/HomePage.kt`

- [ ] **Step 1: 修改 HomePage 类字段**

在 class 体内：
- 移除 `var draftList by observableList<VideoInfo>()`（原第 39 行）
- 在 `selectedTab` 之后添加：
```kotlin
    var draftMgr = DraftManager(sp)
```
- 修改 `created()` 中的草稿加载：
  - 移除 `draftList.addAll(listOf(...))`（原第 61-70 行）
  - 改为调用 `draftMgr.load()`
  - 保留 mock 数据（改为通过 DraftManager 添加）：
```kotlin
    override fun created() {
        super.created()
        userProfile = UserProfile(
            nickname = sp.getString(SP_NICKNAME) ?: "创作者",
            bio = sp.getString(SP_BIO) ?: "记录生活的每一刻",
            avatarEmoji = sp.getString(SP_AVATAR) ?: "🎬"
        )
        draftMgr.load()
        // 首次运行时没有数据，添加 mock
        if (draftMgr.draftList.isEmpty()) {
            draftMgr.add(VideoInfo("1", "午后阳光.mp4", "test1",
                duration = 15200L, createTime = currentTimeMs() - 7200000L))
            draftMgr.add(VideoInfo("2", "城市街景.mp4", "test2",
                duration = 45000L, createTime = currentTimeMs() - 86400000L))
            draftMgr.add(VideoInfo("3", "旅行记录.mp4", "test3",
                duration = 120000L, createTime = currentTimeMs() - 172800000L))
            draftMgr.add(VideoInfo("4", "美食制作.mp4", "test4",
                duration = 32000L, createTime = currentTimeMs() - 3600000L))
        }
    }
```

注意：`sp` 字段是 `private val sp by lazy { ... }`。`draftMgr` 需要 `sp`，而 `sp` 是在 `body()` 第一次调用时初始化的。如果在 `created()` 中使用 `draftMgr`，`sp` 在 `created()` 时可能还没初始化。因此要么将 `sp` 改为非 lazy 的 `acquireModule()` 直接调用，要么在 `created()` 中手动初始化 `sp`。推荐改为：
```kotlin
    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }
    var draftMgr by lazy { DraftManager(sp) }
```

- [ ] **Step 2: 修改 body() 添加操作栏**

在 `BottomTabBar(ctx)` 之前插入操作栏：
```kotlin
            // ─── 选中模式操作栏 ───
            vif({ ctx.draftMgr.isEditing }) {
                DraftActionBar(
                    config = DraftActionBarConfig(
                        isAllSelected = ctx.draftMgr.isAllSelected,
                        selectedCount = ctx.draftMgr.selectedCount,
                        onToggleSelectAll = {
                            if (ctx.draftMgr.isAllSelected) ctx.draftMgr.deselectAll()
                            else ctx.draftMgr.selectAll()
                        },
                        onDelete = { ctx.draftMgr.remove(ctx.draftMgr.selectedIds.toList()) },
                    ),
                    safeAreaBottom = ctx.pagerData.safeAreaInsets.bottom,
                )
            }
```

添加 `DraftActionBar` 引用：
```kotlin
import com.yijian.components.DraftActionBar
import com.yijian.components.DraftActionBarConfig
import com.yijian.manager.DraftManager
```

- [ ] **Step 3: 修改 ClipTabContent — 3 列布局**

在 `ClipTabContent` 中：
- 参数增加 `mgr: DraftManager`
- 将 `ctx.draftList` 替换为 `mgr.draftList`
- 修改 `cardWidth` 计算：
```kotlin
val gap = YijianTheme.Spacing.xs
val cardWidth = (ctx.pagerData.pageViewWidth - YijianTheme.Spacing.sm * 2 - gap * 2) / 3f
```

- 修改 `DraftCard` 调用：
```kotlin
vfor({ mgr.draftList }) { draft ->
    DraftCard(ctx, mgr, draft, cardWidth)
}
```

- 更新 `body()` 中 `ClipTabContent` 调用：`ClipTabContent(ctx, ctx.draftMgr)`

- [ ] **Step 4: 修改 DraftCard — 长按 + 选中态 + checkbox**

```kotlin
private fun ViewContainer<*, *>.DraftCard(
    ctx: HomePage, mgr: DraftManager,
    video: VideoInfo, cardWidth: Float,
) {
    val isSelected = mgr.selectedIds.contains(video.id)
    val isEditing = mgr.isEditing

    View {
        attr {
            size(cardWidth, cardWidth * 9f / 16f + 52f)
            flexDirectionColumn()
            margin(YijianTheme.Spacing.xs)
            backgroundColor(YijianColors.surface)
            borderRadius(YijianTheme.Radius.md)
            overflow(true)
            if (isSelected && isEditing) {
                border(Border(2f, YijianColors.primary))
            }
        }
        event {
            // 长按 → 选中模式
            longClick {
                if (!isEditing) {
                    mgr.enterSelection(video.id)
                }
            }
            // 单击
            click {
                if (isEditing) {
                    mgr.toggleSelection(video.id)
                } else {
                    if (!fileExists(video.path)) {
                        ctx.showError("视频不存在: ${video.title}")
                        return@click
                    }
                    val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
                    ctx.jumpPage("EditorPage", params)
                }
            }
        }

        // 缩略图
        View {
            attr {
                size(cardWidth, cardWidth * 9f / 16f)
                backgroundColor(YijianColors.backgroundLight); allCenter()
            }
            Text { attr { text("🎞"); fontSize(24f) } }

            if (video.duration > 0) {
                View {
                    attr {
                        absolutePosition(bottom = 4f, right = 4f)
                        paddingLeft(4f); paddingRight(4f); paddingTop(1f); paddingBottom(1f)
                        backgroundColor(Color(0xCC000000)); borderRadius(3f)
                    }
                    Text { attr { text(video.formattedDuration); fontSize(9f); color(YijianColors.textPrimary) } }
                }
            }

            // 选中模式 → 右上角 checkbox
            if (isEditing) {
                View {
                    attr {
                        absolutePosition(top = 4f, right = 4f)
                        size(20f, 20f); borderRadius(10f)
                        backgroundColor(if (isSelected) YijianColors.primary else Color(0x00000000))
                        border(Border(2f, if (isSelected) YijianColors.primary else Color(0x88FFFFFF)))
                        allCenter()
                    }
                    if (isSelected) {
                        Text { attr { text("✓"); fontSize(12f); color(Color(0xFFFFFFFF)); fontWeightBold() } }
                    }
                }
            }
        }

        // 标题
        View {
            attr { flex(1f); padding(all = 4f); justifyContentCenter() }
            Text {
                attr {
                    text(video.title); fontSize(11f); color(YijianColors.textPrimary)
                    maxLines(1)
                }
            }
        }
    }
}
```

注意：`longClick` 事件在 Kuikly DSL 中可能需要使用触摸事件模拟。如果 `longClick` 不存在，使用 `touchDown` + `touchUp` 计时判断。

- [ ] **Step 5: 添加返回键拦截**

在 `HomePage` 类中添加：
```kotlin
    override fun onBackPressed(): Boolean {
        if (draftMgr.isEditing) {
            draftMgr.exitEditing()
            return true  // 消费事件，不退出页面
        }
        return super.onBackPressed()
    }
```

- [ ] **Step 6: 添加「新建剪辑」跳转前退出选中模式**

找到「新建剪辑」按钮的 click 事件（ClipTabContent 内），改为：
```kotlin
    event { click {
        if (mgr.isEditing) mgr.exitEditing()
        ctx.jumpPage("MainPage")
    } }
```

- [ ] **Step 7: 添加 `longClick` 事件支持（如 Kuikly DSL 没有内置）**

如果 `longClick` 在 Kuikly DSL 中不可用，使用 `touch` 事件模拟：
```kotlin
event {
    // 长按模拟：touchDown 记录时间，touchUp 检查间隔
    touchDown {
        touchStartTime = System.currentTimeMs()
    }
    touchUp {
        val elapsed = currentTimeMs() - touchStartTime
        if (elapsed >= 500) {  // 500ms 视为长按
            mgr.enterSelection(video.id)
        }
    }
}
```

在 DraftCard 外部的调用闭包中定义 `touchStartTime` 变量：
```kotlin
private fun ViewContainer<*, *>.DraftCard(...) {
    var touchStartMs = 0L
    ...
    event {
        touchDown { touchStartMs = currentTimeMs() }
        touchUp {
            val elapsed = currentTimeMs() - touchStartMs
            if (elapsed >= 500L) {
                mgr.enterSelection(video.id)
            } else if (isEditing) {
                mgr.toggleSelection(video.id)
            } else {
                // 单击跳转逻辑
            }
        }
    }
}
```

- [ ] **Step 8: 验证编译**

Run: `./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: 提交**

```bash
git add shared/src/commonMain/kotlin/com/yijian/pages/HomePage.kt
git commit -m "feat: draft 3-column layout, long-press selection, batch action bar"
```

---

## 自检

- ✅ 所有 spec 需求覆盖：3 列布局（Task 3 Step 3）、长按选中（Step 4/7）、checkbox（Step 4）、操作栏（Step 2）、全选/反选（Step 2）、删除（Step 2）、持久化（Task 1）、返回键拦截（Step 5）
- ✅ 无占位符、TBD、TODO
- ✅ 类型一致性：`DraftManager` 字段名 `selectedIds` / `isEditing` / `isAllSelected` 在全局一致
- ✅ 范围聚焦：专注于草稿选中模式，不涉及 EditorPage 保存落草稿或其他功能
