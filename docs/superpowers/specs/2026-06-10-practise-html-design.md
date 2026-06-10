# practise.html — 练字专项页面设计

**日期**：2026-06-10  
**状态**：已批准

---

## 背景

`HanziPage` 当前通过 `HanziWebView` 组件加载 `assets/hanzi/index.html`，该页面同时包含"笔画动画"和"练字测验"两个 section。需求：新建专注于练字测验的 `practise.html`，并让 `HanziWebView` 组件支持通过 Attr 指定加载哪个页面，`HanziPage` 改为加载 `practise.html`。

---

## 目标

1. 新增 `assets/hanzi/practise.html`，只包含 Stroke Quiz（练字测验）部分。
2. `HanziWebView` 组件增加 `src` 属性，由调用方指定要加载的 HTML 文件名。
3. `HanziWebViewImpl` 响应 `src` prop 来触发 `loadUrl`，移除 `init` 中的硬编码加载。
4. `HanziPage` 使用 `src("practise.html")` 加载新页面。

---

## 涉及文件

| 文件 | 变更类型 |
|---|---|
| `shared/.../hanzi/HanziWebView.kt` | 修改：新增 `HanziWebAttr` 及 `src()` 方法 |
| `androidApp/.../view/HanziWebViewImpl.kt` | 修改：移除 init 中 loadUrl，新增 setProp 处理 `src` |
| `shared/.../pages/HanziPage.kt` | 修改：在 `HanziWeb` attr 中加入 `src("practise.html")` |
| `assets/hanzi/practise.html` | 新建：quiz-only 页面 |

---

## 详细设计

### 1. `HanziWebView.kt` — 新增 `HanziWebAttr`

```kotlin
class HanziWebAttr : Attr() {
    /** 指定加载的 HTML 文件名，例如 "practise.html" */
    fun src(url: String): HanziWebAttr {
        "src" with url
        return this
    }
}

class HanziWebView : DeclarativeBaseView<HanziWebAttr, Event>() {
    override fun createAttr() = HanziWebAttr()
    override fun createEvent() = Event()
    override fun viewName() = "HanziWebView"
}

fun ViewContainer<*, *>.HanziWeb(init: HanziWebView.() -> Unit) {
    addChild(HanziWebView(), init)
}
```

`"src" with url` 通过 KuiklyUI prop 机制将属性值发往 Android 侧，触发 `HanziWebViewImpl.setProp("src", url)`。

---

### 2. `HanziWebViewImpl.kt` — 响应 `src` prop

```kotlin
// init 块中移除 loadUrl(...)，只保留 settings 配置

override fun setProp(propKey: String, propValue: Any): Boolean {
    if (propKey == "src") {
        loadUrl("file:///android_asset/hanzi/$propValue")
        return true
    }
    return false
}
```

**时序优势**：KuiklyUI 在完成 FlexBox 布局后才调用 `setProp`，此时 WebView 已有正确的 Android 视图宽度，`window.innerWidth` 有更高概率返回正确值。`practise.html` 中仍保留 `screen.width` 兜底，确保两种路径都安全。

---

### 3. `HanziPage.kt` — 指定页面

```kotlin
HanziWeb {
    attr {
        flex(1f)
        width(ctx.pagerData.pageViewWidth)
        src("practise.html")
    }
}
```

---

### 4. `practise.html` — 内容规格

**保留**：
- 常用字快速选择字格（`#char-grid`）
- 手动输入框 + 更新按钮（`.char-row`）
- 错误提示（`.error-msg`）
- 练字测验 section（`#quiz-target`、重置按钮、轮廓开关、提示文字）
- `localLoader`（从 `./data/{char}.json` 读取，不走网络）
- `window.onload` 中的 viewport 宽度修复（`screen.width` 覆盖 body 宽度）

**移除**：
- 笔画动画 section（`#animation-target`、播放按钮、动画轮廓开关）
- `animWriter` 变量及相关初始化/事件代码
- 动画相关 CSS

**JS 入口**：`window.onload` → 修复 viewport → `updateWriters('我')`，`updateWriters` 只创建 `quizWriter`。

---

## 不在本次范围内

- 新增导航路由或新的 Kotlin Page
- 修改 `index.html`（保留现有全功能版本不动）
- `HanziWebView` 支持动态切换页面（run-time 修改 src）

---

## 验收标准

1. 编译通过，无 Kotlin 类型错误。
2. HanziPage 打开后只显示练字测验区域，无笔画动画 section。
3. 字格点击正确切换练习汉字。
4. 笔顺写对后显示完成提示；↺ 重置可重新练习。
5. `index.html` 仍可独立在浏览器中打开，功能不受影响。
