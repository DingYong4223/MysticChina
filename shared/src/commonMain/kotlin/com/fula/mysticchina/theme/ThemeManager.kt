package com.fula.mysticchina.theme

import com.tencent.kuikly.core.base.Color

/**
 * 完整主题色定义 — 所有页面用到的颜色全部在此
 */
internal data class ThemeColors(
    val name: String,
    val emoji: String,

    // 背景色
    val background: Color,      // 页面主背景
    val backgroundLight: Color, // 状态栏/顶栏

    // 卡片/表面
    val surface: Color,         // 普通卡片底色
    val surfaceLight: Color,    // 已上线卡片底色（更突出）

    // 主色
    val primary: Color,         // 中国红
    val primaryDark: Color,     // 深红
    val primaryLight: Color,    // 亮红（强调）

    // 渐变
    val gradientStart: Color,
    val gradientEnd: Color,

    // 文字
    val textPrimary: Color,     // 主文字（导航、标题）
    val textSecondary: Color,   // 次要文字（副标题、描述）
    val cardText: Color,        // 卡片上文字（功能卡片名称）
    val textTertiary: Color,    // 辅助文字
    val textDisabled: Color,    // 禁用文字

    // 探索 Tab 专用
    val dotActive: Color,
    val dotInactive: Color,
    val divider: Color,

    // 功能色（保持不变）
    val error: Color,
    val warning: Color,
    val success: Color,
    val overlay: Color,
    val overlayLight: Color,
    val controlBarBg: Color,
    val progressTrack: Color,
    val progressFill: Color,
    val progressThumb: Color,
)

/**
 * 主题预设 — 所有配色方案
 */
internal object ThemePresets {

    /** 墨玉黑 — 深邃墨黑背景，沉稳内敛 */
    val INK_BLACK = ThemeColors(
        name = "墨玉黑", emoji = "🖤",
        background      = Color(0xFF1A1A1A),
        backgroundLight = Color(0xFF2D2D2D),
        surface         = Color(0xFF333333),
        surfaceLight    = Color(0xFF3E3E3E),
        primary         = Color(0xFFD4A574),
        primaryDark     = Color(0xFFA07840),
        primaryLight    = Color(0xFFE8C594),
        gradientStart   = Color(0xFFD4A574),
        gradientEnd     = Color(0xFF8B6914),
        textPrimary     = Color(0xFFFFFFFF),
        textSecondary   = Color(0xFFDDDDDD),
        cardText        = Color(0xFFDDDDDD),   // 浅灰，适配深色卡片
        textTertiary    = Color(0xCCFFFFFF.toLong()),
        textDisabled    = Color(0x66FFFFFF.toLong()),
        dotActive       = Color(0xCCFFFFFF.toLong()),
        dotInactive     = Color(0x33FFFFFF.toLong()),
        divider         = Color(0x26FFFFFF.toLong()),
        error           = Color(0xFFFF4759),
        warning         = Color(0xFFFFB340),
        success         = Color(0xFF00C853),
        overlay         = Color(0x80000000.toLong()),
        overlayLight    = Color(0x40000000.toLong()),
        controlBarBg    = Color(0xCC333333.toLong()),
        progressTrack   = Color(0x4DD4A574.toLong()),
        progressFill    = Color(0xFFD4A574),
        progressThumb   = Color(0xFFD4A574),
    )

    /** 青花瓷 — 素雅青白配色 */
    val BLUE_PORCELAIN = ThemeColors(
        name = "青花瓷", emoji = "🏺",
        background      = Color(0xFFE8F0F8),
        backgroundLight = Color(0xFFD6E4F0),
        surface         = Color(0xFFB0C4DE),   // 暗蓝灰卡片，在浅蓝底上清晰可见
        surfaceLight    = Color(0xFFC8D8E8),   // 稍亮的蓝灰卡片（已上线）
        primary         = Color(0xFF264D7C),
        primaryDark     = Color(0xFF1A3660),
        primaryLight    = Color(0xFF4A7AB5),
        gradientStart   = Color(0xFF4A7AB5),
        gradientEnd     = Color(0xFF264D7C),
        textPrimary     = Color(0xFF1A1A2E),
        textSecondary   = Color(0xFF44445A),
        cardText        = Color(0xFF333333),   // 深灰，适配蓝灰卡片
        textTertiary    = Color(0xCC1A1A2E.toLong()),
        textDisabled    = Color(0x661A1A2E.toLong()),
        dotActive       = Color(0xCC264D7C.toLong()),
        dotInactive     = Color(0x33264D7C.toLong()),
        divider         = Color(0x26264D7C.toLong()),
        error           = Color(0xFFFF4759),
        warning         = Color(0xFFFFB340),
        success         = Color(0xFF00C853),
        overlay         = Color(0x80000000.toLong()),
        overlayLight    = Color(0x40000000.toLong()),
        controlBarBg    = Color(0xCCD6E4F0.toLong()),
        progressTrack   = Color(0x4D264D7C.toLong()),
        progressFill    = Color(0xFF264D7C),
        progressThumb   = Color(0xFF264D7C),
    )

    /** 中国红（原朱砂红）— 正红为主，热烈喜庆 */
    val CHINA_RED = ThemeColors(
        name = "中国红", emoji = "🇨🇳",
        background      = Color(0xFFCC1111),
        backgroundLight = Color(0xFFAA0000),
        surface         = Color(0xFFF5F0F0),
        surfaceLight    = Color(0xFFFFFFFF),
        primary         = Color(0xFFFFD700),
        primaryDark     = Color(0xFFDAA520),
        primaryLight    = Color(0xFFFFE44D),
        gradientStart   = Color(0xFFFFE44D),
        gradientEnd     = Color(0xFFFFD700),
        textPrimary     = Color(0xFFFFFFFF),
        textSecondary   = Color(0xFFFFF0E0),
        cardText        = Color(0xFF333333),   // 深灰，适配白底卡片
        textTertiary    = Color(0xCCFFFFFF.toLong()),
        textDisabled    = Color(0x66FFFFFF.toLong()),
        dotActive       = Color(0xCCFFFFFF.toLong()),
        dotInactive     = Color(0x33FFFFFF.toLong()),
        divider         = Color(0x26FFFFFF.toLong()),
        error           = Color(0xFFFF4759),
        warning         = Color(0xFFFFB340),
        success         = Color(0xFF00C853),
        overlay         = Color(0x80000000.toLong()),
        overlayLight    = Color(0x40000000.toLong()),
        controlBarBg    = Color(0xCCAA0000.toLong()),
        progressTrack   = Color(0x4DFFD700.toLong()),
        progressFill    = Color(0xFFFFD700),
        progressThumb   = Color(0xFFFFD700),
    )

    /** 水墨灰 — 水墨丹青，淡雅写意 */
    val INK_GRAY = ThemeColors(
        name = "水墨灰", emoji = "🎨",
        background      = Color(0xFF2C2C2C),
        backgroundLight = Color(0xFF3A3A3A),
        surface         = Color(0xFF3E3E3E),
        surfaceLight    = Color(0xFF4A4A4A),
        primary         = Color(0xFF8B8B8B),
        primaryDark     = Color(0xFF606060),
        primaryLight    = Color(0xFFAAAAAA),
        gradientStart   = Color(0xFFAAAAAA),
        gradientEnd     = Color(0xFF606060),
        textPrimary     = Color(0xFFFFFFFF),
        textSecondary   = Color(0xFFCCCCCC),
        cardText        = Color(0xFFCCCCCC),   // 浅灰，适配深色卡片
        textTertiary    = Color(0xCCFFFFFF.toLong()),
        textDisabled    = Color(0x66FFFFFF.toLong()),
        dotActive       = Color(0xCCFFFFFF.toLong()),
        dotInactive     = Color(0x33FFFFFF.toLong()),
        divider         = Color(0x26FFFFFF.toLong()),
        error           = Color(0xFFFF4759),
        warning         = Color(0xFFFFB340),
        success         = Color(0xFF00C853),
        overlay         = Color(0x80000000.toLong()),
        overlayLight    = Color(0x40000000.toLong()),
        controlBarBg    = Color(0xCC3A3A3A.toLong()),
        progressTrack   = Color(0x4D8B8B8B.toLong()),
        progressFill    = Color(0xFF8B8B8B),
        progressThumb   = Color(0xFF8B8B8B),
    )

    /** 所有预设列表，索引与 ThemePage 中一致 */
    val ALL: List<ThemeColors> = listOf(CHINA_RED, INK_BLACK, BLUE_PORCELAIN, INK_GRAY)
}

/**
 * 全局主题管理器
 * 负责保存/恢复主题选择，并提供当前主题色
 */
internal object ThemeManager {
    private const val SP_KEY_THEME_INDEX = "theme_index"
    private const val DEFAULT_INDEX = 0

    /** 当前主题索引 */
    var currentThemeIndex: Int = DEFAULT_INDEX

    /** 获取当前主题色 */
    val currentTheme: ThemeColors
        get() = ThemePresets.ALL.getOrElse(currentThemeIndex) { ThemePresets.CHINA_RED }

    /** 切换到指定主题 */
    fun applyTheme(index: Int) {
        currentThemeIndex = index
    }
}
