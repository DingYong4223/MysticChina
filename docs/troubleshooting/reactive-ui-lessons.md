# Kuikly 响应式 UI 避坑指南

> 来源：草稿箱选中功能的多轮调试记录（2026-06-04），记录因 Kuikly 响应式特性导致的反复失败及正确做法。

---

## 1. 不要把 observable 值冻结在普通对象里

**错误做法**：创建配置类、变量捕获 observable 的当前值。

```kotlin
// ❌ 响应式断裂 — config 是一次性快照，selectedCount 变化后不更新
class DraftActionBarConfig(
    val selectedCount: Int,      // 冻结的值
    val isAllSelected: Boolean,  // 冻结的值
)
DraftActionBar(DraftActionBarConfig(
    selectedCount = mgr.selectedCount,   // 读取一次，不再更新
    isAllSelected = mgr.isAllSelected,  // 读取一次，不再更新
))
```

**正确做法**：在 `attr{}` 块内直接读取 observable 字段。

```kotlin
// ✅ 组件直接持有 observable 数据源，attr{} 内读取会被框架追踪
Text {
    attr {
        text(if (mgr.isAllSelected) "反选" else "全选")  // 每次重绘重新读取
        color(if (mgr.selectedCount > 0) Color(0xFFFF3B30) else Color(0xFF666666))
    }
}
```

**原理**：Kuikly 的 `attr{}` 块在布局求值时运行，框架会记录其中对 `observable` 字段的访问作为依赖。当 observable 值变化时，依赖它的 View 自动重绘。

---

## 2. `attr{}` 内的 `val` 和 `attr{}` 外的 `val` 命运不同

```kotlin
// ❌ attr{} 外的 val — 在 View 创建时计算一次，永不更新
vif({ mgr.isEditing }) {
    val sel = mgr.selectedIds.contains(video.id)   // 创建时：false
    View {
        attr {
            backgroundColor(if (sel) primary else transparent)  // 永远是 false
        }
    }
}

// ✅ attr{} 内的 val — 每次重绘时重新计算
vif({ mgr.isEditing }) {
    View {
        attr {
            val sel = mgr.selectedIds.contains(video.id)  // 每次重绘重新算
            backgroundColor(if (sel) primary else transparent)
        }
    }
}
```

---

## 3. `vfor` 要求每项恰好一个子节点，conditional 分支要补齐

```kotlin
// ❌ 崩溃 — 不匹配时产生 0 个节点
vfor({ mgr.selectedIds }) { id ->
    if (id == video.id) {
        Text { attr { text("✓") } }
    }
    // id 不匹配 → 0 个节点 → RuntimeException
}

// ✅ 补齐空节点
vfor({ mgr.selectedIds }) { id ->
    if (id == video.id) {
        Text { attr { text("✓") } }
    } else {
        View { attr { size(0f, 0f) } }
    }
}
```

---

## 4. `vif` → `observable` 可以工作，但 `vif` → `observableList.contains()` 不一定

```kotlin
// ✅ vif 追踪 observable(Boolean) 的变化
vif({ mgr.isEditing }) { ... }

// ⚠️ observableList.contains() 在 vif 中不保证响应式
//    稳妥做法：将 contains 判断放在 attr{} 内配合 vif 外层容器
```

当需要响应 observableList 的增删时，推荐组合模式：
- `vif` 控制容器显隐（追踪 Boolean observable）
- `attr{}` 内读取 list 状态控制样式
- `vif` 控制子元素的显隐（追踪 Boolean observable，不要追踪 contains()）

---

## 5. `.invoke()` 在 event 回调中有效，但**直接传 mgr 调用方法更简洁**

```kotlin
// ✅ 有效，但间接
event { click { config.onToggleSelectAll.invoke() } }

// ✅ 更好 — 直接调用
event { click { mgr.selectAll() } }
```

---

## 6. 调试心法：响应式失效时先问自己

1. **这个值在 `attr{}` 内还是外？** — 外 → 冻结，内 → 响应式
2. **这是 `observable` 字段还是普通 val？** — 普通 val → 不会触发重绘
3. **数据是直接从数据源读取还是从中间对象传递？** — 中间对象 → 可能有快照
4. **这个 observable 的访问链路能追溯到 `attr{}` 吗？** — 不能 → 断裂

---

## 7. `longPress` 和 `click` 可能同时触发

Android 的 GestureDetector 在长按后仍可能触发 `click`（取决于 Kuikly 实现）。如需防止，用标志位或改用 `click` 内判断 `isEditing` 状态。

```kotlin
longPress { if (!mgr.isEditing) mgr.enterSelection(id) }
click {
    if (mgr.isEditing) mgr.toggleSelection(id)  // 编辑态内点击只看 isEditing
    else { /* 正常跳转 */ }
}
```

---

## 8. `toggleSelection` 不要让空列表自动退出编辑态

```kotlin
// ❌ 取消最后一个选中 → 意外退出编辑态
fun toggleSelection(id: String) {
    if (contains(id)) remove(id) else add(id)
    if (isEmpty()) exitEditing()  // ← 这行是 bug
}

// ✅ 只有明确操作才退出
fun toggleSelection(id: String) {
    if (contains(id)) remove(id) else add(id)
}
```

---

**核心原则**：**observable 数据必须直接暴露给 `attr{}` 块，中间不能有任何冻结、拷贝、包装。** 每多一层间接传递，就多一个响应式断裂点。
