# ExploringChina 包名变更 + 首页重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 yijian 框架代码改造为 ExploringChina，全局包名改为 `com.fula.exploringchina`，首页重构为探索中国文化卡片网格，首张卡片为"汉字练习"入口。

**Architecture:** 所有 Kotlin 源文件包路径从 `com.yijian` 迁移到 `com.fula.exploringchina`，目录结构保持一致。首页 `HomePage` 的 Tab 枚举和内容组件全部替换，新增 `HanziPage` 占位页。主题色从蓝紫系改为中国红/金色系。

**Tech Stack:** Kotlin Multiplatform、Kuikly UI DSL、Android Gradle (AGP 8.2.2)

---

## 文件变更地图

### 修改（内容变更）
- `settings.gradle.kts` — rootProject.name
- `shared/build.gradle.kts` — namespace
- `androidApp/build.gradle.kts` — namespace + applicationId
- `androidApp/src/main/AndroidManifest.xml` — activity 全类名、app label
- `androidApp/src/main/res/values/styles.xml` — theme name
- `shared/src/commonMain/kotlin/com/yijian/theme/Colors.kt` — 主题色（仅色值）
- `shared/src/commonMain/kotlin/com/yijian/util/Constants.kt` — APP_NAME、新增 PAGE_HANZI
- `shared/src/commonMain/kotlin/com/yijian/pages/SplashPage.kt` — 品牌文案
- `shared/src/commonMain/kotlin/com/yijian/pages/HomePage.kt` — Tab 重构 + ExploreTabContent

### 新建
- `shared/src/commonMain/kotlin/com/yijian/pages/HanziPage.kt` — 汉字练习占位页

### 包路径迁移（所有 .kt 文件 package 声明 + import）
所有 `com.yijian` → `com.fula.exploringchina`，目录结构同步重命名。
涉及目录：
- `shared/src/commonMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `shared/src/androidMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `shared/src/appleMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `shared/src/iosMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `androidApp/src/main/java/com/yijian/android/` → `com/fula/exploringchina/`

---

## Task 1: Gradle 配置 + AndroidManifest 更新

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `shared/build.gradle.kts`
- Modify: `androidApp/build.gradle.kts`
- Modify: `androidApp/src/main/AndroidManifest.xml`
- Modify: `androidApp/src/main/res/values/styles.xml`

- [ ] **Step 1: 修改 settings.gradle.kts**

将 `rootProject.name = "yijian"` 改为：
```kotlin
rootProject.name = "exploringchina"
```

- [ ] **Step 2: 修改 shared/build.gradle.kts — namespace**

找到 `android {` 块中的 `namespace`，改为：
```kotlin
android {
    namespace = "com.fula.exploringchina"
    compileSdk = 34
    // ... 其余不变
}
```

- [ ] **Step 3: 修改 androidApp/build.gradle.kts**

```kotlin
android {
    namespace = "com.fula.exploringchina"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.fula.exploringchina"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    // ... 其余不变
}
```

- [ ] **Step 4: 修改 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

    <application
        android:allowBackup="true"
        android:label="探索中国"
        android:supportsRtl="true"
        android:theme="@style/Theme.ExploringChina">

        <activity
            android:name="com.fula.exploringchina.MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 5: 修改 styles.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.ExploringChina" parent="Theme.AppCompat.NoActionBar">
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowNoTitle">true</item>
        <item name="android:statusBarColor">@android:color/black</item>
        <item name="android:navigationBarColor">@android:color/black</item>
    </style>
</resources>
```

- [ ] **Step 6: Commit**

```bash
git add settings.gradle.kts shared/build.gradle.kts androidApp/build.gradle.kts \
        androidApp/src/main/AndroidManifest.xml androidApp/src/main/res/values/styles.xml
git commit -m "chore: update gradle namespace, appId, manifest to com.fula.exploringchina"
```

---

## Task 2: Kotlin 包路径迁移（目录重命名 + 全局替换）

**Files:** 所有 `shared/` 和 `androidApp/` 下的 `.kt` 文件

- [ ] **Step 1: 创建新目录结构**

```bash
cd /Users/delanding/ProjDoing/TDF/ExploringChina

# shared commonMain
mkdir -p shared/src/commonMain/kotlin/com/fula/exploringchina

# shared androidMain
mkdir -p shared/src/androidMain/kotlin/com/fula/exploringchina

# shared appleMain
mkdir -p shared/src/appleMain/kotlin/com/fula/exploringchina

# shared iosMain
mkdir -p shared/src/iosMain/kotlin/com/fula/exploringchina

# androidApp
mkdir -p androidApp/src/main/java/com/fula/exploringchina
```

- [ ] **Step 2: 移动 commonMain 源文件**

```bash
cp -r shared/src/commonMain/kotlin/com/yijian/. shared/src/commonMain/kotlin/com/fula/exploringchina/
```

- [ ] **Step 3: 移动其余平台源文件**

```bash
cp -r shared/src/androidMain/kotlin/com/yijian/. shared/src/androidMain/kotlin/com/fula/exploringchina/
cp -r shared/src/appleMain/kotlin/com/yijian/. shared/src/appleMain/kotlin/com/fula/exploringchina/
cp -r shared/src/iosMain/kotlin/com/yijian/. shared/src/iosMain/kotlin/com/fula/exploringchina/
cp -r androidApp/src/main/java/com/yijian/android/. androidApp/src/main/java/com/fula/exploringchina/
```

- [ ] **Step 4: 全局替换 package/import 声明**

```bash
# 替换所有 .kt 文件中的包声明和 import
find shared/src/*/kotlin/com/fula/exploringchina androidApp/src/main/java/com/fula/exploringchina \
  -name "*.kt" \
  -exec sed -i '' 's/package com\.yijian/package com.fula.exploringchina/g' {} \;

find shared/src/*/kotlin/com/fula/exploringchina androidApp/src/main/java/com/fula/exploringchina \
  -name "*.kt" \
  -exec sed -i '' 's/import com\.yijian\./import com.fula.exploringchina./g' {} \;
```

- [ ] **Step 5: 删除旧包目录**

```bash
rm -rf shared/src/commonMain/kotlin/com/yijian
rm -rf shared/src/androidMain/kotlin/com/yijian
rm -rf shared/src/appleMain/kotlin/com/yijian
rm -rf shared/src/iosMain/kotlin/com/yijian
rm -rf androidApp/src/main/java/com/yijian
```

- [ ] **Step 6: 验证替换完整性**

```bash
# 应该输出 0 行（无残留旧包名）
grep -r "com\.yijian" shared/src/*/kotlin/com/fula androidApp/src/main/java/com/fula --include="*.kt" | wc -l
```

预期输出：`0`

- [ ] **Step 7: 验证编译**

```bash
./gradlew :shared:compileKotlinAndroid 2>&1 | tail -20
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor: migrate package com.yijian → com.fula.exploringchina"
```

---

## Task 3: 主题色 + 常量更新

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/fula/exploringchina/theme/Colors.kt`
- Modify: `shared/src/commonMain/kotlin/com/fula/exploringchina/util/Constants.kt`

- [ ] **Step 1: 更新 Colors.kt — 中国红/金色系**

完整替换文件内容：
```kotlin
package com.fula.exploringchina.theme

import com.tencent.kuikly.core.base.Color

/**
 * 探索中国应用主题色板
 * 深色主题 — 中国红/金色系
 */
object YijianColors {

    // 背景色（保持深色）
    val background = Color(0xFF1A1A1A)
    val backgroundLight = Color(0xFF2A2A2A)
    val surface = Color(0xFF333333)
    val surfaceLight = Color(0xFF3D3D3D)

    // 主色 — 中国红/金色系
    val primary = Color(0xFFE8352A)          // 中国红
    val primaryDark = Color(0xFFB82820)
    val accent = Color(0xFFF5A623)           // 金黄
    val gradientStart = Color(0xFFE8352A)    // 中国红
    val gradientEnd = Color(0xFFF5A623)      // 金黄

    // 文字
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xB3FFFFFF)
    val textTertiary = Color(0x80FFFFFF)
    val textDisabled = Color(0x4DFFFFFF)

    // 功能色
    val error = Color(0xFFFF4759)
    val warning = Color(0xFFFFB340)
    val success = Color(0xFF00C853)

    // 透明度覆盖
    val overlay = Color(0x80000000)
    val overlayLight = Color(0x40000000)

    // 控制栏
    val controlBarBg = Color(0xCC1A1A1A)
    val progressTrack = Color(0x4DFFFFFF)
    val progressFill = Color(0xFFE8352A)
    val progressThumb = Color(0xFFFFFFFF)
}
```

- [ ] **Step 2: 更新 Constants.kt — 添加 PAGE_HANZI，更新 APP_NAME**

```kotlin
package com.fula.exploringchina.util

/**
 * 工具常量
 */
object Constants {
    const val APP_NAME = "探索中国"
    const val PAGE_MAIN = "MainPage"
    const val PAGE_PREVIEW = "PreviewPage"
    const val PAGE_SPLASH = "SplashPage"
    const val PAGE_HANZI = "HanziPage"

    // Mock视频数据路径前缀
    const val MOCK_VIDEO_PREFIX = "file:///storage/emulated/0/Movies/"
}

/**
 * 时间格式化工具
 */
object FormatUtil {

    /**
     * 将毫秒格式化为 mm:ss
     */
    fun formatDuration(millis: Long): String {
        if (millis <= 0) return "00:00"
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return buildString {
            if (minutes < 10) append('0')
            append(minutes)
            append(':')
            if (seconds < 10) append('0')
            append(seconds)
        }
    }

    /**
     * 将毫秒格式化为 hh:mm:ss
     */
    fun formatDurationLong(millis: Long): String {
        if (millis <= 0) return "00:00:00"
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return buildString {
            if (hours < 10) append('0')
            append(hours)
            append(':')
            if (minutes < 10) append('0')
            append(minutes)
            append(':')
            if (seconds < 10) append('0')
            append(seconds)
        }
    }
}
```

- [ ] **Step 3: 验证编译**

```bash
./gradlew :shared:compileKotlinAndroid 2>&1 | tail -5
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/fula/exploringchina/theme/Colors.kt \
        shared/src/commonMain/kotlin/com/fula/exploringchina/util/Constants.kt
git commit -m "feat: update theme colors to Chinese red/gold, add PAGE_HANZI constant"
```

---

## Task 4: SplashPage 品牌文案更新

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/fula/exploringchina/pages/SplashPage.kt`

- [ ] **Step 1: 更新 SplashPage.kt**

完整替换文件内容：
```kotlin
package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

private const val TAG = "SplashPage"

/**
 * 启动页 — 品牌展示 → 自动跳转主页
 */
@Page("SplashPage", supportInLocal = true)
internal class SplashPage : BasePager() {

    override fun created() {
        super.created()
        KLog.i(TAG, "SplashPage created — 1.8s 后跳转 HomePage")
        setTimeout(1800) {
            KLog.i(TAG, "跳转 → HomePage")
            jumpPage("HomePage")
        }
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(TAG, "SplashPage 已显示")
    }

    override fun pageDidDisappear() {
        super.pageDidDisappear()
        KLog.i(TAG, "SplashPage 已离开")
    }

    override fun body(): ViewBuilder {
        return {
            attr {
                backgroundColor(YijianColors.background)
            }

            // 居中容器
            View {
                attr {
                    flex(1f)
                    allCenter()
                    flexDirectionColumn()
                }

                // Logo 图标
                View {
                    attr {
                        size(90f, 90f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        borderRadius(22f)
                        allCenter()
                    }
                    Text {
                        attr {
                            text("探")
                            fontSize(42f)
                            color(YijianColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                }

                // 应用名称
                Text {
                    attr {
                        marginTop(YijianTheme.Spacing.xl)
                        text("探索中国")
                        fontSize(YijianTheme.FontSize.display)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                    }
                }

                // 副标题
                Text {
                    attr {
                        marginTop(YijianTheme.Spacing.sm)
                        text("探索中华文化之美")
                        fontSize(YijianTheme.FontSize.body)
                        color(YijianColors.textSecondary)
                    }
                }
            }

            // 底部版本
            Text {
                attr {
                    absolutePosition(bottom = 40f, left = 0f, right = 0f)
                    textAlignCenter()
                    text("v1.0.0 · Powered by KuiklyUI")
                    fontSize(YijianTheme.FontSize.caption)
                    color(YijianColors.textTertiary)
                }
            }
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew :shared:compileKotlinAndroid 2>&1 | tail -5
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/fula/exploringchina/pages/SplashPage.kt
git commit -m "feat: update SplashPage branding to ExploringChina"
```

---

## Task 5: 新建 HanziPage 占位页

**Files:**
- Create: `shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HanziPage.kt`

- [ ] **Step 1: 创建 HanziPage.kt**

```kotlin
package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * 汉字练习页 — 占位，待后续实现
 */
@Page("HanziPage", supportInLocal = true)
internal class HanziPage : BasePager() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }

            // 顶部导航栏
            View {
                attr {
                    height(YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(YijianColors.background)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                    paddingLeft(YijianTheme.Spacing.lg)
                    paddingRight(YijianTheme.Spacing.lg)
                }
                // 返回按钮
                View {
                    attr {
                        size(40f, 40f)
                        allCenter()
                        marginRight(YijianTheme.Spacing.sm)
                    }
                    event { click { ctx.closePage() } }
                    Text {
                        attr {
                            text("‹")
                            fontSize(28f)
                            color(YijianColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                }
                Text {
                    attr {
                        text("汉字练习")
                        fontSize(YijianTheme.FontSize.title)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                        flex(1f)
                    }
                }
            }

            // 内容区 — 占位
            View {
                attr { flex(1f); allCenter(); flexDirectionColumn() }
                Text { attr { text("🖊"); fontSize(64f); marginBottom(YijianTheme.Spacing.xl) } }
                Text {
                    attr {
                        text("汉字练习")
                        fontSize(YijianTheme.FontSize.largeTitle)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                        marginBottom(YijianTheme.Spacing.md)
                    }
                }
                Text {
                    attr {
                        text("精彩内容即将上线")
                        fontSize(YijianTheme.FontSize.body)
                        color(YijianColors.textSecondary)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew :shared:compileKotlinAndroid 2>&1 | tail -5
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HanziPage.kt
git commit -m "feat: add HanziPage placeholder"
```

---

## Task 6: HomePage 重构 — 文化卡片首页

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HomePage.kt`

- [ ] **Step 1: 完整替换 HomePage.kt**

```kotlin
package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.model.UserProfile
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme
import com.fula.exploringchina.util.Constants

private const val SP_NICKNAME = "exploringchina_nickname"
private const val SP_BIO = "exploringchina_bio"
private const val SP_AVATAR = "exploringchina_avatar"

private enum class HomeTab(val label: String, val icon: String) {
    EXPLORE("探索", "🧭"),
    LEARN("学习", "📚"),
    PROFILE("我的", "👤")
}

/** 文化功能卡片数据 */
private data class CultureCard(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val pageName: String?,   // null = 即将上线，禁用点击
)

private val CULTURE_CARDS = listOf(
    CultureCard("🖊", "汉字练习", "写好每一笔", Constants.PAGE_HANZI),
    CultureCard("🔜", "更多功能", "即将上线", null),
)

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    var selectedTab by observable(0)
    var userProfile by observable(UserProfile())
    var showEditNickname by observable(false)
    var showEditBio by observable(false)
    var editingText by observable("")

    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }

    override fun created() {
        super.created()
        userProfile = UserProfile(
            nickname = sp.getString(SP_NICKNAME) ?: "文化探索者",
            bio = sp.getString(SP_BIO) ?: "探索中华文化之美",
            avatarEmoji = sp.getString(SP_AVATAR) ?: "🧭"
        )
    }

    private fun saveNickname() {
        val newVal = editingText.trim()
        if (newVal.isNotEmpty()) {
            userProfile = userProfile.copy(nickname = newVal)
            sp.setString(SP_NICKNAME, newVal)
        }
        showEditNickname = false
        editingText = ""
    }

    private fun saveBio() {
        val newVal = editingText.trim()
        if (newVal.isNotEmpty()) {
            userProfile = userProfile.copy(bio = newVal)
            sp.setString(SP_BIO, newVal)
        }
        showEditBio = false
        editingText = ""
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }

            View {
                attr { flex(1f); flexDirectionColumn() }
                vif({ ctx.selectedTab == 0 }) { ExploreTabContent(ctx) }
                vif({ ctx.selectedTab == 1 }) { LearnTabContent() }
                vif({ ctx.selectedTab == 2 }) { ProfileTabContent(ctx) }
            }

            View { attr { height(1f); backgroundColor(YijianColors.surfaceLight) } }

            BottomTabBar(ctx)

            vif({ ctx.showEditNickname }) {
                EditOverlay(
                    ctx, "修改昵称", ctx.userProfile.nickname,
                    { ctx.saveNickname() },
                    { ctx.showEditNickname = false; ctx.editingText = "" }
                )
            }
            vif({ ctx.showEditBio }) {
                EditOverlay(
                    ctx, "修改简介", ctx.userProfile.bio,
                    { ctx.saveBio() },
                    { ctx.showEditBio = false; ctx.editingText = "" }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 底部 Tab Bar
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.BottomTabBar(ctx: HomePage) {
    View {
        attr {
            height(56f + ctx.pagerData.safeAreaInsets.bottom)
            backgroundColor(YijianColors.background)
            flexDirectionRow()
            alignItemsCenter()
            paddingBottom(ctx.pagerData.safeAreaInsets.bottom)
        }
        HomeTab.values().forEachIndexed { index, tab ->
            val selected = ctx.selectedTab == index
            View {
                attr { flex(1f); height(56f); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter() }
                event { click { ctx.selectedTab = index } }
                View {
                    attr {
                        size(4f, 4f); borderRadius(2f)
                        backgroundColor(if (selected) YijianColors.primary else Color(0x00000000))
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.icon); fontSize(22f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.label); fontSize(10f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        if (selected) fontWeightSemiBold() else fontWeightNormal()
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 探索 Tab — 文化功能卡片网格
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ExploreTabContent(ctx: HomePage) {
    View {
        attr { flex(1f); flexDirectionColumn() }

        // 顶部标题栏
        View {
            attr {
                height(YijianTheme.BarHeight.topBar + ctx.pagerData.statusBarHeight)
                backgroundColor(YijianColors.background)
                flexDirectionRow()
                alignItemsCenter()
                paddingTop(ctx.pagerData.statusBarHeight)
                paddingLeft(YijianTheme.Spacing.lg)
                paddingRight(YijianTheme.Spacing.lg)
            }
            Text {
                attr {
                    text("探索中国文化")
                    fontSize(YijianTheme.FontSize.title)
                    color(YijianColors.textPrimary)
                    fontWeightBold()
                    flex(1f)
                }
            }
        }

        // 卡片网格
        val cardMargin = YijianTheme.Spacing.sm
        val cardSize = (ctx.pagerData.pageViewWidth - cardMargin * 3) / 2f

        Scroller {
            attr { flex(1f); flexDirectionColumn(); paddingTop(YijianTheme.Spacing.md) }
            View {
                attr {
                    flexDirectionRow()
                    flexWrapWrap()
                    paddingLeft(cardMargin)
                    paddingRight(cardMargin)
                }
                CULTURE_CARDS.forEach { card ->
                    CultureCardView(ctx, card, cardSize, cardMargin)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 文化功能卡片
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.CultureCardView(
    ctx: HomePage,
    card: CultureCard,
    cardSize: Float,
    margin: Float
) {
    val enabled = card.pageName != null
    View {
        attr {
            size(cardSize, cardSize)
            margin(margin / 2)
            borderRadius(YijianTheme.Radius.xl)
            overflow(true)
            flexDirectionColumn()
            allCenter()
            if (enabled) {
                backgroundLinearGradient(
                    Direction.TO_BOTTOM_RIGHT,
                    ColorStop(YijianColors.gradientStart, 0f),
                    ColorStop(YijianColors.gradientEnd, 1f)
                )
            } else {
                backgroundColor(YijianColors.surface)
            }
        }
        if (enabled) {
            event { click { ctx.jumpPage(card.pageName!!) } }
        }
        Text {
            attr {
                text(card.emoji)
                fontSize(48f)
                marginBottom(YijianTheme.Spacing.md)
            }
        }
        Text {
            attr {
                text(card.title)
                fontSize(YijianTheme.FontSize.subtitle)
                fontWeightBold()
                color(if (enabled) YijianColors.textPrimary else YijianColors.textTertiary)
                marginBottom(YijianTheme.Spacing.xs)
            }
        }
        Text {
            attr {
                text(card.subtitle)
                fontSize(YijianTheme.FontSize.small)
                color(if (enabled) YijianColors.textSecondary else YijianColors.textDisabled)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 学习 Tab（占位）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.LearnTabContent() {
    View {
        attr { flex(1f); backgroundColor(YijianColors.background); allCenter(); flexDirectionColumn() }
        Text { attr { text("📚"); fontSize(48f); marginBottom(16f) } }
        Text { attr { text("即将上线"); fontSize(16f); color(YijianColors.textPrimary); marginBottom(8f) } }
        Text { attr { text("学习内容正在精心准备中..."); fontSize(12f); color(YijianColors.textSecondary) } }
    }
}

// ═══════════════════════════════════════════════════════════
// 我的 Tab
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ProfileTabContent(ctx: HomePage) {
    Scroller {
        attr { flex(1f); backgroundColor(YijianColors.background); flexDirectionColumn(); paddingTop(YijianTheme.Spacing.xxl) }
        View { attr { allCenter(); flexDirectionColumn() }
            View {
                attr { size(80f, 80f); borderRadius(40f); backgroundColor(YijianColors.surface); allCenter() }
                Text { attr { text(ctx.userProfile.avatarEmoji); fontSize(36f) } }
            }
        }
        View {
            attr { allCenter(); marginBottom(8f); flexDirectionRow(); justifyContentCenter(); alignItemsCenter() }
            event { click { ctx.editingText = ctx.userProfile.nickname; ctx.showEditNickname = true } }
            Text { attr { text(ctx.userProfile.nickname); fontSize(20f); fontWeightBold(); color(YijianColors.textPrimary) } }
            Text { attr { text(" ✏"); fontSize(14f); color(YijianColors.textTertiary) } }
        }
        View {
            attr { allCenter(); marginBottom(YijianTheme.Spacing.xl); paddingLeft(32f); paddingRight(32f) }
            event { click { ctx.editingText = ctx.userProfile.bio; ctx.showEditBio = true } }
            Text { attr { text(ctx.userProfile.bio); fontSize(14f); color(YijianColors.textSecondary); textAlignCenter(); lines(3) } }
        }
        View {
            attr {
                height(1f); backgroundColor(YijianColors.surfaceLight)
                marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg)
            }
        }
        listOf("⚙  设置", "📱  关于探索中国").forEach { label ->
            View {
                attr {
                    height(52f); paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg)
                    flexDirectionRow(); alignItemsCenter(); backgroundColor(YijianColors.background)
                }
                Text { attr { text(label); fontSize(14f); color(YijianColors.textPrimary); flex(1f) } }
                Text { attr { text(">"); fontSize(14f); color(YijianColors.textTertiary) } }
                View {
                    attr {
                        absolutePosition(bottom = 0f, left = YijianTheme.Spacing.lg, right = 0f)
                        height(1f); backgroundColor(YijianColors.surfaceLight)
                    }
                }
            }
        }
        View { attr { height(YijianTheme.Spacing.xxxl) } }
    }
}

// ═══════════════════════════════════════════════════════════
// 编辑覆盖层
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.EditOverlay(
    ctx: HomePage,
    title: String,
    placeholder: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    View {
        attr { absolutePositionAllZero(); backgroundColor(Color(0xCC000000)); allCenter() }
        event { click { onDismiss() } }
        View {
            attr {
                width(ctx.pagerData.pageViewWidth - 48f)
                backgroundColor(YijianColors.surface); borderRadius(16f)
                padding(all = YijianTheme.Spacing.lg); flexDirectionColumn()
            }
            event { click {} }
            Text {
                attr { text(title); fontSize(16f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(YijianTheme.Spacing.md) }
            }
            View {
                attr { flexDirectionRow(); justifyContentFlexEnd(); marginTop(YijianTheme.Spacing.lg) }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundColor(YijianColors.surfaceLight); borderRadius(YijianTheme.Radius.md)
                    }
                    event { click { onDismiss() } }
                    Text { attr { text("取消"); fontSize(14f); color(YijianColors.textPrimary) } }
                }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        borderRadius(YijianTheme.Radius.md); marginLeft(12f)
                    }
                    event { click { onConfirm() } }
                    Text { attr { text("保存"); fontSize(14f); color(YijianColors.textPrimary); fontWeightBold() } }
                }
            }
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew :shared:compileKotlinAndroid 2>&1 | tail -5
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: 完整 APK 构建验证**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -10
```

预期：`BUILD SUCCESSFUL` 并生成 APK 文件。

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HomePage.kt
git commit -m "feat: redesign HomePage with cultural card grid and HanziPage entry"
```

---

## Task 7: 推送到 GitHub

- [ ] **Step 1: 推送所有提交**

```bash
git push origin main
```

预期：所有 commits 成功推送到 `https://github.com/DingYong4223/ExploringChina`。

- [ ] **Step 2: 更新 CLAUDE.md 包名说明**

修改 `CLAUDE.md` 中 "Package & Namespace" 一节：

```markdown
## Package & Namespace

- Shared Kotlin package: `com.fula.exploringchina`
- Android app ID: `com.fula.exploringchina`
- iOS framework: `shared` (static, CocoaPods)
- `rootProject.name` is `"exploringchina"` in `settings.gradle.kts`
```

- [ ] **Step 3: Commit + Push**

```bash
git add CLAUDE.md
git commit -m "docs: update CLAUDE.md package namespace"
git push origin main
```
