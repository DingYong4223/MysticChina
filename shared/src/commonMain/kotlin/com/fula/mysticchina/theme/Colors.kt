package com.fula.mysticchina.theme

import com.tencent.kuikly.core.base.Color

/**
 * 神秘中国应用主题色板
 *
 * 所有颜色从 ThemeManager.currentTheme 读取，支持运行时切换主题。
 * 切换主题后，UI 会在下一次 body 重建时自动使用新颜色。
 */
object MysticChinaColors {

    // ── 背景色 ──
    val background: Color get() = ThemeManager.currentTheme.background
    val backgroundLight: Color get() = ThemeManager.currentTheme.backgroundLight

    // ── 卡片/表面 ──
    val surface: Color get() = ThemeManager.currentTheme.surface
    val surfaceLight: Color get() = ThemeManager.currentTheme.surfaceLight

    // ── 主色 ──
    val primary: Color get() = ThemeManager.currentTheme.primary
    val primaryDark: Color get() = ThemeManager.currentTheme.primaryDark
    val primaryLight: Color get() = ThemeManager.currentTheme.primaryLight
    val accent: Color get() = ThemeManager.currentTheme.primary

    // ── 渐变 ──
    val gradientStart: Color get() = ThemeManager.currentTheme.gradientStart
    val gradientEnd: Color get() = ThemeManager.currentTheme.gradientEnd

    // ── 文字 ──
    val textPrimary: Color get() = ThemeManager.currentTheme.textPrimary
    val textSecondary: Color get() = ThemeManager.currentTheme.textSecondary
    val cardText: Color get() = ThemeManager.currentTheme.cardText
    val textTertiary: Color get() = ThemeManager.currentTheme.textTertiary
    val textDisabled: Color get() = ThemeManager.currentTheme.textDisabled

    // ── 功能色 ──
    val error: Color get() = ThemeManager.currentTheme.error
    val warning: Color get() = ThemeManager.currentTheme.warning
    val success: Color get() = ThemeManager.currentTheme.success

    // ── 覆盖层 ──
    val overlay: Color get() = ThemeManager.currentTheme.overlay
    val overlayLight: Color get() = ThemeManager.currentTheme.overlayLight

    // ── 控制栏 ──
    val controlBarBg: Color get() = ThemeManager.currentTheme.controlBarBg
    val progressTrack: Color get() = ThemeManager.currentTheme.progressTrack
    val progressFill: Color get() = ThemeManager.currentTheme.progressFill
    val progressThumb: Color get() = ThemeManager.currentTheme.progressThumb

    // ── 探索 Tab 专用 ──
    val dotActive: Color get() = ThemeManager.currentTheme.dotActive
    val dotInactive: Color get() = ThemeManager.currentTheme.dotInactive
    val divider: Color get() = ThemeManager.currentTheme.divider
}
