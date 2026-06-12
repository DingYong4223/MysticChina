# practise.html 练字专项页面 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新建 `assets/hanzi/practise.html`（仅含练字测验），让 `HanziWebView` 通过 `src` Attr 指定加载页面，`HanziPage` 改为加载 `practise.html`。

**Architecture:** 在 KuiklyUI 的 `DeclarativeBaseView` 层新增 `HanziWebAttr.src()`，通过 KuiklyUI prop 机制将文件名下传到 Android 端 `HanziWebViewImpl.setProp("src", …)`，移除 `init` 中的硬编码 `loadUrl`。`practise.html` 是 `index.html` 的裁剪版——保留字格选择和练字 section，去掉笔画动画 section 及 `animWriter` 代码。

**Tech Stack:** Kotlin Multiplatform / KuiklyUI DSL, Android WebView, HanziWriter 2.x (本地 JS + 汉字 JSON)

---

## 文件清单

| 操作 | 路径 |
|---|---|
| 新建 | `shared/src/commonMain/assets/hanzi/practise.html` |
| 已存在 | `shared/src/commonMain/assets/hanzi/index.html` |
| 已存在 | `shared/src/commonMain/assets/hanzi/hanzi-writer.min.js` |
| 已存在 | `shared/src/commonMain/assets/hanzi/data/*.json`（71 个） |
| 修改 | `shared/src/commonMain/kotlin/com/fula/exploringchina/hanzi/HanziWebView.kt` |
| 修改 | `androidApp/src/main/java/com/fula/exploringchina/android/view/HanziWebViewImpl.kt` |
| 修改 | `shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HanziPage.kt` |

---

## Task 1：清理重复资源（androidApp/assets）

`shared/src/commonMain/assets/hanzi/` 已包含所有资源（JS 库、71 个 JSON、index.html），由 shared/build.gradle.kts 的 `assets.srcDirs` 自动打包。
`androidApp/src/main/assets/hanzi/` 有重复的部分文件需要清理，以避免 AGP 合并时产生冲突。

**Files:**
- Delete contents: `androidApp/src/main/assets/hanzi/`

- [ ] **Step 1.1：清理 androidApp 下的重复 hanzi 资源**

```bash
rm -rf androidApp/src/main/assets/hanzi
```

- [ ] **Step 1.2：确认 shared 下资源完整**

```bash
echo "JS: $(ls shared/src/commonMain/assets/hanzi/hanzi-writer.min.js)"
echo "HTML: $(ls shared/src/commonMain/assets/hanzi/index.html)"
echo "Data: $(ls shared/src/commonMain/assets/hanzi/data/ | wc -l) files"
```

Expected:
```
JS: shared/src/commonMain/assets/hanzi/hanzi-writer.min.js
HTML: shared/src/commonMain/assets/hanzi/index.html
Data:       71 files
```

---

## Task 2：确认 `index.html` 已存在

`shared/src/commonMain/assets/hanzi/index.html` 在 Task 1 已就位，无需重新创建。验证其包含 `animation-target` 和 `quiz-target` 两个 section 即可。

**Files:** 无变更

- [ ] **Step 2.1：确认 index.html 包含两个 section**

```bash
grep -c "animation-target\|quiz-target" shared/src/commonMain/assets/hanzi/index.html
```

Expected: `2`（各出现一次）

---

## Task 3：新建 `practise.html`

仅包含练字测验 section，去掉笔画动画及相关代码。

**Files:**
- Create: `shared/src/commonMain/assets/hanzi/practise.html`

- [ ] **Step 3.1：写入 practise.html**

创建文件 `shared/src/commonMain/assets/hanzi/practise.html`，内容如下（注意：无 animation-target、无 animWriter、无动画按钮）：

```html
<!DOCTYPE html>
<html lang="zh">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>练字</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background: #1a1a1a; color: #f0f0f0;
      font-family: -apple-system, 'PingFang SC', sans-serif;
      display: flex; flex-direction: column; align-items: center;
      padding: 20px 16px; min-height: 100vh;
    }
    .char-row { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; width: 100%; max-width: 360px; }
    .char-label { font-size: 14px; color: #aaa; }
    .char-input { background: #2a2a2a; border: 1px solid #444; border-radius: 8px; color: #f0f0f0; font-size: 28px; text-align: center; padding: 8px; width: 56px; }
    .btn { background: #e85d04; color: #fff; border: none; border-radius: 8px; padding: 10px 18px; font-size: 14px; cursor: pointer; white-space: nowrap; }
    .btn:active { opacity: 0.8; }
    .btn-outline { background: transparent; border: 1px solid #555; color: #ccc; }
    .char-grid { display: flex; flex-wrap: wrap; gap: 6px; width: 100%; max-width: 360px; margin-bottom: 20px; }
    .char-chip { background: #2a2a2a; border: 1px solid #444; border-radius: 6px; color: #ddd; font-size: 18px; width: 38px; height: 38px; display: flex; align-items: center; justify-content: center; cursor: pointer; }
    .char-chip:active, .char-chip.active { background: #e85d04; border-color: #e85d04; color: #fff; }
    .section { width: 100%; max-width: 360px; margin-bottom: 28px; }
    .section-title { font-size: 13px; color: #888; letter-spacing: 1px; margin-bottom: 12px; }
    .writer-wrap { background: #242424; border-radius: 12px; display: flex; justify-content: center; align-items: center; padding: 12px; margin-bottom: 12px; }
    .controls { display: flex; gap: 8px; flex-wrap: wrap; }
    .hint { font-size: 12px; color: #666; margin-top: 8px; text-align: center; }
    .error-msg { color: #ff6b6b; font-size: 13px; text-align: center; margin-bottom: 12px; display: none; }
  </style>
</head>
<body>
  <div class="char-grid" id="char-grid"></div>
  <div class="char-row">
    <span class="char-label">练习</span>
    <input class="char-input" id="char-input" maxlength="1" value="我">
    <button class="btn" id="btn-update">更新</button>
  </div>
  <div class="error-msg" id="error-msg"></div>

  <div class="section">
    <div class="section-title">练字测验</div>
    <div class="writer-wrap"><div id="quiz-target"></div></div>
    <div class="controls">
      <button class="btn" id="btn-quiz-reset">↺ 重置</button>
      <button class="btn btn-outline" id="btn-outline-quiz">轮廓 开/关</button>
    </div>
    <div class="hint" id="quiz-hint">按笔画顺序描写汉字</div>
  </div>

  <script src="./hanzi-writer.min.js"></script>
  <script>
    var LOCAL_CHARS = '我你他她它们的是了在有不这中人国大来到说时要就出会可也对都而多去能下过子上用年地分家学以发方好那些生知等部被从问题为工作看日月水火山口手目耳心'.split('');
    var SIZE = 260;
    var quizWriter = null;
    var quizOutlineOn = true;

    function localLoader(char, onLoad, onError) {
      var xhr = new XMLHttpRequest();
      xhr.open('GET', './data/' + char + '.json', true);
      xhr.onload = function() {
        if (xhr.status === 200) { try { onLoad(JSON.parse(xhr.responseText)); } catch(e) { onError(e); } }
        else { onError(new Error('HTTP ' + xhr.status)); }
      };
      xhr.onerror = function(e) { onError(e); };
      xhr.send();
    }
    function clearEl(id) {
      var el = document.getElementById(id);
      el.innerHTML = ''; el.style.width = SIZE + 'px'; el.style.height = SIZE + 'px';
    }
    function showError(msg) { var el = document.getElementById('error-msg'); el.textContent = msg; el.style.display = 'block'; }
    function hideError() { document.getElementById('error-msg').style.display = 'none'; }

    function updateWriters(char) {
      var vw = window.innerWidth || window.screen.width || 360;
      if (vw < 100) vw = 360;
      SIZE = Math.max(Math.min(vw - 80, 280), 200);
      hideError(); clearEl('quiz-target');
      document.getElementById('quiz-hint').textContent = '按笔画顺序描写汉字';
      quizWriter = HanziWriter.create('quiz-target', char, {
        width: SIZE, height: SIZE, padding: 8,
        charDataLoader: localLoader,
        onLoadCharDataError: function() { showError('「' + char + '」暂无数据，请从上方选择常用字'); },
        strokeColor: '#e85d04', outlineColor: '#aaa', highlightColor: '#ff9f1c',
        showOutline: quizOutlineOn, showCharacter: false,
        showHintAfterMisses: 2, highlightOnComplete: true
      });
      quizWriter.quiz({ onComplete: function() { document.getElementById('quiz-hint').textContent = '🎉 写完了！按↺重新练习'; } });
      document.querySelectorAll('.char-chip').forEach(function(el) { el.classList.toggle('active', el.textContent === char); });
      document.getElementById('char-input').value = char;
    }

    var grid = document.getElementById('char-grid');
    LOCAL_CHARS.forEach(function(ch) {
      var chip = document.createElement('div'); chip.className = 'char-chip'; chip.textContent = ch;
      chip.addEventListener('click', function() { updateWriters(ch); }); grid.appendChild(chip);
    });
    document.getElementById('btn-update').addEventListener('click', function() { var ch = document.getElementById('char-input').value.trim(); if (ch) updateWriters(ch); });
    document.getElementById('char-input').addEventListener('keydown', function(e) { if (e.key === 'Enter') { var ch = this.value.trim(); if (ch) updateWriters(ch); } });
    document.getElementById('btn-quiz-reset').addEventListener('click', function() {
      if (quizWriter) { document.getElementById('quiz-hint').textContent = '按笔画顺序描写汉字';
        quizWriter.quiz({ onComplete: function() { document.getElementById('quiz-hint').textContent = '🎉 写完了！按↺重新练习'; } }); }
    });
    document.getElementById('btn-outline-quiz').addEventListener('click', function() { quizOutlineOn = !quizOutlineOn; if (quizWriter) quizOutlineOn ? quizWriter.showOutline() : quizWriter.hideOutline(); });

    window.onload = function() {
      var dpWidth = window.screen.width || window.screen.availWidth || 360;
      document.documentElement.style.width = dpWidth + 'px';
      document.body.style.width = dpWidth + 'px';
      document.body.style.maxWidth = 'none';
      setTimeout(function() { updateWriters('我'); }, 50);
    };
  </script>
</body>
</html>
```

- [ ] **Step 3.2：确认结构差异（可选核查）**

```bash
grep -c "animation-target\|animWriter\|btn-animate\|btn-outline-anim" \
  shared/src/commonMain/assets/hanzi/practise.html
```

Expected: `0`（practise.html 中不含任何动画相关标识符）

---

## Task 4：修改 `HanziWebView.kt` — 引入 `HanziWebAttr`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/fula/exploringchina/hanzi/HanziWebView.kt`

- [ ] **Step 4.1：替换文件内容**

将 `shared/src/commonMain/kotlin/com/fula/exploringchina/hanzi/HanziWebView.kt` 完整替换为：

```kotlin
package com.fula.exploringchina.hanzi

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.base.event.Event

/**
 * 汉字练习 WebView — 通过 Kuikly expand-native-view 机制桥接到平台 WebView。
 *
 * Android 侧实现：HanziWebViewImpl
 *
 * 用法：
 *   HanziWeb {
 *       attr {
 *           flex(1f)
 *           width(pageViewWidth)
 *           src("practise.html")   // 指定 assets/hanzi/ 下的 HTML 文件名
 *       }
 *   }
 */

/** 控制 HanziWebView 行为的属性类。 */
class HanziWebAttr : Attr() {
    /**
     * 指定要加载的 HTML 文件名（相对于 assets/hanzi/），例如 "practise.html"。
     * 通过 KuiklyUI prop 机制发往 Android 端，触发 HanziWebViewImpl.setProp("src", …)。
     */
    fun src(url: String): HanziWebAttr {
        "src" with url
        return this
    }
}

class HanziWebView : DeclarativeBaseView<HanziWebAttr, Event>() {
    override fun createAttr(): HanziWebAttr = HanziWebAttr()
    override fun createEvent(): Event = Event()
    override fun viewName(): String = "HanziWebView"
}

fun ViewContainer<*, *>.HanziWeb(init: HanziWebView.() -> Unit) {
    addChild(HanziWebView(), init)
}
```

- [ ] **Step 4.2：编译 shared 模块（快速验证）**

```bash
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -5
```

Expected: `BUILD SUCCESSFUL`

---

## Task 5：修改 `HanziWebViewImpl.kt` — 响应 `src` prop

**Files:**
- Modify: `androidApp/src/main/java/com/fula/exploringchina/android/view/HanziWebViewImpl.kt`

- [ ] **Step 5.1：替换文件内容**

将 `androidApp/src/main/java/com/fula/exploringchina/android/view/HanziWebViewImpl.kt` 完整替换为：

```kotlin
package com.fula.exploringchina.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

/**
 * 汉字练习 WebView — 加载 assets/hanzi/ 下的 HTML 文件。
 *
 * 注册名："HanziWebView"
 * 加载时机：KuiklyUI 完成 FlexBox 布局后调用 setProp("src", filename)，
 *           此时 WebView 已有正确的 Android 视图宽度，viewport 计算更准确。
 */
@SuppressLint("SetJavaScriptEnabled")
class HanziWebViewImpl(context: Context) : WebView(context), IKuiklyRenderViewExport {

    init {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            builtInZoomControls = false
            displayZoomControls = false
        }
        webViewClient = WebViewClient()
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        // loadUrl 不在此处调用 — 等待 KuiklyUI 通过 setProp("src", …) 触发
    }

    /**
     * 响应 "src" prop：加载 assets/hanzi/{value} 页面。
     * KuiklyUI 在完成布局后才调用此方法，WebView 届时已有正确宽度。
     */
    override fun setProp(propKey: String, propValue: Any): Boolean {
        if (propKey == "src") {
            loadUrl("file:///android_asset/hanzi/$propValue")
            return true
        }
        return false
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? = null
}
```

- [ ] **Step 5.2：编译 androidApp 模块验证**

```bash
./gradlew :androidApp:compileDebugKotlin 2>&1 | tail -5
```

Expected: `BUILD SUCCESSFUL`

---

## Task 6：修改 `HanziPage.kt` — 指定 `src("practise.html")`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HanziPage.kt`

- [ ] **Step 6.1：在 HanziWeb attr 块中加入 src**

找到文件中的 `HanziWeb` 调用块（当前约第 64 行），将其替换为：

```kotlin
            // ── 汉字 WebView（占满剩余高度） ─────────────────────
            // flex(1f) 填充高度；width 显式指定宽度（KuiklyUI 原生 View 不自动 stretch）
            // src 指定加载 assets/hanzi/ 下的 HTML 文件
            HanziWeb {
                attr {
                    flex(1f)
                    width(ctx.pagerData.pageViewWidth)
                    src("practise.html")
                }
            }
```

- [ ] **Step 6.2：编译 shared 模块验证类型正确**

```bash
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -5
```

Expected: `BUILD SUCCESSFUL`

---

## Task 7：构建 APK 并验证内容

**Files:** 无新增文件，输出 APK。

- [ ] **Step 7.1：完整构建**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -6
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7.2：验证 practise.html 打包进 APK**

```bash
APK=$(find . -path "*/outputs/apk/debug/*.apk" | head -1)
unzip -l "$APK" | grep "hanzi/"
```

Expected 输出应包含（至少，来源于 `shared/src/commonMain/assets/hanzi/`）：
```
assets/hanzi/hanzi-writer.min.js
assets/hanzi/index.html
assets/hanzi/practise.html
assets/hanzi/data/我.json
```

- [ ] **Step 7.3：验证 practise.html 不含动画标识符**

```bash
unzip -p "$APK" assets/hanzi/practise.html | grep -c "animation-target"
```

Expected: `0`

- [ ] **Step 7.4：安装到模拟器**

```bash
adb install -r "$APK" && \
adb shell am force-stop com.fula.exploringchina && \
sleep 1 && \
adb shell am start \
  -n "com.fula.exploringchina/com.fula.exploringchina.android.MainActivity" \
  --es pageName "HanziPage"
```

Expected: `Success` + 启动日志

- [ ] **Step 7.5：确认 practise.html 加载（logcat）**

```bash
sleep 4 && adb logcat -d | grep "chromium" | grep "CONSOLE\|practise" | tail -5
```

如 practise.html 有 console 输出则可见；无论如何，关键是**没有** `animation-target` 相关 DOM 错误。

- [ ] **Step 7.6：提交**

```bash
git add \
  shared/src/commonMain/assets/hanzi/ \
  shared/src/commonMain/kotlin/com/fula/exploringchina/hanzi/HanziWebView.kt \
  androidApp/src/main/java/com/fula/exploringchina/android/view/HanziWebViewImpl.kt \
  shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HanziPage.kt

git commit -m "feat: add practise.html quiz-only page and make HanziWebView src configurable"
```

---

## 验收检查表

完成所有 task 后，手动确认：

- [ ] HanziPage 打开后只显示"练字测验" section，无"笔画动画" section
- [ ] 字格点击后切换练习字，quiz 重置并立即开始
- [ ] 手动输入框输入汉字后点击"更新"，quiz 切换
- [ ] 按笔顺写完后出现 🎉 完成提示
- [ ] ↺ 重置按钮可重新开始
- [ ] `index.html` 在浏览器中可独立打开（双击文件）并正常显示动画和测验两个 section
