# HanziWebView 空白屏排查报告

**日期**: 2026-06-10
**涉及组件**: `HanziPage` → `HanziWebView` (DeclarativeBaseView) → `HanziWebViewImpl` (Android WebView)
**现象**: 进入"汉字练习"页面后，顶部导航栏渲染正常，但 WebView 区域完全空白。

---

## 问题 1：FRAME 布局属性未应用（核心回归点）

### 症状

WebView 被创建（可在 accessibility dump 中看到 `className: android.webkit.WebView`），但 `boundsInScreen` 始终为 `Rect(0, 142 - 0, 142)`，即 **width=0, height=0**。

### 调用链

```
Kuikly 布局引擎
  → DeclarativeBaseView.setFrameToRenderView(frame)
    → RenderView.setFrame(x, y, width, height)
      → BridgeManager.setRenderViewFrame(pagerId, viewRef, x, y, w, h)
        → KuiklyRenderLayerHandler.setRenderViewFrame(tag, frame)
          → setProp(tag, KRCssConst.FRAME, Rect(left, top, right, bottom))    // ①
            → HanziWebViewImpl.setProp("frame", rect)                          // ②
```

① `KuiklyRenderLayerHandler`: 将布局计算的 dp 值转为 px 后，以 `"frame"` prop 的形式发给 native view。

② `IKuiklyRenderViewExport` 的默认实现：
```kotlin
fun setProp(propKey: String, propValue: Any): Boolean =
    view().setCommonProp(propKey, propValue)
```
其中对 `KRCssConst.FRAME` 的处理是：
```kotlin
KRCssConst.FRAME -> {
    frame = value as Rect  // ← 真正设置 View 布局的地方
    hadSetFrame = true
    dispatchOnSetFrame(value)
}
```

### 根因

`HanziWebViewImpl.setProp` 对所有属性（包括 `"frame"`）返回 `false`，导致布局参数被丢弃。对比参考实现 `VideoRenderViewImpl`，它对非自定义属性调用 `super.setProp()`。

### 修复

```kotlin
// 错误：丢弃所有非 "src" 属性
override fun setProp(propKey: String, propValue: Any): Boolean {
    if (propKey == "src") { ...; return true }
    return false  // ← frame 被丢弃，WebView 0x0
}

// 正确：FRAME 等布局属性交给基类
override fun setProp(propKey: String, propValue: Any): Boolean {
    if (propKey == "src") { ...; return true }
    return super.setProp(propKey, propValue)  // ← 转发给 setCommonProp
}
```

---

## 问题 2：file:// 协议下 XHR 被 CORS 拦截

### 症状

HTML 页面加载成功（`practise.html` 结构渲染），但 HanziWriter 无法加载字符笔顺数据。控制台报错：

```
Access to XMLHttpRequest at 'file:///android_asset/hanzi/data/我.json'
from origin 'null' has been blocked by CORS policy:
Cross origin requests are only supported for protocol schemes:
chrome, chrome-untrusted, data, http, https.
```

### 原因

1. `practise.html` 通过 `XMLHttpRequest` 加载 `./data/{char}.json` 字符数据。
2. WebView 加载方式为 `loadUrl("file:///android_asset/hanzi/practise.html")` → origin 为 `null`。
3. Chromium（Android WebView 133）安全策略：**`file://` 协议的 origin 为 `null`，不允许向 `file://` 发起 XHR**。
4. `allowFileAccessFromFileURLs` / `allowUniversalAccessFromFileURLs` 在 **API 30 弃用，API 31+ 静默忽略**。模拟器运行 API 36 → 这两个 WebView setting 无效。

### 修复方案

#### 方案：虚拟 HTTP origin + shouldInterceptRequest

**思路**：给页面一个 HTTP 协议的 base URL，使 XHR 变为同源请求，再用 `shouldInterceptRequest` 拦截虚拟域名的请求并从本地 assets 返回真实数据。

**实现**：

```kotlin
// 1. 用 loadDataWithBaseURL 替代 loadUrl
const val BASE_URL = "http://hanzi/"
val html = context.assets.open("hanzi/$filename").bufferedReader().use { it.readText() }
loadDataWithBaseURL(BASE_URL, html, "text/html", "UTF-8", null)

// 2. 拦截虚拟域名下的全部请求
webViewClient = object : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (url.startsWith(BASE_URL)) {
            val assetPath = "hanzi/" + URLDecoder.decode(url.removePrefix(BASE_URL), "UTF-8")
            val mimeType = when {
                assetPath.endsWith(".js") -> "application/javascript"
                assetPath.endsWith(".json") -> "application/json"
                else -> "application/octet-stream"
            }
            return WebResourceResponse(mimeType, "UTF-8", context.assets.open(assetPath))
        }
        return super.shouldInterceptRequest(view, request)
    }
}
```

**关键细节**：
- `loadDataWithBaseURL` 设置的 base URL 会成为页面的 origin（`http://hanzi/`）
- XHR 请求 `./data/我.json` 解析为 `http://hanzi/data/我.json` → 同源，CORS 放行
- `shouldInterceptRequest` 拦截 `http://hanzi/` 请求，从 `assets/` 返回真实文件
- URL 路径中的中文字符是 URL 编码的（`%E6%88%91`），需用 `URLDecoder.decode()` 还原后打开 asset

---

## 验证结果

| 检查项 | 修复前 | 修复后 |
|--------|--------|--------|
| WebView bounds | `[0,142][0,142]`（0×0） | `[0,397][1080,2361]`（1080×1964） |
| CORS / XHR 错误 | `blocked by CORS policy` | 无 |
| `HanziWriter` 引用错误 | `ReferenceError: HanziWriter is not defined` | 无 |
| 字符数据加载错误 | `Failed to load character data` | 无 |

---

## 涉及的文件

| 文件 | 变更说明 |
|------|----------|
| `androidApp/.../view/HanziWebViewImpl.kt` | `setProp` 增加 FRAME 转发；`loadDataWithBaseURL` + `shouldInterceptRequest` |
| `shared/.../hanzi/HanziWebView.kt` | 新增 `HanziWebAttr`，定义 `src()` 属性 |
| `shared/.../pages/HanziPage.kt` | 新增 `src("practise.html")` 属性设置 |
| `shared/src/commonMain/assets/hanzi/practise.html` | 新建练字页面 |
| `shared/src/commonMain/assets/hanzi/data/*.json` | 73 个常用汉字的笔顺数据 |

---

## 后续注意

1. **Kuikly DeclarativeBaseView 的 setProp 不能直接 return false**——必须转发给 `super.setProp`，否则 FRAME、OPACITY、BACKGROUND 等标准属性全部失效。
2. **file:// XHR 跨域在 API 31+ 彻底不可用**，不要依赖 `allowFileAccessFromFileURLs`。同等的本地数据加载需求需走 `loadDataWithBaseURL` + `shouldInterceptRequest` 方案。
3. **Kotlin KDoc 中的 `/*` 会被解析为嵌套块注释**，在文档注释中书写 `data/*.json` 会导致编译错误 `Unclosed comment`。需改为 `data/*.json` 或 `data 目录的 JSON 文件`。
