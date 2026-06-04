# 草稿选中模式设计规格

**日期**：2026-06-03
**项目**：yijian（一剪）
**状态**：设计稿，待批准

---

## 背景与目标

为 HomePage 剪辑 Tab 的草稿箱区域增加多选批量删除能力，同时优化卡片布局：

1. **布局**：草稿卡片从 2 列改为 3 列
2. **选中交互**：长按卡片进入选中模式，卡片右上角显示 checkbox
3. **批量操作**：底部操作栏覆盖 Tab Bar，提供「全选」和「删除」
4. **持久化**：选中状态（isEditing）仅内存，草稿数据持久化到 SharedPreferences

---

## DraftManager 模块

新增文件：`manager/DraftManager.kt`

### 职责

- 持有可观察的草稿列表和选中状态
- 提供选中模式相关操作（进入/退出/切换/全选/反选）
- 草稿数据的持久化加载与保存

### 接口

```kotlin
class DraftManager(private val sp: SharedPreferencesModule) {

    // ─── 可观察状态 ───
    val draftList: MutableList<VideoInfo>          // observableList
    val selectedIds: MutableList<String>          // observableList
    val isEditing: Boolean                        // observable

    // ─── 操作方法 ───

    /** 从 SP 加载草稿列表 */
    fun load()

    /** 添加草稿（新增 + 持久化） */
    fun add(info: VideoInfo)

    /** 删除指定 ID 的草稿（删除 + 持久化 + 检查是否自动退出选中模式） */
    fun remove(ids: List<String>)

    /** 长按卡片：进入选中模式，将该卡片设为选中 */
    fun enterSelection(id: String)

    /** 选中模式中单击卡片：切换该卡片选中态 */
    fun toggleSelection(id: String)

    /** 全选所有草稿 */
    fun selectAll()

    /** 取消全选 */
    fun deselectAll()

    /** 退出选中模式（清空 selectedIds） */
    fun exitEditing()

    /** 当前选中数量 */
    val selectedCount: Int get() = selectedIds.size

    /** 是否所有草稿都被选中（决定按钮显示「全选」还是「反选」） */
    val isAllSelected: Boolean get() = draftList.isNotEmpty() && selectedIds.size == draftList.size
}
```

### 持久化

- **Key**: `"yijian_drafts"`（SharedPreferences）
- **Value**: JSON 字符串数组
  ```json
  [
    {"id":"1","title":"午后阳光.mp4","path":"test1","duration":15200,"createTime":...},
    ...
  ]
  ```
- **序列化**：手动 JSON 拼接（Kotlin 字符串模板，不引入额外库），字段包括 `VideoInfo` 的所有属性
- **时机**：`add()` 和 `remove()` 调用后立即持久化
- **加载**：`created()` 时调用 `load()`，反序列化后填充 `draftList`

---

## UI 改造

### HomePage 变更

| 变更项 | 说明 |
|--------|------|
| `draftList` 字段 | 移除，由 `draftMgr.draftList` 替代 |
| 新增字段 | `val draftMgr = DraftManager(sp)` |
| `body()` | 选中模式时在底部分割线和 Tab Bar 之间插入 `DraftActionBar` |
| 返回键拦截 | 在 `pageDidAppear` 或物理返回键 → `isEditing` 为 true 时退出选中模式而非返回 |

### 3 列布局

```kotlin
val gap = YijianTheme.Spacing.xs  // ~4dp
val cardWidth = (ctx.pagerData.pageViewWidth - paddingLeft - paddingRight - gap * 2) / 3f
```

原始 2 列时 `padding` 是 `YijianTheme.Spacing.sm`（~8dp）。3 列使用相同的边距：
- 每行左/右 padding：8dp
- 卡片间距：4dp（gap = `Spacing.xs`）
- `cardWidth = (411 - 16 - 8) / 3 ≈ 129dp`
- `cardHeight = cardWidth * 9/16 + 52 ≈ 125dp`

### DraftCard 变更

```kotlin
private fun ViewContainer<*, *>.DraftCard(ctx: HomePage, mgr: DraftManager, video: VideoInfo, cardWidth: Float) {
    // 正常模式：click → jumpPage
    // 选中模式：click → toggleSelection
    // 长按 → enterSelection（不论是否已选中）
    //
    // 选中模式时，右上角显示 checkbox（圆形，可选/已选两种状态）
    // 已选卡片边框包裹 primary 色
}
```

关键交互：

| 状态 | 单击 | 长按 |
|------|------|------|
| 正常模式 | 跳转 EditorPage | `enterSelection(id)` → 进入选中模式 |
| 选中模式 | `toggleSelection(id)` | 无效果 |

### DraftActionBar

新增文件：`components/DraftActionBar.kt`

```
┌─────────────────────────────────────────┐
│              │                           │
│    全选      │       删除(N)              │  ← 56dp 高，覆盖 Tab Bar
│              │                           │
└─────────────────────────────────────────┘
```

| 属性 | 值 |
|------|-----|
| 高度 | 56f + safeAreaInsets.bottom |
| 背景 | `YijianColors.surface`（#1E1E1E） |
| 分隔线 | 1dp `YijianColors.surfaceLight` |
| 「全选」文字色 | YijianColors.textPrimary（#E0E0E0） |
| 「删除」文字色 | selectedCount > 0 → `#FF3B30`（红） / 否则 `#666666`（灰不可点） |

**行为**：
- 「全选」/「反选」文字随 `isAllSelected` 切换
- `isAllSelected == false` → 显示「全选」，点击 → `selectAll()`
- `isAllSelected == true` → 显示「反选」，点击 → `deselectAll()`
- 「删除」点击 → `remove(selectedIds.toList())` → 自动 `exitEditing()`
- 选中 0 个时「删除」文字灰色，不可点击

### 底部叠加机制

选中模式时，body() 结构变为：

```
┌──────────────────────────────┐
│         内容区（flex:1）       │
├──────────────────────────────┤
│  DraftActionBar（绝对覆盖）    │  ← zIndex > Tab Bar
├──────────────────────────────┤
│  BottomTabBar（被遮住）        │  ← 存在但不可见/不可点击
└──────────────────────────────┘
```

实现方式：在 `BottomTabBar` 之前插入 `DraftActionBar`（渲染顺序决定覆盖层次），或将 BottomTabBar 包裹在 `vif` 中（选中模式隐藏，DraftActionBar 占据其位置）。

---

## 边界条件

| 场景 | 处理 |
|------|------|
| 草稿列表为空 | 显示空态提示，无卡片可长按，无法进入选中模式 |
| 选中模式下点击「新建剪辑」 | `exitEditing()` 后再跳转 MainPage |
| 选中 0 个点击「删除」 | 按钮灰色，不可点击 |
| 删除后列表为空 | 自动 `exitEditing()`，显示空态 |
| 物理返回键 | `isEditing == true` → `exitEditing()` 并消费事件；否则正常返回 |
| 草稿数 < 3 | `isAllSelected` 按实际数量判断，`selectAll()` 选全部 |
| JSON 解析失败 | 反序列化时 try-catch，失败则 `draftList` 置空，不崩溃 |

---

## 文件变更汇总

| 文件 | 操作 | 说明 | 预估行数 |
|------|------|------|---------|
| `manager/DraftManager.kt` | 新增 | 草稿管理 + 持久化 | ~100 |
| `components/DraftActionBar.kt` | 新增 | 底部操作栏组件 | ~70 |
| `pages/HomePage.kt` | 修改 | 集成 DraftManager、3 列布局、长按选中 | +80/-50 |
| **总计** | | | **~250 行** |

---

## 不在本次范围内

- 从 EditorPage 保存后自动落草稿（后期实装）
- 批量删除确认弹窗
- 草稿排序（保持按创建时间倒序）
- 撤销删除
- 多选删除后的 Snackbar 恢复提示
