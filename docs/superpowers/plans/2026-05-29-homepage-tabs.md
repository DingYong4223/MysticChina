# 首页三 Tab 框架实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建由「剪辑 / 学习 / 我的」三个 Tab 组成的首页，替换 MainPage 在导航中的位置，并实现可本地持久化的用户资料页。

**Architecture:** 单 Pager（`HomePage`）内部通过 `selectedTab: Int` observable 状态切换三段内容区域，底部固定 Tab Bar 不发生页面跳转。用户资料通过 KuiklyUI 内置的 `SharedPreferencesModule` 持久化（无需 `expect/actual`，跨平台开箱即用）。

**Tech Stack:** KuiklyUI DSL, `observable`/`observableList`, `vif`, `SharedPreferencesModule`, `Input.textDidChange`

> **API 来源：** 经 KuiklyUI-AI (`skills/kuikly-ui-framework`) 文档验证，所有 API 均来自官方文档。

---

## 文件映射

| 操作 | 文件路径 | 职责 |
|------|---------|------|
| 新建 | `commonMain/kotlin/com/yijian/model/UserProfile.kt` | 用户资料数据类 |
| 新建 | `commonMain/kotlin/com/yijian/pages/HomePage.kt` | 3-Tab 首页完整实现 |
| 修改 | `commonMain/kotlin/com/yijian/pages/SplashPage.kt` | 跳转目标 `"MainPage"` → `"HomePage"` |

---

## Task 1：UserProfile 数据类

**Files:**
- Create: `shared/src/commonMain/kotlin/com/yijian/model/UserProfile.kt`

- [ ] **Step 1：创建数据类文件**

```kotlin
// shared/src/commonMain/kotlin/com/yijian/model/UserProfile.kt
package com.yijian.model

/**
 * 用户资料数据模型。
 * 存储使用 KuiklyUI 内置 SharedPreferencesModule（跨平台，无需 expect/actual）。
 *
 * 存储键：
 *   yijian_nickname   → String
 *   yijian_bio        → String
 *   yijian_avatar     → String
 */
data class UserProfile(
    val nickname: String = "创作者",
    val bio: String = "记录生活的每一刻",
    val avatarEmoji: String = "🎬"
)
```

- [ ] **Step 2：验证编译**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -4
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3：提交**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
git add shared/src/commonMain/kotlin/com/yijian/model/UserProfile.kt
git commit -m "feat: add UserProfile data class"
```

---

## Task 2：HomePage 完整实现

**Files:**
- Create: `shared/src/commonMain/kotlin/com/yijian/pages/HomePage.kt`

> **API 说明：**
> - `acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)` — KuiklyUI 内置持久化，跨平台，来源：`modules.md`
> - `sp.getString(key)` / `sp.setString(key, value)` — 字符串读写，来源：`modules.md`
> - `textDidChange { it.text }` — Input 文本变化事件，来源：`input.md`
> - `vif({ condition }) { ... }` — 条件渲染，来源：`reactive.md`
> - `observableList<T>()` — 不支持初始值，在 `created()` 中添加数据，来源：`reactive.md`

- [ ] **Step 1：创建 HomePage.kt**

```kotlin
// shared/src/commonMain/kotlin/com/yijian/pages/HomePage.kt
package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.model.UserProfile
import com.yijian.model.VideoInfo
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

// ─── SP 键名常量 ───
private const val SP_NICKNAME = "yijian_nickname"
private const val SP_BIO = "yijian_bio"
private const val SP_AVATAR = "yijian_avatar"

// ─── Tab 枚举 ───
private enum class HomeTab(val label: String, val icon: String) {
    CLIP("剪辑", "✂"),
    LEARN("学习", "🎓"),
    PROFILE("我的", "👤")
}

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    // ─── 导航状态 ───
    var selectedTab by observable(0)                    // 0=剪辑 1=学习 2=我的

    // ─── 剪辑 Tab 状态 ───
    var draftList by observableList<VideoInfo>()        // observableList 不支持初始值

    // ─── 我的 Tab 状态 ───
    var userProfile by observable(UserProfile())
    var showEditNickname by observable(false)
    var showEditBio by observable(false)
    var editingText by observable("")

    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }

    override fun created() {
        super.created()
        // 从 SharedPreferences 加载用户资料
        userProfile = UserProfile(
            nickname = sp.getString(SP_NICKNAME) ?: "创作者",
            bio = sp.getString(SP_BIO) ?: "记录生活的每一刻",
            avatarEmoji = sp.getString(SP_AVATAR) ?: "🎬"
        )
        // 加载草稿（mock 数据）
        draftList.addAll(listOf(
            VideoInfo("1", "午后阳光.mp4", "test1", duration = 15200L,
                createTime = System.currentTimeMillis() - 7200000L),
            VideoInfo("2", "城市街景.mp4", "test2", duration = 45000L,
                createTime = System.currentTimeMillis() - 86400000L),
            VideoInfo("3", "旅行记录.mp4", "test3", duration = 120000L,
                createTime = System.currentTimeMillis() - 172800000L),
            VideoInfo("4", "美食制作.mp4", "test4", duration = 32000L,
                createTime = System.currentTimeMillis() - 3600000L),
        ))
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
            attr {
                backgroundColor(YijianColors.background)
                flexDirectionColumn()
            }

            // ─── 内容区 ───
            View {
                attr { flex(1f); flexDirectionColumn() }
                vif({ ctx.selectedTab == 0 }) { ClipTabContent(ctx) }
                vif({ ctx.selectedTab == 1 }) { LearnTabContent() }
                vif({ ctx.selectedTab == 2 }) { ProfileTabContent(ctx) }
            }

            // ─── 分割线 ───
            View { attr { height(1f); backgroundColor(YijianColors.surfaceLight) } }

            // ─── 底部 Tab Bar ───
            BottomTabBar(ctx)

            // ─── 编辑昵称弹层 ───
            if (ctx.showEditNickname) {
                EditOverlay(
                    ctx = ctx,
                    title = "修改昵称",
                    placeholder = ctx.userProfile.nickname,
                    onConfirm = { ctx.saveNickname() },
                    onDismiss = { ctx.showEditNickname = false; ctx.editingText = "" }
                )
            }

            // ─── 编辑简介弹层 ───
            if (ctx.showEditBio) {
                EditOverlay(
                    ctx = ctx,
                    title = "修改简介",
                    placeholder = ctx.userProfile.bio,
                    onConfirm = { ctx.saveBio() },
                    onDismiss = { ctx.showEditBio = false; ctx.editingText = "" }
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 底部 Tab Bar
// ──────────────────────────────────────────────────────────
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
                attr {
                    flex(1f)
                    height(56f)
                    flexDirectionColumn()
                    alignItemsCenter()
                    justifyContentCenter()
                }
                event { click { ctx.selectedTab = index } }

                // 选中指示点（占位保持对齐）
                View {
                    attr {
                        size(4f, 4f)
                        borderRadius(2f)
                        backgroundColor(if (selected) YijianColors.primary else Color(0x00000000))
                        marginBottom(2f)
                    }
                }

                // 图标
                Text {
                    attr {
                        text(tab.icon)
                        fontSize(22f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        marginBottom(2f)
                    }
                }

                // 标签
                Text {
                    attr {
                        text(tab.label)
                        fontSize(10f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        if (selected) fontWeightSemiBold() else fontWeightNormal()
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 编辑覆盖层（昵称 / 简介）
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.EditOverlay(
    ctx: HomePage,
    title: String,
    placeholder: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    View {
        attr {
            absolutePositionAllZero()
            backgroundColor(Color(0xCC000000))
            allCenter()
        }
        event { click { onDismiss() } }

        View {
            attr {
                width(ctx.pagerData.pageViewWidth - 48f)
                backgroundColor(YijianColors.surface)
                borderRadius(16f)
                padding(all = YijianTheme.Spacing.lg)
                flexDirectionColumn()
            }
            event { click { /* 阻止点击穿透 */ } }

            // 标题
            Text {
                attr {
                    text(title)
                    fontSize(16f)
                    fontWeightBold()
                    color(YijianColors.textPrimary)
                    marginBottom(YijianTheme.Spacing.md)
                }
            }

            // 输入框（Input.textDidChange 来源：input.md）
            Input {
                attr {
                    width(ctx.pagerData.pageViewWidth - 48f - YijianTheme.Spacing.lg * 2)
                    height(44f)
                    backgroundColor(YijianColors.backgroundLight)
                    borderRadius(8f)
                    paddingLeft(12f); paddingRight(12f)
                    fontSize(14f)
                    color(YijianColors.textPrimary)
                    placeholder(placeholder)
                    text(ctx.editingText)
                }
                event {
                    textDidChange { ctx.editingText = it.text }
                }
            }

            // 按钮行
            View {
                attr {
                    flexDirectionRow()
                    justifyContentFlexEnd()
                    marginTop(YijianTheme.Spacing.md)
                }

                // 取消
                View {
                    attr {
                        height(36f); paddingLeft(16f); paddingRight(16f)
                        borderRadius(18f)
                        backgroundColor(YijianColors.surfaceLight)
                        allCenter(); marginRight(8f)
                    }
                    event { click { onDismiss() } }
                    Text { attr { text("取消"); fontSize(14f); color(YijianColors.textSecondary) } }
                }

                // 确认
                View {
                    attr {
                        height(36f); paddingLeft(16f); paddingRight(16f)
                        borderRadius(18f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        allCenter()
                    }
                    event { click { onConfirm() } }
                    Text { attr { text("确认"); fontSize(14f); color(YijianColors.textPrimary); fontWeightBold() } }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// Tab 1：剪辑
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.ClipTabContent(ctx: HomePage) {
    View {
        attr { flex(1f); flexDirectionColumn(); backgroundColor(YijianColors.background) }

        // TopBar
        View {
            attr {
                height(YijianTheme.BarHeight.topBar + ctx.pagerData.statusBarHeight)
                paddingTop(ctx.pagerData.statusBarHeight)
                paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg)
                flexDirectionRow(); alignItemsCenter()
            }
            Text {
                attr {
                    text("一剪"); fontSize(YijianTheme.FontSize.title)
                    fontWeightBold(); color(YijianColors.textPrimary); flex(1f)
                }
            }
        }

        // 新建剪辑按钮
        View {
            attr {
                marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg)
                marginBottom(YijianTheme.Spacing.lg)
                height(56f); borderRadius(12f)
                backgroundLinearGradient(
                    Direction.TO_RIGHT,
                    ColorStop(YijianColors.gradientStart, 0f),
                    ColorStop(YijianColors.gradientEnd, 1f)
                )
                flexDirectionRow(); alignItemsCenter(); justifyContentCenter()
            }
            event { click { ctx.jumpPage("MainPage") } }

            Text { attr { text("🎬"); fontSize(20f); marginRight(8f) } }
            Text {
                attr {
                    text("新建剪辑"); fontSize(16f)
                    fontWeightBold(); color(YijianColors.textPrimary)
                }
            }
        }

        // 草稿箱标题行
        View {
            attr {
                flexDirectionRow(); alignItemsCenter()
                paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg)
                marginBottom(YijianTheme.Spacing.sm)
            }
            Text {
                attr { text("草稿箱"); fontSize(14f); fontWeightBold(); color(YijianColors.textPrimary); flex(1f) }
            }
            Text { attr { text("全部 >"); fontSize(12f); color(YijianColors.textTertiary) } }
        }

        // 草稿列表 / 空态
        if (ctx.draftList.isEmpty()) {
            View {
                attr { flex(1f); allCenter(); flexDirectionColumn() }
                Text { attr { text("📂"); fontSize(48f); marginBottom(12f) } }
                Text { attr { text("暂无草稿"); fontSize(16f); color(YijianColors.textSecondary); marginBottom(6f) } }
                Text { attr { text("点击「新建剪辑」开始创作吧"); fontSize(12f); color(YijianColors.textTertiary) } }
            }
        } else {
            Scroller {
                attr { flex(1f); flexDirectionColumn() }
                View {
                    attr {
                        flexDirectionRow(); flexWrapWrap()
                        paddingLeft(YijianTheme.Spacing.sm); paddingRight(YijianTheme.Spacing.sm)
                    }
                    val cardWidth = ctx.pagerData.pageViewWidth / 2 - YijianTheme.Spacing.md
                    for (draft in ctx.draftList) {
                        DraftCard(ctx, draft, cardWidth)
                    }
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.DraftCard(ctx: HomePage, video: VideoInfo, cardWidth: Float) {
    View {
        attr {
            size(cardWidth, cardWidth * 9f / 16f + 52f)
            flexDirectionColumn()
            margin(YijianTheme.Spacing.xs)
            backgroundColor(YijianColors.surface)
            borderRadius(YijianTheme.Radius.md)
            overflow(true)
        }
        event {
            click {
                val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
                ctx.jumpPage("EditorPage", params)
            }
        }

        // 缩略图
        View {
            attr {
                size(cardWidth, cardWidth * 9f / 16f)
                backgroundColor(YijianColors.backgroundLight); allCenter()
            }
            Text { attr { text("🎞"); fontSize(32f) } }

            if (video.duration > 0) {
                View {
                    attr {
                        absolutePosition(bottom = 6f, right = 6f)
                        paddingLeft(5f); paddingRight(5f); paddingTop(2f); paddingBottom(2f)
                        backgroundColor(Color(0xCC000000)); borderRadius(4f)
                    }
                    Text { attr { text(video.formattedDuration); fontSize(10f); color(YijianColors.textPrimary) } }
                }
            }
        }

        // 信息行
        View {
            attr { flex(1f); padding(all = 6f); justifyContentCenter() }
            Text {
                attr { text(video.title); fontSize(12f); color(YijianColors.textPrimary); lines(1); marginBottom(3f) }
            }
            Text {
                attr { text(formatRelativeTime(video.createTime)); fontSize(10f); color(YijianColors.textTertiary) }
            }
        }
    }
}

private fun formatRelativeTime(createTime: Long): String {
    if (createTime == 0L) return "刚刚"
    val diff = System.currentTimeMillis() - createTime
    return when {
        diff < 60_000L -> "刚刚"
        diff < 3_600_000L -> "${diff / 60_000}分钟前"
        diff < 86_400_000L -> "${diff / 3_600_000}小时前"
        else -> "${diff / 86_400_000}天前"
    }
}

// ──────────────────────────────────────────────────────────
// Tab 2：学习（空占位）
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.LearnTabContent() {
    View {
        attr { flex(1f); backgroundColor(YijianColors.background); allCenter(); flexDirectionColumn() }
        Text { attr { text("🎓"); fontSize(48f); marginBottom(16f) } }
        Text { attr { text("即将上线"); fontSize(16f); color(YijianColors.textPrimary); marginBottom(8f) } }
        Text { attr { text("学习内容正在精心准备中..."); fontSize(12f); color(YijianColors.textSecondary) } }
    }
}

// ──────────────────────────────────────────────────────────
// Tab 3：我的
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.ProfileTabContent(ctx: HomePage) {
    Scroller {
        attr { flex(1f); flexDirectionColumn(); backgroundColor(YijianColors.background) }

        // 顶部安全区
        View { attr { height(ctx.pagerData.statusBarHeight + YijianTheme.Spacing.xl) } }

        // 头像
        View {
            attr { allCenter(); marginBottom(12f) }
            View {
                attr { size(80f, 80f); borderRadius(40f); backgroundColor(YijianColors.surface); allCenter() }
                Text { attr { text(ctx.userProfile.avatarEmoji); fontSize(36f) } }
            }
        }

        // 昵称（可编辑）
        View {
            attr { allCenter(); marginBottom(8f); flexDirectionRow(); justifyContentCenter(); alignItemsCenter() }
            event { click { ctx.editingText = ctx.userProfile.nickname; ctx.showEditNickname = true } }
            Text {
                attr { text(ctx.userProfile.nickname); fontSize(20f); fontWeightBold(); color(YijianColors.textPrimary) }
            }
            Text { attr { text(" ✏"); fontSize(14f); color(YijianColors.textTertiary) } }
        }

        // 简介（可编辑）
        View {
            attr { allCenter(); marginBottom(YijianTheme.Spacing.xl); paddingLeft(32f); paddingRight(32f) }
            event { click { ctx.editingText = ctx.userProfile.bio; ctx.showEditBio = true } }
            Text {
                attr { text(ctx.userProfile.bio); fontSize(14f); color(YijianColors.textSecondary); textAlignCenter(); lines(3) }
            }
        }

        // 分割线
        View {
            attr {
                height(1f); backgroundColor(YijianColors.surfaceLight)
                marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg)
            }
        }

        // 统计
        View {
            attr { paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg) }
            Text { attr { text("我的统计"); fontSize(14f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(YijianTheme.Spacing.md) } }
            View {
                attr { flexDirectionRow() }
                listOf(Triple("4", "项目", "📁"), Triple("1", "本月", "📅"), Triple("3m12s", "时长", "⏱")).forEach { (value, label, icon) ->
                    View {
                        attr {
                            flex(1f); backgroundColor(YijianColors.surface); borderRadius(YijianTheme.Radius.md)
                            padding(all = YijianTheme.Spacing.md); allCenter(); flexDirectionColumn()
                            marginLeft(4f); marginRight(4f)
                        }
                        Text { attr { text(icon); fontSize(20f); marginBottom(4f) } }
                        Text { attr { text(value); fontSize(18f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(2f) } }
                        Text { attr { text(label); fontSize(11f); color(YijianColors.textTertiary) } }
                    }
                }
            }
        }

        // 分割线
        View { attr { height(1f); backgroundColor(YijianColors.surfaceLight); marginBottom(YijianTheme.Spacing.sm) } }

        // 设置列表项
        listOf("⚙  设置", "📱  关于一剪").forEach { label ->
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
```

- [ ] **Step 2：验证编译**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | grep -E "error:|Error:|BUILD" | tail -10
```

预期：`BUILD SUCCESSFUL`（无 error）

- [ ] **Step 3：提交**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
git add shared/src/commonMain/kotlin/com/yijian/pages/HomePage.kt
git commit -m "feat: add HomePage with 3-tab layout, SharedPreferencesModule storage"
```

---

## Task 3：更新 SplashPage 跳转目标

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/yijian/pages/SplashPage.kt`

- [ ] **Step 1：修改两处日志和跳转**

找到文件中：
```kotlin
KLog.i(TAG, "SplashPage created — 1.8s 后跳转 MainPage")
```
改为：
```kotlin
KLog.i(TAG, "SplashPage created — 1.8s 后跳转 HomePage")
```

找到：
```kotlin
KLog.i(TAG, "跳转 → MainPage")
jumpPage("MainPage")
```
改为：
```kotlin
KLog.i(TAG, "跳转 → HomePage")
jumpPage("HomePage")
```

- [ ] **Step 2：验证编译**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -4
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3：提交**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
git add shared/src/commonMain/kotlin/com/yijian/pages/SplashPage.kt
git commit -m "feat: route SplashPage → HomePage"
```

---

## Task 4：构建 APK、安装、冒烟测试

- [ ] **Step 1：构建 Debug APK**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
./gradlew :androidApp:assembleDebug 2>&1 | tail -5
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 2：安装**

```bash
APK=$(find /Users/delanding/ProjDoing/TDF/yijian/androidApp/build/outputs/apk/debug -name "*.apk" | head -1)
adb install -r "$APK" && echo "installed"
```

- [ ] **Step 3：启动并截图验证**

```bash
adb shell am force-stop com.yijian.android
sleep 1
adb shell am start -n "com.yijian.android/com.yijian.android.MainActivity"
sleep 7
adb shell screencap -p /sdcard/homepage.png
adb pull /sdcard/homepage.png /tmp/homepage.png
echo "screenshot saved"
```

**验收清单：**
- [ ] SplashPage 正常显示 → 1.8s 后进入 HomePage
- [ ] 底部 Tab Bar：✂ 剪辑 / 🎓 学习 / 👤 我的，默认选中「剪辑」（青色）
- [ ] 剪辑 Tab：渐变「新建剪辑」按钮可见 + 4 条草稿卡片
- [ ] 点击「学习」Tab → 空占位页（🎓 即将上线）
- [ ] 点击「我的」Tab → 用户资料页（默认昵称「创作者」）
- [ ] 点击昵称区域 → 弹出编辑框 → 输入新昵称 → 确认 → UI 更新
- [ ] 重启 App → 昵称持久化（SharedPreferences 生效）
- [ ] 点击草稿卡片 → 进入 EditorPage

- [ ] **Step 4：验证持久化**

```bash
# 在 App 中把昵称改为「测试用户」后执行
adb shell am force-stop com.yijian.android && sleep 2
adb shell am start -n "com.yijian.android/com.yijian.android.MainActivity"
sleep 6
adb shell screencap -p /sdcard/profile_persist.png
adb pull /sdcard/profile_persist.png /tmp/profile_persist.png
# 检查截图中「我的」Tab 显示「测试用户」
```

- [ ] **Step 5：最终提交**

```bash
cd /Users/delanding/ProjDoing/TDF/yijian
git tag -a "v0.2-homepage-tabs" -m "feat: 3-tab homepage with persistent user profile"
```

---

## 自查结果

**Spec 覆盖：**
- ✅ 路由 SplashPage → HomePage（Task 3）
- ✅ 底部 Tab Bar + 选中指示（Task 2，`BottomTabBar`）
- ✅ 剪辑 Tab：新建按钮 + 草稿列表 + 空态（`ClipTabContent`）
- ✅ 学习 Tab：空占位（`LearnTabContent`）
- ✅ 我的 Tab：头像 + 可编辑昵称/简介 + 统计 + 设置列表（`ProfileTabContent`）
- ✅ 持久化：`SharedPreferencesModule`（无 expect/actual，KuiklyUI 内置）

**API 正确性（经 KuiklyUI-AI 验证）：**
- `SharedPreferencesModule.getString/setString` ✅（来源：`modules.md`）
- `textDidChange { it.text }` ✅（来源：`input.md`）
- `observableList<T>()` 无初始值 ✅（来源：`reactive.md`）
- `vif({ condition })` ✅
- `justifyContentFlexEnd` / `justifyContentCenter` ✅（来源：`layout.md`）

**与 Task 1 类型一致：**
- `UserProfile` 数据类在 Task 1 定义，在 Task 2 `HomePage` 中使用 ✅
- `VideoInfo` 复用现有 model ✅
- `HomeTab` 枚举在文件顶部定义，`BottomTabBar` 中通过 `HomeTab.values()` 使用 ✅
