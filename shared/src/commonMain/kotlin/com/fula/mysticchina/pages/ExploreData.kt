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
