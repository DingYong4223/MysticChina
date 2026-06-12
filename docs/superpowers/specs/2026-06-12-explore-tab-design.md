# 探索 Tab 重构设计文档

**日期**: 2026-06-12  
**状态**: 待实现

---

## 1. 目标

将「探索」Tab 从当前简单的 2 列卡片网格，重构为一个可持续扩展的**文化功能导航中心**：

1. 顶部精选轮播 — 开发者手动配置 2-4 张推荐卡片
2. 五个主题分区 — 每区一行横向滑动功能卡片
3. 已上线功能正常可点击；未上线功能置灰展示，提示「即将」
4. 整体采用**中国红**主题配色（深朱红 `#8B0000` 背景）

---

## 2. 视觉设计

### 2.1 页面结构（从上到下）

```
┌─────────────────────────────────┐
│  探索                            │  ← 标题栏：白字 + 左侧红色竖条
├─────────────────────────────────┤
│  [精选卡片1] [精选卡片2] [...]   │  ← 横向可滑动轮播（2-4张）
│       ●  ○  ○                   │  ← 圆点指示器
├─────────────────────────────────┤
│  🈶 文字书法              8个   │  ← 分区标题行
│  [🖊汉字] [📖古诗词] [💬成语] →  │  ← 横向可滑动功能卡片行
├─────────────────────────────────┤
│  🏮 传统文化              8个   │
│  [🎋节气] [🎨传统色] [🧧节日] → │
├─────────────────────────────────┤
│  🧩 益智游戏              5个   │
│  [♟华容道] [⭕五子棋] [🀄麻将] →│
├─────────────────────────────────┤
│  🎵 音乐艺术              6个   │
│  [🎭京剧] [✂️剪纸] [🎼乐器] →  │
├─────────────────────────────────┤
│  🍜 饮食文化              3个   │
│  [🗺菜系] [🍵茶文化] →         │
└─────────────────────────────────┘
```

### 2.2 颜色系统（全量替换）

当前 `MysticChinaColors.kt` 是**浅色主题**（背景 `#FFF0EE`，深色文字）。本次改为**深色主题**（背景 `#8B0000`，白色文字），所有 token 值全部替换：

| Token | 旧值（浅色） | 新值（深色） | 用途 |
|---|---|---|---|
| `background` | `#FFF0EE` | `#8B0000` | 页面主背景（深朱红） |
| `backgroundLight` | `#FFE4E1` | `#700000` | 状态栏/更深层背景 |
| `surface` | `#FFD5D0` | `0x3D000000`（透明度24%黑） | 普通卡片底色 |
| `surfaceLight` | `#FFBDB6` | `0x52000000`（透明度32%黑） | 已上线卡片底色（更突出） |
| `primary` | `#E8352A` | `#DE2910` | 中国红（五星旗红） |
| `primaryDark` | `#B82820` | `#8C0808` | 深红 |
| `primaryLight` | `#FF5A4A` | `#FF4438` | 亮红（强调） |
| `textPrimary` | `#2D0A08` | `#FFFFFF` | 主文字（深色→白） |
| `textSecondary` | `#8B3A35` | `#FFE5E0` | 次要文字（暖白） |
| `textTertiary` | `#BD7672` | `0xCCFFFFFF`（80%白） | 辅助文字 |
| `textDisabled` | `#D4A8A6` | `0x66FFFFFF`（40%白） | 禁用/置灰文字 |
| `controlBarBg` | `#CCFFE4E1` | `0xCC700000`（半透明深红） | 控制栏背景 |

**新增 token**（现有 token 中无对应）：

| Token | 值 | 用途 |
|---|---|---|
| `dotActive` | `0xCCFFFFFF` | 轮播激活圆点 |
| `dotInactive` | `0x33FFFFFF` | 轮播非激活圆点 |
| `divider` | `0x26FFFFFF` | 分区间分割线（15%白） |

> `gradientStart`、`gradientEnd`、`accent`、`error`、`warning`、`success`、`overlay`、`overlayLight`、`progressTrack/Fill/Thumb` 保持原值不变。

### 2.3 精选轮播卡片配色预设

开发者配置卡片时从以下预设选取渐变，保持整体风格统一：

| 名称 | 起始色 | 结束色 | 语义 |
|---|---|---|---|
| `RedFlame` | `#8C0808` | `#DE2910` | 朱红火焰（默认） |
| `IndigoBlue` | `#0D2744` | `#1A6090` | 靛蓝（与红形成传统对比） |
| `InkGreen` | `#0A2A10` | `#28A050` | 墨绿 |
| `PurpleGold` | `#2D1A5C` | `#6030A0` | 紫金 |

### 2.4 功能卡片状态

| 状态 | 视觉 | 交互 |
|---|---|---|
| 已上线 | 正常透明度，`CardSurfaceActive` 底色 | 点击跳转对应页面 |
| 未上线 | 40% 透明度，右上角「即将」徽章 | 点击无响应 |

---

## 3. 数据模型

新建文件：`shared/src/commonMain/kotlin/com/fula/mysticchina/pages/ExploreData.kt`

```kotlin
package com.fula.mysticchina.pages

/** 顶部精选轮播卡片 */
data class FeaturedCard(
    val tag: String,           // 左上角标签，如"今日精选"
    val title: String,         // 主标题，如"汉字闯关"
    val subtitle: String,      // 副标题，如"每关十字，写对才过关"
    val pageName: String?,     // 目标页面名；null = 未上线（仍可出现在轮播中）
    val gradientStart: Long,   // 渐变起始色，使用 GradientPreset 常量
    val gradientEnd: Long,     // 渐变结束色
)

/** 单个功能入口 */
data class FeatureItem(
    val emoji: String,
    val name: String,
    val pageName: String?,     // null = 未上线，自动置灰
)

/** 主题分区 */
data class FeatureCategory(
    val emoji: String,
    val name: String,
    val items: List<FeatureItem>,
)

/** 轮播卡片渐变预设 */
object GradientPreset {
    const val RED_FLAME_START = 0xFF8C0808L
    const val RED_FLAME_END   = 0xFFDE2910L
    const val INDIGO_START    = 0xFF0D2744L
    const val INDIGO_END      = 0xFF1A6090L
    const val INK_GREEN_START = 0xFF0A2A10L
    const val INK_GREEN_END   = 0xFF28A050L
    const val PURPLE_START    = 0xFF2D1A5CL
    const val PURPLE_END      = 0xFF6030A0L
}

/** 顶部精选轮播 — 开发者手动维护，发版时更新 */
val FEATURED_CARDS: List<FeaturedCard> = listOf(
    FeaturedCard(
        tag = "今日精选",
        title = "汉字闯关",
        subtitle = "每关十字，写对才能过关",
        pageName = "HanziPage",
        gradientStart = GradientPreset.RED_FLAME_START,
        gradientEnd = GradientPreset.RED_FLAME_END,
    ),
    // 新增精选卡片追加在此
)

/** 主题分区列表 — 新功能追加到对应分区的 items 列表 */
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

---

## 4. 组件拆分

| 组件 | 文件路径 | 职责 |
|---|---|---|
| `ExploreTabContent` | `components/ExploreTabContent.kt` | 探索 Tab 整体容器；持有 `FEATURED_CARDS` 和 `EXPLORE_CATEGORIES` 数据，组装子组件 |
| `FeaturedCarousel` | `components/FeaturedCarousel.kt` | 横向轮播容器 + 圆点指示器；持有 `currentIndex` observable 状态 |
| `CategorySection` | `components/CategorySection.kt` | 单个分区：标题行 + 横向滑动功能卡片行 |

> `FeaturedCarouselCard` 和 `FeatureCard` 作为各自父组件文件内的私有函数，不单独拆文件（体积小，无复用需求）。

### 4.1 ExploreTabContent

- 接收无参数，直接读取 `FEATURED_CARDS` 和 `EXPLORE_CATEGORIES`
- 竖向 `Scroller`，内含 `FeaturedCarousel` + 各 `CategorySection`
- 背景色：`MysticChinaColors.Background`

### 4.2 FeaturedCarousel

- `Attr`：`cards: List<FeaturedCard>`
- 内部 `observable var currentIndex: Int`（用于圆点高亮）
- 使用 `Scroller` + `flexDirectionRow()` 实现横向滑动
- 卡片宽度 = `pageViewWidth * 0.68f`，确保右侧露出下一张

### 4.3 CategorySection

- `Attr`：`category: FeatureCategory`
- 标题行：emoji + name + `"${items.size}个"` 右对齐
- 功能行：`Scroller` + `flexDirectionRow()`，每张卡片宽度固定 `60dp`
- 未上线卡片（`pageName == null`）：`opacity(0.4f)` + 「即将」徽章，`event { onClick {} }` 空实现（不跳转）

---

## 5. 对现有代码的改动

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `pages/HomePage.kt` | 修改 | 探索 Tab body 替换为 `ExploreTabContent { }` 调用；删除 `CULTURE_CARDS` 常量 |
| `theme/Colors.kt` | 全量替换 | 浅色主题 → 深色主题；按第 2.2 节替换所有背景/文字/表面色 token 的值 |
| `pages/ExploreData.kt` | 新建 | 数据模型定义 + 静态数据列表 |
| `components/ExploreTabContent.kt` | 新建 | — |
| `components/FeaturedCarousel.kt` | 新建 | — |
| `components/CategorySection.kt` | 新建 | — |

**不受影响**：`HanziPage`、`SplashPage`、`MainPage`、学习 Tab、我的 Tab、所有 player/bridge/model 代码。

---

## 6. 可扩展规则

- **新增功能**：在 `ExploreData.kt` 对应分区的 `items` 列表末尾追加一个 `FeatureItem`，功能上线后将 `pageName` 从 `null` 改为实际页面名
- **新增分区**：在 `EXPLORE_CATEGORIES` 末尾追加一个 `FeatureCategory`，UI 自动渲染，无需改组件代码
- **更换精选**：修改 `FEATURED_CARDS` 列表，每次发版手动更新

---

## 7. 不在本次范围内

- 学习 Tab、我的 Tab 的改造
- 搜索功能
- 功能卡片的图片/插画（当前阶段使用 emoji）
- 轮播自动滚动（手动滑动即可，自动播放后续版本考虑）
- 后端/动态配置（所有数据静态硬编码）
