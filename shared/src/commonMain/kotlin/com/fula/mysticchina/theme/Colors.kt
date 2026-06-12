package com.fula.mysticchina.theme

import com.tencent.kuikly.core.base.Color

/**
 * 神秘中国应用主题色板
 * 深色主题 — 中国红/金色系
 */
object MysticChinaColors {

    // 背景色（浅红色系）
    val background = Color(0xFFFFF0EE)      // 最浅红，页面底色
    val backgroundLight = Color(0xFFFFE4E1) // 稍深，输入框/卡片底
    val surface = Color(0xFFFFD5D0)         // 卡片/面板表面
    val surfaceLight = Color(0xFFFFBDB6)    // 分隔线/边框

    // 主色 — 中国红系
    val primary = Color(0xFFE8352A)          // 中国红
    val primaryDark = Color(0xFFB82820)      // 深红
    val primaryLight = Color(0xFFFF5A4A)     // 亮红（hover/highlight）
    val accent = Color(0xFFE8352A)           // 与 primary 保持一致（原金黄已改为红）
    val gradientStart = Color(0xFFFF5A4A)    // 亮红
    val gradientEnd = Color(0xFFB82820)      // 深红

    // 文字（深色，适配浅红背景）
    val textPrimary = Color(0xFF2D0A08)     // 深红棕，主文字
    val textSecondary = Color(0xFF8B3A35)   // 中红棕，次要文字
    val textTertiary = Color(0xFFBD7672)    // 浅红棕，辅助文字
    val textDisabled = Color(0xFFD4A8A6)    // 禁用文字

    // 功能色
    val error = Color(0xFFFF4759)
    val warning = Color(0xFFFFB340)
    val success = Color(0xFF00C853)

    // 透明度覆盖
    val overlay = Color(0x80000000)
    val overlayLight = Color(0x40000000)

    // 控制栏
    val controlBarBg = Color(0xCCFFE4E1)    // 半透明浅红
    val progressTrack = Color(0x4DE8352A)   // 半透明红
    val progressFill = Color(0xFFE8352A)    // 中国红
    val progressThumb = Color(0xFFE8352A)   // 中国红
}
