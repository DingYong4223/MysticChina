package com.yijian.theme

import com.tencent.kuikly.core.base.Color

/**
 * 一剪应用主题色板
 * 深色主题 — 仿剪映风格
 */
object YijianColors {

    // 背景色
    val background = Color(0xFF1A1A1A)
    val backgroundLight = Color(0xFF2A2A2A)
    val surface = Color(0xFF333333)
    val surfaceLight = Color(0xFF3D3D3D)

    // 主色
    val primary = Color(0xFF23D3FD)
    val primaryDark = Color(0xFF1BA8CC)
    val accent = Color(0xFFAD37FE)
    val gradientStart = Color(0xFF23D3FD)
    val gradientEnd = Color(0xFFAD37FE)

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
    val progressFill = Color(0xFF23D3FD)
    val progressThumb = Color(0xFFFFFFFF)
}
