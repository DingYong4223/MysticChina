# KuiklyUI 开发指南

> 从 `KuiklyUI/core` 源码直接提取，2026-06-12。
> 开发前先阅读本文档，API 速查见 [KUIKLY_API.md](KUIKLY_API.md)。

---

## ⚠️ 关键纠错（源码核实，CLAUDE.md 有误）

以下几点与常见文档描述不符，源码已核实：

| 错误写法 | 正确写法 | 说明 |
|---------|---------|------|
| `event { on("name") { … } }` | `event { registerEvent("name", handler) }` | `on()` 方法不存在 |
| `setInterval(scope, 1000L) { }` | `Timer().schedule(0, 1000) { }` + `timer.cancel()` | core 无 `setInterval` 全局函数 |
| `clearInterval(ref)` | `timer.cancel()` | 同上 |
| `Animation.spring(duration, damping, velocity)` | `Animation.springEaseOut(duration, damping, velocity)` | spring() 不是单独工厂方法 |
| `observable()` 顶层函数 | `PagerScope.observable()` | 顶层版本已 `@Deprecated` |
| `lifecycleScope.launch { }` | `lifecycleScope.launch { }` | ✅ 正确（lifecycleScope 是 Pager 属性） |

---

## 1. 编程模型概览

### 核心概念

```
Pager（全屏页面）
  └── body(): ViewBuilder          ← 一次性执行，构建视图树
        ├── View { }               ← 通用容器（DivView）
        ├── Text { }               ← 文本
        ├── Image { }              ← 图片
        ├── Scroller { }           ← 滚动容器
        ├── List { }               ← 虚拟化列表
        └── CustomComponent { }   ← 自定义 ComposeView
              └── body(): ViewBuilder
```

**关键规则：**
- `body()` 只执行一次，UI 更新靠 `observable` 属性驱动
- `attr {}` 是响应式的 — 内部读取的 `observable` 变化时自动重跑整个块
- `event {}` 只执行一次，不响应式
- 所有状态必须在类属性级声明，不能在 `body()` 内声明

### 完整生命周期

```
created()          ← 解析 pageData.params，初始化状态
viewWillLoad()     ← body() 执行前
[body() 执行]      ← 构建视图树（一次性）
viewDidLoad()      ← body 执行完毕，可命令式操作视图
[FlexBox 布局]
viewDidLayout()    ← 首次布局完成，可读取 frame
pageDidAppear()    ← 每次页面出现（push/tab 切换）
pageDidDisappear() ← 每次页面消失
pageWillDestroy()  ← 取消协程、释放资源
viewWillUnload()
viewDidUnload()
viewDestroyed()
```

---

## 2. Page 页面开发

### 最小骨架

```kotlin
@Page("MyPage", supportInLocal = true)
internal class MyPage : BasePager() {

    // 所有状态在类级别声明
    var title by observable("")
    var count by observable(0)
    var items: ObservableList<String> by observableList()

    override fun created() {
        super.created()
        // 解析传入参数
        title = pageData.params.optString("title", "默认标题")
    }

    override fun pageDidAppear() {
        // 每次可见时刷新
    }

    override fun pageWillDestroy() {
        // 取消协程等清理工作
    }

    override fun body(): ViewBuilder {
        val ctx = this   // ❗必须：在 lambda 外捕获 this，避免 this 歧义
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
                backgroundColor(MysticChinaColors.background)
            }
            // 子视图...
        }
    }
}
```

### `val ctx = this` 为什么必须？

`body()` 返回的 `ViewBuilder` 是 `ViewContainer<*,*>.() -> Unit`。在 lambda 内部，`this` 指向 `ViewContainer`，而非 `MyPage`。因此必须先把页面实例捕获到 `ctx`：

```kotlin
// ✅ 正确
override fun body(): ViewBuilder {
    val ctx = this
    return {
        Text { attr { text(ctx.title) } }  // ctx.title 响应式读取
    }
}

// ❌ 错误 — this 是 ViewContainer，没有 title 属性
override fun body(): ViewBuilder {
    return {
        Text { attr { text(this.title) } }  // 编译错误
    }
}
```

---

## 3. 响应式系统

### 声明方式

```kotlin
// 标量
var isPlaying by observable(false)
var progress by observable(0f)
var title by observable("")

// 集合（触发 add/remove/set 时重渲染）
var feedList: ObservableList<VideoInfo> by observableList()
var selectedIds: ObservableSet<String> by observableSet()
```

### 如何触发更新

```kotlin
// 标量 — 直接赋值
isPlaying = true              // 触发所有读取 isPlaying 的 attr{} 重跑

// 集合 — 直接操作（所有 MutableList/MutableSet 方法均响应式）
feedList.add(newItem)
feedList.removeAt(0)
feedList.clear()

// ObservableList 专有 — Myers diff 最小化更新
feedList.diffUpdate(newList) { a, b -> a.id == b.id }
```

### attr{} vs event{} 响应性

```kotlin
View {
    attr {
        // ✅ 响应式：ctx.isPlaying 变化时自动重新执行整个 attr 块
        backgroundColor(if (ctx.isPlaying) Color.RED else Color.GRAY)
    }
    event {
        // ❌ 非响应式：只执行一次，此处不能用 observable
        // 但 lambda 捕获的 ctx 引用是稳定的，可以在回调内读写
        click {
            ctx.isPlaying = !ctx.isPlaying  // ✅ 点击时再去读最新值
        }
    }
}
```

---

## 4. 自定义组件（ComposeView）

### 三步创建

```kotlin
// Step 1: 属性类
class CardAttr : ComposeAttr() {
    var title: String by observable("")
    var imageUrl: String by observable("")
}

// Step 2: 组件类
class CardView : ComposeView<CardAttr, ComposeEvent>() {
    override fun createAttr() = CardAttr()
    override fun createEvent() = ComposeEvent()

    override fun body(): ViewBuilder {
        val ctx = this           // ❗必须
        return {
            View {
                attr {
                    size(200f, 120f)
                    borderRadius(8f)
                    backgroundColor(Color.WHITE)
                }
                Text {
                    attr {
                        text(ctx.getViewAttr().title)
                        fontSize(14f)
                    }
                }
            }
        }
    }
}

// Step 3: DSL 扩展函数
fun ViewContainer<*, *>.CardView(init: CardView.() -> Unit) {
    addChild(CardView(), init)
}
```

### 父子组件通信

```kotlin
// 子组件发射事件
class MyAttr : ComposeAttr() { var count by observable(0) }
class MyEvent : ComposeEvent() {
    fun onTap(handler: (Int) -> Unit) {
        registerEvent("tap") { data ->
            handler(data as? Int ?: 0)
        }
    }
}

class MyView : ComposeView<MyAttr, MyEvent>() {
    override fun createAttr() = MyAttr()
    override fun createEvent() = MyEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                event {
                    click {
                        // 触发自定义事件
                        ctx.emit("tap", ctx.getViewAttr().count)
                    }
                }
            }
        }
    }
}

// 调用处
MyView {
    attr { count = 42 }
    event { onTap { count -> println("tapped: $count") } }
}
```

---

## 5. 布局系统（FlexBox）

### 常用布局模式

```kotlin
// 竖向排列（默认方向）
View {
    attr {
        flex(1f)
        flexDirectionColumn()
    }
    // 子视图从上到下
}

// 横向排列
View {
    attr {
        flexDirectionRow()
        alignItemsCenter()      // 子视图垂直居中
        justifyContentSpaceBetween()
    }
}

// 填满父容器
View {
    attr { flex(1f) }          // 主轴方向填满
}

// 绝对定位填满父容器
View {
    attr { absolutePositionAllZero() }
}

// 绝对定位指定位置
View {
    attr {
        positionAbsolute()
        top(10f)
        right(10f)
        size(44f, 44f)
    }
}

// 两端都有固定宽度，中间填满
View {
    attr { flexDirectionRow() }
    View { attr { width(60f) } }    // 左
    View { attr { flex(1f) } }      // 中（填满）
    View { attr { width(60f) } }    // 右
}
```

### 布局注意事项

1. 默认方向是 `COLUMN`（竖向），横向需要显式 `flexDirectionRow()`
2. `flex(1f)` 在主轴方向填满剩余空间
3. 嵌套 `Scroller` 时必须有固定高度或 `flex(1f)` 限定，否则无限高
4. `padding` 和 `margin` 都是 dp 值

---

## 6. 常用视图速查

### Text

```kotlin
Text {
    attr {
        text("Hello")
        fontSize(16f)
        color(Color.WHITE)
        fontWeightBold()
        lines(2)               // 最多2行，超出省略
        textOverFlowTail()     // "..." 省略
        textAlignCenter()
        lineHeight(22f)
    }
}
```

### Image

```kotlin
Image {
    attr {
        src("https://example.com/image.jpg")
        resizeCover()          // 等比裁剪填满
        size(80f, 80f)
        borderRadius(40f)      // 圆形
        placeholderSrc("placeholder.png")
    }
    event {
        loadSuccess { println("loaded") }
        loadFailure { params -> println("error: ${params.errorCode}") }
    }
}
```

### Scroller（纵向滚动）

```kotlin
Scroller {
    attr {
        flex(1f)
        flexDirectionColumn()
        bouncesEnable(true)
    }
    event {
        scroll { params ->
            println("offsetY: ${params.offsetY}")
        }
    }
    // 子视图...
}
```

### List（虚拟化列表，推荐替代大量 vfor）

```kotlin
List {
    attr { flex(1f) }
    vforLazy({ ctx.items }, maxLoadItem = 20) { item, index, count ->
        ItemView {
            attr { width(ctx.pagerData.pageViewWidth) }
        }
    }
}
```

### 横向滚动

```kotlin
Scroller {
    attr {
        flexDirectionRow()          // 横向
        paddingLeft(16f)
        paddingBottom(8f)
    }
    items.forEach { item ->
        ItemCard { attr { ... } }
    }
    // 末尾留白（paddingRight 对滚动容器无效！）
    View { attr { width(16f) } }
}
```

> ⚠️ **Scroller/List 的 `paddingRight` 不延伸滚动内容**。横向滚动末尾留白必须加空 View。

### Input 输入框

```kotlin
// 类属性
var inputText by observable("")

// body 内
Input {
    attr {
        text(ctx.inputText)
        placeholder("请输入...")
        placeholderColor(Color(0x80FFFFFF))
        fontSize(14f)
        color(Color.WHITE)
        returnKeyTypeDone()
        autofocus(false)
    }
    event {
        textDidChange(isSyncEdit = true) { params ->
            ctx.inputText = params.text
        }
        inputReturn {
            // 点击完成键
        }
        keyboardHeightChange { params ->
            // params.height: 键盘高度（0 = 收起）
        }
    }
}
```

---

## 7. 条件渲染与列表渲染

### 条件指令

```kotlin
vif({ ctx.isLoading }) {
    ActivityIndicator { }
}
velseif({ ctx.hasError }) {
    Text { attr { text("加载失败") } }
}
velse {
    ContentView { }
}
```

**规则：**
- `vif/velseif/velse` 必须相邻，不能插入其他节点
- 条件表达式必须用 lambda `{ }` 包裹（使其响应式）

### 列表渲染（非虚拟化）

```kotlin
// vfor — 标准，仅有 item
vfor({ ctx.items }) { item ->
    Text { attr { text(item.name) } }
}

// vforIndex — 有 index 和 count
vforIndex({ ctx.items }) { item, index, count ->
    Text { attr { text("$index/$count: ${item.name}") } }
}
```

**规则：**
- 接受 `ObservableList`（用 `observableList()` 声明）
- creator 内必须且仅创建 **一个** 子视图

### 懒加载列表（List 内专用）

```kotlin
List {
    attr { flex(1f) }
    vforLazy({ ctx.bigList }) { item, index, count ->
        // 仅渲染可见区域，性能最优
        ItemRow { attr { height(60f) } }
    }
}
```

---

## 8. 动画

### `animateToAttr` — 命令式属性动画（推荐）

```kotlin
// 在 viewRef 或直接调用（响应式事件回调中）
someViewRef?.view?.animateToAttr(Animation.easeInOut(0.3f)) {
    translateY(100f)
    opacity(0f)
}

// 带完成回调
someViewRef?.view?.animateToAttr(
    animation = Animation.springEaseOut(0.4f, 0.7f, 0f),
    completion = { finished -> println("动画结束: $finished") }
) {
    opacity(1f)
    translateY(0f)
}
```

### `attr{}` 内绑定动画（声明式）

```kotlin
attr {
    // 当 ctx.progress 变化时，用动画插值
    animate(Animation.linear(0.2f), ctx.progress)
    // 然后用 progress 设置属性
    opacity(ctx.progress)
}
```

### Animation 工厂方法

```kotlin
Animation.linear(durationS)                              // 线性
Animation.easeIn(durationS)                              // 加速
Animation.easeOut(durationS)                             // 减速
Animation.easeInOut(durationS)                           // 加速再减速
Animation.springLinear(durationS, damping, velocity)     // 弹簧（线性基础）
Animation.springEaseOut(durationS, damping, velocity)    // 弹簧（减速基础，常用）
animation.delay(0.1f)                                    // 延迟
animation.repeatForever(true)                            // 循环
```

---

## 9. 定时器

### 单次延迟（推荐）

```kotlin
// 在 Pager/ComposeView 内
setTimeout(300) {    // 300ms 后执行
    ctx.isVisible = false
}
```

### 重复执行

```kotlin
// 在类属性级
private val ticker = Timer()

override fun pageDidAppear() {
    ticker.schedule(delay = 0, period = 1000) {
        ctx.count++    // 每秒 +1
    }
}

override fun pageWillDestroy() {
    ticker.cancel()
}
```

---

## 10. 模块系统

### 路由（Router）

```kotlin
// 打开新页面
acquireModule<RouterModule>(ModuleConst.ROUTER)
    .openPage("TargetPage", JSONObject().apply {
        put("userId", "123")
    })

// 关闭当前页面
acquireModule<RouterModule>(ModuleConst.ROUTER).closePage()
```

### SharedPreferences（本地存储）

```kotlin
private val sp by lazy {
    acquireModule<SharedPreferencesModule>(ModuleConst.SHARED_PREFERENCES)
}

// 读写
sp.setString("key", "value")
val value = sp.getString("key")    // 不存在返回 ""

sp.setInt("count", 42)
val count = sp.getInt("count")     // 不存在返回 null
```

### 网络请求

```kotlin
acquireModule<NetworkModule>(ModuleConst.NETWORK).httpRequest(
    url = "https://api.example.com/data",
    isPost = false,
    param = JSONObject(),
    headers = JSONObject().apply { put("Authorization", "Bearer token") },
    timeout = 30
) { data, success, errorMsg, response ->
    if (success) {
        val json = data.optString("data")
        // 处理数据（注意：已在子线程，需切回主线程更新 UI）
    }
}
```

### 通知

```kotlin
// 添加监听
private var notifyRef: CallbackRef? = null

override fun pageDidAppear() {
    notifyRef = acquireModule<NotifyModule>(ModuleConst.NOTIFY)
        .addNotify("eventName") { data ->
            // 处理数据
        }
}

override fun pageWillDestroy() {
    notifyRef?.let {
        acquireModule<NotifyModule>(ModuleConst.NOTIFY).removeNotify("eventName", it)
    }
}

// 发送
acquireModule<NotifyModule>(ModuleConst.NOTIFY)
    .postNotify("eventName", JSONObject().apply { put("key", "value") })
```

---

## 11. ViewRef — 命令式视图访问

```kotlin
// 类属性
private var scrollerRef: ViewRef<ScrollerView<*, *>>? = null
private var inputRef: ViewRef<InputView>? = null

// body() 中绑定
Scroller {
    ref { scrollerRef = it }
    attr { flex(1f) }
}

Input {
    ref { inputRef = it }
    attr { placeholder("搜索...") }
}

// 命令式调用（view 可能为 null）
scrollerRef?.view?.setContentOffset(0f, 0f, animated = true)
inputRef?.view?.focus()
inputRef?.view?.blur()
```

---

## 12. 性能最佳实践

### 虚拟化列表

- 超过 20 条数据使用 `List { vforLazy() }` 而非 `Scroller { vfor() }`
- `List` 中固定高度的 item 性能最好，避免动态高度

### 避免不必要的重渲染

```kotlin
// ✅ 只有相关数据变化才更新这个 attr 块
Text {
    attr {
        text(ctx.title)        // 只订阅 title
        fontSize(16f)
    }
}

// ❌ 即使 count 变化，这个视图也会因 isSelected 变化而重跑
Text {
    attr {
        val selected = ctx.isSelected   // 订阅了 isSelected
        val cnt = ctx.count             // 同时订阅了 count
        text(if (selected) "选中" else "未选")
        color(if (selected) Color.RED else Color.GRAY)
    }
}
```

### Scroller padding vs 末尾 View

```kotlin
// ❌ paddingRight/paddingBottom 不延伸滚动内容
Scroller {
    attr { paddingRight(16f) }  // 无效！
}

// ✅ 末尾加空白 View
Scroller {
    attr { flexDirectionRow() }
    // ... 内容 ...
    View { attr { width(16f) } }  // 末尾留白
}
```

### keepAlive — 防止 List 内视图被回收

```kotlin
// 某些需要保持渲染状态的视图（如 WebView、视频）
List {
    vforLazy({ ctx.items }) { item, _, _ ->
        VideoPlayer {
            attr { keepAlive(true) }   // 滚出可视区也不销毁 RenderView
        }
    }
}
```

---

## 13. 颜色使用

```kotlin
// Long 十六进制（最常用）
Color(0xFF8B0000)       // ARGB，0xFF = 不透明
Color(0x80FF0000)       // 50% 透明红色

// 分量
Color(255, 0, 0, 1.0f)  // R=255, G=0, B=0, alpha=1.0

// 预定义常量
Color.WHITE / Color.BLACK / Color.RED / Color.TRANSPARENT / Color.GRAY

// 带透明度变体
Color(0xFF123456).opacity(0.5f)  // 返回新 Color，alpha = 0.5
```

---

## 14. 自定义 Native View 注册（Android）

在 `MainActivity` 中：

```kotlin
// 注册视图
export.renderViewExport("VideoRenderView") { context ->
    VideoRenderViewImpl(context)
}

// 注册模块
export.moduleExport("GalleryModule") { GalleryModuleExport(it) }
```

在 commonMain 中声明桥接类：

```kotlin
class HanziWebView : DeclarativeBaseView<Attr, Event>() {
    override fun viewName() = "HanziWebView"  // 与注册名一致

    // 调用 native 方法
    fun loadCharacter(char: String) {
        callRenderViewMethod("loadCharacter", char)
    }
}

fun ViewContainer<*, *>.HanziWeb(init: HanziWebView.() -> Unit) {
    addChild(HanziWebView(), init)
}
```

---

## 15. 常见坑汇总

| 问题 | 原因 | 解决 |
|-----|-----|-----|
| UI 不更新 | 状态变量未用 `observable` | 用 `by observable()` 声明 |
| `this` 编译错误 | body() lambda 内 this 歧义 | `val ctx = this` 在 lambda 外 |
| 横向滚动末尾被裁 | paddingRight 无效 | 加末尾空 View |
| List 子视图状态丢失 | List 自动回收 RenderView | 设置 `keepAlive(true)` |
| 文本不换行 | 未设置 `lines()` | `lines(0)` = 不限行数 |
| 动画卡顿 | 在主线程做重计算 | 子线程计算，主线程更新 |
| 找不到 `setInterval` | core 不存在此函数 | 用 `Timer().schedule()` |
| 模块 null | 未注册就 `acquireModule` | `getModule` 安全获取或先注册 |
| Scroller 高度无限 | 父容器未设高度/flex | 父容器设 `flex(1f)` 或固定 height |
| 键盘挡住输入框 | 未监听键盘高度 | `keyboardHeightChange` + 调整布局 |
