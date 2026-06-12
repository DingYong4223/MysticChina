# 探索 Tab 重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将「探索」Tab 重构为「精选轮播 + 五个主题分区横向滑动」的文化导航中心，采用深朱红 (#8B0000) 中国红主题。

**Architecture:** 新建 `ExploreData.kt`（静态数据）、`CategorySection.kt`、`FeaturedCarousel.kt`、`ExploreTabContent.kt` 四个文件，均使用 `ViewContainer<*,*>` 扩展函数模式（与项目现有 `HomePage.kt` 风格一致）。`Colors.kt` 全量替换为深色主题。`HomePage.kt` 仅替换探索 Tab 内容，其余 Tab 不受影响。

**Tech Stack:** Kotlin Multiplatform, KuiklyUI (FlexBox DSL, Scroller, Text, View), kotlin.test

---

## File Map

| 操作 | 文件 | 说明 |
|---|---|---|
| 新建 | `shared/src/commonMain/kotlin/com/fula/mysticchina/pages/ExploreData.kt` | 数据模型 + 静态列表 |
| 新建 | `shared/src/commonTest/kotlin/com/fula/mysticchina/ExploreDataTest.kt` | 数据完整性测试 |
| 修改 | `shared/src/commonMain/kotlin/com/fula/mysticchina/theme/Colors.kt` | 浅色 → 深色主题 |
| 新建 | `shared/src/commonMain/kotlin/com/fula/mysticchina/components/CategorySection.kt` | 分区标题 + 横向功能卡片行 |
| 新建 | `shared/src/commonMain/kotlin/com/fula/mysticchina/components/FeaturedCarousel.kt` | 精选轮播 + 圆点指示器 |
| 新建 | `shared/src/commonMain/kotlin/com/fula/mysticchina/components/ExploreTabContent.kt` | 探索 Tab 整体容器 |
| 修改 | `shared/src/commonMain/kotlin/com/fula/mysticchina/pages/HomePage.kt` | 替换探索 Tab 内容，删除旧代码 |

---

## Task 1: 数据模型 + 静态列表 (ExploreData.kt)

**Files:**
- Create: `shared/src/commonMain/kotlin/com/fula/mysticchina/pages/ExploreData.kt`
- Create (test dir): `shared/src/commonTest/kotlin/com/fula/mysticchina/ExploreDataTest.kt`

- [ ] **Step 1: 创建测试目录并写失败测试**

```bash
mkdir -p shared/src/commonTest/kotlin/com/fula/mysticchina
```

创建 `shared/src/commonTest/kotlin/com/fula/mysticchina/ExploreDataTest.kt`：

```kotlin
package com.fula.mysticchina

import com.fula.mysticchina.pages.EXPLORE_CATEGORIES
import com.fula.mysticchina.pages.FEATURED_CARDS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExploreDataTest {

    @Test
    fun `FEATURED_CARDS is not empty`() {
        assertTrue(FEATURED_CARDS.isNotEmpty())
    }

    @Test
    fun `EXPLORE_CATEGORIES has exactly 5 categories`() {
        assertEquals(5, EXPLORE_CATEGORIES.size)
    }

    @Test
    fun `each category has at least one item`() {
        EXPLORE_CATEGORIES.forEach { category ->
            assertTrue(
                category.items.isNotEmpty(),
                "Category '${category.name}' should have at least one item"
            )
        }
    }

    @Test
    fun `first item in 文字书法 points to HanziPage`() {
        val writingCategory = EXPLORE_CATEGORIES.first()
        assertEquals("文字书法", writingCategory.name)
        assertEquals("HanziPage", writingCategory.items.first().pageName)
    }

    @Test
    fun `FEATURED_CARDS first card has non-null pageName`() {
        assertNotNull(FEATURED_CARDS.first().pageName)
    }

    @Test
    fun `coming soon items have null pageName`() {
        // All items except HanziPage should be null (coming soon)
        val allItems = EXPLORE_CATEGORIES.flatMap { it.items }
        val availableItems = allItems.filter { it.pageName != null }
        assertTrue(availableItems.all { it.pageName == "HanziPage" })
    }
}
```

- [ ] **Step 2: 运行测试，确认编译失败（ExploreData.kt 尚未创建）**

```bash
./gradlew :shared:testDebugUnitTest --tests "com.fula.mysticchina.ExploreDataTest" 2>&1 | tail -20
```

预期：编译错误 `Unresolved reference: EXPLORE_CATEGORIES`

- [ ] **Step 3: 创建 ExploreData.kt**

创建 `shared/src/commonMain/kotlin/com/fula/mysticchina/pages/ExploreData.kt`：

```kotlin
package com.fula.mysticchina.pages

import com.tencent.kuikly.core.base.Color

/** 顶部精选轮播卡片 */
data class FeaturedCard(
    val tag: String,          // 左上角标签，如 "今日精选"
    val title: String,        // 主标题，如 "汉字闯关"
    val subtitle: String,     // 副标题，如 "每关十字，写对才能过关"
    val pageName: String?,    // 目标页面名；null = 未上线（仍可出现在轮播）
    val gradientStart: Color, // 渐变起始色，使用 GradientPreset
    val gradientEnd: Color,   // 渐变结束色
)

/** 单个功能入口 */
data class FeatureItem(
    val emoji: String,
    val name: String,
    val pageName: String?,    // null = 未上线，自动置灰
)

/** 主题分区 */
data class FeatureCategory(
    val emoji: String,
    val name: String,
    val items: List<FeatureItem>,
)

/** 轮播卡片渐变预设（中式配色） */
object GradientPreset {
    val RED_FLAME_START   = Color(0xFF8C0808)  // 朱红火焰
    val RED_FLAME_END     = Color(0xFFDE2910)
    val INDIGO_START      = Color(0xFF0D2744)  // 靛蓝
    val INDIGO_END        = Color(0xFF1A6090)
    val INK_GREEN_START   = Color(0xFF0A2A10)  // 墨绿
    val INK_GREEN_END     = Color(0xFF28A050)
    val PURPLE_START      = Color(0xFF2D1A5C)  // 紫金
    val PURPLE_END        = Color(0xFF6030A0)
}

/** 顶部精选轮播 — 每次发版手动更新此列表 */
val FEATURED_CARDS: List<FeaturedCard> = listOf(
    FeaturedCard(
        tag = "今日精选",
        title = "汉字闯关",
        subtitle = "每关十字，写对才能过关",
        pageName = "HanziPage",
        gradientStart = GradientPreset.RED_FLAME_START,
        gradientEnd = GradientPreset.RED_FLAME_END,
    ),
    FeaturedCard(
        tag = "即将上线",
        title = "古诗词背诵",
        subtitle = "三百首经典，逐句检测",
        pageName = null,
        gradientStart = GradientPreset.INDIGO_START,
        gradientEnd = GradientPreset.INDIGO_END,
    ),
    FeaturedCard(
        tag = "即将上线",
        title = "二十四节气",
        subtitle = "今日芒种，麦黄梅熟",
        pageName = null,
        gradientStart = GradientPreset.INK_GREEN_START,
        gradientEnd = GradientPreset.INK_GREEN_END,
    ),
)

/** 主题分区列表 — 新功能上线后将 pageName 从 null 改为实际页面名 */
val EXPLORE_CATEGORIES: List<FeatureCategory> = listOf(
    FeatureCategory(
        emoji = "🈶", name = "文字书法",
        items = listOf(
            FeatureItem("🖊", "汉字练习", "HanziPage"),
            FeatureItem("📖", "古诗词背诵", null),
            FeatureItem("💬", "成语接龙", null),
            FeatureItem("🎙", "飞花令", null),
            FeatureItem("✍️", "字帖临摹", null),
            FeatureItem("🔤", "汉字字源", null),
            FeatureItem("👂", "汉字听写", null),
            FeatureItem("🧩", "偏旁部首", null),
        )
    ),
    FeatureCategory(
        emoji = "🏮", name = "传统文化",
        items = listOf(
            FeatureItem("🎋", "节气日历", null),
            FeatureItem("🧧", "传统节日", null),
            FeatureItem("🎨", "传统色彩", null),
            FeatureItem("🗺", "朝代游戏", null),
            FeatureItem("📍", "地图竞答", null),
            FeatureItem("👘", "汉服图鉴", null),
            FeatureItem("🏙", "地名由来", null),
        )
    ),
    FeatureCategory(
        emoji = "🧩", name = "益智游戏",
        items = listOf(
            FeatureItem("♟", "华容道", null),
            FeatureItem("⭕", "五子棋", null),
            FeatureItem("🀄", "麻将识牌", null),
            FeatureItem("⬛", "围棋入门", null),
            FeatureItem("🔢", "洛书九宫格", null),
        )
    ),
    FeatureCategory(
        emoji = "🎵", name = "音乐艺术",
        items = listOf(
            FeatureItem("🎭", "京剧脸谱", null),
            FeatureItem("✂️", "剪纸图案", null),
            FeatureItem("🎼", "民族乐器", null),
            FeatureItem("🪢", "中国结", null),
            FeatureItem("🖋", "印章篆刻", null),
            FeatureItem("🎵", "简谱练习", null),
        )
    ),
    FeatureCategory(
        emoji = "🍜", name = "饮食文化",
        items = listOf(
            FeatureItem("🗺", "菜系地图", null),
            FeatureItem("🍵", "茶文化百科", null),
            FeatureItem("🍡", "节气食俗", null),
        )
    ),
)
```

- [ ] **Step 4: 运行测试，确认全部通过**

```bash
./gradlew :shared:testDebugUnitTest --tests "com.fula.mysticchina.ExploreDataTest" 2>&1 | tail -20
```

预期：`BUILD SUCCESSFUL` 且 6 个测试全部 PASS

- [ ] **Step 5: 提交**

```bash
git add shared/src/commonMain/kotlin/com/fula/mysticchina/pages/ExploreData.kt \
        shared/src/commonTest/kotlin/com/fula/mysticchina/ExploreDataTest.kt
git commit -m "feat: 探索Tab数据模型 FeaturedCard/FeatureItem/FeatureCategory + EXPLORE_CATEGORIES"
```

---

## Task 2: Colors.kt — 浅色主题替换为深色主题

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/fula/mysticchina/theme/Colors.kt`

- [ ] **Step 1: 替换 Colors.kt 全部内容**

用以下内容完整替换 `shared/src/commonMain/kotlin/com/fula/mysticchina/theme/Colors.kt`：

```kotlin
package com.fula.mysticchina.theme

import com.tencent.kuikly.core.base.Color

/**
 * 神秘中国应用主题色板
 * 深色主题 — 中国红（深朱红 #8B0000 背景）
 */
object MysticChinaColors {

    // 背景色（深红系）
    val background      = Color(0xFF8B0000)  // 深朱红，页面主背景
    val backgroundLight = Color(0xFF700000)  // 更深，状态栏/顶栏区域
    val surface         = Color(0x3D000000)  // 24% 黑，普通卡片底色
    val surfaceLight    = Color(0x52000000)  // 32% 黑，已上线卡片底色（更突出）

    // 主色 — 中国红系（五星旗红）
    val primary      = Color(0xFFDE2910)  // 中国红（五星旗红）
    val primaryDark  = Color(0xFF8C0808)  // 深红
    val primaryLight = Color(0xFFFF4438)  // 亮红（强调）
    val accent       = Color(0xFFDE2910)  // 与 primary 一致

    // 渐变（用于按钮、高亮区域）
    val gradientStart = Color(0xFFFF4438)  // 亮红
    val gradientEnd   = Color(0xFF8C0808)  // 深红

    // 文字（白色系，适配深红背景）
    val textPrimary   = Color(0xFFFFFFFF)  // 纯白，主文字
    val textSecondary = Color(0xFFFFE5E0)  // 暖白，次要文字
    val textTertiary  = Color(0xCCFFFFFF)  // 80% 白，辅助文字
    val textDisabled  = Color(0x66FFFFFF)  // 40% 白，禁用文字

    // 功能色（保持不变）
    val error   = Color(0xFFFF4759)
    val warning = Color(0xFFFFB340)
    val success = Color(0xFF00C853)

    // 透明度覆盖（保持不变）
    val overlay      = Color(0x80000000)
    val overlayLight = Color(0x40000000)

    // 控制栏
    val controlBarBg   = Color(0xCC700000)  // 半透明深红
    val progressTrack  = Color(0x4DDE2910)  // 半透明红
    val progressFill   = Color(0xFFDE2910)  // 中国红
    val progressThumb  = Color(0xFFDE2910)  // 中国红

    // 探索 Tab 专用
    val dotActive   = Color(0xCCFFFFFF)  // 80% 白，轮播激活圆点
    val dotInactive = Color(0x33FFFFFF)  // 20% 白，轮播非激活圆点
    val divider     = Color(0x26FFFFFF)  // 15% 白，分区分割线
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -30
```

预期：`BUILD SUCCESSFUL`（Colors.kt 改变后其他文件引用的 token 名称未变，只是值改变，不影响编译）

- [ ] **Step 3: 提交**

```bash
git add shared/src/commonMain/kotlin/com/fula/mysticchina/theme/Colors.kt
git commit -m "feat: 主题色板替换为深色主题（深朱红背景 #8B0000）"
```

---

## Task 3: CategorySection.kt — 分区标题 + 横向功能卡片

**Files:**
- Create: `shared/src/commonMain/kotlin/com/fula/mysticchina/components/CategorySection.kt`

- [ ] **Step 1: 创建 CategorySection.kt**

创建 `shared/src/commonMain/kotlin/com/fula/mysticchina/components/CategorySection.kt`：

```kotlin
package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.pages.FeatureCategory
import com.fula.mysticchina.pages.FeatureItem
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 主题分区：标题行 + 横向滑动功能卡片行
 *
 * @param ctx     用于点击跳转
 * @param category 分区数据（emoji + name + items）
 */
internal fun ViewContainer<*, *>.CategorySection(ctx: BasePager, category: FeatureCategory) {
    View {
        attr { flexDirectionColumn() }

        // 分区标题行
        View {
            attr {
                height(36f)
                flexDirectionRow()
                alignItemsCenter()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingRight(MysticChinaTheme.Spacing.lg)
            }
            Text {
                attr {
                    text(category.emoji)
                    fontSize(12f)
                    marginRight(MysticChinaTheme.Spacing.xs)
                }
            }
            Text {
                attr {
                    text(category.name)
                    fontSize(MysticChinaTheme.FontSize.small)
                    fontWeightBold()
                    color(MysticChinaColors.primaryLight)
                    flex(1f)
                }
            }
            Text {
                attr {
                    text("${category.items.size}个")
                    fontSize(10f)
                    color(MysticChinaColors.textDisabled)
                }
            }
        }

        // 横向滑动功能卡片行
        Scroller {
            attr {
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingRight(MysticChinaTheme.Spacing.lg)
                paddingBottom(MysticChinaTheme.Spacing.md)
            }
            category.items.forEach { item ->
                FeatureCard(ctx, item)
            }
        }
    }
}

/**
 * 单个功能入口卡片（60dp 宽）
 * 未上线：opacity 0.4 + 右上角「即将」徽章，不响应点击
 */
private fun ViewContainer<*, *>.FeatureCard(ctx: BasePager, item: FeatureItem) {
    val available = item.pageName != null
    View {
        attr {
            width(60f)
            marginRight(MysticChinaTheme.Spacing.sm)
            flexDirectionColumn()
            alignItemsCenter()
            paddingTop(MysticChinaTheme.Spacing.sm)
            paddingBottom(MysticChinaTheme.Spacing.sm)
            paddingLeft(MysticChinaTheme.Spacing.xs)
            paddingRight(MysticChinaTheme.Spacing.xs)
            backgroundColor(
                if (available) MysticChinaColors.surfaceLight else MysticChinaColors.surface
            )
            borderRadius(MysticChinaTheme.Radius.md)
            if (!available) opacity(0.4f)
        }
        if (available) {
            event { click { ctx.jumpPage(item.pageName!!) } }
        }

        Text {
            attr {
                text(item.emoji)
                fontSize(22f)
                marginBottom(4f)
            }
        }
        Text {
            attr {
                text(item.name)
                fontSize(8f)
                color(MysticChinaColors.textSecondary)
                textAlignCenter()
                lines(2)
            }
        }

        // 「即将」徽章 — 绝对定位右上角
        if (!available) {
            View {
                attr {
                    positionAbsolute()
                    top(2f)
                    right(2f)
                    backgroundColor(MysticChinaColors.surface)
                    borderRadius(MysticChinaTheme.Radius.sm)
                    paddingLeft(2f)
                    paddingRight(2f)
                    paddingTop(1f)
                    paddingBottom(1f)
                }
                Text {
                    attr {
                        text("即将")
                        fontSize(6f)
                        color(MysticChinaColors.textDisabled)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -20
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: 提交**

```bash
git add shared/src/commonMain/kotlin/com/fula/mysticchina/components/CategorySection.kt
git commit -m "feat: CategorySection + FeatureCard 组件"
```

---

## Task 4: FeaturedCarousel.kt — 精选轮播 + 圆点指示器

**Files:**
- Create: `shared/src/commonMain/kotlin/com/fula/mysticchina/components/FeaturedCarousel.kt`

> **Note:** 圆点指示器为静态实现（首个圆点高亮）。滚动位置跟踪（动态圆点）标记为 future enhancement，不在本次范围内。

- [ ] **Step 1: 创建 FeaturedCarousel.kt**

创建 `shared/src/commonMain/kotlin/com/fula/mysticchina/components/FeaturedCarousel.kt`：

```kotlin
package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.pages.FeaturedCard
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 顶部精选轮播：横向可滑动的精选卡片 + 静态圆点指示器。
 *
 * @param ctx   用于点击跳转已上线卡片
 * @param cards 精选卡片列表（来自 FEATURED_CARDS）
 */
internal fun ViewContainer<*, *>.FeaturedCarousel(ctx: BasePager, cards: List<FeaturedCard>) {
    val cardWidth = ctx.pagerData.pageViewWidth * 0.68f

    View {
        attr { flexDirectionColumn() }

        // 横向滑动卡片区
        Scroller {
            attr {
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingTop(MysticChinaTheme.Spacing.sm)
                paddingBottom(MysticChinaTheme.Spacing.xs)
            }
            cards.forEach { card ->
                FeaturedCarouselCard(ctx, card, cardWidth)
            }
            // 末尾留白，确保最后一张卡片可完整滑入视图
            View { attr { width(MysticChinaTheme.Spacing.lg) } }
        }

        // 圆点指示器（静态：首个圆点高亮）
        View {
            attr {
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingTop(MysticChinaTheme.Spacing.xs)
                paddingBottom(MysticChinaTheme.Spacing.sm)
            }
            cards.forEachIndexed { index, _ ->
                View {
                    attr {
                        height(4f)
                        width(if (index == 0) 14f else 4f)
                        borderRadius(2f)
                        marginRight(4f)
                        backgroundColor(
                            if (index == 0) MysticChinaColors.dotActive
                            else MysticChinaColors.dotInactive
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单张精选卡片：渐变背景 + 标签 + 标题 + 副标题。
 * 已上线（pageName != null）点击跳转，未上线不响应。
 */
private fun ViewContainer<*, *>.FeaturedCarouselCard(
    ctx: BasePager,
    card: FeaturedCard,
    width: Float,
) {
    val available = card.pageName != null
    View {
        attr {
            width(width)
            height(88f)
            marginRight(MysticChinaTheme.Spacing.sm)
            borderRadius(MysticChinaTheme.Radius.lg)
            overflow(true)
            backgroundLinearGradient(
                Direction.TO_BOTTOM_RIGHT,
                ColorStop(card.gradientStart, 0f),
                ColorStop(card.gradientEnd, 1f),
            )
            flexDirectionColumn()
            padding(all = MysticChinaTheme.Spacing.md)
        }
        if (available) {
            event { click { ctx.jumpPage(card.pageName!!) } }
        }

        // 标签行
        View {
            attr {
                backgroundColor(Color(0x2EFFFFFF))
                borderRadius(MysticChinaTheme.Radius.sm)
                paddingLeft(MysticChinaTheme.Spacing.xs)
                paddingRight(MysticChinaTheme.Spacing.xs)
                paddingTop(2f)
                paddingBottom(2f)
                marginBottom(MysticChinaTheme.Spacing.md)
            }
            Text {
                attr {
                    text(card.tag)
                    fontSize(8f)
                    color(Color(0xF2FFFFFF))
                }
            }
        }

        // 标题 + 副标题
        View {
            attr { flexDirectionColumn(); flex(1f); justifyContentFlexEnd() }
            Text {
                attr {
                    text(card.title)
                    fontSize(MysticChinaTheme.FontSize.subtitle)
                    fontWeightBold()
                    color(MysticChinaColors.textPrimary)
                    marginBottom(2f)
                }
            }
            Text {
                attr {
                    text(card.subtitle)
                    fontSize(MysticChinaTheme.FontSize.small)
                    color(Color(0xA6FFFFFF))
                    lines(1)
                }
            }
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -20
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: 提交**

```bash
git add shared/src/commonMain/kotlin/com/fula/mysticchina/components/FeaturedCarousel.kt
git commit -m "feat: FeaturedCarousel + FeaturedCarouselCard 组件"
```

---

## Task 5: ExploreTabContent.kt — 探索 Tab 整体容器

**Files:**
- Create: `shared/src/commonMain/kotlin/com/fula/mysticchina/components/ExploreTabContent.kt`

- [ ] **Step 1: 创建 ExploreTabContent.kt**

创建 `shared/src/commonMain/kotlin/com/fula/mysticchina/components/ExploreTabContent.kt`：

```kotlin
package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.pages.EXPLORE_CATEGORIES
import com.fula.mysticchina.pages.FEATURED_CARDS
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 探索 Tab 内容。
 * 组装：标题栏 → 精选轮播 → 五个主题分区。
 *
 * 在 HomePage.body() 的 Tab 0 分支调用：ExploreTabContent(ctx)
 */
internal fun ViewContainer<*, *>.ExploreTabContent(ctx: BasePager) {
    View {
        attr {
            flex(1f)
            flexDirectionColumn()
            backgroundColor(MysticChinaColors.background)
        }

        // 顶部标题栏
        View {
            attr {
                height(MysticChinaTheme.BarHeight.topBar + ctx.pagerData.statusBarHeight)
                backgroundColor(MysticChinaColors.backgroundLight)
                flexDirectionRow()
                alignItemsCenter()
                paddingTop(ctx.pagerData.statusBarHeight)
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingRight(MysticChinaTheme.Spacing.lg)
            }
            // 左侧白色竖条装饰
            View {
                attr {
                    width(3f)
                    height(16f)
                    borderRadius(2f)
                    backgroundColor(MysticChinaColors.textPrimary)
                    opacity(0.6f)
                    marginRight(MysticChinaTheme.Spacing.sm)
                }
            }
            Text {
                attr {
                    text("探索")
                    fontSize(MysticChinaTheme.FontSize.title)
                    fontWeightBold()
                    color(MysticChinaColors.textPrimary)
                }
            }
        }

        // 主内容区（可滚动）
        Scroller {
            attr {
                flex(1f)
                flexDirectionColumn()
                backgroundColor(MysticChinaColors.background)
            }

            // 精选轮播
            FeaturedCarousel(ctx, FEATURED_CARDS)

            // 分区分割线 + 各主题分区
            EXPLORE_CATEGORIES.forEachIndexed { index, category ->
                // 分割线
                View {
                    attr {
                        height(1f)
                        backgroundColor(MysticChinaColors.divider)
                        marginLeft(MysticChinaTheme.Spacing.lg)
                        marginRight(MysticChinaTheme.Spacing.lg)
                        marginTop(if (index == 0) MysticChinaTheme.Spacing.xs else 0f)
                    }
                }
                CategorySection(ctx, category)
            }

            // 底部安全间距
            View { attr { height(MysticChinaTheme.Spacing.xxl) } }
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :shared:compileDebugKotlinAndroid 2>&1 | tail -20
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: 提交**

```bash
git add shared/src/commonMain/kotlin/com/fula/mysticchina/components/ExploreTabContent.kt
git commit -m "feat: ExploreTabContent — 探索Tab容器（轮播+五分区）"
```

---

## Task 6: HomePage.kt — 接入新组件，删除旧代码

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/fula/mysticchina/pages/HomePage.kt`

- [ ] **Step 1: 更新 HomePage.kt**

用以下内容完整替换 `shared/src/commonMain/kotlin/com/fula/mysticchina/pages/HomePage.kt`：

```kotlin
package com.fula.mysticchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.components.ExploreTabContent
import com.fula.mysticchina.model.UserProfile
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

private const val SP_NICKNAME = "mysticchina_nickname"
private const val SP_BIO      = "mysticchina_bio"
private const val SP_AVATAR   = "mysticchina_avatar"

private enum class HomeTab(val label: String, val icon: String) {
    EXPLORE("探索", "🧭"),
    LEARN("学习", "📚"),
    PROFILE("我的", "👤")
}

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    var selectedTab    by observable(0)
    var userProfile    by observable(UserProfile())
    var showEditNickname by observable(false)
    var showEditBio      by observable(false)
    var editingText      by observable("")

    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }

    override fun created() {
        super.created()
        userProfile = UserProfile(
            nickname    = sp.getString(SP_NICKNAME) ?: "文化探索者",
            bio         = sp.getString(SP_BIO)      ?: "探索中华文化之美",
            avatarEmoji = sp.getString(SP_AVATAR)   ?: "🧭",
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
            attr { backgroundColor(MysticChinaColors.background); flexDirectionColumn() }

            View {
                attr { flex(1f); flexDirectionColumn() }
                vif({ ctx.selectedTab == 0 }) { ExploreTabContent(ctx) }
                vif({ ctx.selectedTab == 1 }) { LearnTabContent() }
                vif({ ctx.selectedTab == 2 }) { ProfileTabContent(ctx) }
            }

            View { attr { height(1f); backgroundColor(MysticChinaColors.divider) } }

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
            backgroundColor(MysticChinaColors.backgroundLight)
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
                        backgroundColor(if (selected) MysticChinaColors.primary else Color(0x00000000))
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.icon); fontSize(22f)
                        color(if (selected) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.label); fontSize(10f)
                        color(if (selected) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                        if (selected) fontWeightSemiBold() else fontWeightNormal()
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 学习 Tab（占位）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.LearnTabContent() {
    View {
        attr { flex(1f); backgroundColor(MysticChinaColors.background); allCenter(); flexDirectionColumn() }
        Text { attr { text("📚"); fontSize(48f); marginBottom(16f) } }
        Text { attr { text("即将上线"); fontSize(16f); color(MysticChinaColors.textPrimary); marginBottom(8f) } }
        Text { attr { text("学习内容正在精心准备中..."); fontSize(12f); color(MysticChinaColors.textSecondary) } }
    }
}

// ═══════════════════════════════════════════════════════════
// 我的 Tab
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ProfileTabContent(ctx: HomePage) {
    Scroller {
        attr { flex(1f); backgroundColor(MysticChinaColors.background); flexDirectionColumn(); paddingTop(MysticChinaTheme.Spacing.xxl) }
        View { attr { allCenter(); flexDirectionColumn() }
            View {
                attr { size(80f, 80f); borderRadius(40f); backgroundColor(MysticChinaColors.surface); allCenter() }
                Text { attr { text(ctx.userProfile.avatarEmoji); fontSize(36f) } }
            }
        }
        View {
            attr { allCenter(); marginBottom(8f); flexDirectionRow(); justifyContentCenter(); alignItemsCenter() }
            event { click { ctx.editingText = ctx.userProfile.nickname; ctx.showEditNickname = true } }
            Text { attr { text(ctx.userProfile.nickname); fontSize(20f); fontWeightBold(); color(MysticChinaColors.textPrimary) } }
            Text { attr { text(" ✏"); fontSize(14f); color(MysticChinaColors.textTertiary) } }
        }
        View {
            attr { allCenter(); marginBottom(MysticChinaTheme.Spacing.xl); paddingLeft(32f); paddingRight(32f) }
            event { click { ctx.editingText = ctx.userProfile.bio; ctx.showEditBio = true } }
            Text { attr { text(ctx.userProfile.bio); fontSize(14f); color(MysticChinaColors.textSecondary); textAlignCenter(); lines(3) } }
        }
        View {
            attr {
                height(1f); backgroundColor(MysticChinaColors.divider)
                marginLeft(MysticChinaTheme.Spacing.lg); marginRight(MysticChinaTheme.Spacing.lg); marginBottom(MysticChinaTheme.Spacing.lg)
            }
        }
        listOf("⚙  设置", "📱  关于神秘中国").forEach { label ->
            View {
                attr {
                    height(52f); paddingLeft(MysticChinaTheme.Spacing.lg); paddingRight(MysticChinaTheme.Spacing.lg)
                    flexDirectionRow(); alignItemsCenter(); backgroundColor(MysticChinaColors.background)
                }
                Text { attr { text(label); fontSize(14f); color(MysticChinaColors.textPrimary); flex(1f) } }
                Text { attr { text(">"); fontSize(14f); color(MysticChinaColors.textTertiary) } }
                View {
                    attr {
                        absolutePosition(bottom = 0f, left = MysticChinaTheme.Spacing.lg, right = 0f)
                        height(1f); backgroundColor(MysticChinaColors.divider)
                    }
                }
            }
        }
        View { attr { height(MysticChinaTheme.Spacing.xxxl) } }
    }
}

// ═══════════════════════════════════════════════════════════
// 编辑覆盖层（昵称/简介）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.EditOverlay(
    ctx: HomePage,
    title: String,
    placeholder: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    View {
        attr { absolutePositionAllZero(); backgroundColor(Color(0xCC000000)); allCenter() }
        event { click { onDismiss() } }
        View {
            attr {
                width(ctx.pagerData.pageViewWidth - 48f)
                backgroundColor(MysticChinaColors.surface); borderRadius(16f)
                padding(all = MysticChinaTheme.Spacing.lg); flexDirectionColumn()
            }
            event { click {} }
            Text {
                attr { text(title); fontSize(16f); fontWeightBold(); color(MysticChinaColors.textPrimary); marginBottom(MysticChinaTheme.Spacing.md) }
            }
            View {
                attr { flexDirectionRow(); justifyContentFlexEnd(); marginTop(MysticChinaTheme.Spacing.lg) }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundColor(MysticChinaColors.surfaceLight); borderRadius(MysticChinaTheme.Radius.md)
                    }
                    event { click { onDismiss() } }
                    Text { attr { text("取消"); fontSize(14f); color(MysticChinaColors.textPrimary) } }
                }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(MysticChinaColors.gradientStart, 0f),
                            ColorStop(MysticChinaColors.gradientEnd, 1f),
                        )
                        borderRadius(MysticChinaTheme.Radius.md); marginLeft(12f)
                    }
                    event { click { onConfirm() } }
                    Text { attr { text("保存"); fontSize(14f); color(MysticChinaColors.textPrimary); fontWeightBold() } }
                }
            }
        }
    }
}
```

- [ ] **Step 2: 编译验证（Android + iOS）**

```bash
./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosArm64 2>&1 | tail -30
```

预期：`BUILD SUCCESSFUL`（两个目标都通过）

- [ ] **Step 3: 运行全部测试**

```bash
./gradlew :shared:testDebugUnitTest 2>&1 | tail -20
```

预期：`BUILD SUCCESSFUL`，所有测试通过

- [ ] **Step 4: 提交**

```bash
git add shared/src/commonMain/kotlin/com/fula/mysticchina/pages/HomePage.kt
git commit -m "feat: 探索Tab接入ExploreTabContent，删除旧CULTURE_CARDS网格"
```

---

## Task 7: 最终构建验证

- [ ] **Step 1: 完整 Android APK 构建**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -20
```

预期：`BUILD SUCCESSFUL`，APK 生成于 `androidApp/build/outputs/apk/debug/`

- [ ] **Step 2: 在设备/模拟器上安装运行**

```bash
# 查找已连接设备
adb devices

# 安装 APK
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

手动验证以下场景：
- [ ] App 启动，首页背景为深朱红色
- [ ] 探索 Tab：顶部显示「探索」标题 + 左侧白色竖条
- [ ] 精选轮播显示 3 张卡片，横向可滑动，底部有圆点
- [ ] 「汉字闯关」精选卡片点击可跳转到 HanziPage
- [ ] 五个分区（文字书法/传统文化/益智游戏/音乐艺术/饮食文化）均显示
- [ ] 每个分区的卡片横向可滑动
- [ ] 汉字练习卡片正常显示（全不透明），其余卡片置灰（40% 透明度）+ 「即将」徽章
- [ ] 学习 Tab、我的 Tab 正常显示（不受影响）
- [ ] 我的 Tab 昵称/简介编辑弹窗正常工作

- [ ] **Step 3: 提交最终状态**

```bash
git add .
git commit -m "chore: 探索Tab重构完成，全量构建验证通过"
```

---

## 常见编译错误排查

| 错误 | 原因 | 解决 |
|---|---|---|
| `Unresolved reference: ExploreTabContent` | `HomePage.kt` import 未加 | 检查 `import com.fula.mysticchina.components.ExploreTabContent` |
| `Unresolved reference: divider` | `Colors.kt` 未加 `dotActive/dotInactive/divider` | 确认 Task 2 完整执行 |
| `None of the following candidates is applicable` on `Color(0xFF...)` | 某些十六进制字面量超出 Int 范围 | Kotlin 会自动 wrap，这是正常行为；如果报错则加 `.toInt()` |
| `Type mismatch: FeaturedCard.gradientStart` | `Color` 类型不匹配 | `GradientPreset` 使用 `Color(0xFF...)` 而非 `Long` |
