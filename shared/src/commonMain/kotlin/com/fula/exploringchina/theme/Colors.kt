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
