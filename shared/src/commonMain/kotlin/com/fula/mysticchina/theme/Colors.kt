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
